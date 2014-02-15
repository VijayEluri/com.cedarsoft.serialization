package com.cedarsoft.serialization.stax.mate.primitives;

import com.cedarsoft.serialization.StreamSerializer;
import com.cedarsoft.serialization.test.utils.AbstractXmlSerializerTest2;
import com.cedarsoft.serialization.test.utils.Entry;
import org.junit.*;
import org.junit.experimental.theories.*;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.fest.assertions.Fail.fail;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class ByteSerializerTest extends AbstractXmlSerializerTest2<Byte> {
  @Nonnull
  @Override
  protected StreamSerializer<Byte> getSerializer() throws Exception {
    return new ByteSerializer();
  }

  @Test
  public void testNotClose() throws Exception {
    final boolean[] shallAcceptClose = {false};

    OutputStream out = new FilterOutputStream( new ByteArrayOutputStream() ) {
      private boolean closed;

      @Override
      public void close() throws IOException {
        if ( !shallAcceptClose[0] ) {
          fail( "Unacceptable close!" );
        }

        super.close();
        closed = true;
      }
    };

    getSerializer().serialize( ( byte ) 123, out );
    shallAcceptClose[0] = true;
    out.close();
  }

  @DataPoint
  public static final Entry<?> ENTRY1 = create( ( byte ) 123.0, "<byte>123</byte>" );
  @DataPoint
  public static final Entry<?> ENTRY3 = create( ( byte ) -123.5, "<byte>-123</byte>" );

  @DataPoint
  public static final Entry<?> ENTRY4 = create( Byte.MAX_VALUE, "<byte>127</byte>" );
  @DataPoint
  public static final Entry<?> ENTRY5 = create( Byte.MIN_VALUE, "<byte>-128</byte>" );
}