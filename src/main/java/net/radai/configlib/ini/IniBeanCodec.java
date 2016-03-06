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

import net.radai.beanz.Beanz;
import net.radai.beanz.api.*;
import net.radai.beanz.util.ReflectionUtil;
import net.radai.configlib.core.spi.BeanCodec;
import net.radai.configlib.core.util.Inflection;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.ini4j.spi.IniBuilder;
import org.ini4j.spi.IniParser;

import java.io.*;
import java.util.*;

/**
 * Created by Radai Rosenblatt
 */
public class IniBeanCodec implements BeanCodec {
    private final String charset;

    public IniBeanCodec() {
        this("UTF-8");
    }

    public IniBeanCodec(String charset) {
        this.charset = charset;
    }

    public String getCharset() {
        return charset;
    }

    @Override
    public <T> T parse(Class<T> beanClass, InputStream from) throws IOException {
        if (from == null) {
            return null;
        }
        Ini ini;
        try (Reader reader = new InputStreamReader(from, charset)){
            ini = readIni(reader);
        }
        String globalSectionName = ini.getConfig().getGlobalSectionName();
        Bean<T> bean = Beanz.create(beanClass);
        Set<String> sectionNames = ini.keySet();
        for (String sectionName : sectionNames) {
            List<Profile.Section> sectionInstances = ini.getAll(sectionName);
            if (sectionInstances == null || sectionInstances.isEmpty()) {
                throw new IllegalStateException();
            }
            if (sectionName.equals(globalSectionName)) {
                //global section == top-level fields == properties of the top level class
                if (sectionInstances.size() != 1) {
                    throw new IllegalStateException();
                }
                populate(bean, sectionInstances.get(0));
            } else {
                Property property;
                property = bean.getProperty(sectionName);
                if (property == null) {
                    String pluralName = Inflection.pluralize(sectionName); //if section is "dog" maybe there's a prop "dogs"
                    property = bean.getProperty(pluralName);
                }
                if (property == null) {
                    throw new IllegalArgumentException();
                }
                populateFromSections(property, sectionInstances);
            }
        }
        return bean.getBean();
    }

    private static void populate(Bean what, Profile.Section from) {
        Set<String> keys = from.keySet();
        for (String key : keys) {
            List<String> values = from.getAll(key);
            if (values == null || values.isEmpty()) {
                throw new IllegalStateException();
            }
            Property property;
            property = what.getProperty(key);
            if (property != null) {
                populateFromStrings(property, values);
            } else {
                //could not find prop "bob". look for a list/array prop called "bobs" maybe
                String pluralPropName = Inflection.pluralize(key);
                property = what.getProperty(pluralPropName);
                if (property == null) {
                    throw new IllegalArgumentException("cannot find mapping for key " + from.getSimpleName() + "." + key);
                }
                populateFromStrings(property, values);
            }
        }
    }

    private static void populateFromStrings(Property property, List<String> values) {
        PropertyType propertyType = property.getType();
        switch (propertyType) {
            case SIMPLE:
                if (values.size() != 1) {
                    throw new IllegalArgumentException();
                }
                property.set(values.get(0));
                break;
            case ARRAY:
                ((ArrayProperty)property).setFromStrings(values);
                break;
            case COLLECTION:
                ((CollectionProperty)property).setFromStrings(values);
                break;
            case MAP:
                throw new IllegalArgumentException();
            default:
                throw new UnsupportedOperationException("unhandled " + propertyType);
        }
    }

    private static void populateFromSections(Property property, List<Profile.Section> from) {
        PropertyType propertyType = property.getType();
        Class<?> beanClass;
        Collection<Object> values;
        Bean elementPod;
        switch (propertyType) {
            case SIMPLE:
                if (from.size() != 1) {
                    throw new IllegalArgumentException();
                }
                beanClass = ReflectionUtil.erase(property.getValueType());
                elementPod = Beanz.create(beanClass);
                populate(elementPod, from.get(0));
                property.set(elementPod.getBean());
                break;
            case ARRAY:
                ArrayProperty arrayProperty = (ArrayProperty) property;
                beanClass = ReflectionUtil.erase(arrayProperty.getElementType());
                values = new ArrayList<>(from.size());
                for (Profile.Section section : from) {
                    elementPod = Beanz.create(beanClass);
                    populate(elementPod, section);
                    values.add(elementPod.getBean());
                }
                arrayProperty.setArray(values);
                break;
            case COLLECTION:
                CollectionProperty collectionProperty = (CollectionProperty) property;
                beanClass = ReflectionUtil.erase(collectionProperty.getElementType());
                values = new ArrayList<>(from.size());
                for (Profile.Section section : from) {
                    elementPod = Beanz.create(beanClass);
                    populate(elementPod, section);
                    values.add(elementPod.getBean());
                }
                collectionProperty.setCollection(values);
                break;
            case MAP:
                if (from.size() != 1) {
                    throw new IllegalArgumentException();
                }
                MapProperty mapProperty = (MapProperty) property;
                Map<String, String> strMap = toMap(from.get(0));
                mapProperty.setFromStrings(strMap);
                break;
            default:
                throw new UnsupportedOperationException("unhandled " + propertyType);
        }
    }

    @Override
    public <T> void serialize(T beanInstance, OutputStream to) throws IOException {
        Config iniConfig = buildIniConfig();
        Ini ini = new Ini();
        ini.setConfig(iniConfig);
        Profile.Section defaultSection = ini.add(iniConfig.getGlobalSectionName());

        Bean<T> bean = Beanz.wrap(beanInstance);
        for (Map.Entry<String, Property> propEntry : bean.getProperties().entrySet()) {
            String propName = propEntry.getKey();
            Property prop = propEntry.getValue();
            Codec codec = prop.getCodec();
            String singular;
            //TODO - differentiate between nulls and empty sets
            switch (prop.getType()) {
                case SIMPLE:
                    if (codec != null) {
                        //prop --> string
                        String stringValue = prop.getAsString();
                        if (stringValue != null) {
                            defaultSection.put(propName, stringValue);
                        }
                    } else {
                        //prop --> section
                        Object rawValue = prop.get();
                        if (rawValue != null) {
                            Bean innerBean = Beanz.wrap(rawValue);
                            Profile.Section targetSection = ini.add(propName);
                            serializeToSection(innerBean, targetSection);
                        }
                    }
                    break;
                case ARRAY:
                    ArrayProperty arrayProp = (ArrayProperty) prop;
                    singular = Inflection.singularize(propName);
                    if (codec != null) {
                        //prop --> multi value (potentially under singular name)
                        List<String> stringValues = arrayProp.getAsStrings();
                        if (stringValues != null) {
                            defaultSection.putAll(singular, stringValues);
                        }
                    } else {
                        //prop --> multi section (potentially under singular name)
                        serializeToSections(ini, arrayProp.getAsList(), singular);
                    }
                    break;
                case COLLECTION:
                    CollectionProperty collectionProp = (CollectionProperty) prop;
                    singular = Inflection.singularize(propName);
                    if (codec != null) {
                        //prop --> multi value (potentially under singular name)
                        Collection<String> asStrings = collectionProp.getAsStrings();
                        if (asStrings != null) {
                            defaultSection.putAll(singular, new ArrayList<>(asStrings)); //orig might be a set
                        }
                    } else {
                        //prop --> multi section (potentially under singular name)
                        serializeToSections(ini, collectionProp.getCollection(), singular);
                    }
                    break;
                case MAP:
                    MapProperty mapProp = (MapProperty) prop;
                    if (codec != null) {
                        //prop --> section
                        Map<String, String> asStrings = mapProp.getAsStrings();
                        if (asStrings != null) {
                            Profile.Section targetSection = ini.add(propName);
                            targetSection.putAll(asStrings);
                        }
                    } else {
                        throw new UnsupportedOperationException(); //TODO - figure out how to map Map<String, ComplexBean> ?
                    }
                    break;
                default:
                    throw new IllegalStateException("unhandled: " + prop.getType());
            }
        }

        ini.store(to);
    }

    private void serializeToSections(Ini ini, Iterable<?> beans, String propName) {
        if (beans != null) {
            for (Object rawValue : beans) {
                Bean innerBean = Beanz.wrap(rawValue);
                Profile.Section targetSection = ini.add(propName);
                serializeToSection(innerBean, targetSection);
            }
        }
    }

    private static void serializeToSection(Bean<?> bean, Profile.Section section) {
        for (Map.Entry<String, Property> propEntry : bean.getProperties().entrySet()) {
            String propName = propEntry.getKey();
            Property prop = propEntry.getValue();
            Codec codec = prop.getCodec();
            if (codec == null) {
                throw new UnsupportedOperationException(); //ini does not support nested sections
            }
            String singular;
            switch (prop.getType()) {
                case SIMPLE:
                    //prop --> string
                    String stringValue = prop.getAsString();
                    if (stringValue != null) {
                        section.put(propName, stringValue);
                    }
                    break;
                case ARRAY:
                    //prop --> multi value (potentially under singular name)
                    ArrayProperty arrayProp = (ArrayProperty) prop;
                    singular = Inflection.singularize(propName);
                    List<String> stringValues = arrayProp.getAsStrings();
                    if (stringValues != null) {
                        section.putAll(singular, stringValues);
                    }
                    break;
                case COLLECTION:
                    //prop --> multi value (potentially under singular name)
                    CollectionProperty collectionProp = (CollectionProperty) prop;
                    singular = Inflection.singularize(propName);
                    Collection<String> asStrings = collectionProp.getAsStrings();
                    if (asStrings != null) {
                        section.putAll(singular, new ArrayList<>(asStrings)); //turn into a list (orig might be a set)
                    }
                    break;
                case MAP:
                    throw new UnsupportedOperationException(); //ini does not support nested sections
                default:
                    throw new IllegalStateException("unhandled: " + prop.getType());
            }
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
        Config iniConfig = buildIniConfig();
        IniParser parser = IniParser.newInstance(iniConfig);
        Ini ini = new Ini();
        ini.setConfig(iniConfig);
        IniBuilder iniBuilder = IniBuilder.newInstance(ini);
        parser.parse(from, iniBuilder);
        return ini;
    }

    private static Config buildIniConfig() {
        Config iniConfig = new Config();
        iniConfig.setMultiSection(true);
        iniConfig.setMultiOption(true);
        iniConfig.setGlobalSection(true);
        return iniConfig;
    }
}
