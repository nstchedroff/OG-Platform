/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static com.opengamma.financial.InMemoryRegionRepository.REGIONS_FILE_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.InMemoryRegionRepository;
import com.opengamma.financial.RegionRepository;

/**
 * Base class for testing OG-Financial objects to and from Fudge messages.
 */
public class FinancialTestBase {

  private static final Logger s_logger = LoggerFactory.getLogger(FinancialTestBase.class);

  private RegionRepository _regionRepository;
  private FudgeContext _fudgeContext;

  @Before
  public void createFudgeContext() {
    _fudgeContext = new FudgeContext();
    final FinancialFudgeContextConfiguration fudgeConfiguration = new FinancialFudgeContextConfiguration();
    _regionRepository = new InMemoryRegionRepository(new File(REGIONS_FILE_PATH));
    fudgeConfiguration.setRegionRepository(_regionRepository);
    _fudgeContext.setConfiguration(fudgeConfiguration);
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected RegionRepository getRegionRepository() {
    return _regionRepository;
  }

  private FudgeFieldContainer cycleMessage(final FudgeFieldContainer message) {
    final byte[] data = getFudgeContext().toByteArray(message);
    s_logger.info("{} bytes", data.length);
    return getFudgeContext().deserialize(data).getMessage();
  }

  protected <T> T cycleObject(final Class<T> clazz, final T object) {
    s_logger.info("object {}", object);
    final FudgeSerializationContext fudgeSerializationContext = new FudgeSerializationContext(getFudgeContext());
    final FudgeDeserializationContext fudgeDeserializationContext = new FudgeDeserializationContext(getFudgeContext());
    final MutableFudgeFieldContainer messageIn = fudgeSerializationContext.newMessage();
    fudgeSerializationContext.objectToFudgeMsg(messageIn, "test", null, object);
    s_logger.info("message {}", messageIn);
    final FudgeFieldContainer messageOut = cycleMessage(messageIn);
    s_logger.info("message {}", messageOut);
    final T newObject = fudgeDeserializationContext.fieldValueToObject(clazz, messageOut.getByName("test"));
    assertNotNull(newObject);
    s_logger.info("object {}", newObject);
    assertTrue(clazz.isAssignableFrom(newObject.getClass()));
    assertEquals(object.getClass(), newObject.getClass());
    return newObject;
  }

}
