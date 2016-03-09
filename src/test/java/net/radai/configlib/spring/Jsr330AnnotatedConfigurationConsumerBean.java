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
import org.springframework.context.event.EventListener;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Radai Rosenblatt
 */
@Named
public class Jsr330AnnotatedConfigurationConsumerBean {
    @Inject
    private ConfigurationService<Cats> confService;
    @Inject
    private Cats initialConf;
    private List<Cats> cats = new ArrayList<>();

    @EventListener
    public void configChanged(ConfigurationChangeEvent<Cats> event) {
        cats.add(event.getNewConf());
    }

    public ConfigurationService<Cats> getConfService() {
        return confService;
    }

    public Cats getInitialConf() {
        return initialConf;
    }

    public List<Cats> getCats() {
        return cats;
    }
}
