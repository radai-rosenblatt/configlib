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

package net.radai.configlib.core;

import com.google.common.io.ByteStreams;
import net.radai.configlib.beanvalidation.BeanValidationPostProcessor;
import net.radai.configlib.cats.Cats;
import net.radai.configlib.core.api.ConfigurationListener;
import net.radai.configlib.fs.PathWatcher;
import net.radai.configlib.ini.IniBeanCodec;
import net.radai.configlib.util.TestUtil;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/**
 * Created by Radai Rosenblatt
 */
public class SimpleConfigurationServiceTest {

    @Test(expected = IllegalStateException.class)
    public void testCantStartWithNoConf() throws Exception {
        Path dir = Files.createTempDirectory("test");
        Path targetFile = dir.resolve("confFile");
        //start with no conf file
        Assert.assertTrue(Files.exists(dir));
        Assert.assertTrue(Files.notExists(targetFile));
        SimpleConfigurationService<Cats> service = new SimpleConfigurationService<>(
                Cats.class,
                new PathWatcher(targetFile),
                new IniBeanCodec(),
                new BeanValidationPostProcessor()
        );
        service.start();
    }

    @Test
    public void testNormalBoot() throws Exception {
        Path dir = Files.createTempDirectory("test");
        Path targetFile = dir.resolve("confFile");
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("cat.ini");
             OutputStream os = Files.newOutputStream(targetFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)){
            ByteStreams.copy(is, os);
        }
        Assert.assertTrue(Files.exists(targetFile));

        SimpleConfigurationService<Cats> service = new SimpleConfigurationService<>(
                Cats.class,
                new PathWatcher(targetFile),
                new IniBeanCodec(),
                new BeanValidationPostProcessor()
        );
        service.start();

        Cats initial = service.getConfiguration();
        Assert.assertNotNull(initial);
    }

    @Test
    public void testBadConfIgnored() throws Exception {
        Path dir = Files.createTempDirectory("test");
        Path targetFile = dir.resolve("confFile");
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("cat.ini");
             OutputStream os = Files.newOutputStream(targetFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)){
            ByteStreams.copy(is, os);
        }
        byte[] originalConf;
        try (InputStream is = Files.newInputStream(targetFile, StandardOpenOption.READ)) {
            originalConf = ByteStreams.toByteArray(is);
        }
        SimpleConfigurationService<Cats> service = new SimpleConfigurationService<>(
                Cats.class,
                new PathWatcher(targetFile),
                new IniBeanCodec(),
                new BeanValidationPostProcessor()
        );
        //noinspection unchecked
        ConfigurationListener<Cats> listener = Mockito.mock(ConfigurationListener.class);
        service.register(listener);
        service.start();
        Mockito.verifyZeroInteractions(listener); //boot does not trigger listeners
        Cats initial = service.getConfiguration();
        Assert.assertNotNull(initial);

        //change conf to something invalid
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("invalidCat.ini");
             OutputStream os = Files.newOutputStream(targetFile, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)){
            ByteStreams.copy(is, os);
        }
        TestUtil.waitForFsQuiesce();
        Mockito.verifyZeroInteractions(listener); //conf is invalid, listeners should not have been called

        byte[] currentConf;
        try (InputStream is = Files.newInputStream(targetFile, StandardOpenOption.READ)) {
            currentConf = ByteStreams.toByteArray(is);
        }
        Assert.assertFalse(Arrays.equals(originalConf, currentConf)); //yet conf was changed on disk
        Cats current = service.getConfiguration();
        Assert.assertTrue(initial == current);
    }

    @Test
    public void testGoodConfPickedUp() throws Exception {
        Path dir = Files.createTempDirectory("test");
        Path targetFile = dir.resolve("confFile");
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("cats.ini");
             OutputStream os = Files.newOutputStream(targetFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)){
            ByteStreams.copy(is, os);
        }
        SimpleConfigurationService<Cats> service = new SimpleConfigurationService<>(
                Cats.class,
                new PathWatcher(targetFile),
                new IniBeanCodec(),
                new BeanValidationPostProcessor()
        );
        //noinspection unchecked
        ConfigurationListener<Cats> listener = Mockito.mock(ConfigurationListener.class);
        service.register(listener);
        service.start();
        Cats initial = service.getConfiguration();
        //change conf to something valid
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("cat.ini");
             OutputStream os = Files.newOutputStream(targetFile, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)){
            ByteStreams.copy(is, os);
        }
        TestUtil.waitForFsQuiesce();
        Cats current = service.getConfiguration();
        Assert.assertTrue(current != initial);
        Mockito.verify(listener).configurationChanged(Mockito.eq(new SimpleConfigurationChangeEvent<>(Cats.class, initial, current))); //listener was notified
    }
}
