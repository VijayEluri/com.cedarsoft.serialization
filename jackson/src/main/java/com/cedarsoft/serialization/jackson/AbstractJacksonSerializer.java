package com.cedarsoft.serialization.jackson;

import com.cedarsoft.Version;
import com.cedarsoft.VersionException;
import com.cedarsoft.VersionRange;
import com.cedarsoft.serialization.AbstractNameSpaceBasedSerializer;
import com.cedarsoft.serialization.InvalidNamespaceException;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public abstract class AbstractJacksonSerializer<T> extends AbstractNameSpaceBasedSerializer<T, JsonGenerator, JsonParser, JsonProcessingException> {
  @NonNls
  public static final String FIELD_NAME_DEFAULT_TEXT = "$";
  @NonNls
  public static final String PROPERTY_NS = "@ns";

  protected AbstractJacksonSerializer( @NonNls @NotNull String nameSpaceUriBase, @NotNull VersionRange formatVersionRange ) {
    super( nameSpaceUriBase, formatVersionRange );
  }

  @Override
  public void serialize( @NotNull T object, @NotNull OutputStream out ) throws IOException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();

    JsonGenerator generator = jsonFactory.createJsonGenerator( out, JsonEncoding.UTF8 );

    generator.writeStartObject();
    String nameSpace = getNameSpaceUri();
    generator.writeStringField( PROPERTY_NS, nameSpace );

    serialize( generator, object, getFormatVersion() );
    generator.writeEndObject();

    generator.close();
  }

  @NotNull
  @Override
  public T deserialize( @NotNull InputStream in ) throws IOException, VersionException {
    try {
      JsonFactory jsonFactory = JacksonSupport.getJsonFactory();

      JsonParser parser = jsonFactory.createJsonParser( in );
      nextToken( parser, JsonToken.START_OBJECT );

      nextField( parser, PROPERTY_NS );
      Version version = parseAndVerifyNameSpace( parser.getText() );

      T deserialized = deserialize( parser, version );

      if ( parser.getCurrentToken() != JsonToken.END_OBJECT ) {
        throw new JsonParseException( "No consumed everything", parser.getCurrentLocation() );
      }

      if ( parser.nextToken() != null ) {
        throw new JsonParseException( "No consumed everything", parser.getCurrentLocation() );
      }

      parser.close();

      return deserialized;
    } catch ( InvalidNamespaceException e ) {
      throw new IOException( "Could not parse due to " + e.getMessage(), e );
    }
  }

  protected void nextField( @NotNull JsonParser parser, @NotNull @NonNls String fieldName ) throws IOException {
    nextToken( parser, JsonToken.FIELD_NAME );
    String currentName = parser.getCurrentName();

    if ( !fieldName.equals( currentName ) ) {
      throw new JsonParseException( "Invalid field. Expected <" + fieldName + "> but was <" + currentName + ">", parser.getCurrentLocation() );
    }

    parser.nextToken();
  }

  protected void nextToken( @NotNull JsonParser parser, @NotNull JsonToken expected ) throws IOException {
    JsonToken current = parser.nextToken();
    if ( current != expected ) {
      throw new JsonParseException( "Invalid token. Expected <" + expected + "> but got <" + current + ">", parser.getCurrentLocation() );
    }
  }

  protected <T> void serializeArray( @NotNull Iterable<? extends T> elements, @NotNull Class<T> type, @NotNull @NonNls String propertyName, @NotNull JsonGenerator serializeTo, @NotNull Version formatVersion ) throws IOException {
    serializeTo.writeArrayFieldStart( propertyName );
    for ( T element : elements ) {
      serializeTo.writeStartObject();
      serialize( element, type, serializeTo, formatVersion );
      serializeTo.writeEndObject();
    }
    serializeTo.writeEndArray();
  }

  @NotNull
  protected <T> List<? extends T> deserializeArray( @NotNull Class<T> type, @NotNull @NonNls String propertyName, @NotNull JsonParser deserializeFrom, @NotNull Version formatVersion ) throws IOException {
    nextField( deserializeFrom, propertyName );

    List<T> deserialized = new ArrayList<T>();
    while ( deserializeFrom.nextToken() != JsonToken.END_ARRAY ) {
      deserialized.add( deserialize( type, formatVersion, deserializeFrom ) );
    }
    return deserialized;
  }
}