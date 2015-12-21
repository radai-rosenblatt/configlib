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

package net.radai.beanz.api;

import net.radai.beanz.api.Codec;
import net.radai.beanz.api.Property;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static net.radai.beanz.util.ReflectionUtil.erase;

/**
 * @author Radai Rosenblatt
 */
public class Bean {
    private Map<String, Property> properties = new HashMap<>();
    private Map<Type, Codec> codecs = new HashMap<>();

    public void addProperty(Property prop) {
        String name = prop != null ? prop.getName() : null;
        if (prop == null || name == null || name.isEmpty() || properties.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        properties.put(name, prop);
    }

    public Property getProperty(String propName) {
        return properties.get(propName);
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public void addCodec(Type type, Codec codec) {
        if (codec == null || type == null || !ClassUtils.isAssignable(erase(codec.getType()), erase(type), true) || codecs.containsKey(type)) {
            throw new IllegalArgumentException();
        }
        codecs.put(type, codec);
    }
}
