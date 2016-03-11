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

package net.radai.confusion.core.fs;

import net.radai.confusion.core.fs.FSUtil;

/**
 * Created by Radai Rosenblatt
 */
public class FsTestUtil {
    /**
     * waits long enough to allow file system modifications to be picked up
     * @throws InterruptedException
     */
    public static void waitForFsQuiesce() throws InterruptedException {
        Thread.sleep(5 * FSUtil.MIN_DELAY_MILLIS);
    }
}
