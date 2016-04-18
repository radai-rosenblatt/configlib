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

import java.util.concurrent.CountDownLatch;

/**
 * Created by Radai Rosenblatt
 */
public abstract class AbstractPollingStore implements Store {
    private volatile boolean on = false;
    private volatile Thread pollThread;
    private volatile StorePollRunnable runnable;

    @Override
    public synchronized void start() {
        if (on || pollThread != null) {
            throw new IllegalStateException();
        }
        on = true;
        startPollThread();
    }

    @Override
    public synchronized void stop() {
        if (!on || pollThread == null) {
            throw new IllegalStateException();
        }
        on = false;
        stopPollThread();
    }

    protected boolean isOn() {
        return on;
    }

    protected void startPollThread() {
        if (pollThread != null) {
            throw new IllegalStateException();
        }
        runnable = createRunnable();
        pollThread = createThread(runnable);
        pollThread.start();
        runnable.waitUntilWatching();
    }

    protected void stopPollThread() {
        if (pollThread == null) {
            throw new IllegalStateException();
        }
        try {
            runnable.die();
            pollThread.interrupt();
            pollThread.join();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            pollThread = null;
        }
    }

    protected Thread createThread(StorePollRunnable runnable) {
        Thread t = new Thread(runnable, this + " poller");
        t.setDaemon(true);
        return t;
    }

    protected abstract StorePollRunnable createRunnable();

    protected abstract class StorePollRunnable implements Runnable {
        private final CountDownLatch startLatch = new CountDownLatch(1);
        private volatile boolean die = false;

        protected void waitUntilWatching() {
            try {
                startLatch.await();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        protected boolean shouldDie() {
            return die;
        }

        protected void die() {
            die = true;
        }

        protected void markWatching() {
            startLatch.countDown();
        }
    }
}
