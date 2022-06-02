/*
  Copyright 2004-2012, Martian Software, Inc.
  Copyright 2017-Present Facebook, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package com.facebook.nailgun;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Provides NGSession pooling functionality. One parameter, {@code threadCount}, governs its behavior by
 * setting the maximum number of idle NGSession threads it will allow. It creates a pool of size
 * {@code threadCount + 2}, because the Scala plugin sends metrics commands which we would like to
 * complete as soon as possible, without waiting for some compilation unit to end. The IntelliJ IDEA JPS process
 * will limit the number of requests to {@code threadCount} so that we have some threads leftover for metrics.
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
class NGSessionPool {

  final NGSession[] sessions;

  final ArrayBlockingQueue<NGSession> idleSessions;

  /** reference to server we're working for */
  final NGServer server;

  /** have we been shut down? */
  AtomicBoolean done = new AtomicBoolean(false);

  /**
   * Creates a new NGSessionPool operating for the specified server, with the specified number of
   * threads
   *
   * @param server the server to work for
   * @param threadCount the number of threads in the pool
   */
  NGSessionPool(NGServer server, int threadCount) {
    this(server, threadCount, null);
  }

  /**
   * Creates a new NGSessionPool operating for the specified server, with the specified number of
   * threads
   *
   * @param server the server to work for
   * @param threadCount the number of threads in the pool
   * @param instanceCreator the factory method to create new NGSession instances, can be overridden
   *     for testing
   */
  NGSessionPool(NGServer server, int threadCount, Supplier<NGSession> instanceCreator) {
    this.server = server;
    final int numSessions = Math.max(2, threadCount + 2);
    final Supplier<NGSession> creator =
            instanceCreator != null ? instanceCreator : (() -> new NGSession(this, server));
    sessions = new NGSession[numSessions];
    idleSessions = new ArrayBlockingQueue<>(numSessions);
    for (int i = 0; i < numSessions; i++) {
      final NGSession session = creator.get();
      sessions[i] = session;
      idleSessions.offer(session);
    }
    for (NGSession session : sessions) {
      session.start();
    }
  }

  /**
   * Returns an NGSession from the pool, or creates one if necessary
   *
   * @return an NGSession ready to work
   */
  NGSession take() {
    if (done.get()) {
      throw new UnsupportedOperationException("NGSession pool is shutting down");
    }
    try {
      return idleSessions.take();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns an NGSession to the pool.
   *
   * @param session the NGSession to return to the pool
   */
  void give(NGSession session) {
    idleSessions.offer(session);
  }

  /** Shuts down the pool. The function waits for running nails to finish. */
  void shutdown() throws InterruptedException {
    if (!done.compareAndSet(false, true)) {
      return;
    }
    idleSessions.clear();
    for (NGSession session : sessions) {
      session.shutdown();
    }

    // wait for all sessions to complete by either returning from waiting state or finishing their
    // nails
    long start = System.nanoTime();
    for (NGSession session : sessions) {
      long timeout =
          NGConstants.SESSION_TERMINATION_TIMEOUT_MILLIS
              - TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
      if (timeout < 1) {
        // Give all threads a chance to finish or pick up already finished threads
        timeout = 1;
      }
      session.join(timeout);
      if (session.isAlive()) {
        throw new IllegalStateException(
            "NGSession has not completed in "
                + NGConstants.SESSION_TERMINATION_TIMEOUT_MILLIS
                + " ms");
      }
    }
  }
}
