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

import com.google.common.io.ByteStreams;
import net.radai.confusion.core.spi.Poller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Radai Rosenblatt
 */
public class TracingListener implements Poller.Listener{
    private final List<TraceEntry> log = new ArrayList<>();

    @Override
    public void sourceChanged(InputStream newContents) throws IOException{
        long nanoClock = System.nanoTime();
        long clock = System.currentTimeMillis();
        byte[] data = ByteStreams.toByteArray(newContents);
        log.add(new TraceEntry(clock, nanoClock, data));
    }

    public int getNumEvents() {
        return log.size();
    }

    public byte[] getEvent(int i) {
        return log.get(i).data;
    }

    public byte[] getLatestEvent() {
        return log.get(log.size()-1).data;
    }

    public long getLatestNanoTime() {
        return log.get(log.size()-1).nanoClock;
    }

    public static class TraceEntry {
        long systemClock;
        long nanoClock;
        byte[] data;

        public TraceEntry(long systemClock, long nanoClock, byte[] data) {
            this.systemClock = systemClock;
            this.nanoClock = nanoClock;
            this.data = data;
        }
    }
}
