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

package net.radai.confusion.core.spi.store;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Radai Rosenblatt
 */
public class TracingTextStoreListener implements TextStoreListener {
    private final List<Entry> log = new ArrayList<>();

    @Override
    public void sourceChanged(String newContents) {
        long nanoClock = System.nanoTime();
        long clock = System.currentTimeMillis();
        log.add(new Entry(clock, nanoClock, newContents));
    }

    public int getNumEvents() {
        return log.size();
    }

    public String getEvent(int i) {
        return log.get(i).data;
    }

    public String getLatestEvent() {
        return log.get(log.size()-1).data;
    }

    public long getLatestNanoTime() {
        return log.get(log.size()-1).nanoClock;
    }

    public static class Entry {
        long systemClock;
        long nanoClock;
        String data;

        public Entry(long systemClock, long nanoClock, String data) {
            this.systemClock = systemClock;
            this.nanoClock = nanoClock;
            this.data = data;
        }
    }
}
