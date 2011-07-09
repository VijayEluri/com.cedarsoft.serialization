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

package com.cedarsoft.serialization.serializers.json;

import com.cedarsoft.Version;
import com.cedarsoft.VersionException;
import com.cedarsoft.VersionRange;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.io.IOException;

/**
 *
 */
public class ColorSerializer extends AbstractJacksonSerializer<Color> {
  @Nonnull

  public static final String PROPERTY_RED = "red";
  @Nonnull

  public static final String PROPERTY_GREEN = "green";
  @Nonnull

  public static final String PROPERTY_BLUE = "blue";

  public ColorSerializer() {
    super( "color", VersionRange.from( 1, 0, 0 ).to( 1, 0, 0 ) );
  }

  @Override
  public void serialize( @Nonnull JsonGenerator serializeTo, @Nonnull Color object, @Nonnull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
    serializeTo.writeNumberField( PROPERTY_RED, object.getRed() );
    serializeTo.writeNumberField( PROPERTY_GREEN, object.getGreen() );
    serializeTo.writeNumberField( PROPERTY_BLUE, object.getBlue() );
  }

  @Nonnull
  @Override
  public Color deserialize( @Nonnull JsonParser deserializeFrom, @Nonnull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
    assert isVersionReadable( formatVersion );
    //red
    nextFieldValue( deserializeFrom, PROPERTY_RED );
    int red = deserializeFrom.getIntValue();

    nextFieldValue( deserializeFrom, PROPERTY_GREEN );
    int green = deserializeFrom.getIntValue();

    nextFieldValue( deserializeFrom, PROPERTY_BLUE );
    int blue = deserializeFrom.getIntValue();

    closeObject( deserializeFrom );

    return new Color( red, green, blue );
  }
}