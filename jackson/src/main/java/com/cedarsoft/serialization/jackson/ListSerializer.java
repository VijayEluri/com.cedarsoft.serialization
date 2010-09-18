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

package com.cedarsoft.serialization.jackson;

import com.cedarsoft.Version;
import com.cedarsoft.VersionException;
import com.cedarsoft.VersionRange;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializes a collection into a json array
 */
public class ListSerializer extends AbstractJacksonSerializer<List<? extends Object>> {
  public ListSerializer() {
    super( "http://sun.com/java.lang.string", VersionRange.single( 1, 0, 0 ) );
  }

  @Override
  public void serialize( @NotNull JsonGenerator serializeTo, @NotNull List<? extends Object> object, @NotNull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
    serializeTo.writeStartArray();

    for ( int i = 0; i < object.size(); i++ ) {
      Object current = object.get( i );
      serializeElement( serializeTo, current, i );
    }

    serializeTo.writeEndArray();
  }

  protected void serializeElement( @NotNull JsonGenerator serializeTo, @Nullable Object element, int index ) throws IOException {
    if ( element == null ) {
      serializeTo.writeNull();
    } else if ( element instanceof Integer ) {
      serializeTo.writeNumber( ( Integer ) element );
    } else if ( element instanceof Float ) {
      serializeTo.writeNumber( ( Float ) element );
    } else if ( element instanceof Double ) {
      serializeTo.writeNumber( ( Double ) element );
    } else if ( element instanceof Long ) {
      serializeTo.writeNumber( ( Long ) element );
    } else if ( element instanceof Boolean ) {
      serializeTo.writeBoolean( ( Boolean ) element );
    } else {
      serializeTo.writeString( String.valueOf( element ) );
    }
  }

  @NotNull
  @Override
  public List<? extends Object> deserialize( @NotNull JsonParser deserializeFrom, @NotNull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
    List<Object> deserialized = new ArrayList<Object>();

    int index = 0;
    while ( deserializeFrom.nextToken() != JsonToken.END_ARRAY ) {
      deserialized.add( deserializeElement( deserializeFrom, index ) );
      index++;
    }

    return deserialized;
  }

  @Nullable
  protected Object deserializeElement( @NotNull JsonParser deserializeFrom, int index ) throws IOException {
    //noinspection EnumSwitchStatementWhichMissesCases
    switch ( deserializeFrom.getCurrentToken() ) {
      case VALUE_STRING:
        return deserializeFrom.getText();
      case VALUE_NUMBER_INT:
        return deserializeFrom.getIntValue();
      case VALUE_NUMBER_FLOAT:
        return deserializeFrom.getDoubleValue();
      case VALUE_TRUE:
        return true;
      case VALUE_FALSE:
        return false;
      case VALUE_NULL:
        return null;
    }

    return deserializeFrom.getText();
  }

  @Override
  public boolean isObjectType() {
    return false;
  }
}
