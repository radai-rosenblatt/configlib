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

package net.radai.confusion.core.spi.store.adapter;

import net.radai.confusion.core.spi.store.BinaryStore;
import net.radai.confusion.core.spi.store.BinaryStoreListener;
import net.radai.confusion.core.spi.store.TextStore;
import net.radai.confusion.core.spi.store.TextStoreListener;
import net.radai.confusion.core.util.Listeners;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Created by Radai Rosenblatt
 */
public class BinaryToTextStore implements TextStore, BinaryStoreListener {
    private final Logger log = LogManager.getLogger(getClass());
    private final Listeners<TextStoreListener> listeners = new Listeners<>();
    private final BinaryStore delegate;
    private final Adapter adapter;

    public BinaryToTextStore(BinaryStore delegate, Adapter adapter) {
        if (delegate == null || adapter == null) {
            throw new IllegalArgumentException();
        }
        this.delegate = delegate;
        this.adapter = adapter;
        this.delegate.register(this);
    }

    @Override
    public String read() throws IOException {
        byte[] binary = delegate.read();
        return adapter.toText(binary);
    }

    @Override
    public void write(String payload) throws IOException {
        byte[] binary = adapter.toBinary(payload);
        delegate.write(binary);
    }

    @Override
    public void register(TextStoreListener newListener) {
        listeners.register(newListener);
    }

    @Override
    public void unregister(TextStoreListener existingListener) {
        listeners.unregister(existingListener);
    }

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public void sourceChanged(byte[] newContents) {
        String text = adapter.toText(newContents);
        listeners.forEach(listener -> {
            try {
                listener.sourceChanged(text);
            } catch (Exception e) {
                log.error("caught while firing change event", e);
            }
        });
    }

    @Override
    public String toString() {
        return adapter.toString() + "@" + delegate.toString();
    }
}
