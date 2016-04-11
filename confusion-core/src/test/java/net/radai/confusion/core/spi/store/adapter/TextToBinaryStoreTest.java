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

import net.radai.confusion.core.inmem.InMemTextStore;
import net.radai.confusion.core.spi.store.AbstractBinaryStoreTest;
import net.radai.confusion.core.spi.store.BinaryStore;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by Radai Rosenblatt
 */
public class TextToBinaryStoreTest extends AbstractBinaryStoreTest {
    @Override
    protected BinaryStore buildStore() throws Exception {
        InMemTextStore textStore = new InMemTextStore();
        return new TextToBinaryStore(textStore, Adapter.BASE64);
    }

    @Override
    protected void writeUnderlying(BinaryStore store, byte[] value) throws Exception {
        TextToBinaryStore cast = (TextToBinaryStore) store;
        InMemTextStore inMem = (InMemTextStore) ReflectionTestUtils.getField(cast, "delegate");
        inMem.write(Adapter.BASE64.toText(value));
    }

    @Override
    protected byte[] readUnderlying(BinaryStore store) throws Exception {
        TextToBinaryStore cast = (TextToBinaryStore) store;
        InMemTextStore inMem = (InMemTextStore) ReflectionTestUtils.getField(cast, "delegate");
        return Adapter.BASE64.toBinary(inMem.read());
    }
}
