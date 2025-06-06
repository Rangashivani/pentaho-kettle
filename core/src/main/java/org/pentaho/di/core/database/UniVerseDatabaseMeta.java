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


package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains IBM UniVerse database specific information through static final members
 *
 * @author Matt
 * @since 16-nov-2006
 */

public class UniVerseDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  private static final int MAX_VARCHAR_LENGTH = 65535;

  @Override
  public int[] getAccessTypeList() {
    return new int[] {
      DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  /**
   * @see org.pentaho.di.core.database.DatabaseInterface#getNotFoundTK(boolean)
   */
  @Override
  public int getNotFoundTK( boolean useAutoinc ) {
    if ( supportsAutoInc() && useAutoinc ) {
      return 1;
    }
    return super.getNotFoundTK( useAutoinc );
  }

  @Override
  public String getDriverClass() {
    return "com.ibm.u2.jdbc.UniJDBCDriver";
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    return "jdbc:ibm-u2://" + hostname + "/" + databaseName;
  }

  /**
   * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
   *
   * @return true is setFetchSize() is supported!
   */
  @Override
  public boolean isFetchSizeSupported() {
    return false;
  }

  /**
   * @see org.pentaho.di.core.database.DatabaseInterface#getSchemaTableCombination(java.lang.String, java.lang.String)
   */
  @Override
  @SuppressWarnings( "deprecation" )
  public String getSchemaTableCombination( String schemaName, String tablePart ) {
    return getBackwardsCompatibleSchemaTableCombination( schemaName, tablePart );
  }

  /**
   * @return true if the database supports bitmap indexes
   */
  @Override
  public boolean supportsBitmapIndex() {
    return false;
  }

  /**
   * @param tableName
   *          The table to be truncated.
   * @return The SQL statement to truncate a table: remove all rows from it without a transaction
   */
  @Override
  public String getTruncateTableStatement( String tableName ) {
    return "DELETE FROM " + tableName;
  }

  /**
   * UniVerse doesn't even support timestamps.
   */
  @Override
  public boolean supportsTimeStampToDateConversion() {
    return false;
  }

  /**
   * Generates the SQL statement to add a column to the specified table For this generic type, i set it to the most
   * common possibility.
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to add a column to the specified table
   */
  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE " + tablename + " ADD " + getFieldDefinition( v, tk, pk, useAutoinc, true, false );
  }

  /**
   * Generates the SQL statement to modify a column in the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to modify a column in the specified table
   */
  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE " + tablename + " MODIFY " + getFieldDefinition( v, tk, pk, useAutoinc, true, false );
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean useAutoinc,
                                    boolean addFieldName, boolean addCr ) {
    String retval = "";

    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();

    if ( addFieldName ) {
      retval += fieldname + " ";
    }

    int type = v.getType();
    switch ( type ) {
      case ValueMetaInterface.TYPE_DATE:
        retval += "DATE";
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        retval += "CHAR(1)";
        break;
      case ValueMetaInterface.TYPE_NUMBER:
      case ValueMetaInterface.TYPE_INTEGER:
      case ValueMetaInterface.TYPE_BIGNUMBER:
        if ( fieldname.equalsIgnoreCase( tk ) || // Technical key
          fieldname.equalsIgnoreCase( pk ) // Primary key
        ) {
          retval += "INTEGER";
        } else {
          if ( length > 0 ) {
            if ( precision > 0 || length > 18 ) {
              retval += "DECIMAL(" + length + ", " + precision + ")";
            } else {
              retval += "INTEGER";
            }

          } else {
            retval += "DOUBLE PRECISION";
          }
        }
        break;
      case ValueMetaInterface.TYPE_STRING:
        if ( length >= MAX_VARCHAR_LENGTH || length <= 0 ) {
          retval += "VARCHAR(" + MAX_VARCHAR_LENGTH + ")";
        } else {
          retval += "VARCHAR(" + length + ")";
        }
        break;
      default:
        retval += " UNKNOWN";
        break;
    }

    if ( addCr ) {
      retval += Const.CR;
    }

    return retval;
  }

  @Override
  public String[] getReservedWords() {
    return new String[] {
      "@NEW", "@OLD", "ACTION", "ADD", "AL", "ALL", "ALTER", "AND", "AR", "AS", "ASC", "ASSOC", "ASSOCIATED",
      "ASSOCIATION", "AUTHORIZATION", "AVERAGE", "AVG", "BEFORE", "BETWEEN", "BIT", "BOTH", "BY", "CALC",
      "CASCADE", "CASCADED", "CAST", "CHAR", "CHAR_LENGTH", "CHARACTER", "CHARACTER_LENGTH", "CHECK", "COL.HDG",
      "COL.SPACES", "COL.SPCS", "COL.SUP", "COLUMN", "COMPILED", "CONNECT", "CONSTRAINT", "CONV", "CONVERSION",
      "COUNT", "COUNT.SUP", "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME", "DATA", "DATE", "DBA", "DBL.SPC",
      "DEC", "DECIMAL", "DEFAULT", "DELETE", "DESC", "DET.SUP", "DICT", "DISPLAY.NAME", "DISPLAYLIKE",
      "DISPLAYNAME", "DISTINCT", "DL", "DOUBLE", "DR", "DROP", "DYNAMIC", "E.EXIST", "EMPTY", "EQ", "EQUAL",
      "ESCAPE", "EVAL", "EVERY", "EXISTING", "EXISTS", "EXPLAIN", "EXPLICIT", "FAILURE", "FIRST", "FLOAT",
      "FMT", "FOOTER", "FOOTING", "FOR", "FOREIGN", "FORMAT", "FROM", "FULL", "GE", "GENERAL", "GRAND",
      "GRAND.TOTAL", "GRANT", "GREATER", "GROUP", "GROUP.SIZE", "GT", "HAVING", "HEADER", "HEADING", "HOME",
      "IMPLICIT", "IN", "INDEX", "INNER", "INQUIRING", "INSERT", "INT", "INTEGER", "INTO", "IS", "JOIN", "KEY",
      "LARGE.RECORD", "LAST", "LE", "LEADING", "LEFT", "LESS", "LIKE", "LOCAL", "LOWER", "LPTR", "MARGIN",
      "MATCHES", "MATCHING", "MAX", "MERGE.LOAD", "MIN", "MINIMIZE.SPACE", "MINIMUM.MODULUS", "MODULO",
      "MULTI.VALUE", "MULTIVALUED", "NATIONAL", "NCHAR", "NE", "NO", "NO.INDEX", "NO.OPTIMIZE", "NO.PAGE",
      "NOPAGE", "NOT", "NRKEY", "NULL", "NUMERIC", "NVARCHAR", "ON", "OPTION", "OR", "ORDER", "OUTER", "PCT",
      "PRECISION", "PRESERVING", "PRIMARY", "PRIVILEGES", "PUBLIC", "REAL", "RECORD.SIZE", "REFERENCES",
      "REPORTING", "RESOURCE", "RESTORE", "RESTRICT", "REVOKE", "RIGHT", "ROWUNIQUE", "SAID", "SAMPLE",
      "SAMPLED", "SCHEMA", "SELECT", "SEPARATION", "SEQ.NUM", "SET", "SINGLE.VALUE", "SINGLEVALUED", "SLIST",
      "SMALLINT", "SOME", "SPLIT.LOAD", "SPOKEN", "SUBSTRING", "SUCCESS", "SUM", "SUPPRESS", "SYNONYM", "TABLE",
      "TIME", "TO", "TOTAL", "TRAILING", "TRIM", "TYPE", "UNION", "UNIQUE", "UNNEST", "UNORDERED", "UPDATE",
      "UPPER", "USER", "USING", "VALUES", "VARBIT", "VARCHAR", "VARYING", "VERT", "VERTICALLY", "VIEW", "WHEN",
      "WHERE", "WITH", };
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "unijdbc.jar", "asjava.zip" };
  }

  /**
   * @return true if the database supports newlines in a SQL statements.
   */
  @Override
  public boolean supportsNewLinesInSQL() {
    return true;
  }


  @Override
  public int getMaxVARCHARLength() {
    return UniVerseDatabaseMeta.MAX_VARCHAR_LENGTH;
  }

}
