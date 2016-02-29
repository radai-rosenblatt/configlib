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
import net.radai.configlib.core.util.CloseableLock;
import net.radai.configlib.core.util.DelegateInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Radai Rosenblatt
 */
public abstract class AbstractPoller implements Poller {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<Listener> listeners = new ArrayList<>();

    @Override
    public void register(Listener newListener) {
        try (CloseableLock ignored = new CloseableLock(lock.writeLock())){
            if (listeners.contains(newListener)) {
                throw new IllegalStateException("listener already registered: " + newListener);
            }
            listeners.add(newListener);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void unregister(Listener existingListener) {
        try (CloseableLock ignored = new CloseableLock(lock.writeLock())){
            if (!listeners.remove(existingListener)) {
                throw new IllegalStateException("listener not registered: " + existingListener);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected void fire(InputStream input) throws IOException {
        try (CloseableLock ignored = new CloseableLock(lock.readLock())){
            if (listeners.isEmpty()) {
                //TODO - log.warn ?
                return;
            } else if (listeners.size() == 1) {
                listeners.get(0).sourceChanged(input);
            } else {
                byte[] data = ByteStreams.toByteArray(input);
                for (Listener listener : listeners) {
                    try {
                        listener.sourceChanged(new ByteArrayInputStream(data));
                    } catch (Exception e) {
                        //TODO - log
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
