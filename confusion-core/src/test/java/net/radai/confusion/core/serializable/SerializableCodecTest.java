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

import net.radai.confusion.core.spi.codec.AbstractBinaryCodecTest;
import net.radai.confusion.test.RandomUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.Objects;
import java.util.Random;

/**
 * Created by Radai Rosenblatt
 */
public class SerializableCodecTest extends AbstractBinaryCodecTest {

    @Override
    protected SerializableCodec buildCodec() {
        return new SerializableCodec();
    }

    @Override
    protected Class getTestClass() {
        return PayloadClass.class;
    }

    @Test
    public void testRoundTrip() throws Exception {
        SerializableCodec codec = buildCodec();
        PayloadClass original = random();
        byte[] serialized = codec.serialize(original);
        PayloadClass deserialized = codec.parse(PayloadClass.class, serialized);
        Assert.assertEquals(original, deserialized);
    }

    private static PayloadClass random() {
        Random random = new Random();
        int a = random.nextInt();
        String b = random.nextDouble() < 0.5 ? null : RandomUtil.randomString(random);
        return new PayloadClass(a, b);
    }

    public static class PayloadClass implements Serializable {
        private static final long serialVersionUID = 1L;

        public PayloadClass(int a, String b) {
            this.a = a;
            this.b = b;
        }

        private int a;
        private String b;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PayloadClass that = (PayloadClass) o;
            return a == that.a && Objects.equals(b, that.b);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b);
        }
    }
}
