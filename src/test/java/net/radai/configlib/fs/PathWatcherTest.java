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

import com.google.common.io.ByteStreams;
import org.junit.Assert;
import org.junit.Test;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Created by Radai Rosenblatt
 */
public class PathWatcherTest {
    private static final long INTERVAL = 100L;

    @Test
    public void testBasicScenario() throws Exception {
        Path dir = Files.createTempDirectory("test");
        Path targetFile = dir.resolve("targetFile");
        Assert.assertTrue(Files.exists(dir));
        Assert.assertTrue(Files.notExists(targetFile)); //file doesnt exist when we start off
        PathWatcher watcher = new PathWatcher(targetFile);
        TracingListener listener = new TracingListener();
        watcher.register(listener);
        watcher.start();
        Thread.sleep(INTERVAL);
        Assert.assertEquals(0, listener.getNumEvents()); //nothing happened
        Files.createFile(targetFile);
        Assert.assertTrue(Files.exists(targetFile));
        Thread.sleep(INTERVAL);
        Assert.assertEquals(1, listener.getNumEvents());
        Assert.assertArrayEquals(new byte[] {}, listener.getLatestEvent());
        try (OutputStream os = Files.newOutputStream(targetFile, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            os.write(new byte[]{1, 2, 3, 4});
        }
        Thread.sleep(INTERVAL);
        Assert.assertEquals(2, listener.getNumEvents());
        Assert.assertArrayEquals(new byte[] {1, 2, 3, 4}, listener.getLatestEvent());
        try (OutputStream os = Files.newOutputStream(targetFile, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            os.write(new byte[]{5});
        }
        Thread.sleep(INTERVAL);
        Assert.assertEquals(3, listener.getNumEvents());
        Assert.assertArrayEquals(new byte[] {1, 2, 3, 4, 5}, listener.getLatestEvent());
    }

    @Test
    public void testStartStop() throws Exception {
        Path dir = Files.createTempDirectory("test");
        Path targetFile = dir.resolve("targetFile");
        PathWatcher watcher = new PathWatcher(targetFile);
        TracingListener listener = new TracingListener();
        watcher.register(listener);
        try (OutputStream os = Files.newOutputStream(targetFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            os.write(new byte[]{1, 2, 3, 4});
        }
        Thread.sleep(INTERVAL);
        Assert.assertEquals(0, listener.getNumEvents()); //not started
        watcher.start();
        Thread.sleep(INTERVAL);
        Assert.assertEquals(0, listener.getNumEvents()); //nothing happened
        try (OutputStream os = Files.newOutputStream(targetFile, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            os.write(new byte[]{5, 6, 7, 8});
        }
        Thread.sleep(INTERVAL);
        Assert.assertEquals(1, listener.getNumEvents());
        Assert.assertArrayEquals(new byte[] {1, 2, 3, 4, 5, 6, 7, 8}, listener.getLatestEvent());
        watcher.stop();
        try (OutputStream os = Files.newOutputStream(targetFile, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            os.write(new byte[]{9, 10});
        }
        Thread.sleep(INTERVAL);
        Assert.assertEquals(1, listener.getNumEvents()); //stopped
        watcher.start();
        Thread.sleep(INTERVAL);
        Assert.assertEquals(1, listener.getNumEvents()); //nothing happened
        try (OutputStream os = Files.newOutputStream(targetFile, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            os.write(new byte[]{11, 12});
        }
        Thread.sleep(INTERVAL);
        Assert.assertEquals(2, listener.getNumEvents());
        Assert.assertArrayEquals(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, listener.getLatestEvent());
    }

    @Test
    public void testFetch() throws Exception {
        Path dir = Files.createTempDirectory("test");
        Path targetFile = dir.resolve("targetFile");
        PathWatcher watcher = new PathWatcher(targetFile);
        try (OutputStream os = Files.newOutputStream(targetFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            os.write(new byte[]{1, 2, 3, 4});
        }
        Assert.assertArrayEquals(new byte[] {1, 2, 3, 4}, ByteStreams.toByteArray(watcher.fetch()));
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleStart() throws Exception {
        Path dir = Files.createTempDirectory("test");
        Path targetFile = dir.resolve("targetFile");
        PathWatcher watcher = new PathWatcher(targetFile);
        watcher.start();
        watcher.start();
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleStop() throws Exception {
        Path dir = Files.createTempDirectory("test");
        Path targetFile = dir.resolve("targetFile");
        PathWatcher watcher = new PathWatcher(targetFile);
        watcher.start();
        watcher.stop();
        watcher.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void testStopBeforeStart() throws Exception {
        Path dir = Files.createTempDirectory("test");
        Path targetFile = dir.resolve("targetFile");
        PathWatcher watcher = new PathWatcher(targetFile);
        watcher.stop();
    }
}
