/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.position.ManageablePositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for all nodes in a portfolio.
 */
@Path("/portfolios/{portfolioUid}/nodes")
public class PortfolioNodesResource {

  /**
   * The portfolio resource.
   */
  private final PortfolioResource _portfolioResource;

  /**
   * Creates the resource.
   * @param portfolioResource  the parent resource, not null
   */
  public PortfolioNodesResource(final PortfolioResource portfolioResource) {
    ArgumentChecker.notNull(portfolioResource, "PortfolioResource");
    _portfolioResource = portfolioResource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio resource.
   * @return the portfolio resource, not null
   */
  public PortfolioResource getPortfolioResource() {
    return _portfolioResource;
  }

  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public ManageablePositionMaster getPositionMaster() {
    return getPortfolioResource().getPositionMaster();
  }

  /**
   * Gets the URI info.
   * @return the URI info, not null
   */
  public UriInfo getUriInfo() {
    return getPortfolioResource().getUriInfo();
  }

  //-------------------------------------------------------------------------
  @Path("{nodeUid}")
  public PortfolioNodeResource findNode(@PathParam("nodeUid") String uidStr) {
    UniqueIdentifier uid = UniqueIdentifier.parse(uidStr);
    return new PortfolioNodeResource(this, uid);
  }

}
