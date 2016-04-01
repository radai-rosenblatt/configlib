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

import com.google.common.io.ByteStreams;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.responses.EtcdLeaderStatsResponse;
import net.radai.confusion.core.fs.TracingListener;
import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Radai Rosenblatt
 */
public class EtcdWatcherTest {
    @BeforeClass
    public static void makeSureEtcdExists() throws Exception{
        try (EtcdClient client = new EtcdClient(URI.create("http://localhost:4001"))){
            EtcdLeaderStatsResponse resp2 = client.getLeaderStats();
            if (resp2 == null) {
                throw new AssumptionViolatedException("expecting to find local etcd running at localhost:4001");
            }
        }
    }

    @Test
    public void testFetchNull() throws Exception {
        String key = UUID.randomUUID().toString();
        EtcdWatcher watcher = new EtcdWatcher("http://localhost:4001", key, true);
        Assert.assertNull(watcher.fetch());
    }

    @Test
    public void testFetch() throws Exception {
        Random r = new Random();
        String key = UUID.randomUUID().toString();
        EtcdClient client = new EtcdClient(URI.create("http://localhost:4001"));
        byte[] value = new byte[4];
        r.nextBytes(value);
        client.put(key, Base64.getEncoder().encodeToString(value)).send().get();
        EtcdWatcher watcher = new EtcdWatcher("http://localhost:4001", key, true);
        InputStream is = watcher.fetch();
        Assert.assertNotNull(is);
        byte[] bytes = ByteStreams.toByteArray(is);
        Assert.assertArrayEquals(value, bytes);
    }

    @Test
    public void testUpdate() throws Exception {
        Random r = new Random();
        String key = UUID.randomUUID().toString();
        EtcdWatcher watcher = new EtcdWatcher("http://localhost:4001", key, true);
        byte[] value = new byte[4];
        r.nextBytes(value);
        try (OutputStream os = watcher.store()) {
            os.write(value);
        }
        EtcdClient client = new EtcdClient(URI.create("http://localhost:4001"));
        EtcdKeysResponse resp = client.get(key).send().get();
        byte[] returned = Base64.getDecoder().decode(resp.node.value);
        Assert.assertArrayEquals(value, returned);
    }

    @Test
    public void testUpdateNotificationStartNull() throws Exception {
        Random r = new Random();
        String key = UUID.randomUUID().toString();
        EtcdWatcher watcher = new EtcdWatcher("http://localhost:4001", key, true);
        TracingListener listener = new TracingListener();
        watcher.register(listener);
        watcher.start();

        byte[] value = new byte[4];
        r.nextBytes(value);
        EtcdClient client = new EtcdClient(URI.create("http://localhost:4001"));
        client.put(key, Base64.getEncoder().encodeToString(value)).send().get();
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

        EtcdClient client = new EtcdClient(URI.create("http://localhost:4001"));
        client.put(key, Base64.getEncoder().encodeToString(value1)).send().get();

        EtcdWatcher watcher = new EtcdWatcher("http://localhost:4001", key, true);
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
        Assert.assertArrayEquals(value2, Base64.getDecoder().decode(client.get(key).send().get().node.value));

        //now do an external update and assert it was picked up
        client.put(key, Base64.getEncoder().encodeToString(value3)).send().get();
        Thread.sleep(100L);
        Assert.assertEquals(1, listener.getNumEvents());
        Assert.assertArrayEquals(value3, listener.getLatestEvent());
    }
}
