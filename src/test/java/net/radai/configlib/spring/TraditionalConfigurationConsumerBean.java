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
import net.radai.configlib.core.api.ConfigurationService;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Radai Rosenblatt
 */
public class TraditionalConfigurationConsumerBean implements ApplicationListener<SpringConfigurationChangedEvent<Cats>> {
    private Cats conf;
    private ConfigurationService<Cats> confService;
    private List<Cats> cats = new ArrayList<>();

    public TraditionalConfigurationConsumerBean(ConfigurationService<Cats> confService) {
        this.confService = confService;
    }

    public Cats getConf() {
        return conf;
    }

    public void setConf(Cats conf) {
        this.conf = conf;
    }

    public ConfigurationService<Cats> getConfService() {
        return confService;
    }

    public List<Cats> getCats() {
        return cats;
    }

    @Override
    public void onApplicationEvent(SpringConfigurationChangedEvent<Cats> event) {
        cats.add(event.getNewConf());
    }
}
