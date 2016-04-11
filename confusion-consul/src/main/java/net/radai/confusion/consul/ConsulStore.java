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

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetBinaryValue;
import net.radai.confusion.core.spi.store.AbstractBinaryStore;

import java.io.*;

/**
 * Created by Radai Rosenblatt
 */
public class ConsulStore extends AbstractBinaryStore {

    //input
    private final String host;
    private final int port;
    private final String key;
    private final long pollSeconds;

    //components
    private final ConsulClient client;

    public ConsulStore(String key) {
        this("localhost", 8500, key, 3);
    }

    public ConsulStore(String host, int port, String key) {
        this(host, port, key, 30);
    }

    public ConsulStore(String host, int port, String key, long pollSeconds) {
        this.host = host;
        this.port = port;
        this.key = key;
        this.pollSeconds = pollSeconds;
        this.client = new ConsulClient(host, port);
    }

    private final static byte[] EMPTY = new byte[0];
    //package private ON PURPOSE
    static byte[] decode(GetBinaryValue response) {
        if (response == null) {
            return null;
        }
        byte[] blob = response.getValue();
        if (blob == null) {
            return EMPTY;
        }
        return blob;
    }

    @Override
    public byte[] read() throws IOException {
        Response<GetBinaryValue> response = client.getKVBinaryValue(key);
        GetBinaryValue value = response.getValue();
        return decode(value);
    }

    @Override
    public void write(byte[] payload) throws IOException {
        if (payload == null) {
            client.deleteKVValue(key).getValue();
            return;
        }
        Response<Boolean> response = client.setKVBinaryValue(key, payload);
        if (!response.getValue()) {
            //we dont do anything conditionally. should succeed.
            throw new IOException("response from server was false");
        }
    }

    @Override
    protected BinaryPollRunnable createRunnable() {
        return new PollRunnable();
    }

    private class PollRunnable extends AbstractBinaryStore.BinaryPollRunnable {

        @Override
        public void run() {
            Response<GetBinaryValue> response;
            GetBinaryValue value;

            //establish starting state
            response = client.getKVBinaryValue(key);
            value = response.getValue();

            //latest index is either key modification (if key exists) or server global
            boolean isNull = value == null;
            long lastModIndex = isNull ? response.getConsulIndex() : value.getModifyIndex();

            while (!shouldDie()) {
                //go fetch
                markWatching();
                response = client.getKVBinaryValue(key, new QueryParams(pollSeconds, lastModIndex));
                value = response.getValue();
                byte[] blob = decode(value);
                boolean fire = false;

                if (value == null) {
                    if (!isNull) { //!null --> null state change
                        isNull = true;
                        fire = true;
                    }
                } else {
                    isNull = false;
                    long currentModIndex = value.getModifyIndex();
                    if (currentModIndex != lastModIndex) {
                        lastModIndex = currentModIndex;
                        fire = true;
                    }
                }

                if (fire) {
                    fire(blob);
                }
            }
        }
    }
}
