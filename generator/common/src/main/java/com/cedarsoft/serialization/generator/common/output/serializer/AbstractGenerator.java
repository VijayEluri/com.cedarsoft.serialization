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

package com.cedarsoft.serialization.generator.common.output.serializer;

import com.cedarsoft.Version;
import com.cedarsoft.VersionException;
import com.cedarsoft.codegen.CodeGenerator;
import com.cedarsoft.codegen.DecisionCallback;
import com.cedarsoft.codegen.Decorator;
import com.cedarsoft.codegen.NamingSupport;
import com.cedarsoft.codegen.model.DomainObjectDescriptor;
import com.cedarsoft.codegen.model.FieldInitializedInConstructorInfo;
import com.cedarsoft.codegen.model.FieldInitializedInSetterInfo;
import com.cedarsoft.codegen.model.FieldWithInitializationInfo;
import com.cedarsoft.serialization.generator.common.output.GeneratorBase;
import com.google.common.collect.Maps;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Generator base class for serializer generators
 *
 * @param <T> the type of the decision callback
 */
public abstract class AbstractGenerator<T extends DecisionCallback> extends GeneratorBase<T> {
  /**
   * The suffix used for generated serializers
   */

  @Nonnull
  public static final String SERIALIZER_CLASS_NAME_SUFFIX = "Serializer";
  /**
   * The name of the serialize method
   */

  public static final String METHOD_NAME_SERIALIZE = "serialize";
  /**
   * The name of the deserialize method
   */

  public static final String METHOD_NAME_DESERIALIZE = "deserialize";

  public static final String METHOD_NAME_DESERIALIZE_FROM = "deserializeFrom";

  public static final String PARAM_NAME_FORMAT_VERSION = "formatVersion";

  public static final String PARAM_NAME_SERIALIZE_TO = "serializeTo";

  public static final String VAR_NAME_OBJECT = "object";

  protected AbstractGenerator( @Nonnull CodeGenerator codeGenerator ) {
    super( codeGenerator );
  }

  /**
   * Returns the class the serializer extends (including type information!)
   *
   * @param domainType the domain type
   * @return the class the serializer extends
   */
  @Nonnull
  protected abstract JClass createSerializerExtendsExpression( @Nonnull JClass domainType );

  /**
   * Creates the class name for the serializer
   *
   * @param domainClassName the class name of the domain object that is serialized
   * @return the created class name for the serializer
   */
  @Nonnull

  public static String createSerializerClassName( @Nonnull String domainClassName ) {
    return domainClassName + SERIALIZER_CLASS_NAME_SUFFIX;
  }

  /**
   * Generates the serializer
   *
   * @param domainObjectDescriptor the domain object descriptor
   * @return the defined serializer class
   *
   * @throws JClassAlreadyExistsException
   */
  @Nonnull
  public JDefinedClass generateSerializer( @Nonnull DomainObjectDescriptor domainObjectDescriptor ) throws JClassAlreadyExistsException {
    JClass domainType = codeGenerator.ref( domainObjectDescriptor.getQualifiedName() );

    //the class
    JDefinedClass serializerClass = codeModel._class( createSerializerClassName( domainType.fullName() ) )._extends( createSerializerExtendsExpression( domainType ) );

    //the constructor
    JMethod constructor = createConstructor( serializerClass, domainObjectDescriptor );

    JMethod serializeMethod = createSerializeMethodStub( domainType, serializerClass );
    JMethod deserializeMethod = createDeserializeMethodStub( domainType, serializerClass );

    //Add the serialize stuff
    Map<FieldWithInitializationInfo, JVar> fieldToVar = fillDeSerializationMethods( domainObjectDescriptor, serializerClass, serializeMethod, deserializeMethod );

    //Now construct the deserialized object
    constructDeserializedObject( domainObjectDescriptor, deserializeMethod, fieldToVar );

    finishConstructor( serializerClass, constructor );

    return serializerClass;
  }

  protected void finishConstructor( @Nonnull JDefinedClass serializerClass, @Nonnull JMethod constructor ) {
    if ( constructor.listParams().length > 0 ) {
      constructor.body().directStatement( "assert getDelegatesMappings().verify();" );
    }
  }

  protected void constructDeserializedObject( @Nonnull DomainObjectDescriptor domainObjectDescriptor, @Nonnull JMethod deserializeMethod, @Nonnull Map<FieldWithInitializationInfo, JVar> fieldToVar ) {
    deserializeMethod.body().directStatement( "//Constructing the deserialized object" );

    //Now create the constructor for the deserializeMethod
    JClass domainType = codeGenerator.ref( domainObjectDescriptor.getQualifiedName() );
    JInvocation domainTypeInit = JExpr._new( domainType );

    //Add the arguments for the fields
    List<? extends FieldInitializedInConstructorInfo> fieldsToSerialize = domainObjectDescriptor.getFieldsInitializedInConstructor();
    for ( FieldInitializedInConstructorInfo fieldInfo : fieldsToSerialize ) {
      domainTypeInit.arg( fieldToVar.get( fieldInfo ) );
    }

    //Add the object type
    JVar domainObjectVar = deserializeMethod.body().decl( domainType, VAR_NAME_OBJECT, domainTypeInit );

    //Now call the setters
    for ( FieldInitializedInSetterInfo fieldInfo : domainObjectDescriptor.getFieldsInitializedInSetter() ) {
      deserializeMethod.body().add( domainObjectVar.invoke( fieldInfo.getSetterDeclaration().getSimpleName() ).arg( fieldToVar.get( fieldInfo ) ) );
    }

    deserializeMethod.body()._return( domainObjectVar );
  }

  /**
   * Creates the constructor for the given serializer class (if necessary)
   *
   * @param serializerClass        the serialize class
   * @param domainObjectDescriptor the domain object descriptor
   * @return the created constructor
   */
  @Nonnull
  protected abstract JMethod createConstructor( @Nonnull JDefinedClass serializerClass, @Nonnull DomainObjectDescriptor domainObjectDescriptor );

  @Nonnull
  protected JMethod createSerializeMethodStub( @Nonnull JType domainType, @Nonnull JDefinedClass serializerClass ) {
    JMethod serializeMethod = serializerClass.method( JMod.PUBLIC, Void.TYPE, METHOD_NAME_SERIALIZE );
    serializeMethod.annotate( Override.class );
    serializeMethod.param( getSerializeToType(), PARAM_NAME_SERIALIZE_TO );
    serializeMethod.param( domainType, VAR_NAME_OBJECT );
    JVar formatVersion = serializeMethod.param( codeGenerator.ref( Version.class ), PARAM_NAME_FORMAT_VERSION );
    serializeMethod._throws( IOException.class )._throws( ( Class ) getExceptionType() );

    serializeMethod.body().invoke( "verifyVersionWritable" ).arg( formatVersion );

    for ( Decorator decorator : codeGenerator.getDecorators() ) {
      if ( decorator instanceof GeneratorDecorator ) {
        ( ( GeneratorDecorator ) decorator ).decorateSerializeMethod( codeGenerator, domainType, serializerClass, serializeMethod );
      }
    }

    return serializeMethod;
  }

  @Nonnull
  protected JMethod createDeserializeMethodStub( @Nonnull JType domainType, @Nonnull JDefinedClass serializerClass ) {
    JMethod deserializeMethod = serializerClass.method( JMod.PUBLIC, domainType, METHOD_NAME_DESERIALIZE );
    deserializeMethod.param( getSerializeFromType(), METHOD_NAME_DESERIALIZE_FROM );
    JVar formatVersion = deserializeMethod.param( codeGenerator.ref( Version.class ), PARAM_NAME_FORMAT_VERSION );
    deserializeMethod.annotate( Override.class );
    deserializeMethod._throws( IOException.class )._throws( VersionException.class )._throws( ( Class ) getExceptionType() );

    deserializeMethod.body().invoke( "verifyVersionReadable" ).arg( formatVersion );

    for ( Decorator decorator : codeGenerator.getDecorators() ) {
      if ( decorator instanceof GeneratorDecorator ) {
        ( ( GeneratorDecorator ) decorator ).decorateDeserializeMethod( codeGenerator, domainType, serializerClass, deserializeMethod );
      }
    }

    return deserializeMethod;
  }

  @Nonnull
  protected Map<FieldWithInitializationInfo, JVar> fillDeSerializationMethods( @Nonnull DomainObjectDescriptor domainObjectDescriptor, @Nonnull JDefinedClass serializerClass, @Nonnull JMethod serializeMethod, @Nonnull JMethod deserializeMethod ) {
    Map<FieldWithInitializationInfo, JVar> fieldToVar = Maps.newHashMap();

    //Extract the parameters for the serialize method
    JVar serializeTo = serializeMethod.listParams()[0];
    JVar object = serializeMethod.listParams()[1];
    JVar serializeFormatVersion = serializeMethod.listParams()[2];

    //Extract the parameters for the deserialize method
    JVar deserializeFrom = deserializeMethod.listParams()[0];
    JVar deserializeFormatVersion = deserializeMethod.listParams()[1];

    @Nullable JVar wrapper = createDeserializeWrapper( deserializeMethod, deserializeFrom );

    //Generate the serialization and deserialization for every field. We use the ordering of the fields used within the class
    for ( FieldWithInitializationInfo fieldInfo : domainObjectDescriptor.getFieldInfos() ) {
      appendSerializeStatement( serializerClass, serializeMethod, serializeTo, object, serializeFormatVersion, fieldInfo );

      fieldToVar.put( fieldInfo, appendDeserializeStatement( serializerClass, deserializeMethod, deserializeFrom, wrapper, deserializeFormatVersion, fieldInfo ) );
    }

    return fieldToVar;
  }

  /**
   * Creates the deserialize wrapper.
   *
   * This method may be overridden by subclasses if a wrapper shall be used.
   *
   * @param deserializeMethod the deserialize method
   * @param deserializeFrom   the deserialize from
   * @return the wrapper or null if no wrapper exists
   */
  @Nullable
  protected JVar createDeserializeWrapper( @Nonnull JMethod deserializeMethod, @Nonnull JVar deserializeFrom ) {
    return null;
  }

  public void addDelegatingSerializerToConstructor( @Nonnull JDefinedClass serializerClass, @Nonnull JClass fieldType ) {
    JType fieldSerializerType = getSerializerRefFor( fieldType );

    JMethod constructor = ( JMethod ) serializerClass.constructors().next();
    String paramName = NamingSupport.createVarName( fieldSerializerType.name() );

    //Check whether the serializer still exists
    for ( JVar param : constructor.listParams() ) {
      if ( param.type().equals( fieldSerializerType ) ) {
        return;
      }
    }

    //It does not exist, therefore let us add the serializer and map it
    JVar param = constructor.param( fieldSerializerType, paramName );

    constructor.body().add( JExpr.invoke( "add" ).arg( param ).invoke( "responsibleFor" ).arg( JExpr.dotclass( fieldType ) )
                              .invoke( "map" )
                              .arg( JExpr.lit( 1 ) ).arg( JExpr.lit( 0 ) ).arg( JExpr.lit( 0 ) )
                              .invoke( "toDelegateVersion" )
                              .arg( JExpr.lit( 1 ) ).arg( JExpr.lit( 0 ) ).arg( JExpr.lit( 0 ) ) );
  }

  @Nonnull
  protected JClass getSerializerRefFor( @Nonnull JType type ) {
    return codeGenerator.ref( type.fullName() + "Serializer" );
  }

  /**
   * Returns the exception type that is thrown on the serialize and deserialize methods
   *
   * @return the exception type
   */
  @Nonnull
  protected abstract Class<?> getExceptionType();

  /**
   * Returns the type of the serialize from object
   *
   * @return the type of the serialize from object
   */
  @Nonnull
  protected abstract Class<?> getSerializeFromType();

  /**
   * Returns the type of the serializeTo type
   *
   * @return the type of the serializeTo type
   */
  @Nonnull
  protected abstract Class<?> getSerializeToType();

  /**
   * Appends the deserialize statement and return the created var containing the value
   *
   * @param serializerClass   the serializer class
   * @param deserializeMethod the deserialize method
   * @param deserializeFrom   deserialize from
   * @param wrapper           the wrapper that may be used
   * @param formatVersion     the format version
   * @param fieldInfo         the field info
   * @return the var holding the deserialized value
   */
  @Nonnull
  protected abstract JVar appendDeserializeStatement( @Nonnull JDefinedClass serializerClass, @Nonnull JMethod deserializeMethod, @Nonnull JVar deserializeFrom, @Nullable JVar wrapper, @Nonnull JVar formatVersion, @Nonnull FieldWithInitializationInfo fieldInfo );

  /**
   * Appends the serialize statement
   *
   * @param serializerClass the serializer class
   * @param serializeMethod the serialize method
   * @param serializeTo     serialize to
   * @param object          the object that is serialized
   * @param formatVersion   the format version
   * @param fieldInfo       the field info
   */
  protected abstract void appendSerializeStatement( @Nonnull JDefinedClass serializerClass, @Nonnull JMethod serializeMethod, @Nonnull JVar serializeTo, @Nonnull JVar object, @Nonnull JVar formatVersion, @Nonnull FieldWithInitializationInfo fieldInfo );
}
