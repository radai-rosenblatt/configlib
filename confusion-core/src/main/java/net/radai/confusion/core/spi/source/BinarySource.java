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

package net.radai.confusion.core.spi.source;

import net.radai.confusion.core.spi.codec.BinaryCodec;
import net.radai.confusion.core.spi.store.BinaryStore;
import net.radai.confusion.core.spi.store.BinaryStoreListener;

import java.io.IOException;

/**
 * Created by Radai Rosenblatt
 */
public class BinarySource<T> extends AbstractSource<T> implements BinaryStoreListener {
    private final Class<T> beanClass;
    private final BinaryStore store;
    private final BinaryCodec codec;

    public BinarySource(Class<T> beanClass, BinaryStore store, BinaryCodec codec) {
        if (beanClass == null || store == null || codec == null) {
            throw new IllegalArgumentException();
        }
        this.beanClass = beanClass;
        this.store = store;
        this.codec = codec;
        this.store.register(this);
    }

    @Override
    public T read() throws IOException {
        byte[] blob = store.read();
        return codec.parse(beanClass, blob);
    }

    @Override
    public void write(T payload) throws IOException {
        byte[] serialized = codec.serialize(payload);
        store.write(serialized);
    }

    @Override
    public void start() {
        store.start();
    }

    @Override
    public void stop() {
        store.stop();
    }

    @Override
    public void sourceChanged(byte[] newContents) {
        T deserialized = codec.parse(beanClass, newContents);
        fire(deserialized);
    }
}
