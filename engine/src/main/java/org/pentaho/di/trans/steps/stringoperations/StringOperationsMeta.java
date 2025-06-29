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


package org.pentaho.di.trans.steps.stringoperations;

import java.util.List;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This class takes care of the meta data for the StringOperations step.
 *
 * @author Samatar Hassan
 * @since 02 April 2009
 */
@InjectionSupported( localizationPrefix = "StringOperations.Injection." )
public class StringOperationsMeta extends BaseStepMeta implements StepMetaInterface {

  private static Class<?> PKG = StringOperationsMeta.class; // for i18n purposes, needed by Translator2!!

  /** which field in input stream to compare with? */
  @Injection( name = "FIELD_IN_STREAM" )
  private String[] fieldInStream;

  /** output field */
  @Injection( name = "FIELD_OUT_STREAM" )
  private String[] fieldOutStream;

  /** Trim type */
  @Injection( name = "TRIM_TYPE" )
  private String[] trimType;

  /** Lower/Upper type */
  @Injection( name = "LOWER_UPPER" )
  private String[] lowerUpper;

  /** InitCap */
  @Injection( name = "INIT_CAP" )
  private String[] initCap;

  @Injection( name = "ESCAPE" )
  private String[] maskXML;

  @Injection( name = "DIGITS" )
  private String[] digits;

  @Injection( name = "REMOVE_SPECIAL_CHARACTERS" )
  private String[] remove_special_characters;

  /** padding type */
  @Injection( name = "PADDING_TYPE" )
  private String[] padding_type;

  /** Pad length */
  @Injection( name = "PADDING_LENGTH" )
  private String[] padLen;

  @Injection( name = "PADDING_CHAR" )
  private String[] padChar;

  /**
   * The trim type codes
   */
  public static final String[] trimTypeCode = { "none", "left", "right", "both" };

  public static final int TRIM_NONE = 0;

  public static final int TRIM_LEFT = 1;

  public static final int TRIM_RIGHT = 2;

  public static final int TRIM_BOTH = 3;

  /**
   * The trim description
   */
  public static final String[] trimTypeDesc = {
    BaseMessages.getString( PKG, "StringOperationsMeta.TrimType.None" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.TrimType.Left" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.TrimType.Right" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.TrimType.Both" ) };

  /**
   * The lower upper codes
   */
  public static final String[] lowerUpperCode = { "none", "lower", "upper" };

  public static final int LOWER_UPPER_NONE = 0;

  public static final int LOWER_UPPER_LOWER = 1;

  public static final int LOWER_UPPER_UPPER = 2;

  /**
   * The lower upper description
   */
  public static final String[] lowerUpperDesc = {
    BaseMessages.getString( PKG, "StringOperationsMeta.LowerUpper.None" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.LowerUpper.Lower" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.LowerUpper.Upper" ) };

  public static final String[] initCapDesc = new String[] {
    BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG, "System.Combo.Yes" ) };

  public static final String[] initCapCode = { "no", "yes" };

  private static final String[] initCapCodeMDI = { "n", "y" };

  public static final int INIT_CAP_NO = 0;

  public static final int INIT_CAP_YES = 1;

  // digits
  public static final String[] digitsCode = { "none", "digits_only", "remove_digits" };

  private static final String[] digitsCodeMDI = {
    BaseMessages.getString( PKG, "StringOperationsMeta.Digits.None" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.Digits.Only" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.Digits.Remove" ) };

  public static final int DIGITS_NONE = 0;

  public static final int DIGITS_ONLY = 1;

  public static final int DIGITS_REMOVE = 2;

  public static final String[] digitsDesc = new String[] {
    BaseMessages.getString( PKG, "StringOperationsMeta.Digits.None" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.Digits.Only" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.Digits.Remove" ) };

  // mask XML

  public static final String[] maskXMLDesc = new String[] {
    BaseMessages.getString( PKG, "StringOperationsMeta.MaskXML.None" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.MaskXML.EscapeXML" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.MaskXML.CDATA" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.MaskXML.UnEscapeXML" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.MaskXML.EscapeSQL" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.MaskXML.EscapeHTML" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.MaskXML.UnEscapeHTML" ) };

  public static final String[] maskXMLCode = {
    "none", "escapexml", "cdata", "unescapexml", "escapesql", "escapehtml", "unescapehtml" };

  private static final String[] maskXMLCodeMDI = {
    BaseMessages.getString( PKG, "StringOperationsMeta.MaskXML.None" ),
    BaseMessages.getString( PKG, "StringOperations.MDI.Mask.EscapeXML" ),
    BaseMessages.getString( PKG, "StringOperations.MDI.Mask.CDATA" ),
    BaseMessages.getString( PKG, "StringOperations.MDI.Mask.UnescapeXML" ),
    BaseMessages.getString( PKG, "StringOperations.MDI.Mask.EscapeSQL" ),
    BaseMessages.getString( PKG, "StringOperations.MDI.Mask.EscapeHTML" ),
    BaseMessages.getString( PKG, "StringOperations.MDI.Mask.UnescapeHTML" ) };

  public static final int MASK_NONE = 0;
  public static final int MASK_ESCAPE_XML = 1;
  public static final int MASK_CDATA = 2;
  public static final int MASK_UNESCAPE_XML = 3;
  public static final int MASK_ESCAPE_SQL = 4;
  public static final int MASK_ESCAPE_HTML = 5;
  public static final int MASK_UNESCAPE_HTML = 6;

  // remove special characters
  public static final String[] removeSpecialCharactersCode = { "none", "cr", "lf", "crlf", "tab", "espace" };

  private static final String[] removeSpecialCharactersCodeMDI = {
    BaseMessages.getString( PKG, "StringOperationsMeta.RemoveSpecialCharacters.None" ),
    BaseMessages.getString( PKG, "StringOperations.MDI.RemoveSpecialCharacters.CR" ),
    BaseMessages.getString( PKG, "StringOperations.MDI.RemoveSpecialCharacters.LF" ),
    BaseMessages.getString( PKG, "StringOperations.MDI.RemoveSpecialCharacters.CRLF" ),
    BaseMessages.getString( PKG, "StringOperations.MDI.RemoveSpecialCharacters.TAB" ),
    BaseMessages.getString( PKG, "StringOperations.MDI.RemoveSpecialCharacters.Space" ),
  };

  public static final int REMOVE_SPECIAL_CHARACTERS_NONE = 0;

  public static final int REMOVE_SPECIAL_CHARACTERS_CR = 1;

  public static final int REMOVE_SPECIAL_CHARACTERS_LF = 2;

  public static final int REMOVE_SPECIAL_CHARACTERS_CRLF = 3;

  public static final int REMOVE_SPECIAL_CHARACTERS_TAB = 4;

  public static final int REMOVE_SPECIAL_CHARACTERS_ESPACE = 5;

  public static final String[] removeSpecialCharactersDesc = new String[] {
    BaseMessages.getString( PKG, "StringOperationsMeta.RemoveSpecialCharacters.None" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.RemoveSpecialCharacters.CR" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.RemoveSpecialCharacters.LF" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.RemoveSpecialCharacters.CRLF" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.RemoveSpecialCharacters.TAB" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.RemoveSpecialCharacters.Space" ) };

  /**
   * The padding description
   */
  public static final String[] paddingDesc = {
    BaseMessages.getString( PKG, "StringOperationsMeta.Padding.None" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.Padding.Left" ),
    BaseMessages.getString( PKG, "StringOperationsMeta.Padding.Right" ) };

  public static final String[] paddingCode = { "none", "left", "right" };

  public static final int PADDING_NONE = 0;

  public static final int PADDING_LEFT = 1;

  public static final int PADDING_RIGHT = 2;

  public StringOperationsMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the fieldInStream.
   */
  public String[] getFieldInStream() {
    return fieldInStream;
  }

  /**
   * @param keyStream
   *          The fieldInStream to set.
   */
  public void setFieldInStream( String[] keyStream ) {
    this.fieldInStream = keyStream;
  }

  /**
   * @return Returns the fieldOutStream.
   */
  public String[] getFieldOutStream() {
    return fieldOutStream;
  }

  /**
   * @param keyStream
   *          The fieldOutStream to set.
   */
  public void setFieldOutStream( String[] keyStream ) {
    this.fieldOutStream = keyStream;
  }

  public String[] getPadLen() {
    return padLen;
  }

  public void setPadLen( String[] value ) {
    padLen = value;
  }

  public String[] getPadChar() {
    return padChar;
  }

  public void setPadChar( String[] value ) {
    padChar = value;
  }

  public String[] getTrimType() {
    return trimType;
  }

  public void setTrimType( String[] trimType ) {
    this.trimType = trimType;
  }

  public String[] getLowerUpper() {
    return lowerUpper;
  }

  public void setLowerUpper( String[] lowerUpper ) {
    this.lowerUpper = lowerUpper;
  }

  public String[] getInitCap() {
    return initCap;
  }

  public void setInitCap( String[] value ) {
    initCap = value;
  }

  public String[] getMaskXML() {
    return maskXML;
  }

  public void setMaskXML( String[] value ) {
    maskXML = value;
  }

  public String[] getDigits() {
    return digits;
  }

  public void setDigits( String[] value ) {
    digits = value;
  }

  public String[] getRemoveSpecialCharacters() {
    return remove_special_characters;
  }

  public void setRemoveSpecialCharacters( String[] value ) {
    remove_special_characters = value;
  }

  public String[] getPaddingType() {
    return padding_type;
  }

  public void setPaddingType( String[] value ) {
    padding_type = value;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int nrkeys ) {
    fieldInStream = new String[nrkeys];
    fieldOutStream = new String[nrkeys];
    trimType = new String[nrkeys];
    lowerUpper = new String[nrkeys];
    padding_type = new String[nrkeys];
    padChar = new String[nrkeys];
    padLen = new String[nrkeys];
    initCap = new String[nrkeys];
    maskXML = new String[nrkeys];
    digits = new String[nrkeys];
    remove_special_characters = new String[nrkeys];
  }

  @Override
  public Object clone() {
    StringOperationsMeta retval = (StringOperationsMeta) super.clone();
    int nrkeys = fieldInStream.length;

    retval.allocate( nrkeys );
    System.arraycopy( fieldInStream, 0, retval.fieldInStream, 0, nrkeys );
    System.arraycopy( fieldOutStream, 0, retval.fieldOutStream, 0, nrkeys );
    System.arraycopy( trimType, 0, retval.trimType, 0, nrkeys );
    System.arraycopy( lowerUpper, 0, retval.lowerUpper, 0, nrkeys );
    System.arraycopy( padding_type, 0, retval.padding_type, 0, nrkeys );
    System.arraycopy( padChar, 0, retval.padChar, 0, nrkeys );
    System.arraycopy( padLen, 0, retval.padLen, 0, nrkeys );
    System.arraycopy( initCap, 0, retval.initCap, 0, nrkeys );
    System.arraycopy( maskXML, 0, retval.maskXML, 0, nrkeys );
    System.arraycopy( digits, 0, retval.digits, 0, nrkeys );
    System.arraycopy( remove_special_characters, 0, retval.remove_special_characters, 0, nrkeys );

    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      int nrkeys;

      Node lookup = XMLHandler.getSubNode( stepnode, "fields" );
      nrkeys = XMLHandler.countNodes( lookup, "field" );

      allocate( nrkeys );

      for ( int i = 0; i < nrkeys; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( lookup, "field", i );

        fieldInStream[i] = Const.NVL( XMLHandler.getTagValue( fnode, "in_stream_name" ), "" );
        fieldOutStream[i] = Const.NVL( XMLHandler.getTagValue( fnode, "out_stream_name" ), "" );

        trimType[i] = Const.NVL( XMLHandler.getTagValue( fnode, "trim_type" ), "" );
        lowerUpper[i] = Const.NVL( XMLHandler.getTagValue( fnode, "lower_upper" ), "" );
        padding_type[i] = Const.NVL( XMLHandler.getTagValue( fnode, "padding_type" ), "" );
        padChar[i] = Const.NVL( XMLHandler.getTagValue( fnode, "pad_char" ), "" );
        padLen[i] = Const.NVL( XMLHandler.getTagValue( fnode, "pad_len" ), "" );
        initCap[i] = Const.NVL( XMLHandler.getTagValue( fnode, "init_cap" ), "" );
        maskXML[i] = Const.NVL( XMLHandler.getTagValue( fnode, "mask_xml" ), "" );
        digits[i] = Const.NVL( XMLHandler.getTagValue( fnode, "digits" ), "" );
        remove_special_characters[i] = Const.NVL( XMLHandler.getTagValue( fnode, "remove_special_characters" ), "" );

      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "StringOperationsMeta.Exception.UnableToReadStepInfoFromXML" ), e );
    }
  }

  @Override
  public void setDefault() {
    fieldInStream = null;
    fieldOutStream = null;

    int nrkeys = 0;

    allocate( nrkeys );
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 500 );

    retval.append( "    <fields>" ).append( Const.CR );

    for ( int i = 0; i < fieldInStream.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "in_stream_name", fieldInStream[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "out_stream_name", fieldOutStream[i] ) );

      retval.append( "        " ).append( XMLHandler.addTagValue( "trim_type", trimType[i] ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( "lower_upper", lowerUpper[i] ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( "padding_type", padding_type[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "pad_char", padChar[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "pad_len", padLen[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "init_cap", initCap[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "mask_xml", maskXML[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "digits", digits[i] ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( "remove_special_characters", remove_special_characters[i] ) );

      retval.append( "      </field>" ).append( Const.CR );
    }

    retval.append( "    </fields>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {

      int nrkeys = rep.countNrStepAttributes( id_step, "in_stream_name" );

      allocate( nrkeys );
      for ( int i = 0; i < nrkeys; i++ ) {
        fieldInStream[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "in_stream_name" ), "" );
        fieldOutStream[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "out_stream_name" ), "" );

        trimType[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "trim_type" ), "" );
        lowerUpper[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "lower_upper" ), "" );
        padding_type[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "padding_type" ), "" );
        padChar[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "pad_char" ), "" );
        padLen[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "pad_len" ), "" );
        initCap[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "init_cap" ), "" );
        maskXML[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "mask_xml" ), "" );
        digits[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "digits" ), "" );
        remove_special_characters[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "remove_special_characters" ), "" );

      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "StringOperationsMeta.Exception.UnexpectedErrorInReadingStepInfo" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {

      for ( int i = 0; i < fieldInStream.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "in_stream_name", fieldInStream[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "out_stream_name", fieldOutStream[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "trim_type", trimType[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "lower_upper", lowerUpper[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "padding_type", padding_type[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "pad_char", padChar[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "pad_len", padLen[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "init_cap", initCap[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "mask_xml", maskXML[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "digits", digits[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "remove_special_characters", remove_special_characters[i] );

      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "StringOperationsMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  @Override
  public void getFields( Bowl bowl, RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info,
    StepMeta nextStep, VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Add new field?
    for ( int i = 0; i < fieldOutStream.length; i++ ) {
      ValueMetaInterface v;
      String outputField = space.environmentSubstitute( fieldOutStream[i] );
      if ( !Utils.isEmpty( outputField ) ) {
        // Add a new field
        v = new ValueMetaString( outputField );
        v.setLength( 100, -1 );
        v.setOrigin( name );
        inputRowMeta.addValueMeta( v );
      } else {
        v = inputRowMeta.searchValueMeta( fieldInStream[i] );
        if ( v == null ) {
          continue;
        }
        v.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
        int paddingType = getPaddingByDesc( getPaddingType()[i] );
        if ( paddingType == PADDING_LEFT || paddingType == PADDING_RIGHT ) {
          int padLen = Const.toInt( space.environmentSubstitute( getPadLen()[i] ), 0 );
          if ( padLen > v.getLength() ) {
            // alter meta data
            v.setLength( padLen );
          }
        }
      }
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    CheckResult cr;
    String error_message = "";
    boolean first = true;
    boolean error_found = false;

    if ( prev == null ) {

      error_message +=
        BaseMessages.getString( PKG, "StringOperationsMeta.CheckResult.NoInputReceived" ) + Const.CR;
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo );
      remarks.add( cr );
    } else {

      for ( int i = 0; i < fieldInStream.length; i++ ) {
        String field = fieldInStream[i];

        ValueMetaInterface v = prev.searchValueMeta( field );
        if ( v == null ) {
          if ( first ) {
            first = false;
            error_message +=
              BaseMessages.getString( PKG, "StringOperationsMeta.CheckResult.MissingInStreamFields" ) + Const.CR;
          }
          error_found = true;
          error_message += "\t\t" + field + Const.CR;
        }
      }
      if ( error_found ) {
        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "StringOperationsMeta.CheckResult.FoundInStreamFields" ), stepinfo );
      }
      remarks.add( cr );

      // Check whether all are strings
      first = true;
      error_found = false;
      for ( int i = 0; i < fieldInStream.length; i++ ) {
        String field = fieldInStream[i];

        ValueMetaInterface v = prev.searchValueMeta( field );
        if ( v != null ) {
          if ( v.getType() != ValueMetaInterface.TYPE_STRING ) {
            if ( first ) {
              first = false;
              error_message +=
                BaseMessages.getString( PKG, "StringOperationsMeta.CheckResult.OperationOnNonStringFields" )
                  + Const.CR;
            }
            error_found = true;
            error_message += "\t\t" + field + Const.CR;
          }
        }
      }
      if ( error_found ) {
        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "StringOperationsMeta.CheckResult.AllOperationsOnStringFields" ), stepinfo );
      }
      remarks.add( cr );

      if ( fieldInStream.length > 0 ) {
        for ( int idx = 0; idx < fieldInStream.length; idx++ ) {
          if ( Utils.isEmpty( fieldInStream[idx] ) ) {
            cr =
              new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
                PKG, "StringOperationsMeta.CheckResult.InStreamFieldMissing", new Integer( idx + 1 )
                  .toString() ), stepinfo );
            remarks.add( cr );

          }
        }
      }

      // Check if all input fields are distinct.
      for ( int idx = 0; idx < fieldInStream.length; idx++ ) {
        for ( int jdx = 0; jdx < fieldInStream.length; jdx++ ) {
          if ( fieldInStream[idx].equals( fieldInStream[jdx] ) && idx != jdx && idx < jdx ) {
            error_message =
              BaseMessages.getString(
                PKG, "StringOperationsMeta.CheckResult.FieldInputError", fieldInStream[idx] );
            cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo );
            remarks.add( cr );
          }
        }
      }

    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new StringOperations( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new StringOperationsData();
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

  public static String getTrimTypeCode( int i ) {
    if ( i < 0 || i >= trimTypeCode.length ) {
      return trimTypeCode[0];
    }
    return trimTypeCode[i];
  }

  public static String getLowerUpperCode( int i ) {
    if ( i < 0 || i >= lowerUpperCode.length ) {
      return lowerUpperCode[0];
    }
    return lowerUpperCode[i];
  }

  public static String getInitCapCode( int i ) {
    if ( i < 0 || i >= initCapCode.length ) {
      return initCapCode[0];
    }
    return initCapCode[i];
  }

  public static String getMaskXMLCode( int i ) {
    if ( i < 0 || i >= maskXMLCode.length ) {
      return maskXMLCode[0];
    }
    return maskXMLCode[i];
  }

  public static String getDigitsCode( int i ) {
    if ( i < 0 || i >= digitsCode.length ) {
      return digitsCode[0];
    }
    return digitsCode[i];
  }

  public static String getRemoveSpecialCharactersCode( int i ) {
    if ( i < 0 || i >= removeSpecialCharactersCode.length ) {
      return removeSpecialCharactersCode[0];
    }
    return removeSpecialCharactersCode[i];
  }

  public static String getPaddingCode( int i ) {
    if ( i < 0 || i >= paddingCode.length ) {
      return paddingCode[0];
    }
    return paddingCode[i];
  }

  public static String getTrimTypeDesc( int i ) {
    if ( i < 0 || i >= trimTypeDesc.length ) {
      return trimTypeDesc[0];
    }
    return trimTypeDesc[i];
  }

  public static String getLowerUpperDesc( int i ) {
    if ( i < 0 || i >= lowerUpperDesc.length ) {
      return lowerUpperDesc[0];
    }
    return lowerUpperDesc[i];
  }

  public static String getInitCapDesc( int i ) {
    if ( i < 0 || i >= initCapDesc.length ) {
      return initCapDesc[0];
    }
    return initCapDesc[i];
  }

  public static String getMaskXMLDesc( int i ) {
    if ( i < 0 || i >= maskXMLDesc.length ) {
      return maskXMLDesc[0];
    }
    return maskXMLDesc[i];
  }

  public static String getDigitsDesc( int i ) {
    if ( i < 0 || i >= digitsDesc.length ) {
      return digitsDesc[0];
    }
    return digitsDesc[i];
  }

  public static String getRemoveSpecialCharactersDesc( int i ) {
    if ( i < 0 || i >= removeSpecialCharactersDesc.length ) {
      return removeSpecialCharactersDesc[0];
    }
    return removeSpecialCharactersDesc[i];
  }

  public static String getPaddingDesc( int i ) {
    if ( i < 0 || i >= paddingDesc.length ) {
      return paddingDesc[0];
    }
    return paddingDesc[i];
  }

  private static int getTrimTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < trimTypeCode.length; i++ ) {
      if ( trimTypeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static int getLowerUpperByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < lowerUpperCode.length; i++ ) {
      if ( lowerUpperCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getInitCapByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < initCapCode.length; i++ ) {
      if ( initCapCode[i].equalsIgnoreCase( tt ) || initCapCodeMDI[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getMaskXMLByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < maskXMLCode.length; i++ ) {
      if ( maskXMLCode[i].equalsIgnoreCase( tt ) || maskXMLCodeMDI[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getDigitsByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < digitsCode.length; i++ ) {
      if ( digitsCode[i].equalsIgnoreCase( tt ) || digitsCodeMDI[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getRemoveSpecialCharactersByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < removeSpecialCharactersCode.length; i++ ) {
      if ( removeSpecialCharactersCode[i].equalsIgnoreCase( tt )
        || removeSpecialCharactersCodeMDI[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getPaddingByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < paddingCode.length; i++ ) {
      if ( paddingCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static int getTrimTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < trimTypeDesc.length; i++ ) {
      if ( trimTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getTrimTypeByCode( tt );
  }

  public static int getLowerUpperByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < lowerUpperDesc.length; i++ ) {
      if ( lowerUpperDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getLowerUpperByCode( tt );
  }

  public static int getInitCapByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < initCapDesc.length; i++ ) {
      if ( initCapDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getInitCapByCode( tt );
  }

  public static int getMaskXMLByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < maskXMLDesc.length; i++ ) {
      if ( maskXMLDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getMaskXMLByCode( tt );
  }

  public static int getDigitsByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < digitsDesc.length; i++ ) {
      if ( digitsDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getDigitsByCode( tt );
  }

  public static int getRemoveSpecialCharactersByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < removeSpecialCharactersDesc.length; i++ ) {
      if ( removeSpecialCharactersDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getRemoveSpecialCharactersByCode( tt );
  }

  public static int getPaddingByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < paddingDesc.length; i++ ) {
      if ( paddingDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getPaddingByCode( tt );
  }
}
