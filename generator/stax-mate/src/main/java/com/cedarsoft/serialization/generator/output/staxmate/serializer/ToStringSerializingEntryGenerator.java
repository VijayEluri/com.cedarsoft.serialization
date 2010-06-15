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

package com.cedarsoft.serialization.generator.output.staxmate.serializer;

import com.cedarsoft.serialization.generator.decision.XmlDecisionCallback;
import com.cedarsoft.serialization.generator.model.FieldDeclarationInfo;
import com.cedarsoft.serialization.generator.model.FieldInfo;
import com.cedarsoft.serialization.generator.output.CodeGenerator;
import com.cedarsoft.serialization.generator.output.serializer.SerializeToGenerator;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class ToStringSerializingEntryGenerator implements SerializingEntryGenerator {
  @NotNull
  private final CodeGenerator<XmlDecisionCallback> codeGenerator;

  public ToStringSerializingEntryGenerator( @NotNull CodeGenerator<XmlDecisionCallback> codeGenerator ) {
    this.codeGenerator = codeGenerator;
    asAttribute = new AsAttributeGenerator( codeGenerator );
    asElement = new AsElementGenerator( codeGenerator );
  }

  @Override
  public void appendSerializing( @NotNull JDefinedClass serializerClass, @NotNull JMethod method, @NotNull JVar serializeTo, @NotNull JVar object, @NotNull FieldDeclarationInfo fieldInfo ) {
    method.body().directStatement( "//" + fieldInfo.getSimpleName() );

    JExpression objectAsString = codeGenerator.getParseExpressionFactory().createToStringExpression( object.invoke( fieldInfo.getGetterDeclaration().getSimpleName() ), fieldInfo );

    SerializeToGenerator serializeToHandler = getStrategy( fieldInfo );
    method.body().add( serializeToHandler.createAddToSerializeToExpression(serializerClass, serializeTo, objectAsString, fieldInfo ) );
  }

  @NotNull
  @Override
  public JVar appendDeserializing( @NotNull JDefinedClass serializerClass, @NotNull JMethod method, @NotNull JVar deserializeFrom, @NotNull JVar formatVersion, @NotNull FieldDeclarationInfo fieldInfo ) {
    method.body().directStatement( "//" + fieldInfo.getSimpleName() );
    SerializeToGenerator serializeToHandler = getStrategy( fieldInfo );

    JExpression readToStringExpression = serializeToHandler.createReadFromDeserializeFromExpression(serializerClass, deserializeFrom, fieldInfo );

    JClass fieldType = codeGenerator.getModel().ref( fieldInfo.getType().toString() );
    return method.body().decl( fieldType, fieldInfo.getSimpleName(), codeGenerator.getParseExpressionFactory().createParseExpression( readToStringExpression, fieldInfo ) );
  }

  @NotNull
  private SerializeToGenerator getStrategy( @NotNull FieldInfo fieldInfo ) {
    XmlDecisionCallback.Target target = codeGenerator.getDecisionCallback().getSerializationTarget( fieldInfo );
    switch ( target ) {
      case ELEMENT:
        return asElement;
      case ATTRIBUTE:
        return asAttribute;
    }

    throw new IllegalStateException( "Should not reach! " + fieldInfo );
  }

  @NotNull
  private final SerializeToGenerator asElement;
  @NotNull
  private final SerializeToGenerator asAttribute;

}