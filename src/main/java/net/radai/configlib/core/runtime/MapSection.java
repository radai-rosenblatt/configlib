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

import net.radai.beanz.util.ReflectionUtil;

import java.lang.reflect.Type;

/**
 * @author Radai Rosenblatt
 */
public class MapSection implements Section {
    private String name;
    private Type keyType;
    private Type valueType;

    public MapSection(String name, Type keyType, Type valueType) {
        this.name = name;
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(": Map<");
        if (keyType != null) {
            sb.append(ReflectionUtil.prettyPrint(keyType));
        } else {
            sb.append("?");
        }
        sb.append(", ");
        if (valueType != null) {
            sb.append(ReflectionUtil.prettyPrint(valueType));
        } else {
            sb.append("?");
        }
        sb.append(">");
        return sb.toString();
    }
}
