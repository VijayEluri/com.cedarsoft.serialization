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

package com.cedarsoft.serialization.generator.model;

import com.cedarsoft.serialization.generator.output.serializer.ParseExpressionFactory;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.Types;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 *
 */
public class DefaultFieldTypeInformation implements FieldTypeInformation {
  @NotNull
  private final TypeMirror typeMirror;

  @NotNull
  private final Types types;

  public DefaultFieldTypeInformation( @NotNull TypeMirror typeMirror, @NotNull Types types ) {
    this.typeMirror = typeMirror;
    this.types = types;
  }

  @NotNull
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  @NotNull
  public Types getTypes() {
    return types;
  }

  @NotNull
  @Override
  public TypeMirror getType() {
    return typeMirror;
  }

  @Override
  public boolean isType( @NotNull Class<?> type ) {
    return DomainObjectDescriptor.isType( getType(), type );
  }

  @Override
  public boolean isCollectionType() {
    try {
      getCollectionParam();
      return true;
    } catch ( IllegalStateException ignore ) {
      return false;
    }
  }

  @Override
  @NotNull
  public TypeMirror getCollectionParam() {
    TypeMirror type = getType();
    if ( !( type instanceof DeclaredType ) ) {
      throw new IllegalStateException( "Invalid type: " + type );
    }

    TypeDeclaration declaredType = ( ( DeclaredType ) type ).getDeclaration();

    if ( declaredType.getQualifiedName().equals( Collection.class.getName() ) ) {
      return getFirstTypeParam( ( DeclaredType ) type );
    }

    for ( InterfaceType interfaceType : declaredType.getSuperinterfaces() ) {
      if ( interfaceType.getDeclaration().getQualifiedName().equals( Collection.class.getName() ) ) {
        return getFirstTypeParam( ( DeclaredType ) type );
      }
    }

    throw new IllegalStateException( "Invalid type: " + type );
  }

  @NotNull
  private static TypeMirror getFirstTypeParam( @NotNull DeclaredType type ) {
    Collection<TypeMirror> typeArguments = type.getActualTypeArguments();
    if ( typeArguments.size() != 1 ) {
      throw new IllegalStateException( "Invalid type arguments: " + typeArguments );
    }

    return typeArguments.iterator().next();
  }
}
