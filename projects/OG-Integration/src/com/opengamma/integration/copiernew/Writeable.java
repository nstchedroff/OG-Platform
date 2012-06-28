/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copiernew;

/**
 * Interface for a writer
 */
public interface Writeable<E> {

  E addOrUpdate(E datum);
        
}
