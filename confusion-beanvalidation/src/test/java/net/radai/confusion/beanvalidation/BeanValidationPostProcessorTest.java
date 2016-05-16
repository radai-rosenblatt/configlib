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
import net.radai.confusion.core.spi.validator.ValidationResults;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * Created by Radai Rosenblatt
 */
public class BeanValidationPostProcessorTest {

    @Test
    public void testSingleCat() {
        BeanValidator postProcessor = new BeanValidator();
        ValidationResults<Set<ConstraintViolation<Object>>> validationResults;

        Cat invalid = new Cat("bob", "cannot exist", Arrays.asList("tuna", "lettuce"));
        validationResults = postProcessor.validate(null, invalid);
        Assert.assertFalse(validationResults.isValid());

        Cat valid = new Cat("bob", "much better now", Arrays.asList("tuna", "laser pointers"));
        validationResults = postProcessor.validate(null, valid);
        Assert.assertTrue(validationResults.isValid());
        Assert.assertTrue(validationResults.getValidatorOutput().isEmpty());
    }

    @Test
    public void testCats() {
        BeanValidator postProcessor = new BeanValidator();
        ValidationResults<Set<ConstraintViolation<Object>>> validationResults;

        Cats invalid = new Cats(null, null, new Cat(null, null, null), new Cat(null, null, Collections.singletonList("LETTUCE")));
        validationResults = postProcessor.validate(null, invalid);
        Assert.assertFalse(validationResults.isValid());
    }
}
