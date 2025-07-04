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


package org.pentaho.di;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import junit.framework.ComparisonFailure;

import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;
import org.pentaho.di.trans.steps.sort.SortRowsMeta;

public class TestUtilities {

  private static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

  /**
   * Return the end of line character based on value returned by getFileFormat.
   * 
   * @return the end of line character sequence
   */
  public static String getEndOfLineCharacters() {
    return ( getFileFormat().equalsIgnoreCase( "DOS" ) ? "\r\n" : "\n" );
  }

  /**
   * Return the file format based on the OS type. We set the file format to DOS if it is Windows since that is the only
   * Windows file type that shows up in the TextFileInput dialog.
   * 
   * @return String the file format
   */
  public static String getFileFormat() {
    if ( System.getProperty( "os.name" ).startsWith( "Windows" ) ) {
      return "DOS";
    } else {
      return "Unix";
    }
  }

  /**
   * Check the 2 lists comparing the rows in order. If they are not the same fail the test.
   * 
   * @param rows1
   *          set 1 of rows to compare
   * @param rows2
   *          set 2 of rows to compare
   * @throws TestFailedException
   */
  public static void checkRows( List<RowMetaAndData> rows1, List<RowMetaAndData> rows2 ) throws TestFailedException {

    // we call this passing in -1 as the fileNameColumn
    checkRows( rows1, rows2, -1 );
  }

  /**
   * Check the 2 lists comparing the rows in order. If they are not the same fail the test.
   * 
   * @param rows1
   *          set 1 of rows to compare
   * @param rows2
   *          set 2 of rows to compare
   * @param fileNameColumn
   *          Number of the column containing the filename. This is only checked for being non-null (some systems maybe
   *          canonize names differently than we input).
   */
  public static void checkRows( List<RowMetaAndData> rows1, List<RowMetaAndData> rows2, int fileNameColumn )
    throws TestFailedException {

    int idx = 1;
    if ( rows1.size() != rows2.size() ) {
      throw new TestFailedException( "Number of rows is not the same: " + rows1.size() + " and " + rows2.size() );
    }
    Iterator<RowMetaAndData> itrRows1 = rows1.iterator();
    Iterator<RowMetaAndData> itrRows2 = rows2.iterator();

    while ( itrRows1.hasNext() && itrRows2.hasNext() ) {
      RowMetaAndData rowMetaAndData1 = itrRows1.next();
      RowMetaAndData rowMetaAndData2 = itrRows2.next();

      RowMetaInterface rowMetaInterface1 = rowMetaAndData1.getRowMeta();

      Object[] rowObject1 = rowMetaAndData1.getData();
      Object[] rowObject2 = rowMetaAndData2.getData();

      if ( rowMetaAndData1.size() != rowMetaAndData2.size() ) {
        throw new TestFailedException( "row number " + idx + " is not equal" );
      }

      int[] fields = new int[rowMetaInterface1.size()];
      for ( int ydx = 0; ydx < rowMetaInterface1.size(); ydx++ ) {
        fields[ydx] = ydx;
      }

      try {
        if ( fileNameColumn >= 0 ) {
          rowObject1[fileNameColumn] = rowObject2[fileNameColumn];
        }
        if ( rowMetaAndData1.getRowMeta().compare( rowObject1, rowObject2, fields ) != 0 ) {
          throw new ComparisonFailure( "row nr " + idx + " is not equal", rowMetaInterface1.getString( rowObject1 ),
              rowMetaInterface1.getString( rowObject2 ) );
        }
      } catch ( KettleValueException e ) {
        throw new TestFailedException( "row nr " + idx + " is not equal" );
      }
      idx++;
    }
  }

  /**
   * Creates a dummy
   * 
   * @param name
   * @param pluginRegistry
   * @return StepMata
   */
  public static synchronized StepMeta createDummyStep( String name, PluginRegistry pluginRegistry ) {
    DummyTransMeta dummyTransMeta = new DummyTransMeta();
    String dummyPid = pluginRegistry.getPluginId( StepPluginType.class, dummyTransMeta );
    StepMeta dummyStep = new StepMeta( dummyPid, name, dummyTransMeta );

    return dummyStep;
  }

  /**
   * Create an injector step.
   * 
   * @param name
   * @param registry
   * @return StepMeta
   */
  public static synchronized StepMeta createInjectorStep( String name, PluginRegistry pluginRegistry ) {
    // create an injector step...
    InjectorMeta injectorMeta = new InjectorMeta();

    // Set the information of the injector
    String injectorPid = pluginRegistry.getPluginId( StepPluginType.class, injectorMeta );
    StepMeta injectorStep = new StepMeta( injectorPid, name, injectorMeta );

    return injectorStep;
  }

  /**
   * Create an empty temp file and return it's absolute path.
   * 
   * @param fileName
   * @return
   * @throws IOException
   */
  public static synchronized String createEmptyTempFile( String fileName ) throws IOException {
    return createEmptyTempFile( fileName, null );
  }

  /**
   * Create an empty temp file and return it's absolute path.
   * 
   * @param fileName
   * @param suffix
   *          A suffix to add at the end of the file name
   * @return
   * @throws IOException
   */
  public static synchronized String createEmptyTempFile( String fileName, String suffix ) throws IOException {
    File tempFile = File.createTempFile( fileName, ( Utils.isEmpty( suffix ) ? "" : suffix ) );
    tempFile.deleteOnExit();
    return tempFile.getAbsolutePath();
  }

  /**
   * Creates a the folder folderName under the java io temp directory. We suffix the file with ???
   * 
   * @param folderName
   * @return
   */
  public static synchronized String createTempFolder( String folderName ) {

    String absoluteFolderPath =
        System.getProperty( "java.io.tmpdir" ) + "/" + folderName + "_" + System.currentTimeMillis();

    if ( new File( absoluteFolderPath ).mkdir() ) {
      return absoluteFolderPath;
    } else {
      return null;
    }
  }

  /**
   * Returns the current date using this classes DATE_FORMAT_NOW format string.
   * 
   * @return
   */
  public static String now() {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat( DATE_FORMAT_NOW );
    return sdf.format( cal.getTime() );
  }

  /**
   * Write the file to be used as input (as a temporary file).
   * 
   * @return Absolute file name/path of the created file.
   * @throws IOException
   *           UPON
   */
  public static String writeTextFile( String folderName, String fileName, String delimiter ) throws IOException {

    String absolutePath = null;
    File file = new File( folderName + "/" + fileName + ".txt" );
    absolutePath = file.getAbsolutePath();
    String endOfLineCharacters = TestUtilities.getEndOfLineCharacters();

    FileWriter fout = new FileWriter( file );
    fout.write( "A" + delimiter + "B" + delimiter + "C" + delimiter + "D" + delimiter + "E" + endOfLineCharacters );
    fout.write( "1" + delimiter + "b1" + delimiter + "c1" + delimiter + "d1" + delimiter + "e1" + endOfLineCharacters );
    fout.write( "2" + delimiter + "b2" + delimiter + "c2" + delimiter + "d2" + delimiter + "e2" + endOfLineCharacters );
    fout.write( "3" + delimiter + "b3" + delimiter + "c3" + delimiter + "d3" + delimiter + "e3" + endOfLineCharacters );

    fout.close();

    return absolutePath;
  }

  /**
   * Create and return a SortRows step.
   * 
   * @param name
   * @param sortFields
   *          [] Fields to sort by
   * @param ascending
   *          [] Boolean indicating whether the corresponding field is to be sorted in ascending or descending order.
   * @param caseSensitive
   *          [] Boolean indicating whether the corresponding field is to have case as a factor in the sort.
   * @param directory
   *          The directory in the file system where the sort is to take place if it can't fit into memory?
   * @param sortSize
   *          ???
   * @param pluginRegistry
   *          The environment's Kettle plugin registry.
   * @return
   */
  public static synchronized StepMeta createSortRowsStep( String name, String[] sortFields, boolean[] ascending,
      boolean[] caseSensitive, String directory, int sortSize, PluginRegistry pluginRegistry ) {

    SortRowsMeta sortRowsMeta = new SortRowsMeta();
    sortRowsMeta.setSortSize( Integer.toString( sortSize / 10 ) );
    sortRowsMeta.setFieldName( sortFields );
    sortRowsMeta.setAscending( ascending );
    sortRowsMeta.setCaseSensitive( caseSensitive );
    sortRowsMeta.setDirectory( directory );

    String sortRowsStepPid = pluginRegistry.getPluginId( StepPluginType.class, sortRowsMeta );
    StepMeta sortRowsStep = new StepMeta( sortRowsStepPid, name, sortRowsMeta );

    return sortRowsStep;
  }

  /**
   * 65-90 = big, 97-122 - small
   * 
   * @param rng
   * @param characters
   * @param length
   * @return
   */
  public static String generateString( Random rng, int length ) {
    char[] text = new char[length];
    for ( int i = 0; i < length; i++ ) {
      int ch = -1;
      double db = rng.nextDouble();
      if ( rng.nextInt() % 2 == 0 ) {
        ch = 65 + (int) ( db * 26 );
      } else {
        ch = 97 + (int) ( db * 26 );
      }
      text[i] = (char) ch;
    }
    return new String( text );
  }

  public static String getStringFromInput( InputStream in ) throws IOException {
    StringBuilder sb = new StringBuilder();
    InputStreamReader is = null;
    BufferedReader br = null;
    try {
      is = new InputStreamReader( in );
      br = new BufferedReader( is );
      String read = br.readLine();
      while ( read != null ) {
        sb.append( read );
        read = br.readLine();
      }
    } finally {
      if ( is != null ) {
        try {
          is.close();
        } catch ( IOException e ) {
          // Suppress
        }
      }
      if ( br != null ) {
        try {
          br.close();
        } catch ( IOException e ) {
          // Suppress
        }
      }
    }
    return sb.toString();
  }

  public static Trans loadAndRunTransformation( String path, Object... parameters ) throws Exception {
    TransMeta transMeta = new TransMeta( DefaultBowl.getInstance(), path );
    transMeta.setTransformationType( TransMeta.TransformationType.Normal );

    Trans trans = new Trans( transMeta );
    if ( parameters != null ) {
      if ( parameters.length % 2 == 1 ) {
        throw new IllegalArgumentException( "Parameters should be an array of pairs 'parameter'-'value'-..." );
      }

      for ( int i = 0; i < parameters.length; i += 2 ) {
        Object parameter = parameters[i];
        Object value = parameters[i + 1];
        trans.setParameterValue( parameter.toString(), value.toString() );
      }
    }

    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();

    return trans;
  }
}
