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

package net.radai.configlib.core;

import net.radai.beanz.*;
import net.radai.beanz.api.Bean;
import net.radai.beanz.api.Codec;
import net.radai.beanz.api.Property;
import net.radai.beanz.codecs.ArrayCodec;
import net.radai.beanz.codecs.CollectionCodec;
import net.radai.beanz.codecs.MapCodec;
import net.radai.beanz.util.ReflectionUtil;
import net.radai.configlib.core.util.EnglishUtil;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.ini4j.spi.IniBuilder;
import org.ini4j.spi.IniParser;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Radai Rosenblatt
 */
public class ConfigLib {

    public static <T> T parse(Class<T> confClass, Reader from) throws IOException {
        Bean bean = Beanz.analyze(confClass);
        Ini ini = readIni(from);
        T instance;
        try {
            instance = confClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("unable to instantiate " + confClass, e);
        }
        Set<String> sectionNames = ini.keySet();
        for (String sectionName : sectionNames) {
            List<Profile.Section> sectionInstances = ini.getAll(sectionName);
            if (sectionName.equals("?")) {
                //default section == top-level fields == properties of the top level class
                assert sectionInstances.size() == 1;
                handleTopLevelSection(bean, instance, sectionInstances.get(0));
            } else {
                handleSection(bean, instance, sectionName, sectionInstances);
            }
        }
        return instance;
    }

    private static void handleTopLevelSection(Bean bean, Object instance, Profile.Section defaultSection) {
        Set<String> propNames = defaultSection.keySet();
        for (String propName : propNames) {
            List<String> values = defaultSection.getAll(propName);
            Property property = bean.getProperty(propName);
            if (property != null) {
                setPropertyFromValues(property, instance, values);
                continue;
            }
            //maybe there's a plural property ...
            String pluralPropName = EnglishUtil.derivePlural(propName);
            property = bean.getProperty(pluralPropName);
            if (property != null && (property.isArray() || property.isCollection())) {
                setPropertyFromValues(property, instance, values);
                continue;
            }
            throw new UnsupportedOperationException();
        }
    }

    private static void setPropertyFromValues(Property property, Object instance, List<String> values) {
        Codec codec = property.getCodec();
        if (codec == null) {
            throw new IllegalStateException();
        }
        if (values == null || values.isEmpty()) {
            throw new UnsupportedOperationException(); //TODO - support setting null ?
        } else {
            if (property.isCollection()) {
                CollectionCodec collectionCodec = (CollectionCodec) codec;
                Codec elementCodec = collectionCodec.getElementCodec();
                Collection collection = ReflectionUtil.instantiateCollection(ReflectionUtil.erase(property.getValueType()));
                for (String value : values) {
                    //noinspection unchecked
                    collection.add(elementCodec.decode(value));
                }
                property.set(instance, collection);
            } else if (property.isArray()) {
                ArrayCodec arrayCodec = (ArrayCodec) codec;
                Codec elementCodec = arrayCodec.getElementCodec();
                Object array = Array.newInstance(ReflectionUtil.erase(property.getElementType()), values.size());
                for (int i=0; i<values.size(); i++) {
                    Array.set(array, i, elementCodec.decode(values.get(i)));
                }
                property.set(instance, array);
            } else if (property.isMap()){
                throw new UnsupportedOperationException();
            } else { //simple property
                if (values.size() != 1) {
                    throw new UnsupportedOperationException();
                }
                String value = values.get(0);
                property.set(instance, codec.decode(value));
            }
        }
    }

    private static void setPropertyFromSections(Property property, Object instance, List<Profile.Section> sections) {
        if (sections == null || sections.isEmpty()) {
            throw new UnsupportedOperationException(); //TODO - support setting null ?
        }
        if (property.isMap()) {
            if (sections.size() != 1) {
                throw new UnsupportedOperationException();
            }
            MapCodec mapCodec = (MapCodec) property.getCodec();
            if (mapCodec != null) {
                throw new UnsupportedOperationException();
            }
            Profile.Section section = sections.get(0);
            Map map = ReflectionUtil.instantiateMap(ReflectionUtil.erase(property.getValueType()));
            Set<String> propNames = section.keySet();
            for (String propName : propNames) {
                List<String> values = section.getAll(propName);
            }
        }
    }

    private static void handleSection(Bean desc, Object instance, String sectionName, List<Profile.Section> instances) {
        Property property = desc.getProperty(sectionName);
        if (property != null) {
            setPropertyFromSections(property, instance, instances);

        }
        String pluralPropName = EnglishUtil.derivePlural(sectionName);
        property = desc.getProperty(pluralPropName);
        if (property != null) {
            if (property.isArray()) {
                throw new UnsupportedOperationException();
            } else if (property.isCollection()) {
                Type type = property.getElementType();
                Class collectionClass = ReflectionUtil.erase(property.getValueType());
                Collection resultCollection = ReflectionUtil.instantiateCollection(collectionClass);
                for (Profile.Section instance : instances) {

                    //TODO - finish
                }
                property.set(instance, resultCollection);
            } else {
                throw new UnsupportedOperationException();
            }
        }
//        Section section = desc.getSection(sectionName);
//        if (section != null) {
//            throw new UnsupportedOperationException();
//        }

        int g = 7;
    }

    private static Object sectionToObject(Profile.Section section, Type type) {
        Object bean = ReflectionUtil.instantiate(type);
        return bean;
    }

    private static Ini readIni(Reader from) throws IOException {
        org.ini4j.Config iniConfig = org.ini4j.Config.getGlobal();
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
