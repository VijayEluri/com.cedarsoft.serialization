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

package com.cedarsoft.test.io2;

import com.cedarsoft.serialization.StreamSerializer;
import com.cedarsoft.serialization.test.utils.AbstractXmlSerializerTest2;
import com.cedarsoft.serialization.test.utils.Entry;
import com.cedarsoft.test.Car;
import com.cedarsoft.test.Extra;
import com.cedarsoft.test.Model;
import com.cedarsoft.test.Money;
import com.cedarsoft.test.io.ModelSerializer;
import org.junit.experimental.theories.*;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 *
 */
public class CarSerializerTest extends AbstractXmlSerializerTest2<Car> {
  @Nonnull
  @Override
  protected StreamSerializer<Car> getSerializer() throws Exception {
    MoneySerializer moneySerializer = new MoneySerializer();
    return new CarSerializer( moneySerializer, new ExtraSerializer( moneySerializer ), new ModelSerializer() );
  }

  @DataPoint
  public static final Entry<?> ENTRY2 = create(
    new Car( new Model( "Ford" ), Color.ORANGE, new Money( 19000, 00 ), Arrays.asList( new Extra( "Whoo effect", new Money( 99, 98 ) ), new Extra( "Better Whoo effect", new Money( 199, 00 ) ) ) ),
    CarSerializerTest.class.getResourceAsStream( "car3.xml" ) );

  @Override
  protected void verifyDeserialized( @Nonnull Car deserialized, @Nonnull Car original ) {
    assertEquals( original.getColor(), deserialized.getColor() );
    assertEquals( original.getBasePrice(), deserialized.getBasePrice() );
    //.... (and much more)
  }
}
