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

package net.radai.confusion.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * Created by Radai Rosenblatt
 */
public class Listeners<T> {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<T> listeners = new ArrayList<>();

    public void register(T newListener) {
        if (newListener == null) {
            throw new IllegalArgumentException();
        }
        try (CloseableLock ignored = new CloseableLock(lock.writeLock())){
            if (listeners.contains(newListener)) {
                throw new IllegalStateException("listener already registered: " + newListener);
            }
            listeners.add(newListener);
        }
    }

    public void unregister(T existingListener) {
        if (existingListener == null) {
            throw new IllegalArgumentException();
        }
        try (CloseableLock ignored = new CloseableLock(lock.writeLock())){
            if (!listeners.remove(existingListener)) {
                throw new IllegalStateException("listener not registered: " + existingListener);
            }
        }
    }

    public void forEach(Consumer<T> action) {
        if (action == null) {
            throw new IllegalArgumentException();
        }
        try (CloseableLock ignored = new CloseableLock(lock.readLock())){
            listeners.forEach(listener -> {
                try {
                    action.accept(listener);
                } catch (Exception e) {
                    //TODO - log error
                }
            });
        }
    }
}
