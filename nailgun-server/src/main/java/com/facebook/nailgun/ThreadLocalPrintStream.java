/*
  Copyright 2004-2012, Martian Software, Inc.
  Copyright 2017-Present Facebook, Inc.

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

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The class name is pretty descriptive. This creates a PrintStream much like a FilterOutputStream,
 * but with the wrapped PrintStream being local to the current Thread. By setting System.out to a
 * ThreadLocalPrintStream, different Threads can write to different PrintStreams simply by using
 * System.out. Of course, the init() method must be called by the Thread that wishes to use the
 * wrapped stream.
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
public class ThreadLocalPrintStream extends PrintStream {

  // Airbnb Fork: We've implemented new logic below to avoid losing console output in the following scenario:
  // 1. Nailgun Server starts and has its own stdout PrintStream
  // 2. Nailgun Client #1 connects and runs a QuickTest which first starts a Dropwizard server.
  //    Long-lived threads are spawned from the Dropwizard server and these threads use a InheritableThreadLocal that writes to Client #1's stdout.
  //    The QuickTest case then makes a request and asserts the response and all resulting output goes to Client #1's stdout.
  // 3. Client #1 disconnects
  // 4. Nailgun Client 2 connects runs the same test as Client #1.
  //    The Dropwizard server is already started, so nothing is done
  //    The QuickTest case then makes a request and asserts the response and all resulting output:
  //    a) Would normally have been dropped by nailgun, as the InheritableThreadLocal is referring to Client #1's stdout which is now closed.
  //    b) The Airbnb fork, instead sends the output to clientConnectedPrintStreamRef, which was set when Client #2 connected.
  // 5. Client #2 disconnects
  // 6. A log is emitted by the Dropwizard server
  //    a) Would normally have been dropped by Nailgun, as the InheritableThreadLocal is referring to Client #1's stdout which is the client
  //       that was connected when the Dropwizard server started.
  //    b) The Airbnb fork, instead sends the output to originalPrintStream, which is the original PrintStream used by the Nailgun server.

  /** The PrintStreams for the various threads (created by threads spawned from client connections) */
  private InheritableThreadLocal streams = null;

  /**
   * Airbnb Fork: A ref to the PrintStream associated with the most recently connected client.
   * This is our first fallback when we can't use the InheritableThreadLocal.
   */
  private AtomicReference<PrintStream> clientConnectedPrintStreamRef = null;

  /*
   * The original PrintStream used by the Nailgun server. This is the second fallback
   * This is our second fallback when we can't use the InheritableThreadLocal.
   */
  private final PrintStream originalPrintStream;

  /**
   * Creates a new InheritedThreadLocalPrintStream
   *
   * @param clientConnectedPrintStreamRef the PrintStream that will be used if the current thread has not
   *     called init()
   */
  public ThreadLocalPrintStream(PrintStream originalPrintStream, AtomicReference<PrintStream> clientConnectedPrintStreamRef) {
    super(originalPrintStream);
    streams = new InheritableThreadLocal();
    this.originalPrintStream = originalPrintStream;
    this.clientConnectedPrintStreamRef = clientConnectedPrintStreamRef;
    init(null);
  }

  /**
   * Sets the PrintStream for the current thread
   *
   * @param streamForCurrentThread the PrintStream for the current thread
   */
  public void init(PrintStream streamForCurrentThread) {
    streams.set(streamForCurrentThread);
  }

  /**
   * Returns this thread's PrintStream
   *
   * @return this thread's PrintStream
   */
  PrintStream getPrintStream() {
    PrintStream result = (PrintStream) streams.get();
    if (result == null || result instanceof ThreadLocalPrintStream || isError(result)) {
      PrintStream clientConnectedPrintStream = clientConnectedPrintStreamRef.get();
      if (clientConnectedPrintStream == null || clientConnectedPrintStream.checkError()) {
        return originalPrintStream;
      } else {
        return clientConnectedPrintStream;
      }
    } else {
      return result;
    }
  }

  private boolean isError(PrintStream printStream) {
    printStream.flush(); // We flush first so checkError can detect a closing/closed stream...
    return printStream.checkError();
  }

  //  BEGIN delegated java.io.PrintStream methods

  /** @see java.io.PrintStream#checkError() */
  public boolean checkError() {
    return (getPrintStream().checkError());
  }

  /** @see java.io.PrintStream#close() */
  public void close() {
    getPrintStream().close();
  }

  /** @see java.io.PrintStream#flush() */
  public void flush() {
    getPrintStream().flush();
  }

  /** @see java.io.PrintStream#print(boolean) */
  public void print(boolean b) {
    getPrintStream().print(b);
  }

  /** @see java.io.PrintStream#print(char) */
  public void print(char c) {
    getPrintStream().print(c);
  }

  /** @see java.io.PrintStream#print(char[]) */
  public void print(char[] s) {
    getPrintStream().print(s);
  }

  /** @see java.io.PrintStream#print(double) */
  public void print(double d) {
    getPrintStream().print(d);
  }

  /** @see java.io.PrintStream#print(float) */
  public void print(float f) {
    getPrintStream().print(f);
  }

  /** @see java.io.PrintStream#print(int) */
  public void print(int i) {
    getPrintStream().print(i);
  }

  /** @see java.io.PrintStream#print(long) */
  public void print(long l) {
    getPrintStream().print(l);
  }

  /** @see java.io.PrintStream#print(Object) */
  public void print(Object obj) {
    getPrintStream().print(obj);
  }

  /** @see java.io.PrintStream#print(String) */
  public void print(String s) {
    getPrintStream().print(s);
  }

  /** @see java.io.PrintStream#println() */
  public void println() {
    getPrintStream().println();
  }

  /** @see java.io.PrintStream#println(boolean) */
  public void println(boolean x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(char) */
  public void println(char x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(char[]) */
  public void println(char[] x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(double) */
  public void println(double x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(float) */
  public void println(float x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(int) */
  public void println(int x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(long) */
  public void println(long x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(Object) */
  public void println(Object x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(String) */
  public void println(String x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#write(byte[],int,int) */
  public void write(byte[] buf, int off, int len) {
    getPrintStream().write(buf, off, len);
  }

  /** @see java.io.PrintStream#write(int) */
  public void write(int b) {
    getPrintStream().write(b);
  }

  //  END delegated java.io.PrintStream methods

  //  BEGIN delegated java.io.FilterOutputStream methods

  /** @see java.io.FilterOutputStream#write(byte[]) */
  public void write(byte[] b) throws IOException {
    getPrintStream().write(b);
  }

  //  END delegated java.io.FilterOutputStream methods

  //  Note: Should java.lang.Object methods be delegated? If not, and
  //  someone synchronizes on this stream, processes might be blocked
  //  that shouldn't be.  It would certainly be stupid to delegate
  //  finalize().  Not so clear are hashcode(), equals(), notify(), and
  //  the wait() methods.
}
