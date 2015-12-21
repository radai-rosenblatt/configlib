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

package net.radai.beanz.properties;

import net.radai.beanz.api.Bean;
import net.radai.beanz.util.ReflectionUtil;
import net.radai.beanz.api.Codec;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * Created by Radai Rosenblatt
 */
public class FieldProperty extends SimpleProperty {
    private final Field field;

    public FieldProperty(Bean containingBean, String name, Type type, Codec codec, Field field) {
        super(containingBean, name, type, codec);
        this.field = field;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public Object get(Object bean) {
        try {
            return field.get(bean);
        } catch (IllegalAccessException e) {
            //todo - support using private fields
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void set(Object bean, Object value) {
        try {
            field.set(bean, value);
        }  catch (IllegalAccessException e) {
            //todo - support using private fields
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        String typeName = ReflectionUtil.prettyPrint(getValueType());
        int modifiers = field.getModifiers();
        String accessModifier = "";
        if (Modifier.isPrivate(modifiers)) {
            accessModifier = "private";
        } else if (Modifier.isProtected(modifiers)) {
            accessModifier = "protected";
        } else if (Modifier.isPublic(modifiers)) {
            accessModifier = "public";
        }
        return typeName + " " + getName() + ": "
                + accessModifier + ReflectionUtil.prettyPrint(field.getGenericType()) + field.getName();
    }
}
