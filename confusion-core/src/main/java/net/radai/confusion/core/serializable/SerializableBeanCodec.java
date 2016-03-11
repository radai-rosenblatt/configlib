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

package net.radai.confusion.core.serializable;

import net.radai.confusion.core.spi.BeanCodec;

import java.io.*;

/**
 * Created by Radai Rosenblatt
 */
public class SerializableBeanCodec implements BeanCodec {

    @Override
    public <T> T parse(Class<T> beanClass, InputStream from) throws IOException {
        try (ObjectInputStream is = new ObjectInputStream(from)) {
            //noinspection unchecked
            return (T) is.readObject();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <T> void serialize(T beanInstance, OutputStream to) throws IOException {
        try (ObjectOutputStream os = new ObjectOutputStream(to)) {
            os.writeObject(beanInstance);
        }
    }
}
