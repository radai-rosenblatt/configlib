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
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdLeaderStatsResponse;
import net.radai.confusion.core.spi.store.AbstractTextStoreTest;
import net.radai.confusion.core.spi.store.TextStore;
import org.junit.*;

import java.net.URI;
import java.util.UUID;

/**
 * Created by Radai Rosenblatt
 */
public class EtcdStoreTest extends AbstractTextStoreTest {
    private String key;

    @BeforeClass
    public static void makeSureEtcdExists() throws Exception{
        try (EtcdClient client = new EtcdClient(URI.create("http://localhost:4001"))){
            EtcdLeaderStatsResponse resp2 = client.getLeaderStats();
            if (resp2 == null) {
                throw new AssumptionViolatedException("expecting to find local etcd running at localhost:4001");
            }
        }
    }

    @Before
    public void setup() {
        key = UUID.randomUUID().toString();
    }

    @Override
    protected TextStore buildStore() throws Exception {
        return new EtcdStore("http://localhost:4001", key);
    }

    @Override
    protected void writeUnderlying(TextStore store, String value) throws Exception {
        try (EtcdClient client = new EtcdClient(URI.create("http://localhost:4001"))) {
            if (value == null) {
                client.delete(key).send().get();
            } else {
                client.put(key, value).send().get();
            }
        } catch (EtcdException e) {
            if (value == null && e.errorCode == 100) {
                return; //double deletion would do this
            }
            throw e;
        }
    }

    @Override
    protected String readUnderlying(TextStore store) throws Exception {
        try (EtcdClient client = new EtcdClient(URI.create("http://localhost:4001"))) {
            return client.get(key).send().get().node.value;
        } catch (EtcdException e) {
            if (e.errorCode == 100) {
                return null;
            }
            throw e;
        }
    }
}
