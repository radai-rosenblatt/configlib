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

package net.radai.confusion.core.jaxb;

import net.radai.confusion.core.jaxb.JaxbCodec;
import net.radai.confusion.core.spi.codec.AbstractTextCodecTest;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by Radai Rosenblatt
 */
public class JaxbCodecTest extends AbstractTextCodecTest {
    @Override
    protected JaxbCodec buildCodec() {
        return new JaxbCodec();
    }

    @Override
    protected Class getTestClass() {
        return JaxbTestClass.class;
    }

    @Test
    public void testRoundTrip() throws Exception {
        JaxbTestClass original = new JaxbTestClass();
        original.id = 42;
        original.uuid = UUID.randomUUID();

        JaxbCodec codec = buildCodec();
        String serialized = codec.serialize(original);
        Assert.assertNotNull(serialized);

        JaxbTestClass deserialized = codec.parse(JaxbTestClass.class, serialized);
        Assert.assertEquals(original, deserialized);
    }

    @XmlRootElement(name = "outer")
    public static class JaxbTestClass {
        @XmlAttribute
        private int id;
        @XmlElement
        private UUID uuid;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JaxbTestClass that = (JaxbTestClass) o;
            return id == that.id &&
                    Objects.equals(uuid, that.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, uuid);
        }
    }
}
