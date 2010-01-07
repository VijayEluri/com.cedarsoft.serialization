package com.cedarsoft.serialization.stax;

import com.cedarsoft.AssertUtils;
import com.cedarsoft.Version;
import com.cedarsoft.VersionRange;
import com.cedarsoft.serialization.AbstractXmlSerializerTest;
import org.codehaus.staxmate.out.SMOutputElement;
import org.jetbrains.annotations.NotNull;
import org.testng.*;
import org.testng.annotations.*;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.Collection;

import static org.testng.Assert.*;

/**
 *
 */
public class DelegatingStaxMateSerializerTest extends AbstractXmlSerializerTest<Number> {
  private MySerializer serializer;

  @BeforeMethod
  protected void setUp() throws Exception {
    AbstractStaxMateSerializingStrategy<Integer> intSerializer = new AbstractStaxMateSerializingStrategy<Integer>( "int", Integer.class, new VersionRange( new Version( 1, 0, 0 ), new Version( 1, 0, 0 ) ) ) {
      @Override
      @NotNull
      public void serialize( @NotNull SMOutputElement serializeTo, @NotNull Integer object ) throws IOException, XMLStreamException {
        serializeTo.addCharacters( object.toString() );

      }

      @Override
      @NotNull
      public Integer deserialize( @NotNull XMLStreamReader deserializeFrom, @NotNull Version formatVersion ) throws IOException, XMLStreamException {
        getText( deserializeFrom );
        return 1;
      }
    };

    AbstractStaxMateSerializingStrategy<Double> doubleSerializer = new AbstractStaxMateSerializingStrategy<Double>( "double", Double.class, new VersionRange( new Version( 1, 0, 0 ), new Version( 1, 0, 0 ) ) ) {
      @Override
      @NotNull
      public void serialize( @NotNull SMOutputElement serializeTo, @NotNull Double object ) throws IOException, XMLStreamException {
        serializeTo.addCharacters( object.toString() );

      }

      @Override
      @NotNull
      public Double deserialize( @NotNull XMLStreamReader deserializeFrom, @NotNull Version formatVersion ) throws IOException, XMLStreamException {
        getText( deserializeFrom );
        return 2.0;
      }
    };
    serializer = new MySerializer( intSerializer, doubleSerializer );
  }

  @NotNull
  @Override
  protected AbstractStaxMateSerializer<Number> getSerializer() {
    return serializer;
  }

  @NotNull
  @Override
  protected Number createObjectToSerialize() {
    return 1;
  }

  @NotNull
  @Override
  protected String getExpectedSerialized() {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<number xmlns=\"http://number/1.0.0\" type=\"int\">1</number>";
  }

  @Override
  protected void verifyDeserialized( @NotNull Number deserialized ) {
    assertEquals( 1, deserialized );
  }

  @Test
  public void testIt() throws IOException, SAXException {
    Assert.assertEquals( serializer.getStrategies().size(), 2 );

    AssertUtils.assertXMLEqual( new String( serializer.serializeToByteArray( 1 ) ).trim(), "<number xmlns=\"http://number/1.0.0\" type=\"int\">1</number>" );
    AssertUtils.assertXMLEqual( new String( serializer.serializeToByteArray( 2.0 ) ).trim(), "<number xmlns=\"http://number/1.0.0\" type=\"double\">2.0</number>" );
  }


  public static class MySerializer extends AbstractDelegatingStaxMateSerializer<Number> {
    public MySerializer( @NotNull StaxMateSerializingStrategy<? extends Number>... serializingStrategies ) {
      super( "number", new VersionRange( new Version( 1, 0, 0 ), new Version( 1, 0, 0 ) ), serializingStrategies );
    }

    public MySerializer( @NotNull Collection<? extends StaxMateSerializingStrategy<? extends Number>> serializingStrategies ) {
      super( "number", new VersionRange( new Version( 1, 0, 0 ), new Version( 1, 0, 0 ) ), serializingStrategies );
    }
  }
}
