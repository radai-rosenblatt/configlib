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

package net.radai.configlib.core.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

/**
 * Created by Radai Rosenblatt
 */
public class FSUtil {
    public static final long POLL_MILLIS = 10L;
    public static final int POLLS_TO_QUIESCE = 5;
    public static final long MIN_DELAY_MILLIS = POLLS_TO_QUIESCE * POLL_MILLIS;

    /**
     * waits for a file to be "stable" (no modification over some period of time)
     * @param path file to wait until stable
     */
    public static void waitUntilQuiet(Path path) {
        try {
            FileTime prevMtime = Files.getLastModifiedTime(path);
            FileTime mTime;
            int numSame = 0;
            long start = System.nanoTime();
            while (true) {
                Thread.sleep(POLL_MILLIS);
                mTime = Files.getLastModifiedTime(path);
                if (prevMtime.compareTo(mTime) == 0) {
                    numSame++;
                    if (numSame >= POLLS_TO_QUIESCE) {
                        break;
                    }
                }
                prevMtime = mTime;
            }
            long took = System.nanoTime() - start;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
