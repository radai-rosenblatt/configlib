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
public class PropertyResolver {

    public static Property resolve(Class clazz, String propName) {
        //look for a getter/setter pair
        Method getter = findGetter(clazz, propName);
        Method setter = findSetter(clazz, propName);
        if (getter != null && setter != null) {
            Type getterType = getter.getGenericReturnType();
            Type setterType = setter.getGenericParameterTypes()[0];
            if (!getterType.equals(setterType)) {
                //TODO - be smart about boxing?
                throw new IllegalArgumentException("ambiguous property " + clazz.getSimpleName() + "." + propName + ": getter is " + getter + " while setter is " + setter);
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
            throw new IllegalArgumentException("ambiguous property " + clazz.getSimpleName() + "." + propName + ": getter/setter type is " + methodPropType + " while field type is " + fieldType);
        }
        return new CompositeProperty(methodProperty, new FieldProperty(propName, field));
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
