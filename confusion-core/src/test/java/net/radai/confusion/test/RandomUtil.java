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

package net.radai.confusion.test;

import java.util.Random;

/**
 * Created by Radai Rosenblatt
 */
public class RandomUtil {

    public static String randomString(Random random) {
        StringBuilder sb = new StringBuilder();
        int length = 1 + random.nextInt(1023);
        for (int i=0; i<length; i++) {
            char c = (char)(random.nextInt(26) + 'a');
            sb.append(c);
        }
        return sb.toString();
    }

    public static byte[] randomBlob(Random random) {
        byte[] data = new byte[1 + random.nextInt(1023)];
        random.nextBytes(data);
        return data;
    }
}
