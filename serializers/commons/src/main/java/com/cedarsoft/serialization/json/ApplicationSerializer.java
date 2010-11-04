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

package com.cedarsoft.serialization.json;

import com.cedarsoft.Version;
import com.cedarsoft.VersionException;
import com.cedarsoft.VersionRange;
import com.cedarsoft.app.Application;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ApplicationSerializer extends AbstractJacksonSerializer<Application> {
  @NonNls
  public static final String PROPERTY_NAME = "name";
  @NonNls
  public static final String PROPERTY_VERSION = "version";

  public ApplicationSerializer( @NotNull VersionSerializer versionSerializer ) {
    super( "application", VersionRange.from( 1, 0, 0 ).to( 1, 0, 0 ) );
    add( versionSerializer ).responsibleFor( Version.class ).map( 1, 0, 0 ).toDelegateVersion( 1, 0, 0 );
    assert getDelegatesMappings().verify();
  }

  @Override
  public void serialize( @NotNull JsonGenerator serializeTo, @NotNull Application object, @NotNull Version formatVersion ) throws IOException, JsonProcessingException {
    verifyVersionReadable( formatVersion );
    //name
    serializeTo.writeStringField( PROPERTY_NAME, object.getName() );
    //version
    serialize( object.getVersion(), Version.class, PROPERTY_VERSION, serializeTo, formatVersion );
  }

  @NotNull
  @Override
  public Application deserialize( @NotNull JsonParser deserializeFrom, @NotNull Version formatVersion ) throws VersionException, IOException, JsonProcessingException {
    //name
    nextFieldValue( deserializeFrom, PROPERTY_NAME );
    String name = deserializeFrom.getText();
    //version
    Version version = deserialize( Version.class, PROPERTY_VERSION, formatVersion, deserializeFrom );
    //Finally closing element
    closeObject( deserializeFrom );
    //Constructing the deserialized object
    return new Application( name, version );
  }

}