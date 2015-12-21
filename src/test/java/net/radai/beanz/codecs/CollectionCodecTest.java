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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Radai Rosenblatt
 */
public class CollectionCodecTest {
    @SuppressWarnings("unused")
    private List<Integer> f;

    @Test
    public void testList() throws Exception {
        CollectionCodec codec = new CollectionCodec(getClass().getDeclaredField("f").getGenericType(), Integer.class, Codecs.BUILT_INS.get(Integer.class));
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        String encoded = codec.encode(list);
        Assert.assertEquals("[1, 2, 3, 4, 5]", encoded);
        //noinspection unchecked
        List<Integer> decoded = (List<Integer>) codec.decode(encoded);
        Assert.assertTrue(decoded != list);
        Assert.assertEquals(list, decoded);
    }
}
