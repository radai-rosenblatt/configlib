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

package net.radai.confusion.dagger2;

import dagger.Module;
import dagger.Provides;
import net.radai.confusion.cats.Cats;

import javax.inject.Named;

/**
 * Created by Radai Rosenblatt
 */
@Module
public class ConfigurationParametersModule {
    @Provides
    @Named("configurationClass")
    static Class getConfigurationClass() {
        return Cats.class;
    }
}
