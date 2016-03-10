/*
 * This file is part of Confusion.
 *
 * Confusion is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Confusion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Confusion.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.radai.confusion.spring;

import net.radai.confusion.core.api.ConfigurationChangeEvent;
import net.radai.confusion.core.api.ConfigurationService;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

/**
 * Created by Radai Rosenblatt
 */
public class SpringConfigurationChangedEvent<T> extends ApplicationEvent
        implements ConfigurationChangeEvent<T>, ResolvableTypeProvider {
    private final ConfigurationChangeEvent<T> delegate;

    public SpringConfigurationChangedEvent(ConfigurationService<T> source, ConfigurationChangeEvent<T> delegate) {
        super(source);
        this.delegate = delegate;
    }

    @Override
    public T getOldConf() {
        return delegate.getOldConf();
    }

    @Override
    public T getNewConf() {
        return delegate.getNewConf();
    }

    @Override
    public Class<T> getConfigurationType() {
        return delegate.getConfigurationType();
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), getConfigurationType());
    }
}
