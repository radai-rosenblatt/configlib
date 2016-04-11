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
import net.radai.confusion.core.spi.store.*;
import org.junit.*;

import java.util.UUID;

/**
 * Created by Radai Rosenblatt
 */
public class ConsulStoreTest extends AbstractBinaryStoreTest {
    private String key;

    @BeforeClass
    public static void makeSureConsulExists() {
        try {
            ConsulClient client = new ConsulClient("localhost", 8500);
            client.getStatusLeader();
        } catch (TransportException e) {
            throw new AssumptionViolatedException("expecting to find local consul running at localhost:8500", e);
        }
    }

    @Before
    public void setup() {
        key = UUID.randomUUID().toString();
    }

    @Override
    protected BinaryStore buildStore() throws Exception {
        return new ConsulStore(key);
    }

    @Override
    protected void writeUnderlying(BinaryStore store, byte[] value) throws Exception {
        ConsulClient client = new ConsulClient("localhost", 8500);
        if (value == null) {
            client.deleteKVValue(key).getValue();
            return;
        }
        Response<Boolean> resp = client.setKVBinaryValue(key, value);
        Assert.assertTrue(resp.getValue());
    }

    @Override
    protected byte[] readUnderlying(BinaryStore store) throws Exception {
        ConsulClient client = new ConsulClient("localhost", 8500);
        Response<GetBinaryValue> resp = client.getKVBinaryValue(key);
        return ConsulStore.decode(resp.getValue());
    }
}
