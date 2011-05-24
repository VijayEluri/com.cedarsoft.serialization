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
import com.cedarsoft.file.BaseName;
import com.cedarsoft.file.Extension;
import com.cedarsoft.file.FileName;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;

import javax.annotation.Nonnull;

import javax.inject.Inject;
import java.io.IOException;

public class FileNameSerializer extends AbstractJacksonSerializer<FileName> {

  public static final String PROPERTY_BASENAME = "baseName";

  public static final String PROPERTY_EXTENSION = "extension";

  @Inject
  public FileNameSerializer( @Nonnull BaseNameSerializer baseNameSerializer, @Nonnull ExtensionSerializer extensionSerializer ) {
    super( "file-name", VersionRange.from( 1, 0, 0 ).to( 1, 0, 0 ) );
    add( baseNameSerializer ).responsibleFor( BaseName.class ).map( 1, 0, 0 ).toDelegateVersion( 1, 0, 0 );
    add( extensionSerializer ).responsibleFor( Extension.class ).map( 1, 0, 0 ).toDelegateVersion( 1, 0, 0 );
    assert getDelegatesMappings().verify();
  }

  @Override
  public void serialize( @Nonnull JsonGenerator serializeTo, @Nonnull FileName object, @Nonnull Version formatVersion ) throws IOException, JsonProcessingException {
    verifyVersionReadable( formatVersion );
    //baseName
    serialize( object.getBaseName(), BaseName.class, PROPERTY_BASENAME, serializeTo, formatVersion );
    //extension
    serialize( object.getExtension(), Extension.class, PROPERTY_EXTENSION, serializeTo, formatVersion );
  }

  @Nonnull
  @Override
  public FileName deserialize( @Nonnull JsonParser deserializeFrom, @Nonnull Version formatVersion ) throws VersionException, IOException, JsonProcessingException {
    //baseName
    BaseName baseName = deserialize( BaseName.class, PROPERTY_BASENAME, formatVersion, deserializeFrom );
    //extension
    Extension extension = deserialize( Extension.class, PROPERTY_EXTENSION, formatVersion, deserializeFrom );
    //Finally closing element
    closeObject( deserializeFrom );
    //Constructing the deserialized object
    return new FileName( baseName, extension );
  }
}
