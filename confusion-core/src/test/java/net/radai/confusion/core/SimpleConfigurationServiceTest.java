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

import net.radai.confusion.core.api.ConfigurationChangeEvent;
import net.radai.confusion.core.api.ConfigurationListener;
import net.radai.confusion.core.spi.BeanPostProcessor;
import net.radai.confusion.core.spi.source.Source;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by Radai Rosenblatt
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleConfigurationServiceTest {

    @Mock
    private Source<ConfClass> source;
    @Mock
    private BeanPostProcessor postProcessor;
    @Mock
    private ConfigurationListener<ConfClass> listener;

    private SimpleConfigurationService<ConfClass> confService;

    @Before
    public void setup() {
        confService = new SimpleConfigurationService<>(ConfClass.class, source, postProcessor);
        Mockito.verify(source).register(Mockito.eq(confService)); //happens @constructor time
        confService.register(listener);
    }

    @Test(expected = IllegalStateException.class)
    public void testCantStartWithNoConf() throws Exception {
        Mockito.when(source.read()).thenReturn(null);
        Mockito.when(postProcessor.validate(Mockito.any(), Mockito.isNull())).thenReturn(new BeanPostProcessor.Decision<>(false, null, false));
        confService.start();
    }

    @Test
    public void testNormalBoot() throws Exception {
        ConfClass c1 = new ConfClass();
        Mockito.when(source.read()).thenReturn(c1);
        Mockito.when(postProcessor.validate(Mockito.any(), Mockito.eq(c1))).thenReturn(new BeanPostProcessor.Decision<>(true, c1, false));

        confService.start();
        Mockito.verify(source).start();
        ConfClass initial = confService.getConfiguration();
        Assert.assertTrue(initial == c1);
        Mockito.verifyNoMoreInteractions(listener); //listeners not called on boot
    }

    @Test
    public void testBadConfIgnored() throws Exception {
        //conf 1 is valid
        ConfClass c1 = new ConfClass();
        Mockito.when(postProcessor.validate(Mockito.any(), Mockito.eq(c1))).thenReturn(new BeanPostProcessor.Decision<>(true, c1, false));
        //conf 2 is invalid
        ConfClass c2 = new ConfClass();
        Mockito.when(postProcessor.validate(Mockito.any(), Mockito.eq(c2))).thenReturn(new BeanPostProcessor.Decision<>(false, null, false));
        //current state is 1
        Mockito.when(source.read()).thenReturn(c1);

        confService.start();
        Assert.assertTrue(confService.getConfiguration() == c1);

        confService.sourceChanged(c2);
        Assert.assertTrue(confService.getConfiguration() == c1); //still 1
        Mockito.verifyNoMoreInteractions(listener); //listeners not called on bad conf
    }

    @Test
    public void testGoodConfPickedUp() throws Exception {
        //conf 1 is valid
        ConfClass c1 = new ConfClass();
        Mockito.when(postProcessor.validate(Mockito.any(), Mockito.eq(c1))).thenReturn(new BeanPostProcessor.Decision<>(true, c1, false));
        //conf 2 also valid
        ConfClass c2 = new ConfClass();
        Mockito.when(postProcessor.validate(Mockito.any(), Mockito.eq(c2))).thenReturn(new BeanPostProcessor.Decision<>(true, c2, false));
        //current state is 1
        Mockito.when(source.read()).thenReturn(c1);

        confService.start();
        Assert.assertTrue(confService.getConfiguration() == c1);

        confService.sourceChanged(c2);
        Assert.assertTrue(confService.getConfiguration() == c2); //picked up
        ArgumentCaptor<ConfigurationChangeEvent<ConfClass>> capture = ArgumentCaptor.forClass(ConfigurationChangeEvent.class);
        Mockito.verify(listener).configurationChanged(capture.capture());
        ConfigurationChangeEvent<ConfClass> event = capture.getValue();
        Assert.assertEquals(ConfClass.class, event.getConfigurationType());
        Assert.assertEquals(c1, event.getOldConf());
        Assert.assertEquals(c2, event.getNewConf());
    }

    private static class ConfClass {
        //nothing
    }
}
