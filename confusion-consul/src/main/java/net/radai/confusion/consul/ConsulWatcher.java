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

package net.radai.confusion.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetBinaryValue;
import net.radai.confusion.core.spi.PollerSupport;

import java.io.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Radai Rosenblatt
 */
public class ConsulWatcher extends PollerSupport {
    //input
    private final String host;
    private final int port;
    private final String key;
    private final long pollSeconds;

    //components
    private final ConsulClient client;
    private volatile Thread pollThread;
    private volatile PollRunnable runnable;

    //state
    private volatile boolean on = false;

    public ConsulWatcher(String key) {
        this("localhost", 8500, key, 30);
    }

    public ConsulWatcher(String host, int port, String key) {
        this(host, port, key, 30);
    }

    public ConsulWatcher(String host, int port, String key, long pollSeconds) {
        this.host = host;
        this.port = port;
        this.key = key;
        this.pollSeconds = pollSeconds;
        this.client = new ConsulClient(host, port);
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
        Response<GetBinaryValue> response = client.getKVBinaryValue(key);
        GetBinaryValue value = response.getValue();
        return value == null ? null : new ByteArrayInputStream(value.getValue());
    }

    @Override
    public OutputStream store() throws IOException {
        return new ConsulOutputStream();
    }

    private void startPollThread() {
        if (pollThread != null) {
            throw new IllegalStateException();
        }
        runnable = new PollRunnable();
        pollThread = new Thread(runnable, "watch thread for " + host + ":" + port + "/v1/kv/" + key);
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
            //TODO - come back here when https://github.com/Ecwid/consul-api/issues/47 is fixed
            pollThread.interrupt(); //doesnt really work :-(
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
            Response<GetBinaryValue> response;
            GetBinaryValue value;

            //establish starting state
            response = client.getKVBinaryValue(key);
            value = response.getValue();

            //latest index is either key modification (if key exists) or server global
            boolean isNull = value == null;
            long lastModIndex = isNull ? response.getConsulIndex() : value.getModifyIndex();

            while (on) {
                //go fetch
                startLatch.countDown();
                response = client.getKVBinaryValue(key, new QueryParams(pollSeconds, lastModIndex));
                value = response.getValue();
                byte[] blob = value == null ? null : value.getValue();
                boolean fire = false;

                if (value == null) {
                    if (!isNull) { //!null --> null state change
                        isNull = true;
                        fire = true;
                    }
                } else {
                    isNull = false;
                    long currentModIndex = value.getModifyIndex();
                    if (currentModIndex != lastModIndex) {
                        lastModIndex = currentModIndex;
                        fire = true;
                    }
                }

                if (fire) {
                    fire(blob);
                }
            }
        }
    }

    /**
     * writes into a temp file and attempts to atomically swap to the destination file
     * when the temp file is closed (==when all writes are complete). also pauses watcher
     * during the swap to avoid getting an event for our own modifications.
     */
    private class ConsulOutputStream extends OutputStream {
        private final ByteArrayOutputStream underlying;
        private boolean successful = true;

        public ConsulOutputStream() {
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
                synchronized (ConsulWatcher.this) {
                    if (on) {
                        stopPollThread(); //so we dont get an event for what we're about to do
                    }
                    try {
                        Response<Boolean> response = client.setKVBinaryValue(key, underlying.toByteArray());
                        if (!response.getValue()) {
                            //we dont do anything conditionally. should succeed.
                            throw new IOException("response from server was false");
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
