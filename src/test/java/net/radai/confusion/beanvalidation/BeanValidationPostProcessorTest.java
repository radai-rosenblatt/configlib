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

package net.radai.confusion.beanvalidation;

import net.radai.confusion.cats.Cat;
import net.radai.confusion.cats.Cats;
import net.radai.confusion.core.spi.BeanPostProcessor;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Radai Rosenblatt
 */
public class BeanValidationPostProcessorTest {

    @Test
    public void testSingleCat() {
        BeanValidationPostProcessor postProcessor = new BeanValidationPostProcessor();
        BeanPostProcessor.Decision<Cat> decision;

        Cat invalid = new Cat("bob", "cannot exist", Arrays.asList("tuna", "lettuce"));
        decision = postProcessor.validate(null, invalid);
        Assert.assertFalse(decision.isUpdateConf());

        Cat valid = new Cat("bob", "much better now", Arrays.asList("tuna", "laser pointers"));
        decision = postProcessor.validate(null, valid);
        Assert.assertTrue(decision.isUpdateConf());
        Assert.assertTrue(decision.getConfToUse() == valid);
    }

    @Test
    public void testCats() {
        BeanValidationPostProcessor postProcessor = new BeanValidationPostProcessor();
        BeanPostProcessor.Decision<Cats> decision;

        Cats invalid = new Cats(null, null, new Cat(null, null, null), new Cat(null, null, Collections.singletonList("LETTUCE")));
        decision = postProcessor.validate(null, invalid);
        Assert.assertFalse(decision.isUpdateConf());
    }
}
