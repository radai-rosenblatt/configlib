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

package net.radai.confusion.core.spi.store;

import net.radai.confusion.core.spi.PayloadType;
import net.radai.confusion.test.RandomUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * Created by Radai Rosenblatt
 */
public abstract class AbstractBinaryStoreTest extends AbstractStoreTest {

    protected abstract BinaryStore buildStore() throws Exception;
    protected abstract void writeUnderlying(BinaryStore store, byte[] value) throws Exception;
    protected abstract byte[] readUnderlying(BinaryStore store) throws Exception;

    @Test
    public void testTypeGetterValue() throws Exception {
        Assert.assertEquals(PayloadType.BINARY, buildStore().getPayloadType());
    }

    @Test
    public void testRead() throws Exception {
        byte[] data = random();
        BinaryStore store = buildStore();
        writeUnderlying(store, data);
        byte[] read = store.read();
        Assert.assertArrayEquals(data, read);
    }

    @Test
    public void testReadNull() throws Exception {
        BinaryStore store = buildStore();
        writeUnderlying(store, null);
        Assert.assertNull(readUnderlying(store));
        Assert.assertNull(store.read());
    }

    @Test
    public void testWrite() throws Exception {
        byte[] data = random();
        BinaryStore store = buildStore();
        store.write(data);
        byte[] written = readUnderlying(store);
        Assert.assertArrayEquals(data, written);
    }

    @Test
    public void testEmptyArray() throws Exception {
        byte[] data = new byte[0];
        BinaryStore store = buildStore();
        store.write(data);
        byte[] written = readUnderlying(store);
        Assert.assertArrayEquals(data, written);
        byte[] read = store.read();
        Assert.assertArrayEquals(data, read);
    }

    @Test
    public void testNullTransitions() throws Exception {
        TracingBinaryStoreListener listener = new TracingBinaryStoreListener();
        BinaryStore store = buildStore();
        store.register(listener);
        store.start();
        waitForQuiesce();

        //initial random state
        byte[] data = random();
        store.write(data);
        waitForQuiesce();
        Assert.assertArrayEquals(data, store.read());
        Assert.assertArrayEquals(data, listener.getEvent(0));

        //!null --> null
        store.write(null);
        waitForQuiesce();
        Assert.assertNull(store.read());
        Assert.assertEquals(2, listener.getNumEvents());
        Assert.assertNull(listener.getEvent(1));

        //double null
        store.write(null);
        waitForQuiesce();
        Assert.assertNull(store.read());
        int numEvents;
        if (store.reportsConsecutiveNulls()) {
            numEvents = 3;
            Assert.assertNull(listener.getEvent(2));
        } else {
            numEvents = 2;
        }
        Assert.assertEquals(numEvents, listener.getNumEvents());

        //null --> !null
        data = random();
        store.write(data);
        waitForQuiesce();
        Assert.assertArrayEquals(data, store.read());
        Assert.assertArrayEquals(data, listener.getLatestEvent());
    }

    @Test
    public void testNoEventsBeforeStart() throws Exception {
        TracingBinaryStoreListener listener = new TracingBinaryStoreListener();
        BinaryStore store = buildStore();
        store.register(listener);
        waitForQuiesce();
        Assert.assertEquals(0, listener.getNumEvents());
        writeUnderlying(store, random());
        writeUnderlying(store, random());
        waitForQuiesce();
        Assert.assertEquals(0, listener.getNumEvents());
    }

    @Test
    public void testEventsWhileStarted() throws Exception {
        TracingBinaryStoreListener listener = new TracingBinaryStoreListener();
        BinaryStore store = buildStore();
        store.register(listener);
        store.start();
        waitForQuiesce();
        Assert.assertEquals(0, listener.getNumEvents());
        byte[] data = random();
        writeUnderlying(store, data);
        waitForQuiesce();
        Assert.assertEquals(1, listener.getNumEvents());
        Assert.assertArrayEquals(data, listener.getEvent(0));
    }

    @Test
    public void testNoEventsAfterStopped() throws Exception {
        TracingBinaryStoreListener listener = new TracingBinaryStoreListener();
        BinaryStore store = buildStore();
        store.register(listener);
        store.start();
        waitForQuiesce();
        store.stop();
        waitForQuiesce();
        writeUnderlying(store, random());
        waitForQuiesce();
        Assert.assertEquals(0, listener.getNumEvents());
    }

    protected byte[] random() {
        return RandomUtil.randomBlob(new Random());
    }
}
