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

import com.cedarsoft.Version;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.junit.experimental.theories.*;
import org.junit.runner.*;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Abstract test class for testing the support for multiple format versions
 * Attention: it is necessary to define at least one DataPoint:
 * <pre>&#064;DataPoint<br/>public static final Entry&lt;BaseName&gt; entry1 = create(<br/> Version.valueOf( 1, 0, 0 ),<br/> serializedAsByteArray; );</pre>
 * @param <T> the type that is deserialized
 */
@RunWith( Theories.class )
public abstract class AbstractVersionTest2<T> {
  /**
   * This method checks old serialized objects
   *
   * @throws IOException
   * @throws SAXException
   */
  @Theory
  public void testVersion( @NotNull Entry entry ) throws Exception {
    Serializer<T> serializer = getSerializer();

    Version version = entry.getVersion();
    byte[] serialized = entry.getSerialized( serializer );

    T deserialized = serializer.deserialize( new ByteArrayInputStream( serialized ) );
    verifyDeserialized( deserialized, version );
  }

  /**
   * Returns the serializer
   *
   * @return the serializer
   */
  @NotNull
  protected abstract Serializer<T> getSerializer() throws Exception;

  /**
   * Verifies the deserialized object.
   *
   * @param deserialized the deserialized object
   * @param version      the version
   */
  protected abstract void verifyDeserialized( @NotNull T deserialized, @NotNull Version version ) throws Exception;

  @NotNull
  protected static Entry create( @NotNull Version version, @NotNull byte[] serialized ) {
    return new DefaultEntry( version, serialized );
  }


  public interface Entry {
    @NotNull
    byte[] getSerialized( @NotNull Serializer<?> serializer ) throws Exception;

    @NotNull
    Version getVersion();
  }

  public static class DefaultEntry implements Entry {
    @NotNull
    final byte[] serialized;
    @NotNull
    @NonNls
    private final Version version;

    public DefaultEntry( @NotNull Version version, @NotNull byte[] serialized ) {
      this.version = version;
      this.serialized = serialized;
    }

    @Override
    @NotNull
    public byte[] getSerialized( @NotNull Serializer<?> serializer ) {
      return serialized;
    }

    @Override
    @NotNull
    public Version getVersion() {
      return version;
    }
  }
}