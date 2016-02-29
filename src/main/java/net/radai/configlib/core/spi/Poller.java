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

package net.radai.configlib.core.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Radai Rosenblatt
 */
public interface Poller {
    void start();
    void stop();
    void register(Listener newListener);
    void unregister(Listener existingListener);
    InputStream fetch() throws IOException;
    OutputStream store() throws IOException;

    interface Listener {
        void sourceChanged(InputStream newContents) throws IOException;
    }
}
