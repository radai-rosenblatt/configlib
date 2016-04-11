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

import net.radai.confusion.core.api.*;
import net.radai.confusion.core.spi.BeanPostProcessor;
import net.radai.confusion.core.spi.source.Source;
import net.radai.confusion.core.spi.source.SourceListener;
import net.radai.confusion.core.util.Listeners;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Radai Rosenblatt
 */
public class SimpleConfigurationService<T> implements ConfigurationService<T>, ServiceLifecycle, SourceListener<T> {
    private final Logger log = LogManager.getLogger(getClass());

    //configuration
    private final Class<T> confBeanClass;

    //components
    private final Source<T> source;
    private final BeanPostProcessor postProcessor;
    private final Listeners<ConfigurationListener<T>> listeners = new Listeners<>();

    //state
    private final AtomicReference<T> ref = new AtomicReference<>(null);
    private volatile boolean on = false;

    public SimpleConfigurationService(
            Class<T> confBeanClass,
            Source<T> source,
            BeanPostProcessor postProcessor
    ) {
        if (confBeanClass == null || source == null || postProcessor == null) {
            throw new IllegalArgumentException("all arguments are mandatory");
        }
        this.confBeanClass = confBeanClass;
        this.source = source;
        this.postProcessor = postProcessor;
        this.source.register(this);
        //we only load the actual conf when we're started
    }

    @Override
    public void register(ConfigurationListener<T> newListener) {
        listeners.register(newListener);
    }

    @Override
    public void unregister(ConfigurationListener<T> existingListener) {
        listeners.unregister(existingListener);
    }

    @Override
    public T getConfiguration() {
        T latest = ref.get(); //grab ref 1st to avoid race with stop()
        if (!on) {
            throw new IllegalStateException();
        }
        return latest;
    }

    @Override
    public Class<T> getConfigurationType() {
        return confBeanClass;
    }

    @Override
    public synchronized void start() {
        if (on) {
            throw new IllegalStateException();
        }
        try {
            if (!loadConf(source.read(), false)) {
                //means conf did not pass validation
                throw new IllegalStateException("unable to load initial configuration");
            }
        } catch (IllegalStateException e) {
            throw e; //pass-through
        } catch (Exception e) {
            throw new IllegalStateException("while loading initial configuration", e);
        }
        source.start();
        on = true;
    }

    @Override
    public synchronized void stop() {
        if (!on) {
            throw new IllegalStateException();
        }
        //order of on toggle vs ref clear is important
        on = false;
        source.stop();
        ref.set(null);
    }

    @Override
    public boolean isStarted() {
        return on;
    }

    @Override
    public void close() {
        stop();
    }

    @Override
    public void sourceChanged(T newValue) throws Exception {
        loadConf(newValue, true);
    }

    /**
     * handles loading a conf from an input stream
     * @param newBean (potential) new configuration
     * @param notifyListeners notify conf listeners (if conf passes validation)
     * @return true if process resulted in new configuration being loaded
     */
    private synchronized boolean loadConf(T newBean, boolean notifyListeners) throws IOException {
        T oldBean = ref.get();
        BeanPostProcessor.Decision<T> decision = postProcessor.validate(oldBean, newBean);
        if (!decision.isUpdateConf()) {
            return false; //do nothing
        }
        T processedBean = decision.getConfToUse();
        if (decision.isPersistConf()) {
            source.write(processedBean); //may throw IOException
        }
        if (!ref.compareAndSet(oldBean, newBean)) {
            throw new IllegalStateException(); //should never happen - source is sequential and so is start().
        }
        if (notifyListeners) {
            ConfigurationChangeEvent<T> event = new SimpleConfigurationChangeEvent<>(getConfigurationType(), oldBean, newBean);
            listeners.forEach(listener -> listener.configurationChanged(event));
        }
        return true;
    }
}
