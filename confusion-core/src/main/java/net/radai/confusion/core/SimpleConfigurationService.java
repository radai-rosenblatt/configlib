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
import net.radai.confusion.core.spi.BeanCodec;
import net.radai.confusion.core.spi.BeanPostProcessor;
import net.radai.confusion.core.spi.Poller;
import net.radai.confusion.core.util.Listeners;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Radai Rosenblatt
 */
public class SimpleConfigurationService<T> implements Poller.Listener, ConfigurationService<T>, ConfigurationServiceLifecycle, AutoCloseable {
    //configuration
    private final Class<T> confBeanClass;

    //components
    private final Poller poller;
    private final BeanCodec codec;
    private final BeanPostProcessor postProcessor;
    private final Listeners<ConfigurationListener<T>> listeners = new Listeners<>();

    //state
    private final AtomicReference<T> ref = new AtomicReference<>(null);
    private volatile boolean on = false;

    public SimpleConfigurationService(
            Class<T> confBeanClass,
            Poller poller,
            BeanCodec codec,
            BeanPostProcessor postProcessor
    ) {
        if (confBeanClass == null || poller == null || codec == null || postProcessor == null) {
            throw new IllegalArgumentException("all arguments are mandatory");
        }
        this.confBeanClass = confBeanClass;
        this.poller = poller;
        this.codec = codec;
        this.postProcessor = postProcessor;
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
        boolean success;
        try (InputStream is = poller.fetch()){
            success = loadConf(is, false);
        } catch (Exception e) {
            //TODO - handle properly
            throw new IllegalStateException(e);
        }
        if (!success) {
            throw new IllegalStateException("unable to load initial configuration");
        }
        poller.register(this);
        poller.start();
        on = true;
    }

    @Override
    public synchronized void stop() {
        if (!on) {
            throw new IllegalStateException();
        }
        on = false;
        poller.unregister(this);
        ref.set(null);
        poller.stop();
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
    public void sourceChanged(InputStream newContents) throws IOException {
        loadConf(newContents, true);
    }

    /**
     * handles loading a conf from an input stream
     * @param source source of (potential) new configuration
     * @param notifyListeners notify conf listeners (if conf passes validation)
     * @return true if process resulted in new configuration being loaded
     * @throws IOException
     */
    private boolean loadConf(InputStream source, boolean notifyListeners) throws IOException {
        T oldBean = ref.get();
        T newBean = source == null ? null : codec.parse(confBeanClass, source);
        BeanPostProcessor.Decision<T> decision = postProcessor.validate(oldBean, newBean);
        if (!decision.isUpdateConf()) {
            return false; //do nothing
        }
        T processedBean = decision.getConfToUse();
        if (decision.isPersistConf()) {
            try (OutputStream out = poller.store()) {
                codec.serialize(processedBean, out);
            }
        }
        if (!ref.compareAndSet(oldBean, newBean)) {
            throw new IllegalStateException(); //should never happen - poller is sequential and so is start().
        }
        if (notifyListeners) {
            ConfigurationChangeEvent<T> event = new SimpleConfigurationChangeEvent<>(getConfigurationType(), oldBean, newBean);
            listeners.forEach(listener -> listener.configurationChanged(event));
        }
        return true;
    }
}
