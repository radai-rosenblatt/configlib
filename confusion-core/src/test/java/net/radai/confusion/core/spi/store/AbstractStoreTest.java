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

package net.radai.confusion.core.spi.store;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Radai Rosenblatt
 */
public abstract class AbstractStoreTest {

    protected abstract Store buildStore() throws Exception;
    protected void waitForQuiesce() throws Exception {
        Thread.sleep(150L);
    }

    @Test
    public void testTypeGetter() throws Exception {
        Assert.assertNotNull(buildStore().getPayloadType());
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleStart() throws Exception {
        Store store = buildStore();
        store.start();
        store.start();
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleStop() throws Exception {
        Store store = buildStore();
        store.start();
        store.stop();
        store.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void testStopBeforeStart() throws Exception {
        Store store = buildStore();
        store.stop();
    }

    @Test
    public void testStartStop() throws Exception {
        Store store = buildStore();
        store.start();
        store.stop();
    }
}
