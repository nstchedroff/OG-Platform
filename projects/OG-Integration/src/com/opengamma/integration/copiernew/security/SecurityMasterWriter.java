package com.opengamma.integration.copiernew.security;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.copiernew.Writeable;
import com.opengamma.master.security.*;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.beancompare.BeanCompare;
import com.opengamma.util.beancompare.BeanDifference;

import javax.time.calendar.ZonedDateTime;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 6/25/12
 * Time: 2:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class SecurityMasterWriter implements Writeable<ManageableSecurity> {

  SecurityMaster _securityMaster;
  private BeanCompare _beanCompare;

  public SecurityMasterWriter(SecurityMaster securityMaster) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _securityMaster = securityMaster;
    _beanCompare = new BeanCompare();
  }

  @Override
  public ManageableSecurity addOrUpdate(ManageableSecurity security) {
    ArgumentChecker.notNull(security, "security");

    SecuritySearchRequest searchReq = new SecuritySearchRequest();
    ExternalIdSearch idSearch = new ExternalIdSearch(security.getExternalIdBundle());  // match any one of the IDs
    searchReq.setVersionCorrection(VersionCorrection.ofVersionAsOf(ZonedDateTime.now())); // valid now
    searchReq.setExternalIdSearch(idSearch);
    searchReq.setFullDetail(true);
    searchReq.setSortOrder(SecuritySearchSortOrder.VERSION_FROM_INSTANT_DESC);
    SecuritySearchResult searchResult = _securityMaster.search(searchReq);
    ManageableSecurity foundSecurity = searchResult.getFirstSecurity();
    if (foundSecurity != null) {
      List<BeanDifference<?>> differences;
      try {
        differences = _beanCompare.compare(foundSecurity, security);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Error comparing securities with ID bundle " + security.getExternalIdBundle(), e);
      }
      if (differences.size() == 1 && differences.get(0).getProperty().propertyType() == UniqueId.class) {
        // It's already there, don't update or add it
        return foundSecurity;
      } else {
        SecurityDocument updateDoc = new SecurityDocument(security);
        updateDoc.setUniqueId(foundSecurity.getUniqueId());
        SecurityDocument result = _securityMaster.update(updateDoc);
        return result.getSecurity();
      }
    } else {
      // Not found, so add it
      SecurityDocument addDoc = new SecurityDocument(security);
      SecurityDocument result = _securityMaster.add(addDoc);
      return result.getSecurity();
    }
  }
}
