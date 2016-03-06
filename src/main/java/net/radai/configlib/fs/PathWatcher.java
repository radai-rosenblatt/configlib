/*
 * This file is part of ConfigLib.
 *
 * ConfigLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ConfigLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ConfigLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.radai.configlib.fs;

import net.radai.configlib.core.spi.PollerSupport;
import net.radai.configlib.core.util.FSUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Radai Rosenblatt
 */
public class PathWatcher extends PollerSupport {
    private final Path path;
    private volatile Thread pollThread;
    private volatile boolean on = false;

    public PathWatcher(Path path) {
        this.path = path;
    }

    @Override
    public InputStream fetch() throws IOException {
        try {
            return Files.newInputStream(path, StandardOpenOption.READ);
        } catch (NoSuchFileException e) {
            return null;
        }
    }

    @Override
    public OutputStream store() throws IOException {
        return new FileSwapOutputStream(path);
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

    private void startPollThread() {
        if (pollThread != null) {
            throw new IllegalStateException();
        }
        PollRunnable runnable = new PollRunnable();
        pollThread = new Thread(runnable, "watch thread for " + path);
        pollThread.setDaemon(true);
        pollThread.start();
        runnable.waitUntilWatching();
    }

    private void stopPollThread() {
        if (pollThread == null) {
            throw new IllegalStateException();
        }
        try {
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

        private void waitUntilWatching() {
            try {
                startLatch.await();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void run() {
            WatchEvent.Kind[] kinds = {
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
            };
            Path folder = path.getParent();
            while (on) {
                try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                    WatchKey watchKey = folder.register(watchService, kinds);
                    while (on) {
                        startLatch.countDown();
                        WatchKey key = watchService.take();
                        boolean matched = false;
                        for (WatchEvent<?> event : key.pollEvents()) {
                            Path changed = (Path) event.context();
                            Path fullChanged = folder.resolve(changed);
                            if (fullChanged.equals(path)) {
                                matched = true;
                                break; //dont bother with other events
                            }
                        }
                        if (matched) {
                            //to avoid multiple firings for non-atomic modifications we wait a while.
                            FSUtil.waitUntilQuiet(path);
                            //then purge everything that happened while we were waiting
                            watchKey.pollEvents();
                            //and only then fire (using what should hopefully be a stable file state)
                            try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
                                fire(is);
                            } catch (IOException e) {
                                int g = 6;
                                //TODO - log
                            }
                        }
                        if (!watchKey.reset()) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    int g = 6;
                    //TODO - log
                }
            }
        }
    }

    /**
     * writes into a temp file and attempts to atomically swap to the destination file
     * when the temp file is closed (==when all writes are complete). also pauses watcher
     * during the swap to avoid getting an event for our own modifications.
     */
    private class FileSwapOutputStream extends OutputStream {
        private final Path finalDestination;
        private final Path tempDestination;
        private final OutputStream underlying;
        private boolean successful = true;

        public FileSwapOutputStream(Path finalDestination) throws IOException {
            this.finalDestination = finalDestination;
            this.tempDestination = Files.createTempFile(null, null);
            this.underlying = Files.newOutputStream(tempDestination, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
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
                synchronized (PathWatcher.this) {
                    if (on) {
                        stopPollThread(); //so we dont get an event for what we're about to do
                    }
                    try {
                        //atomically swap the file into place
                        Files.move(
                                tempDestination,
                                finalDestination,
                                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
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
