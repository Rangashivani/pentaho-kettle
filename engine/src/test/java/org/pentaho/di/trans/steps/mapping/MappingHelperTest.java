/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.mapping;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.IMetaStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.pentaho.di.trans.step.BaseStepHelper.IS_VALID_REFERENCE;
import static org.pentaho.di.trans.step.BaseStepHelper.REFERENCE_PATH;
import static org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS;
import static org.pentaho.di.trans.step.StepHelperInterface.SUCCESS_RESPONSE;

public class MappingHelperTest {

  MappingMeta mappingMeta;
  MappingHelper mappingHelper;
  TransMeta transMeta;
  Repository repository;
  Bowl defaultBowl;
  IMetaStore metaStore;

  @Before
  public void setUp() {
    transMeta = mock( TransMeta.class );
    mappingMeta = mock( MappingMeta.class );
    repository = mock( Repository.class );
    defaultBowl = mock( Bowl.class );
    metaStore = mock( IMetaStore.class );
    mappingHelper = new MappingHelper( mappingMeta );

    when( transMeta.getRepository() ).thenReturn( repository );
    when( transMeta.getBowl() ).thenReturn( defaultBowl );
    when( transMeta.getMetaStore() ).thenReturn( metaStore );
    when( transMeta.environmentSubstitute( anyString() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    when( mappingMeta.getFileName() ).thenReturn( "/path/transFile.ktr" );
    when( mappingMeta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
  }

  @Test
  public void testReferencePath() {
    try ( MockedStatic<MappingMeta> mappingMetaMockedStatic = mockStatic( MappingMeta.class ) ) {
      mappingMetaMockedStatic.when( () -> MappingMeta.loadMappingMeta( defaultBowl, mappingMeta, repository, metaStore, transMeta, false ) )
          .thenReturn( mock( TransMeta.class ) );
      JSONObject response = mappingHelper.stepAction( REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( REFERENCE_PATH ) );
      assertEquals( "/path/transFile.ktr", response.get( REFERENCE_PATH ) );
      assertEquals( true, response.get( IS_VALID_REFERENCE ) );
    }
  }

  @Test
  public void testReferencePath_throwsException() {
    try ( MockedStatic<MappingMeta> mappingMetaMockedStatic = mockStatic( MappingMeta.class ) ) {
      mappingMetaMockedStatic.when( () -> MappingMeta.loadMappingMeta( defaultBowl, mappingMeta, repository, metaStore, transMeta, false ) )
          .thenThrow( new KettleException( "invalid Trans" ) );
      JSONObject response = mappingHelper.stepAction( REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( IS_VALID_REFERENCE ) );
      assertEquals( false, response.get( IS_VALID_REFERENCE ) );
    }
  }
}
