package com.wso2telco.dep.lifecycleextension;

import com.wso2telco.dep.lifecycleextension.notification.NotificationService;
import com.wso2telco.dep.lifecycleextension.notification.impl.NotificationServiceImpl;
import com.wso2telco.dep.lifecycleextension.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.executors.APIExecutor;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;


public class APICustomExecutor extends APIExecutor {

    private NotificationService notificationService;

    public APICustomExecutor(){
        notificationService=new NotificationServiceImpl();
    }

    @Override
    public boolean execute(RequestContext context, String currentState, String targetState) {

        System.out.println("work 1");
            if(super.execute(context, currentState, targetState)){
                System.out.println("work 2");
                if(currentState.equalsIgnoreCase(Constants.STATE_CREATED)){
                    System.out.println("work 3");
                    notificationService.sendApiProviderEmail(context);
                    return true;
                }
                return true;
            }
            return false;
    }
}
