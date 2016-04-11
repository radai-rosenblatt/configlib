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

package net.radai.confusion.etcd;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.requests.EtcdKeyGetRequest;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import net.radai.confusion.core.spi.store.AbstractTextStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Radai Rosenblatt
 */
public class EtcdStore extends AbstractTextStore {
    private final Logger log = LogManager.getLogger(getClass());

    //input
    private final String uri;
    private final String key;

    public EtcdStore(String uri, String key) {
        this.uri = uri;
        this.key = key;
    }

    @Override
    protected TextPollRunnable createRunnable() {
        return new PollRunnable();
    }

    @Override
    public String read() throws IOException {
        try (EtcdClient client = new EtcdClient(URI.create(uri))) {
            EtcdKeyGetRequest req = client.get(key);
            EtcdKeysResponse response = req.send().get();
            return response.node.value;
        } catch (EtcdException e) {
            if (e.errorCode == 100) {
                return null;
            }
            throw new IOException(e);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void write(String payload) throws IOException {
        try (EtcdClient client = new EtcdClient(URI.create(uri))) {
            if (payload == null) {
                client.delete(key).send().get();
            } else {
                client.put(key, payload).send().get();
            }
        } catch (EtcdException e) {
            if (payload == null && e.errorCode == 100) {
                return; //double deletion would do this
            }
            throw new IOException(e);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private class PollRunnable extends AbstractTextStore.TextPollRunnable {

        @Override
        public void run() {
            boolean firstSample = true;
            boolean isNull = true;
            long lastModified = -1;
            while (!shouldDie()) {
                markWatching();
                try (EtcdClient client = new EtcdClient(URI.create(uri))) {
                    while (!shouldDie()) {
                        if (firstSample) { //establish initial state
                            try {
                                EtcdKeysResponse response = client.get(key).send().get();
                                lastModified = response.node.modifiedIndex;
                                isNull = false;
                            } catch (EtcdException e) { //can only mean key not found?
                                lastModified = e.index;
                                isNull = true;
                            }
                            firstSample = false;
                        } else {
                            boolean fire = false;
                            String blob = null;

                            try {
                                EtcdKeysResponse response = client.get(key).timeout(3, TimeUnit.SECONDS).waitForChange(lastModified+1).send().get();
                                if (lastModified != response.node.modifiedIndex) { //always holds if isNull
                                    fire = true;
                                    lastModified = response.node.modifiedIndex;
                                    blob = response.node.value;
                                    isNull = false;
                                }
                            } catch (EtcdException e) { //can only mean key not found?
                                if (!isNull) {
                                    fire = true;
                                    lastModified = e.index;
                                    blob = null;
                                    isNull = true;
                                }
                            } catch (TimeoutException e) {
                                //unchanged
                                fire = false;
                            }

                            if (fire) {
                                fire(blob);
                            }
                        }
                    }
                } catch (Exception e) { //we get here if the connection was severed
                    log.error("while polling etcd", e);
                }
            }
        }
    }
}
