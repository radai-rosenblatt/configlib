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
import net.radai.beanz.api.Codec;
import net.radai.beanz.api.Property;

import java.lang.reflect.Type;

/**
 * Created by Radai Rosenblatt
 */
public class CompositeProperty implements Property {
    private final Property[] delegates;

    public CompositeProperty(Property... delegates) {
        if (delegates.length < 2) {
            throw new IllegalArgumentException();
        }
        Property firstDelegate = delegates[0];
        String propName = firstDelegate.getName();
        Type type = firstDelegate.getValueType();
        Bean containingBean = firstDelegate.getContainingBean();
        for (Property delegate : delegates) {
            if (!propName.equals(delegate.getName())
                    || !type.equals(delegate.getValueType())
                    || !containingBean.equals(delegate.getContainingBean())) {
                throw new IllegalArgumentException();
            }
        }
        this.delegates = delegates;
    }

    @Override
    public Bean getContainingBean() {
        return delegates[0].getContainingBean();
    }

    @Override
    public String getName() {
        return delegates[0].getName();
    }

    @Override
    public Type getValueType() {
        return delegates[0].getValueType();
    }

    @Override
    public boolean isReadable() {
        for (Property delegate : delegates) {
            if (delegate.isReadable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isWritable() {
        for (Property delegate : delegates) {
            if (delegate.isWritable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object get(Object bean) {
        for (Property delegate : delegates) {
            if (delegate.isReadable()) {
                return delegate.get(bean);
            }
        }
        throw new IllegalStateException(); //not readable
    }

    @Override
    public void set(Object bean, Object value) {
        for (Property delegate : delegates) {
            if (delegate.isWritable()) {
                delegate.set(bean, value);
                return;
            }
        }
        throw new IllegalStateException(); //not writable
    }

    @Override
    public Codec getCodec() {
        return delegates[0].getCodec();
    }
}
