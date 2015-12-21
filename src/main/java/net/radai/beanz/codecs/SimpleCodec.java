/*
 * This file is part of ConfigLib.
 *
 * ConfigLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ConfigLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ConfigLib.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of ConfigLib.
 *
 * ConfigLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ConfigLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ConfigLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.radai.beanz.codecs;

import net.radai.beanz.api.Codec;
import net.radai.beanz.util.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created by Radai Rosenblatt
 */
public class SimpleCodec implements Codec {
    private Type type;
    private Method encodeMethod = null;
    private Method decodeMethod = null;

    public SimpleCodec(Type type, Method encodeMethod, Method decodeMethod) {
        if (type == null || encodeMethod == null || decodeMethod == null || !ReflectionUtil.isStatic(decodeMethod)) {
            throw new IllegalArgumentException();
        }
        this.type = type;
        this.encodeMethod = encodeMethod;
        this.decodeMethod = decodeMethod;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Object decode(String encoded) {
        try {
            return decodeMethod.invoke(null, encoded);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String encode(Object object) {
        try {
            if (ReflectionUtil.isStatic(encodeMethod)) {
                return (String) encodeMethod.invoke(null, object);
            } else {
                return (String) encodeMethod.invoke(object);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return ReflectionUtil.prettyPrint(getType()) + " codec: " + ReflectionUtil.prettyPrint(encodeMethod) + " / " + ReflectionUtil.prettyPrint(decodeMethod);
    }
}
