package com.cedarsoft.serialization.stax;

import com.cedarsoft.Version;
import com.cedarsoft.VersionException;
import com.cedarsoft.serialization.SerializingStrategySupport;
import org.codehaus.staxmate.out.SMOutputElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.*;

import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class SerializingStrategySupportTest {
  @Test
  public void testGenerics() {
    SerializingStrategySupport<String, MyStrategy> support = new SerializingStrategySupport<String, MyStrategy>( Arrays.asList( new MyStrategy() ) );
    SerializingStrategySupport<String, StaxMateSerializingStrategy<String>> support2 = new SerializingStrategySupport<String, StaxMateSerializingStrategy<String>>( Arrays.asList( new MyStrategy() ) );

    List<StaxMateSerializingStrategy<? extends String>> strategies1 = Arrays.<StaxMateSerializingStrategy<? extends String>>asList( new MyStrategy() );

    SerializingStrategySupport<String, StaxMateSerializingStrategy<String>> support5 = new SerializingStrategySupport<String, StaxMateSerializingStrategy<String>>( strategies1 );

    Collection<? extends StaxMateSerializingStrategy<? extends String>> strategies = new ArrayList<StaxMateSerializingStrategy<? extends String>>();
    try {
      SerializingStrategySupport<String, StaxMateSerializingStrategy<String>> support3 = new SerializingStrategySupport<String, StaxMateSerializingStrategy<String>>( strategies );
    } catch ( Exception ignore ) {
    }
  }

  private static class MyStrategy implements StaxMateSerializingStrategy<String> {
    @Override
    @NotNull
    public String getId() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void serialize( @NotNull String object, @NotNull OutputStream out ) throws IOException {
      throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public String deserialize( @NotNull InputStream in ) throws IOException, VersionException {
      throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Version getFormatVersion() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean supports( @NotNull Object object ) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void serialize( @NotNull SMOutputElement serializeTo, @NotNull String object ) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    public String deserialize( @NotNull @NonNls XMLStreamReader deserializeFrom, @NotNull Version formatVersion ) throws IOException {
      throw new UnsupportedOperationException();
    }
  }

}
