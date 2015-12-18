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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Radai Rosenblatt
 */
public interface Property {
    String getName();
    Type getType();
    boolean isReadable();
    boolean isWritable();

    default boolean isArray() {
        Type type = getType();
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

    default boolean isCollection() {
        Type type = getType();
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

    default boolean isMap() {
        Type type = getType();
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

    default Type getElementType() {
        if (isArray()) {
            Type type = getType();
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
        if (isCollection()) {
            Type type = getType();
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
        if (isMap()) {
            Type type = getType();
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

    default Type getKeyType() {
        if (isMap()) {
            Type type = getType();
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
}
