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

import com.google.common.io.ByteStreams;
import net.radai.configlib.core.util.Listeners;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * an abstract base class for pollers that deals with listener registration
 * Created by Radai Rosenblatt
 */
public abstract class PollerSupport implements Poller {
    private final Listeners<Listener> listeners = new Listeners<>();

    @Override
    public void register(Listener newListener) {
        listeners.register(newListener);
    }

    @Override
    public void unregister(Listener existingListener) {
        listeners.unregister(existingListener);
    }

    protected void fire(InputStream input) throws IOException {
        byte[] data = ByteStreams.toByteArray(input); //read everything
        listeners.forEach(listener -> {
            try {
                listener.sourceChanged(new ByteArrayInputStream(data));
            } catch (Exception e) {
                //TODO - log error
                int g = 6;
            }
        });
    }
}
