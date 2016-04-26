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

package net.radai.confusion.core.fs;

import net.radai.confusion.core.spi.store.AbstractBinaryStore;
import net.radai.confusion.core.util.IOUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.List;

/**
 * Created by Radai Rosenblatt
 */
public class PathStore extends AbstractBinaryStore {
    private final Logger log = LogManager.getLogger(getClass());
    private final Path path;

    public PathStore(Path path) {
        this.path = path;
    }

    public PathStore(File file) {
        this(file.toPath());
    }

    public PathStore(String path) {
        this(Paths.get(path));
    }

    @Override
    protected BinaryPollRunnable createRunnable() {
        return new PollRunnable();
    }

    @Override
    public byte[] read() throws IOException {
        try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
            return IOUtil.read(is);
        } catch (NoSuchFileException e) {
            return null;
        }
    }

    @Override
    public void write(byte[] payload) throws IOException {
        if (payload == null) {
            try {
                Files.delete(path);
            } catch (NoSuchFileException e) {
                //dont care about double delete
            }
            return;
        }
        Path temp = Files.createTempFile(path.getParent(), null, null);
        try (OutputStream os = Files.newOutputStream(temp, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            os.write(payload);
        }
        Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE);
    }

    @Override
    public String toString() {
        return "PathStore for " + path;
    }

    private class PollRunnable extends AbstractBinaryStore.BinaryPollRunnable {

        @Override
        public void run() {
            WatchEvent.Kind[] kinds = {
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
            };
            boolean officiallyStarted = false;
            Path folder = path.getParent();
            while (!shouldDie()) {
                try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                    WatchKey watchKey = folder.register(watchService, kinds);
                    while (!shouldDie()) {
                        if (!officiallyStarted) {
                            markWatching();
                            officiallyStarted = true;
                        }
                        WatchKey key = watchService.take();
                        boolean matched = false;
                        List<WatchEvent<?>> events = key.pollEvents();
                        for (WatchEvent<?> event : events) {
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
                            byte[] contents = null;
                            boolean error;
                            try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
                                contents = IOUtil.read(is);
                                error = false;
                            } catch (NoSuchFileException e) {
                                contents = null;
                                error = false; //not an error
                            } catch (IOException e) {
                                error = true;
                                log.error("while reading modified " + path, e);
                            }

                            if (!error) {
                                fire(contents);
                            }
                        }
                        if (!watchKey.reset()) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    if (!shouldDie()) {
                        log.error("interrupted while watching " + path, e);
                    }
                } catch (NoSuchFileException e) {
                    if (!officiallyStarted) {
                        markWatching();
                        officiallyStarted = true;
                    }
                    try {
                        Thread.sleep(100L); //parent dir isnt there. wait to retry
                    } catch (InterruptedException ignored) {
                    }
                } catch (Exception e) {
                    log.error("while watching " + path, e);
                }
            }
        }
    }
}
