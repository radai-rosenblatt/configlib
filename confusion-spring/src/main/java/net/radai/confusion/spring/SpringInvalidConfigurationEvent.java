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

import net.radai.confusion.core.api.ConfigurationService;
import net.radai.confusion.core.api.InvalidConfigurationEvent;
import net.radai.confusion.core.spi.validator.ValidationResults;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

/**
 * Created by Radai Rosenblatt
 */
public class SpringInvalidConfigurationEvent<T> extends ApplicationEvent
        implements InvalidConfigurationEvent <T>, ResolvableTypeProvider {
    private final InvalidConfigurationEvent<T> delegate;

    public SpringInvalidConfigurationEvent(ConfigurationService<T> source, InvalidConfigurationEvent<T> delegate) {
        super(source);
        this.delegate = delegate;
    }
    @Override
    public T getInvalidConf() {
        return delegate.getInvalidConf();
    }

    @Override
    public ValidationResults<?> getValidationResults() {
        return delegate.getValidationResults();
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
