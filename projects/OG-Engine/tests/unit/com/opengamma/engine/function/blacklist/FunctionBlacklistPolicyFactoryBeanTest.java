/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.engine.function.blacklist.FunctionBlacklistPolicy.Entry;
import com.opengamma.id.UniqueId;

/**
 * Tests the {@link FunctionBlacklistPolicyFactoryBean} class.
 */
@Test
public class FunctionBlacklistPolicyFactoryBeanTest {

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testNoUniqueIdOrName() {
    final FunctionBlacklistPolicyFactoryBean bean = new FunctionBlacklistPolicyFactoryBean();
    bean.afterPropertiesSet();
  }

  public void testNoName() {
    final FunctionBlacklistPolicyFactoryBean bean = new FunctionBlacklistPolicyFactoryBean();
    bean.setUniqueId(UniqueId.of("Test", "Foo"));
    bean.afterPropertiesSet();
    final FunctionBlacklistPolicy policy = bean.getObject();
    assertEquals(policy.getUniqueId(), UniqueId.of("Test", "Foo"));
    assertEquals(policy.getName(), "Foo");
    assertTrue(policy.getEntries().isEmpty());
  }

  public void testNoUniqueId() {
    final FunctionBlacklistPolicyFactoryBean bean = new FunctionBlacklistPolicyFactoryBean();
    bean.setName("Foo");
    bean.afterPropertiesSet();
    final FunctionBlacklistPolicy policy = bean.getObject();
    assertEquals(policy.getUniqueId(), UniqueId.of("com.opengamma.engine.function.blacklist", "Foo"));
    assertEquals(policy.getName(), "Foo");
    assertTrue(policy.getEntries().isEmpty());
  }

  public void testDefaultObjects() {
    final FunctionBlacklistPolicyFactoryBean bean = new FunctionBlacklistPolicyFactoryBean();
    bean.setName("Foo");
    bean.setDefaultEntryActivationPeriod(60);
    bean.setWildcard(true);
    bean.setFunction(false);
    bean.setParameterizedFunction(true);
    bean.setParameterizedFunctionActivationPeriod(1000);
    bean.setPartialNode(false);
    bean.setBuildNode(true);
    bean.setExecutionNode(false);
    bean.afterPropertiesSet();
    final FunctionBlacklistPolicy policy = bean.getObject();
    assertEquals(policy.getDefaultEntryActivationPeriod(), 60);
    final Set<Entry> entries = policy.getEntries();
    assertEquals(entries.size(), 3);
    assertTrue(entries.contains(Entry.WILDCARD));
    assertTrue(entries.contains(Entry.PARAMETERIZED_FUNCTION.activationPeriod(1000)));
    assertTrue(entries.contains(Entry.BUILD_NODE));
  }

  public void testArbitraryEntries() {
    final FunctionBlacklistPolicyFactoryBean bean = new FunctionBlacklistPolicyFactoryBean();
    bean.setName("Foo");
    bean.setWildcard(true);
    bean.setEntries(Arrays.asList(Entry.BUILD_NODE, Entry.PARTIAL_NODE));
    bean.afterPropertiesSet();
    final FunctionBlacklistPolicy policy = bean.getObject();
    final Set<Entry> entries = policy.getEntries();
    assertEquals(entries.size(), 3);
    assertTrue(entries.contains(Entry.WILDCARD));
    assertTrue(entries.contains(Entry.PARTIAL_NODE));
    assertTrue(entries.contains(Entry.BUILD_NODE));
  }

}