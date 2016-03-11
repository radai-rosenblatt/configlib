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

import net.radai.confusion.core.spi.SimplePostProcessor;
import net.radai.confusion.core.fs.PathWatcher;

import java.io.File;

/**
 * Created by Radai Rosenblatt
 */
public class Confusion {

    public static <T> SimpleConfigurationService<T> create(Class<T> configClass, File configFile) {
        throw new UnsupportedOperationException("TBD");
//        return new SimpleConfigurationService<>(
//                configClass,
//                new PathWatcher(configFile.toPath()),
//                new IniBeanCodec("UTF-8"),
//                new SimplePostProcessor()
//        );
    }
}
