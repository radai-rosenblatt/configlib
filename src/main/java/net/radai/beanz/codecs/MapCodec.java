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

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static net.radai.beanz.util.ReflectionUtil.erase;

/**
 * Created by Radai Rosenblatt
 */
public class MapCodec implements Codec {
    private Type type;
    private Type keyType;
    private Type valueType;
    private Codec keyCodec;
    private Codec valueCodec;

    public MapCodec(Type type, Type keyType, Type valueType, Codec keyCodec, Codec valueCodec) {
        if (type == null || keyType == null || valueType == null || keyCodec == null || valueCodec == null
                || !ReflectionUtil.isMap(type)
                || !ClassUtils.isAssignable(erase(keyType), erase(ReflectionUtil.getKeyType(type)), true)
                || !ClassUtils.isAssignable(erase(valueType), erase(ReflectionUtil.getElementType(type)), true)
                || !ClassUtils.isAssignable(erase(keyCodec.getType()), erase(keyType), true)
                || !ClassUtils.isAssignable(erase(valueCodec.getType()), erase(valueType), true)) {
            throw new IllegalArgumentException();
        }
        this.type = type;
        this.keyType = keyType;
        this.valueType = valueType;
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
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
        if (!(encoded.startsWith("{") && encoded.endsWith("}"))) {
            throw new IllegalArgumentException();
        }
        String[] elements = encoded.substring(1, encoded.length()-1).split("\\s*,\\s*");
        Map map = ReflectionUtil.instantiateMap(erase(type));
        for (String element : elements) {
            String[] kvPair = element.split("\\s*=\\s*");
            Object key = keyCodec.decode(kvPair[0]);
            Object value = valueCodec.decode(kvPair[1]);
            //noinspection unchecked
            map.put(key, value);
        }
        return map;
    }

    @Override
    public String encode(Object object) {
        if (object == null) {
            return "";
        }
        if (!ReflectionUtil.isMap(object.getClass())) {
            throw new IllegalArgumentException();
        }
        Map<?, ?> map = (Map) object;
        if (map.isEmpty()) {
            return "{}";
        }
        StrBuilder sb = new StrBuilder();
        sb.append("{");
        for (Map.Entry<?, ?> entry  : map.entrySet()) {
            sb.append(keyCodec.encode(entry.getKey())).append("=").append(valueCodec.encode(entry.getValue())).append(", ");
        }
        sb.delete(sb.length()-2, sb.length()); //last ", "
        sb.append("}");
        return sb.toString();
    }
}
