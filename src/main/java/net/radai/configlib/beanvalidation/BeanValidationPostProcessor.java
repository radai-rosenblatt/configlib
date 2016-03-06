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

package net.radai.configlib.beanvalidation;

import net.radai.configlib.core.spi.BeanPostProcessor;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * Created by Radai Rosenblatt
 */
public class BeanValidationPostProcessor implements BeanPostProcessor{
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator(); //thread safe

    private boolean allowNullObject;

    public BeanValidationPostProcessor() {
        this(false);
    }

    public BeanValidationPostProcessor(boolean allowNullObject) {
        this.allowNullObject = allowNullObject;
    }

    @Override
    public <T> Decision<T> validate(T currConf, T discoveredConf) throws IllegalArgumentException {
        if (discoveredConf == null) {
            //nulls are not allowed under bean validation spec, so special handling
            return new Decision<>(allowNullObject, null, false);
        }
        Set<ConstraintViolation<T>> violations = validator.validate(discoveredConf);
        if (!violations.isEmpty()) {
            return new Decision<>(false, null, false);
        }
        return new Decision<>(true, discoveredConf, false);
    }
}
