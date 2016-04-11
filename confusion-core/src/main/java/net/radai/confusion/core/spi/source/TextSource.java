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

import net.radai.confusion.core.spi.codec.TextCodec;
import net.radai.confusion.core.spi.store.TextStore;
import net.radai.confusion.core.spi.store.TextStoreListener;

import java.io.IOException;

/**
 * Created by Radai Rosenblatt
 */
public class TextSource<T> extends AbstractSource<T> implements TextStoreListener {
    private final Class<T> beanClass;
    private final TextStore store;
    private final TextCodec codec;

    public TextSource(Class<T> beanClass, TextStore store, TextCodec codec) {
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
        String string = store.read();
        return codec.parse(beanClass, string);
    }

    @Override
    public void write(T payload) throws IOException {
        String serialized = codec.serialize(payload);
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
    public void sourceChanged(String newContents) {
        T deserialized = codec.parse(beanClass, newContents);
        fire(deserialized);
    }
}
