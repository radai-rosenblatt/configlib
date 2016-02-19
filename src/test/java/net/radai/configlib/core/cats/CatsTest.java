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

package net.radai.configlib.core.cats;

import net.radai.configlib.core.ConfigLib;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Radai Rosenblatt
 */
public class CatsTest {

    @Test
    public void testParsingCats() throws Exception {
        Cats cats;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("cats.ini")) {
            Reader reader = new InputStreamReader(is, Charset.forName("UTF-8"));
            cats = ConfigLib.parse(Cats.class, reader);
        }
        Assert.assertNotNull(cats);
        Assert.assertEquals("bob", cats.getCreator());
        Assert.assertEquals(Arrays.asList("funny cats", "and stuff"), cats.getComments());
        Assert.assertEquals(Arrays.asList(
                new Cat("Maru", "Master of Boxes", Arrays.asList("boxes", "more boxes")),
                new Cat("Tardar Sauce", "Grumpy Cat", Collections.singletonList("nothing")),
                new Cat("Snowball", "Kitler", null)
                ), cats.getCats());
    }
}
