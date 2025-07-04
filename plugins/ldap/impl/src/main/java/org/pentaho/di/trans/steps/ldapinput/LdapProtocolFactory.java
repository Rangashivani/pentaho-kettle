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


package org.pentaho.di.trans.steps.ldapinput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;

public class LdapProtocolFactory {
  protected static final List<Class<? extends LdapProtocol>> protocols = initProtocols();

  private static List<Class<? extends LdapProtocol>> initProtocols() {
    List<Class<? extends LdapProtocol>> protocols = new ArrayList<Class<? extends LdapProtocol>>();
    protocols.add( LdapProtocol.class );
    protocols.add( LdapSslProtocol.class );
    protocols.add( LdapTlsProtocol.class );
    return protocols;
  }

  private final LogChannelInterface log;

  private static String getName( Class<? extends LdapProtocol> protocol ) throws KettleException {
    try {
      return protocol.getMethod( "getName" ).invoke( null ).toString();
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /**
   * Returns the connection types understood by the factory
   *
   * @return the connection types understood by the factory
   * @throws KettleException
   */
  public static final List<String> getConnectionTypes( LogChannelInterface log ) {
    List<String> result = new ArrayList<String>();
    synchronized ( protocols ) {
      for ( Class<? extends LdapProtocol> protocol : protocols ) {
        try {
          result.add( getName( protocol ) );
        } catch ( KettleException e ) {
          log.logError( "Unable to get name for " + protocol.getCanonicalName() );
        }
      }
    }
    return result;
  }

  public LdapProtocolFactory( LogChannelInterface log ) {
    this.log = log;
  }

  /**
   * Creates the LdapProtocol appropriate for the LdapMeta
   *
   * @param variableSpace
   *          the variable space for environment substitutions
   * @param meta
   *          the ldap meta
   * @param binaryAttributes
   *          binary attributes to associate with the connection
   * @return an LdapProtocol
   * @throws KettleException
   */
  public LdapProtocol createLdapProtocol( Bowl bowl, VariableSpace variableSpace, LdapMeta meta,
    Collection<String> binaryAttributes ) throws KettleException {
    String connectionType = variableSpace.environmentSubstitute( meta.getProtocol() );

    synchronized ( protocols ) {
      for ( Class<? extends LdapProtocol> protocol : protocols ) {
        if ( getName( protocol ).equals( connectionType ) ) {
          try {
            return protocol.getConstructor(
              Bowl.class,
              LogChannelInterface.class,
              VariableSpace.class,
              LdapMeta.class,
              Collection.class ).newInstance( bowl, log, variableSpace, meta, binaryAttributes );
          } catch ( Exception e ) {
            throw new KettleException( e );
          }
        }
      }
    }
    return null;
  }
}
