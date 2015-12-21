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
import net.radai.beanz.codecs.Codecs;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by Radai Rosenblatt
 */
public class CodecsTest {

    @Test
    public void testShortCodec() throws Exception {
        Map<Type, Codec> codecs = Codecs.BUILT_INS;
        Codec shortCodec = codecs.get(short.class);
        short decoded = (short) shortCodec.decode("6");
        Assert.assertEquals(6, decoded);
        String encoded = shortCodec.encode((short) 6);
        Assert.assertEquals("6", encoded);
    }

    @Test
    public void testStringCodec() throws Exception {
        Codec stringCodec = Codecs.BUILT_INS.get(String.class);
        String bob = "bob";
        String decoded = (String) stringCodec.decode(bob);
        //noinspection StringEquality
        Assert.assertTrue(decoded == bob);
        String encoded = stringCodec.encode(bob);
        //noinspection StringEquality
        Assert.assertTrue(encoded == bob);
    }


}
