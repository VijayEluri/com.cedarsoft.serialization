/**
 * Copyright (C) 2010 cedarsoft GmbH.
 *
 * Licensed under the GNU General Public License version 3 (the "License")
 * with Classpath Exception; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *         http://www.cedarsoft.org/gpl3ce.txt
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

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.*;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.testng.Assert.*;

/**
 * Abstract class for serializer tests
 *
 * @param <T> the type of the serialized object
 */
public abstract class AbstractSerializerTest<T> {
  /**
   * Default test method that checks the serialization and deserialization using the latest format
   *
   * @throws IOException
   * @throws SAXException
   */
  @Test
  public void testSerializer() throws Exception {
    Serializer<T> serializer = getSerializer();

    T objectToSerialize = createObjectToSerialize();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.serialize( objectToSerialize, out );

    byte[] serialized = out.toByteArray();
    verifySerialized( serialized );

    T deserialized = serializer.deserialize( new ByteArrayInputStream( serialized ) );

    verifyDeserialized( deserialized );
  }

  /**
   * Returns the serializer
   *
   * @return the serializer
   */
  @NotNull
  protected abstract Serializer<T> getSerializer() throws Exception;

  /**
   * Verifies the serialized object
   *
   * @param serialized the serialized object
   * @throws SAXException
   * @throws IOException
   */
  protected abstract void verifySerialized( @NotNull byte[] serialized ) throws Exception;

  /**
   * Creates the object to serialize
   *
   * @return the object to serialize
   */
  @NotNull
  protected abstract T createObjectToSerialize() throws Exception;

  /**
   * Verifies the deserialized object.
   * The default implementation simply calls equals
   *
   * @param deserialized the deserialized object
   */
  protected void verifyDeserialized( @NotNull T deserialized ) throws Exception  {
    assertEquals( deserialized, createObjectToSerialize() );
  }
}
