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

package net.radai.confusion.core.inmem;

import net.radai.confusion.core.spi.store.AbstractBinaryStore;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Radai Rosenblatt
 */
public class InMemBinaryStore extends AbstractBinaryStore {
    private final Object lock = new Object();
    private volatile byte[] payload;

    @Override
    protected BinaryPollRunnable createRunnable() {
        return new BinaryPollRunnable() {
            @Override
            public void run() {
                byte[] lastTime = payload;
                byte[] sample;
                markWatching();
                while (!shouldDie()) {
                    try {
                        synchronized (lock) {
                            while ((sample = payload) == lastTime) {
                                lock.wait();
                            }
                        }
                        lastTime = sample;
                        fire(copy(sample));
                    } catch (Exception e) {
                        if (!shouldDie()) {
                            e.printStackTrace(System.err);
                        }
                    }
                }
            }
        };
    }

    @Override
    public byte[] read() throws IOException {
        return copy(payload);
    }

    @Override
    public void write(byte[] payload) throws IOException {
        byte[] copy = copy(payload);
        synchronized (lock) {
            this.payload = copy;
            lock.notifyAll();
        }
    }

    private static byte[] copy(byte[] payload) { //returns a different instance ON PURPOSE
        return payload == null ? null : Arrays.copyOf(payload, payload.length);
    }

    @Override
    public String toString() {
        return "in-mem binary";
    }
}
