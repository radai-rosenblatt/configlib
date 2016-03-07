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

package net.radai.configlib.spring;

import net.radai.configlib.cats.Cats;
import net.radai.configlib.core.api.ConfigurationChangeEvent;
import net.radai.configlib.core.api.ConfigurationService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Radai Rosenblatt
 */
public class ConfigurationConsumerBean implements ApplicationListener<PayloadApplicationEvent<ConfigurationChangeEvent<?>>> {
    private Cats conf;
    private ConfigurationService<Cats> confService;
    private List<Cats> cats = new ArrayList<>();

    public ConfigurationConsumerBean(ConfigurationService<Cats> confService) {
        this.confService = confService;
    }

    public Cats getConf() {
        return conf;
    }

    public void setConf(Cats conf) {
        this.conf = conf;
    }

    public List<Cats> getCats() {
        return cats;
    }

    @EventListener
    public void configChanged(ConfigurationChangeEvent<Cats> event) {
        int g = 6;
    }

    @Override
    public void onApplicationEvent(PayloadApplicationEvent<ConfigurationChangeEvent<?>> event) {
        cats.add((Cats) event.getPayload().getNewConf());
    }
}
