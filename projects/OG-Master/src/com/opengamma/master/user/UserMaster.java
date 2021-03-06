/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.master.AbstractMaster;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose user master.
 * <p>
 * The user master provides a uniform view over a set of user definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
@PublicSPI
public interface UserMaster extends AbstractMaster<UserDocument>, ChangeProvider {

  /**
   * Searches for users matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  UserSearchResult search(UserSearchRequest request);
  // TODO kirk 2012-08-20 -- History will be added when the basics work.
}
