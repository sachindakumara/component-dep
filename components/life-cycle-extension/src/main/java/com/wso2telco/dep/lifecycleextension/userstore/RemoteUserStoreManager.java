package com.wso2telco.dep.lifecycleextension.userstore;

public interface RemoteUserStoreManager {

    String[] getUserListOfRole(String role) ;
    String getUserClaimValue(String userName, String claim);
}
