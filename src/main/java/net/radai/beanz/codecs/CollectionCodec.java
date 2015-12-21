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

package net.radai.beanz.codecs;

import net.radai.beanz.api.Codec;
import net.radai.beanz.util.ReflectionUtil;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.text.StrBuilder;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;

import static net.radai.beanz.util.ReflectionUtil.erase;

/**
 * Created by Radai Rosenblatt
 */
public class CollectionCodec implements Codec {
    private Type type;
    private Type elementType;
    private Codec elementCodec;

    public CollectionCodec(Type type, Type elementType, Codec elementCodec) {
        if (type == null || elementType == null || elementCodec == null
                || !ClassUtils.isAssignable(erase(elementCodec.getType()), erase(elementType), true)
                || !ReflectionUtil.isCollection(type)) {
            throw new IllegalArgumentException();
        }
        this.type = type;
        this.elementType = elementType;
        this.elementCodec = elementCodec;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Object decode(String encoded) {
        if (encoded == null || (encoded = encoded.trim()).isEmpty()) {
            return null;
        }
        if (!(encoded.startsWith("[") && encoded.endsWith("]"))) {
            throw new IllegalArgumentException();
        }
        String[] elements = encoded.substring(1, encoded.length()-1).split("\\s*,\\s*");
        Collection collection = ReflectionUtil.instantiateCollection(ReflectionUtil.erase(type));
        for (String element : elements) {
            //noinspection unchecked
            collection.add(elementCodec.decode(element));
        }
        return collection;
    }

    @Override
    public String encode(Object object) {
        if (object == null) {
            return "";
        }
        if (!ReflectionUtil.isCollection(object.getClass())) {
            throw new IllegalArgumentException();
        }
        Collection collection = (Collection) object;
        if (collection.isEmpty()) {
            return "[]";
        }
        StrBuilder sb = new StrBuilder();
        sb.append("[");
        for (Object element : collection) {
            sb.append(elementCodec.encode(element)).append(", ");
        }
        sb.delete(sb.length()-2, sb.length()); //last ", "
        sb.append("]");
        return sb.toString();
    }

    public Codec getElementCodec() {
        return elementCodec;
    }
}
