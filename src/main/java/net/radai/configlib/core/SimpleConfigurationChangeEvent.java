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

package net.radai.configlib.core;

import net.radai.configlib.core.api.ConfigurationChangeEvent;

import java.util.Objects;

/**
 * Created by Radai Rosenblatt
 */
public class SimpleConfigurationChangeEvent<T> implements ConfigurationChangeEvent<T> {
    private final Class<T> confClass;
    private final T oldConf;
    private final T newConf;

    public SimpleConfigurationChangeEvent(Class<T> confClass, T oldConf, T newConf) {
        this.confClass = confClass;
        this.oldConf = oldConf;
        this.newConf = newConf;
    }

    @Override
    public T getOldConf() {
        return oldConf;
    }

    @Override
    public T getNewConf() {
        return newConf;
    }

    @Override
    public Class<T> getConfigurationType() {
        return confClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleConfigurationChangeEvent<?> that = (SimpleConfigurationChangeEvent<?>) o;
        return Objects.equals(oldConf, that.oldConf) && Objects.equals(newConf, that.newConf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldConf, newConf);
    }
}
