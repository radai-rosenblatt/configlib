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

package net.radai.confusion.core;

import net.radai.confusion.core.spi.codec.Codec;
import net.radai.confusion.core.spi.source.Sources;
import net.radai.confusion.core.spi.store.Store;
import net.radai.confusion.core.spi.validator.NopValidator;
import net.radai.confusion.core.spi.validator.NotNullValidator;

/**
 * Created by Radai Rosenblatt
 */
public class Confusion {

    public static <T> SimpleConfigurationService<T> create(
            Class<T> configClass,
            Store store,
            Codec codec
    ) {
        return create(configClass, store, codec, true);
    }

    public static <T> SimpleConfigurationService<T> create(
            Class<T> configClass,
            Store store,
            Codec codec,
            boolean allowNull
    ) {
        return new SimpleConfigurationService<>(
                configClass,
                Sources.from(configClass, store, codec),
                allowNull ? new NopValidator() : new NotNullValidator()
        );
    }
}
