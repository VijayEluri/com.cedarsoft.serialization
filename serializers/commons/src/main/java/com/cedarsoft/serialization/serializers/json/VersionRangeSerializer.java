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
import java.io.IOException;

public class VersionRangeSerializer extends AbstractJacksonSerializer<VersionRange> {

  public static final String PROPERTY_MIN = "min";

  public static final String PROPERTY_MAX = "max";

  public static final String PROPERTY_INCLUDELOWER = "includeLower";

  public static final String PROPERTY_INCLUDEUPPER = "includeUpper";

  public VersionRangeSerializer() {
    super( "version-range", VersionRange.from( 1, 0, 0 ).to( 1, 0, 0 ) );
  }

  @Override
  public void serialize( @Nonnull JsonGenerator serializeTo, @Nonnull VersionRange object, @Nonnull Version formatVersion )
    throws IOException, JsonProcessingException {
    verifyVersionReadable( formatVersion );

    serializeTo.writeStringField( PROPERTY_MIN, object.getMin().format() );
    serializeTo.writeStringField( PROPERTY_MAX, object.getMax().format() );

    //includeLower
    serializeTo.writeBooleanField( PROPERTY_INCLUDELOWER, object.isIncludeLower() );
    //includeUpper
    serializeTo.writeBooleanField( PROPERTY_INCLUDEUPPER, object.isIncludeUpper() );
  }

  @Nonnull
  @Override
  public VersionRange deserialize( @Nonnull JsonParser deserializeFrom, @Nonnull Version formatVersion )
    throws VersionException, IOException, JsonProcessingException {
    //min
    nextFieldValue( deserializeFrom, PROPERTY_MIN );
    Version min = Version.parse( deserializeFrom.getText() );
    //max
    nextFieldValue( deserializeFrom, PROPERTY_MAX );
    Version max = Version.parse( deserializeFrom.getText() );
    //includeLower
    nextFieldValue( deserializeFrom, PROPERTY_INCLUDELOWER );
    boolean includeLower = deserializeFrom.getBooleanValue();
    //includeUpper
    nextFieldValue( deserializeFrom, PROPERTY_INCLUDEUPPER );
    boolean includeUpper = deserializeFrom.getBooleanValue();
    //Finally closing element
    closeObject( deserializeFrom );
    //Constructing the deserialized object
    return new VersionRange( min, max, includeLower, includeUpper );
  }

}