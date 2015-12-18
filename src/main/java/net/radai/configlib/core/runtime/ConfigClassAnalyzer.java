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

package net.radai.configlib.core.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by Radai Rosenblatt
 */
public class ConfigClassAnalyzer {

    //TODO - convert this to use Type and then would handle Config<T>
    public static Config analyze(Class clazz) {
        Config config = new Config();

        Map<String, Property> properties = findAllProperties(clazz);
        properties.remove("class"); //built in to all java classes.

        //now we need to decide which of these are properties of the conf class, and which are sections
        for (Map.Entry<String, Property> entry : properties.entrySet()) {
            String propName = entry.getKey();
            Property prop = entry.getValue();

            //a property is something we can convert to/from a String. anything we cant is a section
            if (prop.isMap()) {
                Type keyType = prop.getKeyType();
                Type valueType = prop.getElementType();
                //maps are sections, for now.
                config.addSection(new MapSection(propName, keyType, valueType));
            } else if (prop.isCollection() || prop.isArray()) {
                //lists are simple properties
                config.addProperty(prop);
            } else {
                //anything that is parsable from a String is a simple prop, otherwise a section
                //TODO - finish this
                Type valueType = prop.getType();
                if (config.hasCodecFor(valueType)) {
                    config.addProperty(prop);
                } else {
                    Codec codec = findCodecFor(valueType);
                    if (codec != null) {
                        config.addCodec(codec);
                        config.addProperty(prop);
                    } else {
                        config.addSection(new BeanSection(propName, valueType));
                    }
                }
            }
        }

        return config;
    }

    public static Codec findCodecFor(Type type) {
        if (type.equals(String.class)) {
            return Codec.NOP_CODEC;
        }
        return null;
    }

    public static Map<String, Property> findAllProperties(Class clazz) {
        Map<String, Property> properties = new HashMap<>();

        //methods 1st
        for (Method method : clazz.getMethods()) {
            if (isGetter(method) || isSetter(method)) {
                String propName = propNameFrom(method);
                if (!properties.containsKey(propName)) {
                    try {
                        properties.put(propName, resolve(clazz, propName));
                    } catch (AmbiguousPropertyException e) {
                        //not a property
                    }
                }
            }
        }

        //fields later
        Class c = clazz;
        while (c != null) {
            for (Field f : c.getDeclaredFields()) {
                int modifiers = f.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }
                String fieldName = f.getName();
                if (properties.containsKey(fieldName)) {
                    continue;
                }
                try {
                    properties.put(fieldName, resolve(clazz, fieldName));
                } catch (AmbiguousPropertyException e) {
                    //not a property
                }
            }
            c = c.getSuperclass();
        }

        return properties;
    }

    public static Property resolve(Class clazz, String propName) {
        //look for a getter/setter pair
        Method getter = findGetter(clazz, propName);
        Method setter = findSetter(clazz, propName);
        if (getter != null && setter != null) {
            Type getterType = getter.getGenericReturnType();
            Type setterType = setter.getGenericParameterTypes()[0];
            if (!getterType.equals(setterType)) {
                //TODO - be smart about boxing?
                throw new AmbiguousPropertyException("ambiguous property " + clazz.getSimpleName() + "." + propName + ": getter is " + getter + " while setter is " + setter);
            }
            return new MethodProperty(propName, getter, setter);
        }
        //look for a field, because one (or both) of the above are missing
        Field field = findField(clazz, propName);
        if (getter == null && setter == null) {
            return field == null ? null : new FieldProperty(propName, field);
        }
        MethodProperty methodProperty = new MethodProperty(propName, getter, setter); //one of them is != null
        if (field == null) {
            return methodProperty;
        }
        Type methodPropType = methodProperty.getType();
        Type fieldType = field.getGenericType();
        if (!methodPropType.equals(fieldType)) {
            //TODO - be smart about boxing?
            throw new AmbiguousPropertyException("ambiguous property " + clazz.getSimpleName() + "." + propName + ": getter/setter type is " + methodPropType + " while field type is " + fieldType);
        }
        return new CompositeProperty(methodProperty, new FieldProperty(propName, field));
    }

    private static boolean isGetter(Method method) {
        Type returnType = method.getGenericReturnType();
        if (returnType.equals(void.class)) {
            return false; //should return something
        }
        Type[] argumentTypes = method.getGenericParameterTypes();
        if (argumentTypes != null && argumentTypes.length != 0) {
            return false; //should not accept any arguments
        }
        String name = method.getName();
        if (name.startsWith("get")) {
            if (name.length() < 4) {
                return false; //has to be getSomething
            }
            String fourthChar = name.substring(3, 4);
            return fourthChar.toUpperCase(Locale.ROOT).equals(fourthChar); //getSomething (upper case)
        } else if (name.startsWith("is")) {
            if (name.length() < 3) {
                return false; //isSomething
            }
            String thirdChar = name.substring(2, 3);
            //noinspection SimplifiableIfStatement
            if (!thirdChar.toUpperCase(Locale.ROOT).equals(thirdChar)) {
                return false; //has to start with uppercase (or something that uppercases to itself, like a number?)
            }
            return returnType.equals(boolean.class) || returnType.equals(Boolean.class);
        } else {
            return false;
        }
    }

    private static boolean isSetter(Method method) {
        Type returnType = method.getGenericReturnType();
        if (!returnType.equals(void.class)) {
            return false; //should not return anything
        }
        Type[] argumentTypes = method.getGenericParameterTypes();
        if (argumentTypes == null || argumentTypes.length != 1) {
            return false; //should accept exactly one argument
        }
        String name = method.getName();
        if (name.startsWith("set")) {
            if (name.length() < 4) {
                return false; //setSomething
            }
            String fourthChar = name.substring(3, 4);
            return fourthChar.toUpperCase(Locale.ROOT).equals(fourthChar); //setSomething (upper case)
        }
        return false;
    }

    private static String propNameFrom(Method method) {
        String methodName = method.getName();
        if (methodName.startsWith("get") || methodName.startsWith("set")) {
            if (methodName.length() > 4) {
                return methodName.substring(3, 4).toLowerCase(Locale.ROOT) + methodName.substring(4);
            } else {
                return methodName.substring(3, 4).toLowerCase(Locale.ROOT);
            }
        }
        if (methodName.startsWith("is")) {
            if (methodName.length() > 3) {
                return methodName.substring(2, 3).toLowerCase(Locale.ROOT) + methodName.substring(3);
            } else {
                return methodName.substring(2, 3).toLowerCase(Locale.ROOT);
            }
        }
        throw new IllegalArgumentException("method name " + methodName + " does not contain a property name");
    }

    private static Method findSetter(Class clazz, String propName) {
        String expectedName = "set" + propName.substring(0, 1).toUpperCase(Locale.ROOT) + propName.substring(1);
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(expectedName)) {
                continue;
            }
            if (!method.getReturnType().equals(void.class)) {
                continue;
            }
            Type[] argTypes = method.getGenericParameterTypes();
            if (argTypes == null || argTypes.length != 1) {
                continue;
            }
            return method;
        }
        return null;
    }

    private static Method findGetter(Class clazz, String propName) {
        Set<String> expectedNames = new HashSet<>(Arrays.asList(
                "get" + propName.substring(0, 1).toUpperCase(Locale.ROOT) + propName.substring(1),
                "is" + propName.substring(0, 1).toUpperCase(Locale.ROOT) + propName.substring(1) //bool props
        ));
        for (Method method : clazz.getMethods()) {
            String methodName = method.getName();
            if (!expectedNames.contains(methodName)) {
                continue;
            }
            if (method.getParameterCount() != 0) {
                continue; //getters take no arguments
            }
            Type returnType = method.getGenericReturnType();
            if (returnType.equals(void.class)) {
                continue; //getters return something
            }
            if (methodName.startsWith("is") && !(returnType.equals(Boolean.class) || returnType.equals(boolean.class))) {
                continue; //isSomething() only valid for booleans
            }
            return method;
        }
        return null;
    }

    private static Field findField(Class clazz, String propName) {
        Class c = clazz;
        while (c != null) {
            for (Field f : c.getDeclaredFields()) {
                if (!f.getName().equals(propName)) {
                    continue;
                }
                int modifiers = f.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }
                return f;
            }
            c = c.getSuperclass();
        }
        return null;
    }
}
