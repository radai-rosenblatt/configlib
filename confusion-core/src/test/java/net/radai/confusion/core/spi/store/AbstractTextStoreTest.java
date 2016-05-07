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
public abstract class AbstractTextStoreTest extends AbstractStoreTest {
    protected abstract TextStore buildStore() throws Exception;
    protected abstract void writeUnderlying(TextStore store, String value) throws Exception;
    protected abstract String readUnderlying(TextStore store) throws Exception;
    protected void waitForQuiesce() throws Exception {
        Thread.sleep(150L);
    }

    @Test
    public void testTypeGetterValue() throws Exception {
        Assert.assertEquals(PayloadType.TEXT, buildStore().getPayloadType());
    }

    @Test
    public void testRead() throws Exception {
        String data = random();
        TextStore store = buildStore();
        writeUnderlying(store, data);
        String read = store.read();
        Assert.assertEquals(data, read);
    }

    @Test
    public void testReadNull() throws Exception {
        TextStore store = buildStore();
        writeUnderlying(store, null);
        Assert.assertNull(readUnderlying(store));
        Assert.assertNull(store.read());
    }

    @Test
    public void testWrite() throws Exception {
        String data = random();
        TextStore store = buildStore();
        store.write(data);
        String written = readUnderlying(store);
        Assert.assertEquals(data, written);
    }

    @Test
    public void testEmptyString() throws Exception {
        String data = "";
        TextStore store = buildStore();
        store.write(data);
        String written = readUnderlying(store);
        Assert.assertEquals(data, written);
        String read = store.read();
        Assert.assertEquals(data, read);
    }

    @Test
    public void testNullTransitions() throws Exception {
        TracingTextStoreListener listener = new TracingTextStoreListener();
        TextStore store = buildStore();
        store.register(listener);
        store.start();
        waitForQuiesce();

        //initial random state
        String data = random();
        store.write(data);
        waitForQuiesce();
        Assert.assertEquals(data, store.read());
        Assert.assertEquals(data, listener.getEvent(0));

        //!null --> null
        store.write(null);
        waitForQuiesce();
        Assert.assertNull(store.read());
        Assert.assertNull(listener.getEvent(1));

        //double null
        store.write(null);
        waitForQuiesce();
        Assert.assertNull(store.read());
        Assert.assertEquals(2, listener.getNumEvents()); //no new event

        //null --> !null
        data = random();
        store.write(data);
        waitForQuiesce();
        Assert.assertEquals(data, store.read());
        Assert.assertEquals(data, listener.getEvent(2));
    }

    @Test
    public void testNoEventsBeforeStart() throws Exception {
        TracingTextStoreListener listener = new TracingTextStoreListener();
        TextStore store = buildStore();
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
        TracingTextStoreListener listener = new TracingTextStoreListener();
        TextStore store = buildStore();
        store.register(listener);
        store.start();
        waitForQuiesce();
        Assert.assertEquals(0, listener.getNumEvents());
        String data = random();
        writeUnderlying(store, data);
        waitForQuiesce();
        Assert.assertEquals(1, listener.getNumEvents());
        Assert.assertEquals(data, listener.getEvent(0));
    }

    @Test
    public void testNoEventsAfterStopped() throws Exception {
        TracingTextStoreListener listener = new TracingTextStoreListener();
        TextStore store = buildStore();
        store.register(listener);
        store.start();
        waitForQuiesce();
        store.stop();
        waitForQuiesce();
        writeUnderlying(store, random());
        waitForQuiesce();
        Assert.assertEquals(0, listener.getNumEvents());
    }

    @Test
    public void testEventOnWrite() throws Exception {
        TracingTextStoreListener listener = new TracingTextStoreListener();
        TextStore store = buildStore();
        store.register(listener);
        store.start();
        waitForQuiesce();
        String value = random();
        store.write(value);
        waitForQuiesce();
        Assert.assertEquals(1, listener.getNumEvents());
        Assert.assertEquals(value, listener.getLatestEvent());
        store.write(null);
        waitForQuiesce();
        Assert.assertEquals(2, listener.getNumEvents());
        Assert.assertNull(listener.getLatestEvent());
        if (store.reportsConsecutiveNulls()) {
            store.write(null);
            waitForQuiesce();
            Assert.assertEquals(3, listener.getNumEvents());
            Assert.assertNull(listener.getLatestEvent());
        }
    }

    protected String random() {
        return RandomUtil.randomString(new Random());
    }
}
