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

package net.radai.confusion.core.spi.source;

import net.radai.confusion.core.util.Listeners;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Radai Rosenblatt
 */
public abstract class AbstractSource<T> implements Source<T> {
    private final Logger log = LogManager.getLogger(getClass());
    private final Listeners<SourceListener<T>> listeners = new Listeners<>();

    @Override
    public void register(SourceListener<T> newListener) {
        listeners.register(newListener);
    }

    @Override
    public void unregister(SourceListener<T> existingListener) {
        listeners.unregister(existingListener);
    }

    protected void fire(T newConf) {
        listeners.forEach(listener -> {
            try {
                listener.sourceChanged(newConf);
            } catch (Exception e) {
                log.error("caught while firing change event", e);
            }
        });
    }
}
