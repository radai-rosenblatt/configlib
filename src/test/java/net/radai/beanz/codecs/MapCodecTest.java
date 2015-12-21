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

import java.util.*;

/**
 * Created by Radai Rosenblatt
 */
public class MapCodecTest {
    @SuppressWarnings("unused")
    private Map<String, Boolean> f;

    @Test
    public void testMap() throws Exception {
        MapCodec codec = new MapCodec(
                getClass().getDeclaredField("f").getGenericType(),
                String.class, Boolean.class, Codecs.BUILT_INS.get(String.class), Codecs.BUILT_INS.get(Boolean.class));
        Map<String, Boolean> original = new HashMap<>();
        original.put("a", Boolean.TRUE);
        original.put("b", Boolean.FALSE);
        String encoded = codec.encode(original);
        //noinspection unchecked
        Map<String, Boolean> decoded = (Map<String, Boolean>) codec.decode(encoded);
        Assert.assertTrue(original != decoded);
        Assert.assertEquals(original, decoded);
    }
}
