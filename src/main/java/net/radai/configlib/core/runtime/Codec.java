package net.radai.configlib.core.runtime;

import java.lang.reflect.Type;

/**
 * @author Radai Rosenblatt
 */
public interface Codec {
    Type getType();
    Object decode(String encoded);
    String encode(Object object);

    Codec NOP_CODEC = new Codec() {
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
            return "NOP";
        }
    };
}
