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

package net.radai.beanz;

import net.radai.beanz.api.Bean;
import net.radai.beanz.api.Codec;
import net.radai.beanz.codecs.*;
import net.radai.beanz.properties.CompositeProperty;
import net.radai.beanz.properties.FieldProperty;
import net.radai.beanz.properties.MethodProperty;
import net.radai.beanz.api.Property;
import net.radai.beanz.util.ReflectionUtil;
import net.radai.beanz.api.AmbiguousPropertyException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by Radai Rosenblatt
 */
public class Beanz {

    public static Bean analyze(Class clazz) {
        Bean bean = new Bean();

        Map<String, Property> properties = new HashMap<>();
        Map<Type, Codec> codecs = new HashMap<>(Codecs.BUILT_INS);

        //methods 1st
        for (Method method : clazz.getMethods()) {
            if (isGetter(method) || isSetter(method)) {
                String propName = propNameFrom(method);
                if (!properties.containsKey(propName)) {
                    try {
                        properties.put(propName, resolve(bean, clazz, propName, codecs));
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
                    properties.put(fieldName, resolve(bean, clazz, fieldName, codecs));
                } catch (AmbiguousPropertyException e) {
                    //not a property
                }
            }
            c = c.getSuperclass();
        }

        properties.forEach((s, property) -> bean.addProperty(property));
        codecs.forEach(bean::addCodec);
        return bean;
    }

    private static Property resolve(Bean bean, Class clazz, String propName, Map<Type, Codec> codecs) {
        //look for a getter/setter pair
        Method getter = findGetter(clazz, propName);
        Method setter = findSetter(clazz, propName);
        Field field = findField(clazz, propName);
        if (getter == null && setter == null && field == null) {
            return null;
        }

        Type type = resolvePropertyType(clazz, propName, getter, setter, field);
        Codec codec = resolvePropertyCodec(clazz, propName, type, getter, setter, field, codecs);

        if (getter != null && setter != null) {
            return new MethodProperty(bean, propName, type, codec, getter, setter);
        }
        if (getter == null && setter == null) {
            return new FieldProperty(bean, propName, type, codec, field);
        }
        //we have either a getter or a setter
        MethodProperty methodProperty = new MethodProperty(bean, propName, type, codec, getter, setter);
        if (field == null) {
            //and no field
            return methodProperty; //one of them is != null;
        }
        return new CompositeProperty(methodProperty, new FieldProperty(bean, propName, type, codec, field));
    }

    private static Type resolvePropertyType(Class clazz, String propName, Method getter, Method setter, Field field) {
        Type getterType = getter != null ? getter.getGenericReturnType() : null;
        Type setterType = setter != null ? setter.getGenericParameterTypes()[0] : null;
        Type fieldType = field != null ? field.getGenericType() : null;

        if (getter != null && setter != null) {
            if (!getterType.equals(setterType)) {
                //TODO - be smart about boxing?
                //TODO - also be smart about missing generics? (List vs List<Something>)
                throw new AmbiguousPropertyException("ambiguous property " + clazz.getSimpleName() + "." + propName + ": getter is " + getter + " while setter is " + setter);
            }
            return getterType;
        }
        if (getter == null && setter == null) {
            return fieldType;
        }
        //we have just one method
        Type methodType = getterType != null ? getterType : setterType;
        if (field == null) {
            return methodType;
        }
        if (!fieldType.equals(methodType)) {
            //TODO - be smart about boxing?
            throw new AmbiguousPropertyException("ambiguous property " + clazz.getSimpleName() + "." + propName + ": getter/setter type is " + methodType + " while field type is " + fieldType);
        }
        return methodType;
    }

    private static Codec resolvePropertyCodec (
            Class clazz, String propName, Type type,
            Method getter, Method setter, Field field,
            Map<Type, Codec> codecs) {
        //TODO - check for overrides in annotations on methods > field > class > type
        Codec forType = codecs.get(type);
        if (forType != null) {
            return forType;
        }
        Codec result = null;
        if (ReflectionUtil.isArray(type) || ReflectionUtil.isCollection(type)) {
            Type elementType = ReflectionUtil.getElementType(type);
            Codec elementCodec = resolvePropertyCodec(null, null, elementType, null, null, null, codecs);
            if (elementCodec == null) {
                return null; //cant handle the elements == cant handle the collection/array
            }
            if (ReflectionUtil.isArray(type)) {
                result = new ArrayCodec(type, elementType, elementCodec);
            } else {
                result = new CollectionCodec(type, elementType, elementCodec);
            }
        } else if (ReflectionUtil.isMap(type)) {
            Type keyType = ReflectionUtil.getKeyType(type);
            Type valueType = ReflectionUtil.getElementType(type);
            Codec keyCodec = resolvePropertyCodec(null, null, keyType, null, null, null, codecs);
            Codec valueCodec = resolvePropertyCodec(null, null, valueType, null, null, null, codecs);
            if (keyCodec == null || valueCodec == null) {
                return null;
            }
            result = new MapCodec(type, keyType, valueType, keyCodec, valueCodec);
        } else {
            //see if this type has toString + valueOf(String)
            Class erased = ReflectionUtil.erase(type);
            Method encodeMethod = null;
            Method decodeMethod = null;
            for (Method method : erased.getMethods()) {
                int modifiers = method.getModifiers();
                Type[] argumentTypes = method.getGenericParameterTypes();
                String methodName = method.getName();
                if ("toString".equals(methodName)
                        && !Modifier.isStatic(modifiers)
                        && method.getParameterCount() == 0
                        && method.getGenericReturnType().equals(String.class)
                        && method.getDeclaringClass() == erased) {
                    //public String toString() defined directly on the target type (not inherited)
                    if (encodeMethod != null) {
                        throw new IllegalStateException();
                    }
                    encodeMethod = method;
                    continue;
                }
                if (("fromString".equals(methodName) || "valueOf".equals(methodName))
                        && Modifier.isStatic(modifiers)
                        && method.getParameterCount() == 1
                        && String.class.equals(argumentTypes[0])
                        && method.getDeclaringClass() == erased) {
                    if (decodeMethod != null) {
                        throw new IllegalStateException();
                    }
                    decodeMethod = method;
                    //noinspection UnnecessaryContinue
                    continue;
                }
            }
            if (encodeMethod != null && decodeMethod != null) {
                result = new SimpleCodec(type, encodeMethod, decodeMethod);
            }
        }
        if (result != null) {
            codecs.put(type, result);
        }
        return result;
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
