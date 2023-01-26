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

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

/**
 * Security manager which does nothing other than trap checkExit, or delegate all non-deprecated
 * methods to a base manager.
 *
 * @author Pete Kirkham
 */
public class NGSecurityManager extends SecurityManager {
  final SecurityManager base;

  /**
   * Construct an NGSecurityManager with the given base.
   *
   * @param base the base security manager, or null for no base.
   */
  public NGSecurityManager(SecurityManager base) {
    this.base = base;
  }

  @Override
  public void checkExit(int status) {
    if (base != null) {
      base.checkExit(status);
    }

    throw new NGExitException(status);
  }

  @Override
  public void checkPermission(Permission perm) {
    if (base != null) {
      base.checkPermission(perm);
    }
  }

  @Override
  public void checkPermission(Permission perm, Object context) {
    if (base != null) {
      base.checkPermission(perm, context);
    }
  }

  /** Avoid constructing a FilePermission object in checkRead if base manager is null. */
  @Override
  public void checkRead(String file) {
    if (base != null) {
      base.checkRead(file);
    }
  }

  @Override
  public void checkCreateClassLoader() {
    if (base != null) {
      base.checkCreateClassLoader();
    }
  }

  @Override
  public void checkAccess(Thread t) {
    if (base != null) {
      base.checkAccess(t);
    }
  }

  @Override
  public void checkAccess(ThreadGroup g) {
    if (base != null) {
      base.checkAccess(g);
    }
  }

  @Override
  public void checkExec(String cmd) {
    if (base != null) {
      base.checkExec(cmd);
    }
  }

  @Override
  public void checkLink(String lib) {
    if (base != null) {
      base.checkLink(lib);
    }
  }

  @Override
  public void checkRead(FileDescriptor fd) {
    if (base != null) {
      base.checkRead(fd);
    }
  }

  @Override
  public void checkRead(String file, Object context) {
    if (base != null) {
      base.checkRead(file, context);
    }
  }

  @Override
  public void checkWrite(FileDescriptor fd) {
    if (base != null) {
      base.checkWrite(fd);
    }
  }

  @Override
  public void checkWrite(String file) {
    if (base != null) {
      base.checkWrite(file);
    }
  }

  @Override
  public void checkDelete(String file) {
    if (base != null) {
      base.checkDelete(file);
    }
  }

  @Override
  public void checkConnect(String host, int port) {
    if (base != null) {
      base.checkConnect(host, port);
    }
  }

  @Override
  public void checkConnect(String host, int port, Object context) {
    if (base != null) {
      base.checkConnect(host, port, context);
    }
  }

  @Override
  public void checkListen(int port) {
    if (base != null) {
      base.checkListen(port);
    }
  }

  @Override
  public void checkAccept(String host, int port) {
    if (base != null) {
      base.checkAccept(host, port);
    }
  }

  @Override
  public void checkMulticast(InetAddress maddr) {
    if (base != null) {
      base.checkMulticast(maddr);
    }
  }

  @Override
  public void checkPropertiesAccess() {
    if (base != null) {
      base.checkPropertiesAccess();
    }
  }

  @Override
  public void checkPropertyAccess(String key) {
    if (base != null) {
      base.checkPropertyAccess(key);
    }
  }

  @Override
  public void checkPrintJobAccess() {
    if (base != null) {
      base.checkPrintJobAccess();
    }
  }

  @Override
  public void checkPackageAccess(String pkg) {
    if (base != null) {
      base.checkPackageAccess(pkg);
    }
  }

  @Override
  public void checkPackageDefinition(String pkg) {
    if (base != null) {
      base.checkPackageDefinition(pkg);
    }
  }

  @Override
  public void checkSetFactory() {
    if (base != null) {
      base.checkSetFactory();
    }
  }

  @Override
  public void checkSecurityAccess(String target) {
    if (base != null) {
      base.checkSecurityAccess(target);
    }
  }
}
