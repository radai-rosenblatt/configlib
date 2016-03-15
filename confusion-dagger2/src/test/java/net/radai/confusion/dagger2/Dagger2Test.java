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

package net.radai.confusion.dagger2;

import net.radai.confusion.core.api.ConfigurationService;
import org.junit.Test;

/**
 * Created by Radai Rosenblatt
 */
public class Dagger2Test {
    @Test
    public void testSomething() throws Exception {
        Application app1 = DaggerApplication.create();
        ConfigurationService cs1 = app1.config();
        ConfigurationService cs2 = app1.config();
        Application app2 = DaggerApplication.create();
        ConfigurationService cs3 = app2.config();
        ConfigurationService cs4 = app2.config();
        int g = 8;
    }
}
