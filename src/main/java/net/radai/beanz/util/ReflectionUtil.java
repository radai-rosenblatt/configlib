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

package net.radai.beanz.util;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author Radai Rosenblatt
 */
public class ReflectionUtil {

    public static Class erase(Type type) {
        if (type instanceof GenericArrayType) {
            return Object.class; //TODO - get more info?
        }
        if (type instanceof WildcardType) {
            throw new UnsupportedOperationException();
        }
        if (type instanceof TypeVariable) {
            throw new UnsupportedOperationException();
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return (Class) parameterizedType.getRawType();
        }
        return (Class) type;
    }

    public static Object instantiate(Type type) {
        Class erased = erase(type);
        if (Collection.class.isAssignableFrom(erased)) {
            return instantiateCollection(erased);
        }
        try {
            return erased.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Collection instantiateCollection (Class collectionClass) {
        if (!Collection.class.isAssignableFrom(collectionClass)) {
            throw new IllegalArgumentException();
        }
        if (!Modifier.isAbstract(collectionClass.getModifiers())) {
            try {
                //noinspection unchecked
                return (Collection<?>) collectionClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        if (List.class.isAssignableFrom(collectionClass)) {
            return new ArrayList<>();
        }
        if (Set.class.isAssignableFrom(collectionClass)) {
            return new HashSet<>();
        }
        throw new UnsupportedOperationException();
    }

    public static Map<?, ?> instantiateMap(Class mapClass) {
        if (!Map.class.isAssignableFrom(mapClass)) {
            throw new IllegalArgumentException();
        }
        if (!Modifier.isAbstract(mapClass.getModifiers())) {
            try {
                //noinspection unchecked
                return (Map<?, ?>) mapClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return new HashMap<>();
    }

    public static boolean isArray(Type type) {
        if (type instanceof ParameterizedType) {
            return false;
        }
        if (type instanceof Class<?>) {
            Class clazz = (Class) type;
            return clazz.isArray();
        }
        if (type instanceof GenericArrayType) {
            return true;
        }
        throw new UnsupportedOperationException();
    }

    public static boolean isCollection(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return Collection.class.isAssignableFrom((Class<?>) parameterizedType.getRawType());
        }
        if (type instanceof Class<?>) {
            Class clazz = (Class) type;
            return Collection.class.isAssignableFrom(clazz);
        }
        if (type instanceof GenericArrayType) {
            return false;
        }
        throw new UnsupportedOperationException();
    }

    public static boolean isMap(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType());
        }
        if (type instanceof Class<?>) {
            Class clazz = (Class) type;
            return Map.class.isAssignableFrom(clazz);
        }
        if (type instanceof GenericArrayType) {
            return false;
        }
        throw new UnsupportedOperationException();
    }

    public static Type getElementType(Type type) {
        if (isArray(type)) {
            if (type instanceof Class<?>) {
                Class clazz = (Class) type;
                return clazz.getComponentType();
            }
            if (type instanceof GenericArrayType) {
                GenericArrayType arrayType = (GenericArrayType) type;
                return arrayType.getGenericComponentType();
            }
            throw new UnsupportedOperationException();
        }
        if (isCollection(type)) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length != 1) {
                    throw new UnsupportedOperationException();
                }
                return typeArguments[0];
            }
            if (type instanceof Class<?>) {
                //this is something like a plain List(), no generic information, so type unknown
                return null;
            }
            throw new UnsupportedOperationException();
        }
        if (isMap(type)) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length != 2) {
                    throw new UnsupportedOperationException();
                }
                return typeArguments[1];
            }
            if (type instanceof Class<?>) {
                //this is something like a plain Map(), no generic information, so type unknown
                return null;
            }
        }
        throw new UnsupportedOperationException();
    }

    public static Type getKeyType(Type type) {
        if (isMap(type)) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length != 2) {
                    throw new UnsupportedOperationException();
                }
                return typeArguments[0];
            }
            if (type instanceof Class<?>) {
                //this is something like a plain Map(), no generic information, so type unknown
                return null;
            }
        }
        throw new UnsupportedOperationException();
    }

    public static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    public static String prettyPrint(Type type) {
        if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            return prettyPrint(genericArrayType.getGenericComponentType()) + "[]";
        }
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            return wildcardType.toString();
        }
        if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) type;
            return typeVariable.getName();
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            StringBuilder sb = new StringBuilder();
            sb.append(prettyPrint(parameterizedType.getRawType()));
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments != null && typeArguments.length > 0) {
                sb.append("<");
                for (Type typeArgument : typeArguments) {
                    sb.append(prettyPrint(typeArgument)).append(", ");
                }
                sb.delete(sb.length()-2, sb.length()); // last ", "
                sb.append(">");
            }
            return sb.toString();
        }
        Class clazz = (Class) type;
        if (clazz.isArray()) {
            return prettyPrint(clazz.getComponentType()) + "[]";
        }
        return clazz.getSimpleName();
    }

    public static String prettyPrint(Method method) {
        StringBuilder sb = new StringBuilder();
        Class<?> declaredOn = method.getDeclaringClass();
        sb.append(prettyPrint(declaredOn)).append(".").append(method.getName()).append("(");
        if (method.getParameterCount() > 0) {
            for (Type paramType : method.getGenericParameterTypes()) {
                sb.append(prettyPrint(paramType)).append(", ");
            }
            sb.delete(sb.length()-2, sb.length());
        }
        sb.append(")");
        return sb.toString();
    }
}
