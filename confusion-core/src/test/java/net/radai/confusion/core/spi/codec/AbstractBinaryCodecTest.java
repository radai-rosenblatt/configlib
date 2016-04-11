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

package net.radai.confusion.core.spi.codec;

import net.radai.confusion.core.spi.PayloadType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Radai Rosenblatt
 */
public abstract class AbstractBinaryCodecTest extends AbstractCodecTest {

    @Override
    protected abstract BinaryCodec buildCodec();

    @Test
    public void testTypeGetterValue() {
        Assert.assertEquals(PayloadType.BINARY, buildCodec().getPayloadType());
    }

    @Test
    public void testParseNull() throws Exception {
        BinaryCodec codec = buildCodec();
        Object parsed = codec.parse(getTestClass(), null);
        Assert.assertNull("null blob should result in null object", parsed);
    }

    @Test
    public void testSerializeNull() throws Exception {
        BinaryCodec codec = buildCodec();
        byte[] serialized = codec.serialize(null);
        Assert.assertNull("null object should result in null blob", serialized);
    }
}
