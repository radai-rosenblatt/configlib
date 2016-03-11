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
public interface BeanPostProcessor {
    <T> Decision<T> validate(T currConf, T discoveredConf) throws IllegalArgumentException;

    class Decision<T> {
        private final boolean updateConf;  //or ignore
        private final T confToUse;         //if update, what to use as the new conf
        private final boolean persistConf; //in addition to using the above conf, save it back to source

        public Decision(boolean updateConf, T confToUse, boolean persistConf) {
            this.updateConf = updateConf;
            this.confToUse = confToUse;
            this.persistConf = persistConf;
        }

        public boolean isUpdateConf() {
            return updateConf;
        }

        public T getConfToUse() {
            return confToUse;
        }

        public boolean isPersistConf() {
            return persistConf;
        }
    }
}
