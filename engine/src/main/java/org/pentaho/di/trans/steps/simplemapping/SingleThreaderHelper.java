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
package org.pentaho.di.trans.steps.simplemapping;

import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.steps.singlethreader.SingleThreaderMeta;

import java.util.Map;

public class SingleThreaderHelper extends BaseStepHelper {

  private final SingleThreaderMeta singleThreaderMeta;

  public SingleThreaderHelper( SingleThreaderMeta singleThreaderMeta ) {
    this.singleThreaderMeta = singleThreaderMeta;
  }

  /**
   * Handles step-specific actions for Single Threader step.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta,
                                        Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( method.equalsIgnoreCase( REFERENCE_PATH ) ) {
      response = getReferencePath( transMeta );
    } else {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    }

    return response;
  }

  /**
   * Fetches the reference path of the sub transformation used in the Single Threader Step.
   *
   * @return  A JSON object containing:
   * - "referencePath": The full path to the referenced transformation, with environment variables substituted.
   * - "isTransReference": A boolean indicating that this is a transformation reference.
   *
   */
  private JSONObject getReferencePath( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    response.put( REFERENCE_PATH, transMeta.environmentSubstitute( singleThreaderMeta.getFileName() ) );
    try {
      SingleThreaderMeta.loadMappingMeta( transMeta.getBowl(), singleThreaderMeta, transMeta.getRepository(),
          null, transMeta, false );
      response.put( IS_VALID_REFERENCE, true );
    } catch( KettleException kettleException ) {
      response.put( IS_VALID_REFERENCE, false );
    }
    return response;
  }
}
