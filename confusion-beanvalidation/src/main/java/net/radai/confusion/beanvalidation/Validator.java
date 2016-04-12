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

import net.radai.confusion.core.spi.validator.ValidatorDecision;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.util.Set;

/**
 * Created by Radai Rosenblatt
 */
public class Validator implements net.radai.confusion.core.spi.validator.Validator {
    private static final javax.validation.Validator validator = Validation.buildDefaultValidatorFactory().getValidator(); //thread safe

    private boolean allowNullObject;

    public Validator() {
        this(false);
    }

    public Validator(boolean allowNullObject) {
        this.allowNullObject = allowNullObject;
    }

    @Override
    public <T> ValidatorDecision<T> validate(T currConf, T discoveredConf) throws IllegalArgumentException {
        if (discoveredConf == null) {
            //nulls are not allowed under bean validation spec, so special handling
            return new ValidatorDecision<>(allowNullObject, null, false);
        }
        Set<ConstraintViolation<T>> violations = validator.validate(discoveredConf);
        if (!violations.isEmpty()) {
            return new ValidatorDecision<>(false, null, false);
        }
        return new ValidatorDecision<>(true, discoveredConf, false);
    }
}
