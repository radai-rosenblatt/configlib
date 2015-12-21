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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Radai Rosenblatt
 */
public class Codecs {
    public static final Codec NOP_CODEC = new Codec() {
        @Override
        public Type getType() {
            return String.class;
        }

        @Override
        public Object decode(String encoded) {
            return encoded;
        }

        @Override
        public String encode(Object object) {
            return (String) object;
        }

        @Override
        public String toString() {
            return "String codec: NOP";
        }
    };

    public static final Codec CHAR_CODEC = new Codec() {
        @Override
        public Type getType() {
            return Character.class;
        }

        @Override
        public Object decode(String encoded) {
            if (encoded == null || encoded.length() != 1) {
                throw new IllegalArgumentException();
            }
            return encoded.charAt(0);
        }

        @Override
        public String encode(Object object) {
            char c = (Character) object;
            return String.valueOf(c);
        }

        @Override
        public String toString() {
            return "Character codec : special sauce";
        }
    };

    public static final Map<Type, Codec> BUILT_INS = new HashMap<>();

    static {
        BUILT_INS.put(Boolean.class, codecFor(Boolean.class));
        BUILT_INS.put(Byte.class, codecFor(Byte.class));
        BUILT_INS.put(Character.class, CHAR_CODEC); //has to be special
        BUILT_INS.put(Short.class, codecFor(Short.class));
        BUILT_INS.put(Integer.class, codecFor(Integer.class));
        BUILT_INS.put(Long.class, codecFor(Long.class));
        BUILT_INS.put(Float.class, codecFor(Float.class));
        BUILT_INS.put(Double.class, codecFor(Double.class));
        BUILT_INS.put(String.class, NOP_CODEC);

        //reflection boxes primitives anyway
        BUILT_INS.put(boolean.class, BUILT_INS.get(Boolean.class));
        BUILT_INS.put(byte.class, BUILT_INS.get(Byte.class));
        BUILT_INS.put(char.class, BUILT_INS.get(Character.class));
        BUILT_INS.put(short.class, BUILT_INS.get(Short.class));
        BUILT_INS.put(int.class, BUILT_INS.get(Integer.class));
        BUILT_INS.put(long.class, BUILT_INS.get(Long.class));
        BUILT_INS.put(float.class, BUILT_INS.get(Float.class));
        BUILT_INS.put(double.class, BUILT_INS.get(Double.class));
    }

    private static SimpleCodec codecFor(Class clazz) {
        try {
            //noinspection unchecked
            return new SimpleCodec(clazz, clazz.getMethod("toString"), clazz.getMethod("valueOf", String.class));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}
