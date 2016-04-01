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
import net.radai.confusion.core.spi.PollerSupport;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Radai Rosenblatt
 */
public class EtcdWatcher extends PollerSupport {
    //input
    private final String uri;
    private final String key;
    private final boolean base64Encode;

    //components
    private volatile Thread pollThread;
    private volatile PollRunnable runnable;

    //state
    private volatile boolean on = false;

    public EtcdWatcher(String uri, String key, boolean base64Encode) {
        this.uri = uri;
        this.key = key;
        this.base64Encode = base64Encode;
    }

    @Override
    public synchronized void start() {
        if (on || pollThread != null) {
            throw new IllegalStateException();
        }
        on = true;
        startPollThread();
    }

    @Override
    public synchronized void stop() {
        if (!on || pollThread == null) {
            throw new IllegalStateException();
        }
        on = false;
        stopPollThread();
    }

    @Override
    public InputStream fetch() throws IOException {
        try (EtcdClient client = new EtcdClient(URI.create(uri))) {
            EtcdKeyGetRequest req = client.get(key);
            EtcdKeysResponse response = req.send().get();
            byte[] blob = decode(response);
            return new ByteArrayInputStream(blob);
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
    public OutputStream store() throws IOException {
        return new EtcdOutputStream();
    }

    private byte[] decode(EtcdKeysResponse response) {
        if (base64Encode) {
            return Base64.getDecoder().decode(response.node.value);
        } else {
            return response.node.value.getBytes(Charset.forName("UTF-8"));
        }
    }

    private String encode(byte[] raw) {
        if (base64Encode) {
            return Base64.getEncoder().encodeToString(raw);
        } else {
            return new String(raw, Charset.forName("UTF-8"));
        }
    }

    private void startPollThread() {
        if (pollThread != null) {
            throw new IllegalStateException();
        }
        runnable = new PollRunnable();
        pollThread = new Thread(runnable, "watch thread for " + uri + "/v2/keys/" + key);
        pollThread.setDaemon(true);
        pollThread.start();
        runnable.waitUntilWatching();
    }

    private void stopPollThread() {
        if (pollThread == null) {
            throw new IllegalStateException();
        }
        try {
            runnable.on = false;
            pollThread.interrupt();
            pollThread.join();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            pollThread = null;
        }
    }

    private class PollRunnable implements Runnable {
        private final CountDownLatch startLatch = new CountDownLatch(1);
        private volatile boolean on = true;

        private void waitUntilWatching() {
            try {
                startLatch.await();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void run() {
            boolean firstSample = true;
            boolean isNull = true;
            long lastModified = -1;
            while (on) {
                startLatch.countDown();
                try (EtcdClient client = new EtcdClient(URI.create(uri))) {
                    while (on) {
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
                            byte[] blob = null;

                            try {
                                EtcdKeysResponse response = client.get(key).timeout(3, TimeUnit.SECONDS).waitForChange(lastModified+1).send().get();
                                if (lastModified != response.node.modifiedIndex) { //always holds if isNull
                                    fire = true;
                                    lastModified = response.node.modifiedIndex;
                                    blob = decode(response);
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
                    int g = 7;
                    //todo - log
                }
            }
        }
    }

    /**
     * writes into a temp file and attempts to atomically swap to the destination file
     * when the temp file is closed (==when all writes are complete). also pauses watcher
     * during the swap to avoid getting an event for our own modifications.
     */
    private class EtcdOutputStream extends OutputStream {
        private final ByteArrayOutputStream underlying;
        private boolean successful = true;

        public EtcdOutputStream() {
            underlying = new ByteArrayOutputStream();
        }

        @Override
        public void write(int b) throws IOException {
            try {
                underlying.write(b);
            } catch (Exception e) {
                successful = false;
                throw e;
            }
        }

        @Override
        public void write(byte[] b) throws IOException {
            try {
                underlying.write(b);
            } catch (Exception e) {
                successful = false;
                throw e;
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            try {
                underlying.write(b, off, len);
            } catch (Exception e) {
                successful = false;
                throw e;
            }
        }

        @Override
        public void flush() throws IOException {
            try {
                underlying.flush();
            } catch (Exception e) {
                successful = false;
                throw e;
            }
        }

        @Override
        public void close() throws IOException {
            underlying.close();
            if (successful) {
                synchronized (EtcdWatcher.this) {
                    if (on) {
                        stopPollThread(); //so we dont get an event for what we're about to do
                    }
                    try (EtcdClient client = new EtcdClient(URI.create(uri))) {
                        String strValue = encode(underlying.toByteArray());
                        try {
                            client.put(key, strValue).send().get();
                        } catch (IOException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new IOException(e);
                        }
                    } finally {
                        if (on) {
                            startPollThread(); //continue watching
                        }
                    }
                }
            }
        }
    }
}
