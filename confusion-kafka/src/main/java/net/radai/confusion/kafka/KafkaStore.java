/*
 * This file is part of Confusion.
 *
 * Confusion is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Confusion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Confusion.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.radai.confusion.kafka;

import net.radai.confusion.core.spi.store.AbstractBinaryStore;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by Radai Rosenblatt
 */
public class KafkaStore extends AbstractBinaryStore {
    private final Logger log = LogManager.getLogger(getClass());

    //input
    private final String host;
    private final int port;
    private final String topic;
    private final long pollMillis;

    public KafkaStore(String host, int port, String topic, long pollMillis) {
        this.host = host;
        this.port = port;
        this.topic = topic;
        this.pollMillis = pollMillis;
    }

    @Override
    protected BinaryPollRunnable createRunnable() {
        return new PollRunnable();
    }

    @Override
    public byte[] read() throws IOException {
        Properties props = buildConsumerProperties(host, port);
        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props)) {
            List<PartitionInfo> partitions = consumer.partitionsFor(topic);
            if (partitions == null || partitions.isEmpty()) {
                return null; //no topic == no data
            }
            if (partitions.size() != 1) {
                throw new IllegalStateException("configuration topic " + topic + " expected to have a single partition. instead found " + partitions.size());
            }
            PartitionInfo partition = partitions.get(0);
            TopicPartition topicPartition = new TopicPartition(topic, partition.partition());
            consumer.assign(Collections.singletonList(topicPartition));
            consumer.seekToEnd(); //seeks to the end of assigned topics
            long position = consumer.position(topicPartition);
            if (position > 0) {
                consumer.seek(topicPartition, position-1); //we want to read the last one (if it exists)
            }
            ConsumerRecords<String, byte[]> records = consumer.poll(100); //0 bugs out
            ConsumerRecord<String, byte[]> lastRecord = null;
            for (ConsumerRecord<String, byte[]> record : records) {
                lastRecord = record;
            }
            if (lastRecord != null) {
                byte[] value = lastRecord.value();
                log.debug("read value {} from partition {} offset {}", describe(value), lastRecord.partition(), lastRecord.offset());
                return value;
            } else {
                log.debug("read value null from empty topic");
                return null;
            }
        }
    }

    @Override
    public void write(byte[] payload) throws IOException {
        try (KafkaProducer<String, byte[]> producer = new KafkaProducer<>(KafkaStore.buildProducerProperties(host, port))) {
            RecordMetadata record = producer.send(new ProducerRecord<>(topic, payload)).get();
            log.debug("wrote value {} into partition {} offset {}", describe(payload), record.partition(), record.offset());
        } catch (ExecutionException | InterruptedException e) {
            throw new IOException("while publishing to kafka", e);
        }
    }

    @Override
    public boolean reportsConsecutiveNulls() {
        return true;
    }

    private class PollRunnable extends AbstractBinaryStore.BinaryPollRunnable {

        @Override
        public void run() {
            boolean firstConnection = true;
            long position = 0;
            boolean reportedTopicGone = false;
            Properties props = buildConsumerProperties(host, port);
            while (!shouldDie()) {
                markWatching();
                try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props)) {
                    while (!shouldDie()) {
                        List<PartitionInfo> partitions = consumer.partitionsFor(topic);
                        if (partitions == null || partitions.isEmpty()) {
                            //topic isnt there, wait for it
                            position = 0; //reset our position in case it was there before and got deleted
                            try {
                                if (!reportedTopicGone) {
                                    reportedTopicGone = true;
                                    log.warn("configuration topic {} not found. waiting for it to exist", topic);
                                }
                                Thread.sleep(pollMillis);
                            } catch (InterruptedException e) {
                                log.error("interrupted", e);
                            }
                            continue;
                        }
                        reportedTopicGone = false;
                        if (partitions.size() != 1) {
                            String error = "configuration topic " + topic + " expected to have a single partition. instead found " + partitions.size();
                            log.error(error);
                            throw new IllegalStateException(error);
                        }
                        PartitionInfo partition = partitions.get(0);
                        TopicPartition topicPartition = new TopicPartition(topic, partition.partition());
                        consumer.assign(Collections.singletonList(topicPartition));
                        if (firstConnection) {
                            firstConnection = false;
                            consumer.seekToEnd(); //seeks to the end of assigned topics
                            position = consumer.position(topicPartition);
                        } else {
                            //pick up where we left off (so we get any changes that might have occurred while we were disconnected)
                            consumer.seek(topicPartition, position);
                        }
                        break;
                    }

                    //we're connected, topic looks fine, start listening.
                    while (!shouldDie()) {
                        log.trace("listening on kafka topic {} offset {} for at most {} millis", topic, position, pollMillis);
                        ConsumerRecords<String, byte[]> records = consumer.poll(pollMillis);
                        boolean gotSomething = records != null && !records.isEmpty();
                        if (gotSomething) {
                            ConsumerRecord<String, byte[]> lastRecord = null;
                            for (ConsumerRecord<String, byte[]> record : records) {
                                lastRecord = record;
                            }
                            assert lastRecord != null;
                            position = lastRecord.offset();
                            byte[] value = lastRecord.value();
                            log.debug("found value {} at partition {} offset {}", describe(value), lastRecord.partition(), lastRecord.offset());
                            fire(value);
                        }
                    }
                } catch (Exception e) { //we get here if the connection was severed
                    log.error("while polling kafka", e);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "kafka://" + host + ":" + port + "/" + topic;
    }

    private static String describe(byte[] value) {
        return value == null ? "null" : "byte[" + value.length + "]";
    }

    private static Properties buildCommonProperties(String kafkaHost, int kafkaPort) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaHost + ":" + kafkaPort);
        return props;
    }

    static Properties buildConsumerProperties(String kafkaHost, int kafkaPort) {
        Properties props = buildCommonProperties(kafkaHost, kafkaPort);
        props.put("group.id", UUID.randomUUID().toString()); //every listener is unique
        props.put("enable.auto.commit", "false");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        return props;
    }

    static Properties buildProducerProperties(String kafkaHost, int kafkaPort) {
        Properties props = buildCommonProperties(kafkaHost, kafkaPort);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        return props;
    }
}
