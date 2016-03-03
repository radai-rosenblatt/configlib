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

package net.radai.configlib.core.util;

import java.util.Locale;

/**
 * Created by Radai Rosenblatt
 */
public class EnglishUtil {
    public static boolean isPlural(String propName) {
        return propName.toLowerCase(Locale.ROOT).endsWith("s");
    }

    public static String derivePlural(String propName) {
        String lowercase = propName.toLowerCase(Locale.ROOT);
        if (lowercase.endsWith("s") || lowercase.endsWith("x")) {
            return propName + "es"; //asses, axes
        }
        if (lowercase.endsWith("y")) {
            return propName.substring(0, propName.length()-1) + "ies"; //parties
        }
        return propName + "s"; //cats
    }

    public static String deriveSingular(String propName) {
        String lowercase = propName.toLowerCase(Locale.ROOT);
        if (lowercase.endsWith("ies")) {
            return propName.substring(0, propName.length()-3) + "y"; //pantries --> pantry
        }
        if (lowercase.endsWith("s")) {
            return propName.substring(0, propName.length()-1); //cats --> cat, likes --> like
        }
        return propName;
    }
}
