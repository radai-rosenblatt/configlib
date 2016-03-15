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

import com.ecwid.consul.transport.TransportException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetBinaryValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.google.common.io.ByteStreams;
import net.radai.confusion.core.fs.TracingListener;
import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Radai Rosenblatt
 */
public class ConsulWatcherTest {

    @BeforeClass
    public static void makeSureConsulExists() {
        try {
            ConsulClient client = new ConsulClient("localhost", 8500);
            client.getStatusLeader();
        } catch (TransportException e) {
            throw new AssumptionViolatedException("expecting to find local concul running at localhost:8500", e);
        }
    }

    @Test
    public void testFetchNull() throws Exception {
        String key = UUID.randomUUID().toString();
        ConsulWatcher watcher = new ConsulWatcher(key);
        Assert.assertNull(watcher.fetch());
    }

    @Test
    public void testFetch() throws Exception {
        Random r = new Random();
        String key = UUID.randomUUID().toString();
        ConsulClient client = new ConsulClient("localhost", 8500);
        byte[] value = new byte[4];
        r.nextBytes(value);
        PutParams params = new PutParams();
        params.setCas(0L);
        Response<Boolean> resp = client.setKVBinaryValue(key, value, params);
        Assert.assertTrue(resp.getValue());
        ConsulWatcher watcher = new ConsulWatcher(key);
        InputStream is = watcher.fetch();
        Assert.assertNotNull(is);
        byte[] bytes = ByteStreams.toByteArray(is);
        Assert.assertArrayEquals(value, bytes);
    }

    @Test
    public void testUpdate() throws Exception {
        Random r = new Random();
        String key = UUID.randomUUID().toString();
        ConsulWatcher watcher = new ConsulWatcher(key);
        byte[] value = new byte[4];
        r.nextBytes(value);
        try (OutputStream os = watcher.store()) {
            os.write(value);
        }
        ConsulClient client = new ConsulClient("localhost", 8500);
        Response<GetBinaryValue> resp = client.getKVBinaryValue(key);
        Assert.assertArrayEquals(value, resp.getValue().getValue());
    }

    @Test
    public void testUpdateNotificationStartNull() throws Exception {
        Random r = new Random();
        String key = UUID.randomUUID().toString();
        ConsulWatcher watcher = new ConsulWatcher(key);
        TracingListener listener = new TracingListener();
        watcher.register(listener);
        watcher.start();

        byte[] value = new byte[4];
        r.nextBytes(value);
        ConsulClient client = new ConsulClient("localhost", 8500);
        client.setKVBinaryValue(key, value);
        Thread.sleep(100L);

        Assert.assertEquals(1, listener.getNumEvents());
        Assert.assertArrayEquals(value, listener.getLatestEvent());
    }

    @Test
    public void testNotNotifiedOfOwnUpdates() throws Exception {
        Random r = new Random();
        byte[] value1 = new byte[4];
        byte[] value2 = new byte[4];
        byte[] value3 = new byte[4];
        r.nextBytes(value1);
        r.nextBytes(value2);
        r.nextBytes(value3);
        String key = UUID.randomUUID().toString();

        ConsulClient client = new ConsulClient("localhost", 8500);
        client.setKVBinaryValue(key, value1);

        ConsulWatcher watcher = new ConsulWatcher("localhost", 8500, key, 1);
        TracingListener listener = new TracingListener();
        watcher.register(listener);
        watcher.start();
        Thread.sleep(100L);
        Assert.assertEquals(0, listener.getNumEvents()); //nothing happened

        try (OutputStream os = watcher.store()) {
            os.write(value2);
        }
        Thread.sleep(100L);
        Assert.assertEquals(0, listener.getNumEvents()); //nothing should happened
        byte[] readValue = ByteStreams.toByteArray(watcher.fetch());
        //and yet value was updated
        Assert.assertArrayEquals(value2, readValue);
        Assert.assertArrayEquals(value2, client.getKVBinaryValue(key).getValue().getValue());

        //now do an external update and assert it was picked up
        client.setKVBinaryValue(key, value3);
        Thread.sleep(100L);
        Assert.assertEquals(1, listener.getNumEvents());
        Assert.assertArrayEquals(value3, listener.getLatestEvent());
    }
}
