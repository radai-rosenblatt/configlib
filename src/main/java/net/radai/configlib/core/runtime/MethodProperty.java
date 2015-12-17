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

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created by Radai Rosenblatt
 */
public class MethodProperty implements Property {
    private final String name;
    private final Method getter;
    private final Method setter;

    public MethodProperty(String name, Method getter, Method setter) {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        if (getter != null) {
            return getter.getGenericReturnType();
        }
        return setter.getGenericParameterTypes()[0];
    }

    @Override
    public boolean isReadable() {
        return getter != null;
    }

    @Override
    public boolean isWritable() {
        return setter != null;
    }

    @Override
    public String toString() {
        String typeName = getType().getTypeName().substring(getType().getTypeName().lastIndexOf(".")+1);
        String result = typeName + " " + name + ": ";
        if (getter != null) {
            result += getter.getName() + "()";
        } else {
            result += "-";
        }
        result += " / ";
        if (setter != null) {
            result += setter.getName() + "(" + typeName + ")"; //TODO - reflect real arg to setter
        } else {
            result += "-";
        }
        return result;
    }
}
