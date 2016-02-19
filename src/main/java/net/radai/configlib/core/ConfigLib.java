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
import net.radai.beanz.api.*;
import net.radai.beanz.util.ReflectionUtil;
import net.radai.configlib.core.util.EnglishUtil;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.ini4j.spi.IniBuilder;
import org.ini4j.spi.IniParser;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by Radai Rosenblatt
 */
public class ConfigLib {

    public static <T> T parse(Class<T> confClass, Reader from) throws IOException {
        Pod<T> pod = Beanz.wrap(confClass);
        Ini ini = readIni(from);
        Set<String> sectionNames = ini.keySet();
        for (String sectionName : sectionNames) {
            List<Profile.Section> sectionInstances = ini.getAll(sectionName);
            if (sectionInstances == null || sectionInstances.isEmpty()) {
                throw new IllegalStateException();
            }
            if (sectionName.equals("?")) {
                //default section == top-level fields == properties of the top level class
                if (sectionInstances.size() != 1) {
                    throw new IllegalStateException();
                }
                populate(pod, sectionInstances.get(0));
            } else {
                Property property;
                property = pod.resolve(sectionName);
                if (sectionInstances.size() == 1) {
                    if (property == null) {
                        throw new IllegalArgumentException();
                    }
                    populate(pod, property, sectionInstances.get(0));
                } else {
                    if (property == null) {
                        String pluralName = EnglishUtil.derivePlural(sectionName); //if section is "dog" maybe there's a prop "dogs"
                        property = pod.resolve(pluralName);
                        if (property == null) {
                            throw new IllegalArgumentException();
                        }
                    }
                    populateFromSections(pod, property, sectionInstances);
                }
            }
        }
        return pod.getBean();
    }

    private static void populate(Pod what, Profile.Section from) {
        Set<String> keys = from.keySet();
        for (String key : keys) {
            List<String> values = from.getAll(key);
            if (values == null || values.isEmpty()) {
                throw new IllegalStateException();
            }
            Property property;
            if (values.size() == 1) {
                //single value
                String value = values.get(0);
                property = what.resolve(key);
                if (property == null) {
                    throw new IllegalArgumentException();
                }
                populateFromString(what, property, value);
            } else {
                //multiple values
                property = what.resolve(key);
                if (property != null) {
                    populateFromStrings(what, property, values);
                } else {
                    //could not find prop "bob". look for a list/array prop called "bobs" maybe
                    String pluralPropName = EnglishUtil.derivePlural(key);
                    property = what.resolve(pluralPropName);
                    if (property == null) {
                        throw new IllegalArgumentException("cannot find mapping for key " + from.getSimpleName() + "." + key);
                    }
                    populateFromStrings(what, property, values);
                }
            }
        }
    }

    private static void populateFromString(Pod pod, Property property, String value) {
        PropertyType propertyType = property.getType();
        switch (propertyType) {
            case SIMPLE:
                pod.set(property, value);
                break;
            case ARRAY:
                ((ArrayProperty)property).setFromStrings(pod.getBean(), Collections.singletonList(value));
                break;
            case COLLECTION:
                ((CollectionProperty)property).setFromStrings(pod.getBean(), Collections.singletonList(value));
                break;
            case MAP:
                throw new IllegalArgumentException();
            default:
                throw new UnsupportedOperationException("unhandled " + propertyType);
        }
    }

    private static void populateFromStrings(Pod pod, Property property, List<String> values) {
        PropertyType propertyType = property.getType();
        switch (propertyType) {
            case SIMPLE:
                throw new IllegalArgumentException();
            case ARRAY:
                ((ArrayProperty)property).setFromStrings(pod.getBean(), values);
                break;
            case COLLECTION:
                ((CollectionProperty)property).setFromStrings(pod.getBean(), values);
                break;
            case MAP:
                throw new IllegalArgumentException();
            default:
                throw new UnsupportedOperationException("unhandled " + propertyType);
        }
    }

    private static void populate(Pod pod, Property property, Profile.Section from) {
        PropertyType propertyType = property.getType();
        switch (propertyType) {
            case SIMPLE:
                Type beanType = property.getValueType();
                Pod innerPod = Beanz.wrap(ReflectionUtil.erase(beanType));
                populate(innerPod, from);
                pod.set(property, innerPod.getBean());
                break;
            case ARRAY:
            case COLLECTION:
                throw new IllegalArgumentException(); //TODO - consider treating as collection/array of size 1?
            case MAP:
                MapProperty mapProperty = (MapProperty) property;
                Map<String, String> strMap = toMap(from);
                mapProperty.setFromStrings(pod.getBean(), strMap);
                break;
            default:
                throw new UnsupportedOperationException("unhandled " + propertyType);
        }
    }

    private static void populateFromSections(Pod pod, Property property, List<Profile.Section> from) {
        PropertyType propertyType = property.getType();
        Class<?> beanClass;
        Collection<Object> values;
        switch (propertyType) {
            case SIMPLE:
                throw new IllegalArgumentException();
            case ARRAY:
                ArrayProperty arrayProperty = (ArrayProperty) property;
                beanClass = ReflectionUtil.erase(arrayProperty.getElementType());
                values = new ArrayList<>(from.size());
                for (Profile.Section section : from) {
                    Pod elementPod = Beanz.wrap(beanClass);
                    populate(elementPod, section);
                    values.add(elementPod.getBean());
                }
                arrayProperty.setArray(pod.getBean(), values);
                break;
            case COLLECTION:
                CollectionProperty collectionProperty = (CollectionProperty) property;
                beanClass = ReflectionUtil.erase(collectionProperty.getElementType());
                values = new ArrayList<>(from.size());
                for (Profile.Section section : from) {
                    Pod elementPod = Beanz.wrap(beanClass);
                    populate(elementPod, section);
                    values.add(elementPod.getBean());
                }
                collectionProperty.setCollection(pod.getBean(), values);
                break;
            case MAP:
                throw new IllegalArgumentException();
            default:
                throw new UnsupportedOperationException("unhandled " + propertyType);
        }
    }

    private static Map<String, String> toMap(Profile.Section from) {
        Map<String, String> result = new HashMap<>();
        for (String key : from.keySet()) {
            List<String> values = from.getAll(key);
            if (values.size() != 1) {
                throw new IllegalArgumentException();
            }
            result.put(key, values.get(0));
        }
        return result;
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
