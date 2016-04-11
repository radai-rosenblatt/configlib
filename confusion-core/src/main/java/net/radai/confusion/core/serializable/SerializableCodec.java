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

import net.radai.confusion.core.spi.codec.BinaryCodec;

import java.io.*;

/**
 * Created by Radai Rosenblatt
 */
public class SerializableCodec implements BinaryCodec {

    @Override
    public <T> T parse(Class<T> beanClass, byte[] from) {
        if (from == null) {
            return null;
        }
        try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(from))) {
            //noinspection unchecked
            return (T) is.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <T> byte[] serialize(T beanInstance) {
        if (beanInstance == null) {
            return null;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(beanInstance);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return os.toByteArray();
    }
}
