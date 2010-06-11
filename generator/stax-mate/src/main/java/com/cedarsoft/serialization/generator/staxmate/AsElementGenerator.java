package com.cedarsoft.serialization.generator.staxmate;

import com.cedarsoft.serialization.generator.model.FieldWithInitializationInfo;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import org.jetbrains.annotations.NotNull;

/**
 * Generates a new element
 */
public class AsElementGenerator implements SerializeToGenerator {
  @Override
  @NotNull
  public JInvocation createAddToSerializeToExpression( @NotNull JExpression serializeTo, @NotNull JExpression objectAsString, @NotNull FieldWithInitializationInfo fieldInfo ) {
    return serializeTo.invoke( "addElementWithCharacters" )
      .arg( serializeTo.invoke( "getNamespace" ) )
      .arg( fieldInfo.getSimpleName() )
      .arg( objectAsString );
  }

  @Override
  @NotNull
  public JInvocation createReadFromDeserializeFromExpression( @NotNull JExpression deserializeFrom, @NotNull FieldWithInitializationInfo fieldInfo ) {
    return JExpr.invoke( "getChildText" ).arg( deserializeFrom ).arg( fieldInfo.getSimpleName() );
  }
}
