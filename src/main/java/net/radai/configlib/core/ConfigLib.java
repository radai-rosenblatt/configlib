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

package net.radai.configlib.core;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.ini4j.spi.IniBuilder;
import org.ini4j.spi.IniParser;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Set;

/**
 * Created by Radai Rosenblatt
 */
public class ConfigLib {

    public static <T> T parse(Class<T> confClass, Reader from) throws IOException {
        Ini ini = readIni(from);
        T instance;
        try {
            instance = (T) confClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("unable to instantiate " + confClass, e);
        }
        Set<String> sectionNames = ini.keySet();
        for (String sectionName : sectionNames) {
            List<Profile.Section> sectionInstances = ini.getAll(sectionName);
            if (sectionName.equals("?")) {
                //default section == top-level fields == properties of the top level class
                assert sectionInstances.size() == 1;
                handleTopLevelSection(instance, sectionInstances.get(0));
            } else {
                handleSection(instance, sectionName, sectionInstances);
            }
        }
        return instance;
    }

    private static void handleTopLevelSection(Object confObject, Profile.Section defaultSection) {
        Set<String> propNames = defaultSection.keySet();
        for (String propName : propNames) {
            List<String> values = defaultSection.getAll(propName);
            populateProperty(confObject, propName, values);
        }
    }

    private static void populateProperty(Object targetObject, String propName, List<String> values) {
        boolean resolved = false;
        //1st attempt - look for setX(x) directly
        //2nd attempt - look for setXs(Collection<x>) or setXs(x[])
    }

    private static void handleSection(Object target, String propName, List<Profile.Section> instances) {
        Class<?> targetClass = target.getClass();
        int instanceCount = instances.size();
        boolean resolved = false;
        if (instanceCount == 1) {
            //1st look for a set[PropName] method
        }
        if (!resolved) {
            //
        }
    }

    private static Ini readIni(Reader from) throws IOException {
        Config iniConfig = Config.getGlobal();
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
}
