package com.wso2telco.dep.lifecycleextension.userstore.impl;

import com.wso2telco.dep.lifecycleextension.userstore.RemoteUserStoreManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class RemoteUserStoreManagerImpl implements RemoteUserStoreManager{

    private static Log log = LogFactory.getLog(RemoteUserStoreManagerImpl.class);

    @Override
    public String[] getUserListOfRole(String role) {
        String[] userList = null;

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            log.info("Getting users for the role : " + role);
            UserStoreManager userstoremanager = privilegedCarbonContext.getUserRealm().getUserStoreManager();
            userList = userstoremanager.getUserListOfRole(role);


        } catch (Exception ex){
            log.error(ex);
            throw new RuntimeException(ex);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        return userList;
    }

    @Override
    public String getUserClaimValue(String userName, String claim) {

        String claimValue = "";
        log.info("Getting claim value for the userName : " + userName + " and claim : " + claim);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            UserStoreManager userstoremanager = privilegedCarbonContext.getUserRealm().getUserStoreManager();
            claimValue = userstoremanager.getUserClaimValue(userName, claim, null);
            log.info("Claim value : " + claimValue);

        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        return claimValue;
    }
}
