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
import net.radai.confusion.core.spi.validator.ValidatorDecision;
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
        net.radai.confusion.beanvalidation.Validator postProcessor = new net.radai.confusion.beanvalidation.Validator();
        ValidatorDecision<Cat> validatorDecision;

        Cat invalid = new Cat("bob", "cannot exist", Arrays.asList("tuna", "lettuce"));
        validatorDecision = postProcessor.validate(null, invalid);
        Assert.assertFalse(validatorDecision.isUpdateConf());

        Cat valid = new Cat("bob", "much better now", Arrays.asList("tuna", "laser pointers"));
        validatorDecision = postProcessor.validate(null, valid);
        Assert.assertTrue(validatorDecision.isUpdateConf());
        Assert.assertTrue(validatorDecision.getConfToUse() == valid);
    }

    @Test
    public void testCats() {
        net.radai.confusion.beanvalidation.Validator postProcessor = new net.radai.confusion.beanvalidation.Validator();
        ValidatorDecision<Cats> validatorDecision;

        Cats invalid = new Cats(null, null, new Cat(null, null, null), new Cat(null, null, Collections.singletonList("LETTUCE")));
        validatorDecision = postProcessor.validate(null, invalid);
        Assert.assertFalse(validatorDecision.isUpdateConf());
    }
}
