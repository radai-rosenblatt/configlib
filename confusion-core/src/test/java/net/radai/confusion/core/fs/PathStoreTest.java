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

import net.radai.confusion.core.spi.store.AbstractBinaryStoreTest;
import net.radai.confusion.core.spi.store.BinaryStore;
import net.radai.confusion.core.spi.store.TracingBinaryStoreListener;
import net.radai.confusion.core.util.IOUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;

/**
 * Created by Radai Rosenblatt
 */
public class PathStoreTest extends AbstractBinaryStoreTest {
    private Path dir;
    private Path file;

    @Before
    public void setup() throws Exception{
        dir = Files.createTempDirectory("test");
        file = dir.resolve("targetFile");
    }

    @Override
    protected PathStore buildStore() throws Exception {
        return new PathStore(file);
    }

    @Override
    protected void writeUnderlying(BinaryStore store, byte[]value) throws Exception {
        if (value == null) {
            try {
                Files.delete(file);
            } catch (NoSuchFileException e) {
                //dont care
            }
            return;
        }
        Path temp = Files.createTempFile(dir, null, null);
        try (OutputStream os = Files.newOutputStream(temp, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            os.write(value);
        }
        Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE);
        FsTestUtil.waitForFsQuiesce();
    }

    @Override
    protected byte[] readUnderlying(BinaryStore store) throws Exception {
        if (!Files.exists(file)) {
            return null;
        }
        try (InputStream is = Files.newInputStream(file, StandardOpenOption.READ)) {
            return IOUtil.read(is);
        }
    }

    @Test
    public void testNullWhenNoFile() throws Exception {
        Assert.assertTrue(Files.exists(dir));
        Assert.assertTrue(Files.notExists(file)); //file doesnt exist when we start off
        PathStore store = buildStore();
        Assert.assertNull(store.read());
    }

    @Test
    public void testLateDirectoryCreation() throws Exception {
        Files.delete(dir);
        Assert.assertTrue(Files.notExists(dir));
        Assert.assertTrue(Files.notExists(file));
        TracingBinaryStoreListener listener = new TracingBinaryStoreListener();
        PathStore store = buildStore();
        store.register(listener);
        store.start();
        Assert.assertNull(store.read());
        try {
            store.write(random());
        } catch (IOException e) {
            //expected
        }
        waitForQuiesce();
        Assert.assertEquals(0, listener.getNumEvents());
        Files.createDirectory(dir);
        Assert.assertTrue(Files.exists(dir));
        waitForQuiesce();
        store.write(random());
        Thread.sleep(200L); //must match sleep-if-not-exists code
        Assert.assertEquals(1, listener.getNumEvents());
    }
}
