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

package net.radai.confusion.core.spi;

/**
 * Created by Radai Rosenblatt
 */
public class SimplePostProcessor implements BeanPostProcessor {
    private boolean allowNullObject = false;

    public SimplePostProcessor() {
    }

    public SimplePostProcessor(boolean allowNullObject) {
        this.allowNullObject = allowNullObject;
    }

    public boolean isAllowNullObject() {
        return allowNullObject;
    }

    public void setAllowNullObject(boolean allowNullObject) {
        this.allowNullObject = allowNullObject;
    }

    @Override
    public <T> Decision<T> validate(T currConf, T discoveredConf) throws IllegalArgumentException {
        if (discoveredConf == null) {
            return new Decision<>(allowNullObject, null, false);
        }
        return handleNonNoll(currConf, discoveredConf);
    }

    protected <T> Decision<T> handleNonNoll(T currConf, T discoveredConf) throws IllegalArgumentException {
        return new Decision<>(true, discoveredConf, false);
    }
}
