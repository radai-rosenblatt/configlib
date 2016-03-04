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

package net.radai.configlib.ini;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.ini4j.spi.IniBuilder;
import org.ini4j.spi.IniParser;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Created by Radai Rosenblatt
 */
public class IniUtil {
    public static Ini read(Reader from) throws IOException {
        Config iniConfig = new Config();
        iniConfig.setMultiSection(true);
        iniConfig.setMultiOption(true);
        iniConfig.setGlobalSection(true);
        IniParser parser = IniParser.newInstance(iniConfig);
        Ini ini = new Ini();
        ini.setConfig(iniConfig);
        IniBuilder iniBuilder = IniBuilder.newInstance(ini);
        parser.parse(from, iniBuilder);
        return ini;
    }

    public static boolean equals(Ini a, Ini b, boolean preserveOrder) {
        Set<String> aKeys = a.keySet();
        Set<String> bKeys = b.keySet();
        if (!aKeys.equals(bKeys)) {
            return false;
        }
        for (String key : aKeys) {
            //mutable copies in case !preserveOrder
            List<Profile.Section> aValues = new ArrayList<>(a.getAll(key));
            List<Profile.Section> bValues = new ArrayList<>(b.getAll(key));
            if (aValues.size() != bValues.size()) {
                return false;
            }
            if (preserveOrder) {
                for (int i = 0; i < aValues.size(); i++) {
                    Profile.Section aSection = aValues.get(i);
                    Profile.Section bSection = bValues.get(i);
                    if (!equals(aSection, bSection, true)) {
                        return false;
                    }
                }
            } else {
                outer:
                for (Iterator<Profile.Section> aIter = aValues.iterator(); aIter.hasNext(); ) {
                    Profile.Section aSection = aIter.next();
                    for (Iterator<Profile.Section> bIter = bValues.iterator(); bIter.hasNext(); ) {
                        Profile.Section bSection = bIter.next();
                        if (equals(aSection, bSection, false)) {
                            aIter.remove();
                            bIter.remove();
                            continue outer;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean equals(Profile.Section a, Profile.Section b, boolean preserveOrder) {
        Set<String> aKeys = a.keySet();
        Set<String> bKeys = b.keySet();
        if (!aKeys.equals(bKeys)) {
            return false;
        }
        for (String key : aKeys) {
            List<String> aValues = a.getAll(key);
            List<String> bValues = b.getAll(key);
            if (preserveOrder) {
                if (!aValues.equals(bValues)) {
                    return false;
                }
            } else {
                Map<String, Integer> aHist = histogram(aValues);
                Map<String, Integer> bHist = histogram(bValues);
                if (!aHist.equals(bHist)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Map<String, Integer> histogram(Iterable<String> aValues) {
        Map<String, Integer> aCount = new HashMap<>();
        for(String aValue : aValues) {
            aCount.compute(aValue, (s, c) -> c == null ? 1 : c + 1);
        }
        return aCount;
    }
}
