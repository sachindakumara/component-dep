package com.wso2telco.dep.lifecycleextension.notification;

import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

public interface NotificationService {

    void sendApiProviderEmail(RequestContext context);

}
