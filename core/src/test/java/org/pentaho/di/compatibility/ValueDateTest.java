/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.compatibility;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

/**
 * Test class for the basic functionality of ValueDate.
 *
 * @author Sven Boden
 */
public class ValueDateTest {
  private Date dt = null;
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  @Before
  public void setUp() throws ParseException {
    TimeZone.setDefault( TimeZone.getTimeZone( "CET" ) );
    SimpleDateFormat format = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS", Locale.US );
    dt = format.parse( "2006/06/07 01:02:03.004" );
  }

  /**
   * Constructor test 1.
   */
  @Test
  public void testConstructor1() {
    ValueDate vs = new ValueDate();
    assertEquals( Value.VALUE_TYPE_DATE, vs.getType() );
    assertEquals( "Date", vs.getTypeDesc() );
    assertNull( vs.getDate() );
    assertEquals( -1, vs.getLength() );
    assertEquals( -1, vs.getPrecision() );

    ValueDate vs1 = new ValueDate( dt );

    // Length and precision are ignored
    vs1.setLength( 2 );
    assertEquals( -1, vs1.getLength() );
    assertEquals( -1, vs1.getPrecision() );

    vs1.setLength( 4, 2 );
    assertEquals( -1, vs1.getLength() );
    assertEquals( 2, vs1.getPrecision() );

    vs1.setPrecision( 3 );
    assertEquals( 3, vs1.getPrecision() );
  }

  /**
   * Test the getters of ValueDate.
   */
  @Test
  public void testGetters() {
    TimeZone.setDefault( TimeZone.getTimeZone( "CET" ) );

    ValueDate vs1 = new ValueDate();
    ValueDate vs2 = new ValueDate( dt );

    assertEquals( false, vs1.getBoolean() );
    assertEquals( false, vs2.getBoolean() );

    assertNull( vs1.getString() );
    assertEquals( "2006/06/07 01:02:03.004", vs2.getString() );

    assertEquals( 0.0D, vs1.getNumber(), 0.001D );

    // 1.149634923004E12
    // 1.149656523004E12
    assertEquals( 1.149634923004E12, vs2.getNumber(), 0.001E12D );

    assertEquals( 0L, vs1.getInteger() );
    assertEquals( 1149634923004L, vs2.getInteger() );
    assertEquals( BigDecimal.ZERO, vs1.getBigNumber() );
    assertEquals( new BigDecimal( 1149634923004L ), vs2.getBigNumber() );

    assertNull( vs1.getDate() );
    assertEquals( 1149634923004L, vs2.getDate().getTime() );

    assertNull( vs1.getSerializable() );
    assertEquals( dt, vs2.getSerializable() );
  }

  /**
   * Test the setters of ValueDate.
   */
  @Test
  public void testSetters() {
    TimeZone.setDefault( TimeZone.getTimeZone( "CET" ) );

    ValueDate vs = new ValueDate();

    try {
      vs.setString( null );
      fail( "Expected NullPointerException" );
    } catch ( NullPointerException ex ) {
    }

    vs.setString( "unknown" );
    assertNull( vs.getDate() );
    vs.setString( "2006/06/07 01:02:03.004" );
    assertEquals( dt, vs.getDate() );

    vs.setDate( dt );
    assertEquals( dt, vs.getDate() );

    vs.setBoolean( true );
    assertNull( vs.getDate() );
    vs.setBoolean( false );
    assertNull( vs.getDate() );

    vs.setNumber( dt.getTime() );
    assertEquals( dt, vs.getDate() );

    vs.setInteger( dt.getTime() );
    assertEquals( dt, vs.getDate() );

    vs.setBigNumber( new BigDecimal( dt.getTime() ) );
    assertEquals( dt, vs.getDate() );

    // setSerializable is ignored ???
  }
}
