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

import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import net.radai.confusion.core.spi.store.AbstractBinaryStoreTest;
import net.radai.confusion.core.spi.store.BinaryStore;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.I0Itec.zkclient.exception.ZkTimeoutException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.BeforeClass;
import scala.collection.Seq;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Radai Rosenblatt
 */
public class KafkaStoreTest extends AbstractBinaryStoreTest {
    private static final String ZK_ADDR = "localhost:2181";
    private static Boolean serverAutoCreatesTopics; //null
    private static Integer defaultNumPartitions; //null
    private static boolean precreateTopic = false;
    private String topicName;

    @BeforeClass
    public static void makeSureZookeeperAndKafkaExist() throws Exception {
        //verify zookeeper and kafka, check for server behaviour w.r.t undefined topics
        Properties props = KafkaStore.buildConsumerProperties("localhost", 9092);
        ZkClient zkClient = null;
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            String randomTopicName = UUID.randomUUID().toString();
            zkClient = new ZkClient(
                    ZK_ADDR,
                    (int) TimeUnit.SECONDS.toMillis(5),
                    (int) TimeUnit.SECONDS.toMillis(5),
                    ZKStringSerializer$.MODULE$
            );
            ZkUtils zkUtils = new ZkUtils(zkClient, new ZkConnection(ZK_ADDR), false);
            Seq<String> topics = zkUtils.getAllTopics();
            Assert.assertFalse(topics.contains(randomTopicName));
            List<PartitionInfo> partitions = consumer.partitionsFor(randomTopicName);
            if (partitions == null) {
                serverAutoCreatesTopics = Boolean.FALSE;
            } else {
                serverAutoCreatesTopics = Boolean.TRUE;
                defaultNumPartitions = partitions.size();
            }
            if (!serverAutoCreatesTopics || defaultNumPartitions != 1) {
                precreateTopic = true;
            }
        } catch (ZkTimeoutException e) {
            throw new AssumptionViolatedException("expecting to find local zookeeper running at " + ZK_ADDR, e);
        } catch (TimeoutException e) {
            throw new AssumptionViolatedException("expecting to find local kafka running at localhost:9092", e);
        } finally {
            if (zkClient != null) {
                //noinspection ThrowFromFinallyBlock
                zkClient.close();
            }
        }
    }

    @Before
    public void setup() throws Exception {
        topicName = UUID.randomUUID().toString();
        if (precreateTopic) {
            ZkClient zkClient = null;
            try {
                zkClient = new ZkClient(
                        ZK_ADDR,
                        (int) TimeUnit.SECONDS.toMillis(5),
                        (int) TimeUnit.SECONDS.toMillis(5),
                        ZKStringSerializer$.MODULE$
                );
                ZkUtils zkUtils = new ZkUtils(zkClient, new ZkConnection(ZK_ADDR), false);
                AdminUtils.createTopic(zkUtils, topicName, 1, 1, new Properties());
            } finally {
                if (zkClient != null) {
                    //noinspection ThrowFromFinallyBlock
                    zkClient.close();
                }
            }
        }
    }

    @Override
    protected KafkaStore buildStore() throws Exception {
        return new KafkaStore("localhost", 9092, topicName, 3);
    }

    @Override
    protected void writeUnderlying(BinaryStore store, byte[] value) throws Exception {
        try (KafkaProducer<String, byte[]> producer = new KafkaProducer<>(KafkaStore.buildProducerProperties("localhost", 9092))) {
            producer.send(new ProducerRecord<>(topicName, value)).get();
        }
    }

    @Override
    protected byte[] readUnderlying(BinaryStore store) throws Exception {
        Properties props = KafkaStore.buildConsumerProperties("localhost", 9092);
        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props)) {
            List<PartitionInfo> partitions = consumer.partitionsFor(topicName);
            if (partitions == null) {
                //topic doesnt exist and server isnt configured to auto-create
                return null;
            }
            if (partitions.size() != 1) {
                throw new IllegalStateException("configuration topic " + topicName + " must have exactly one partition. found " + partitions.size());
            }
            PartitionInfo partition = partitions.get(0);
            TopicPartition topicPartition = new TopicPartition(topicName, partition.partition());
            consumer.assign(Collections.singletonList(topicPartition));
            consumer.seekToEnd(); //seeks to the end of assigned topics
            long position = consumer.position(topicPartition);
            if (position > 0) {
                consumer.seek(topicPartition, position - 1); //we want to read the last one (if it exists)
            }
            ConsumerRecords<String, byte[]> records = consumer.poll(100);
            ConsumerRecord<String, byte[]> lastRecord = null;
            for (ConsumerRecord<String, byte[]> record : records) {
                lastRecord = record;
            }
            return lastRecord != null ? lastRecord.value() : null;
        } catch (TimeoutException e) {
            throw new AssumptionViolatedException("expecting to find local kafka running at localhost:9092", e);
        }
    }
}
