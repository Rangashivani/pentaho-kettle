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


package org.pentaho.di.core.plugins;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation when applied to a PluginTypeInterface instance describes an ordered list of categories. The
 * categories are internationalized when applied using the Messages located in the package where i18nPackageClass is
 * located.
 *
 * @author nbaker
 *
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface PluginTypeCategoriesOrder {

  String[] getNaturalCategoriesOrder() default "";

  Class<?> i18nPackageClass();
}
