/**
 * Copyright (C) cedarsoft GmbH.
 *
 * Licensed under the GNU General Public License version 3 (the "License")
 * with Classpath Exception; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *         http://www.cedarsoft.org/gpl3ce
 *         (GPL 3 with Classpath Exception)
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation. cedarsoft GmbH designates this
 * particular file as subject to the "Classpath" exception as provided
 * by cedarsoft GmbH in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
 * or visit www.cedarsoft.com if you need additional information or
 * have any questions.
 */

package com.cedarsoft.serialization;

import com.cedarsoft.StillContainedException;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of SerializedObjectsAccess that is based on simple files.
 */
public class DirBasedSerializedObjectsAccess implements SerializedObjectsAccess {
  @NotNull
  @NonNls
  private final File baseDir;

  @NotNull
  @NonNls
  private final String metaFileName;

  public DirBasedSerializedObjectsAccess( @NotNull File baseDir, @NotNull String metaFileName ) {
    assert baseDir.exists();
    assert baseDir.isDirectory();

    this.metaFileName = metaFileName;
    this.baseDir = baseDir;
  }

  @Override
  @NotNull
  public Set<? extends String> getStoredIds() throws FileNotFoundException {
    assert baseDir.exists();
    File[] dirs = baseDir.listFiles( ( FileFilter ) DirectoryFileFilter.DIRECTORY );
    if ( dirs == null ) {
      throw new FileNotFoundException( "Could not list dirs in " + baseDir.getAbsolutePath() );
    }

    Set<String> ids = new HashSet<String>();
    for ( File dir : dirs ) {
      ids.add( dir.getName() );
    }

    return ids;
  }

  @Override
  @NotNull
  public OutputStream openOut( @NotNull @NonNls String id ) throws FileNotFoundException {
    File dir = getDirectory( id );
    if ( dir.exists() ) {
      dir.mkdir();
    }

    File file = new File( dir, metaFileName );
    if ( file.exists() ) {
      throw new StillContainedException( id );
    }
    return new BufferedOutputStream( new FileOutputStream( file ) );
  }

  @Override
  @NotNull
  public InputStream getInputStream( @NotNull @NonNls String id ) throws FileNotFoundException {
    return new BufferedInputStream( new FileInputStream( getMetaFile( id ) ) );
  }

  @NotNull
  public File getMetaFile( @NotNull @NonNls String id ) {
    return new File( getDirectory( id ), metaFileName );
  }

  @NotNull
  public File getDirectory( @NotNull @NonNls String id ) {
    return new File( baseDir, id );
  }

  @NotNull
  public File getBaseDir() {
    return baseDir;
  }
}