package org.pentaho.di.trans.step.mqtt;

import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;

import java.util.Map;

public class MQTTConsumerHelper extends BaseStepHelper {
  private final MQTTConsumerMeta mqttConsumerMeta;

  public MQTTConsumerHelper( MQTTConsumerMeta mqttConsumerMeta ) {
    this.mqttConsumerMeta = mqttConsumerMeta;
  }

  /**
   * Handles step-specific actions for MQTT Consumer step.
   */
  @Override
  protected JSONObject handleStepAction(String method, TransMeta transMeta,
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
   * Fetches the reference path of the sub transformation used in the MQTT Consumer step.
   *
   * @return  A JSON object containing:
   * - "referencePath": The full path to the referenced transformation, with environment variables substituted.
   * - "isTransReference": A boolean indicating that this is a transformation reference.
   *
   */
  private JSONObject getReferencePath( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    response.put( REFERENCE_PATH, transMeta.environmentSubstitute( mqttConsumerMeta.getTransformationPath() ) );
    try {
      MQTTConsumerMeta.loadMappingMeta( transMeta.getBowl(), mqttConsumerMeta, transMeta.getRepository(),
          null, transMeta, false );
      response.put( IS_VALID_REFERENCE, true );
    } catch( KettleException kettleException ) {
      response.put( IS_VALID_REFERENCE, false );
    }
    return response;
  }
}
