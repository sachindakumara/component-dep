package com.wso2telco.dep.lifecycleextension.notification.impl;

import com.wso2telco.dep.lifecycleextension.notification.NotificationService;
import com.wso2telco.dep.lifecycleextension.userstore.RemoteUserStoreManager;
import com.wso2telco.dep.lifecycleextension.userstore.impl.RemoteUserStoreManagerImpl;
import com.wso2telco.dep.lifecycleextension.util.Constants;
import com.wso2telco.dep.lifecycleextension.util.EmailNotificationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.Arrays;
import java.util.List;


public class NotificationServiceImpl implements NotificationService{

    private static Log log = LogFactory.getLog(NotificationServiceImpl.class);

    private RemoteUserStoreManager remoteUserStoreManager;

    private EmailService emailService;

    public NotificationServiceImpl(){
        remoteUserStoreManager = new RemoteUserStoreManagerImpl();
        emailService = new EmailService();
    }

    @Override
    public void sendApiProviderEmail(RequestContext context) {


        try {


        GenericArtifactManager artifactManager = APIUtil.getArtifactManager(context.getSystemRegistry(),
                    APIConstants.API_KEY);
        String artifactId = context.getResource().getUUID();
        GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
        API api = APIUtil.getAPI(apiArtifact);

        List<String> userList = Arrays.asList(remoteUserStoreManager.getUserListOfRole(Constants.ADMIN_ROLE));

        if(userList.contains(api.getId().getProviderName())){

            String subject = Constants.SUBJECT_API_PROVIDER_EMAIL;

            if(userList != null && userList.size() > 0){
                for (String user : userList) {

                    String content = EmailNotificationUtil.getApiProviderEmailContent(api.getId().getApiName(),
                            remoteUserStoreManager.getUserClaimValue(user,
                                    Constants.CLAIM_GIVEN_NAME));

                    emailService.sendEmail(remoteUserStoreManager.getUserClaimValue(user,
                            Constants.CLAIM_EMAIL),subject,content);
                }
            }else{
                log.info("User list is either null or empty. [ userList : " + userList + " ]");
            }
        }

        }catch (Exception e){
            log.error("Failed to validate user details for send email ",e);
        }
    }
}
