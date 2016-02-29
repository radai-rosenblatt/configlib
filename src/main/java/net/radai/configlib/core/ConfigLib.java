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

import net.radai.configlib.core.api.ConfigurationService;
import net.radai.configlib.core.spi.NopPostProcessor;
import net.radai.configlib.fs.PathWatcher;
import net.radai.configlib.ini.IniBeanCodec;

import java.nio.file.Path;

/**
 * Created by Radai Rosenblatt
 */
public class ConfigLib {

    public static <T> ConfigurationService<T> create(Class<T> configClass, Path configFile) {
        return new ConfigurationService<>(
                configClass,
                true,
                new PathWatcher(configFile),
                new IniBeanCodec("UTF-8"),
                new NopPostProcessor()
        );
    }
}
