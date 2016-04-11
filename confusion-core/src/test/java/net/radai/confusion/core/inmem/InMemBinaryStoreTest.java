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

import net.radai.confusion.core.spi.store.AbstractBinaryStoreTest;
import net.radai.confusion.core.spi.store.BinaryStore;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by Radai Rosenblatt
 */
public class InMemBinaryStoreTest extends AbstractBinaryStoreTest {
    @Override
    protected BinaryStore buildStore() throws Exception {
        return new InMemBinaryStore();
    }

    @Override
    protected void writeUnderlying(BinaryStore store, byte[] value) throws Exception {
        InMemBinaryStore inMem = (InMemBinaryStore) store;
        ReflectionTestUtils.setField(inMem, "payload", value);
        final Object lock = ReflectionTestUtils.getField(inMem, "lock");
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Override
    protected byte[] readUnderlying(BinaryStore store) throws Exception {
        InMemBinaryStore inMem = (InMemBinaryStore) store;
        return (byte[]) ReflectionTestUtils.getField(inMem, "payload");
    }
}
