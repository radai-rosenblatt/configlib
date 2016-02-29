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

package net.radai.configlib.core.api;

import net.radai.configlib.core.spi.BeanCodec;
import net.radai.configlib.core.spi.BeanPostProcessor;
import net.radai.configlib.core.spi.Poller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Radai Rosenblatt
 */
public class ConfigurationService<T> implements Poller.Listener {
    //configuration
    private final Class<T> confBeanClass;
    private final boolean allowNullConf;

    //components
    private final Poller poller;
    private final BeanCodec codec;
    private final BeanPostProcessor postProcessor;

    //state
    private final AtomicReference<T> ref = new AtomicReference<>(null);
    private volatile boolean on = false;

    public ConfigurationService(
            Class<T> confBeanClass,
            boolean allowNullConf,
            Poller poller,
            BeanCodec codec,
            BeanPostProcessor postProcessor
    ) {
        this.confBeanClass = confBeanClass;
        this.allowNullConf = allowNullConf;
        this.poller = poller;
        this.codec = codec;
        this.postProcessor = postProcessor;
        try (InputStream in = poller.fetch()) {
            loadConf(in);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    public T getConfiguration() {
        T latest = ref.get(); //grab ref 1st to avoid race with shutdown
        if (!on) {
            throw new IllegalStateException();
        }
        return latest;
    }

    public synchronized void start() {
        if (on) {
            throw new IllegalStateException();
        }
        T confBean;
        try (InputStream is = poller.fetch()){
            confBean = codec.parse(confBeanClass, is);
            postProcessor.validate(null, confBean);
        } catch (IOException e) {
            //TODO - handle properly
            throw new IllegalStateException(e);
        }
        ref.set(confBean);
        poller.register(this);
        poller.start();
        on = true;
    }

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
    public void sourceChanged(InputStream newContents) throws IOException {
        loadConf(newContents);
    }

    private void loadConf(InputStream source) throws IOException {
        T oldBean = ref.get();
        T newBean = codec.parse(confBeanClass, source);
        BeanPostProcessor.Decision<T> decision = postProcessor.validate(oldBean, newBean);
        if (!decision.isUpdateConf()) {
            return; //do nothing
        }
        T processedBean = decision.getConfToUse();
        if (processedBean == null && !allowNullConf) {
            throw new IllegalStateException();
        }
        if (decision.isPersistConf()) {
            try (OutputStream out = poller.store()) {
                codec.serialize(processedBean, out);
            }
        }
        if (!ref.compareAndSet(oldBean, newBean)) {
            throw new IllegalStateException(); //there should only be a single thread driving this. this shouldnt happen
        }
        //TODO - call listeners with new conf
    }
}
