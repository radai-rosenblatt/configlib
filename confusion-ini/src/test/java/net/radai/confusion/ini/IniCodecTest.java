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

package net.radai.confusion.ini;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import net.radai.confusion.cats.Cat;
import net.radai.confusion.cats.Cats;
import net.radai.confusion.core.spi.codec.AbstractTextCodecTest;
import org.ini4j.Ini;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Radai Rosenblatt
 */
public class IniCodecTest extends AbstractTextCodecTest {

    @Override
    protected IniCodec buildCodec() {
        return new IniCodec();
    }

    @Override
    protected Class getTestClass() {
        return Cats.class;
    }

    @Test
    public void testParsingCats() throws Exception {
        IniCodec codec = buildCodec();
        Cats cats;
        byte[] contents;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("cats.ini")) {
            contents = ByteStreams.toByteArray(is);
        }
        cats = codec.parse(Cats.class, new String(contents));
        Assert.assertNotNull(cats);
        Assert.assertEquals("bob", cats.getCreator());
        Assert.assertEquals(Arrays.asList("funny cats", "and stuff"), cats.getComments());
        Assert.assertEquals(Arrays.asList(
                new Cat("Maru", "Master of Boxes", Arrays.asList("boxes", "more boxes")),
                new Cat("Tardar Sauce", "Grumpy Cat", Collections.singletonList("nothing")),
                new Cat("Snowball", "Kitler", null)
                ), cats.getCats());

        String serialized = codec.serialize(cats);
        Ini original = IniUtil.read(new InputStreamReader(new ByteArrayInputStream(contents), "UTF-8"));
        Ini produced = IniUtil.read(new StringReader(serialized));
        Assert.assertTrue(IniUtil.equals(original, produced, false));
    }

    @Test
    public void testParsingCat() throws Exception {
        IniCodec codec = buildCodec();
        Cats cats;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("cat.ini")) {
            String serialized = CharStreams.toString(new InputStreamReader(is, "UTF-8"));
            cats = codec.parse(Cats.class, serialized);
        }

        Assert.assertNotNull(cats);
        Assert.assertEquals("bob", cats.getCreator());
        Assert.assertEquals(Collections.singletonList("just a single cat"), cats.getComments());
        Assert.assertEquals(Collections.singletonList(
                new Cat("Colonel Meow", "Guinness World Record Holder For Longest Cat Hair", Collections.singletonList("baths"))
        ), cats.getCats());
    }
}
