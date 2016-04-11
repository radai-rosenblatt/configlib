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

import net.radai.confusion.core.inmem.InMemBinaryStore;
import net.radai.confusion.core.spi.store.AbstractTextStoreTest;
import net.radai.confusion.core.spi.store.TextStore;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by Radai Rosenblatt
 */
public class BinaryToTextStoreTest extends AbstractTextStoreTest {
    @Override
    protected BinaryToTextStore buildStore() throws Exception {
        InMemBinaryStore binaryStore = new InMemBinaryStore();
        return new BinaryToTextStore(binaryStore, Adapter.UTF8);
    }

    @Override
    protected void writeUnderlying(TextStore store, String value) throws Exception {
        BinaryToTextStore cast = (BinaryToTextStore) store;
        InMemBinaryStore inMem = (InMemBinaryStore) ReflectionTestUtils.getField(cast, "delegate");
        byte[] binary = Adapter.UTF8.toBinary(value);
        inMem.write(binary);
    }

    @Override
    protected String readUnderlying(TextStore store) throws Exception {
        BinaryToTextStore cast = (BinaryToTextStore) store;
        InMemBinaryStore inMem = (InMemBinaryStore) ReflectionTestUtils.getField(cast, "delegate");
        byte[] binary = inMem.read();
        return Adapter.UTF8.toText(binary);
    }
}
