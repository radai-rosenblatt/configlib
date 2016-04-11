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

import net.radai.confusion.core.spi.store.AbstractTextStore;

import java.io.IOException;

/**
 * Created by Radai Rosenblatt
 */
public class InMemTextStore extends AbstractTextStore {
    private final Object lock = new Object();
    private volatile String payload;

    @Override
    protected TextPollRunnable createRunnable() {
        return new TextPollRunnable() {
            @Override
            public void run() {
                String lastTime = payload;
                String sample;
                markWatching();
                while (!shouldDie()) {
                    try {
                        synchronized (lock) {
                            //noinspection StringEquality
                            while ((sample = payload) == lastTime) { //we change for exact same instance, not equals
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
    public String read() throws IOException {
        return copy(payload);
    }

    @Override
    public void write(String payload) throws IOException {
        String copy = copy(payload);
        synchronized (lock) {
            this.payload = copy;
            lock.notifyAll();
        }
    }

    private static String copy(String payload) { //returns a different instance ON PURPOSE
        //noinspection RedundantStringConstructorCall
        return payload == null ? null : new String(payload);
    }

    @Override
    public String toString() {
        return "in-mem text";
    }
}
