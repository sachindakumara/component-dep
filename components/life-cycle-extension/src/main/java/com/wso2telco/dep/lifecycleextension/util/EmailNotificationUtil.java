package com.wso2telco.dep.lifecycleextension.util;

public class EmailNotificationUtil {


    public static String getApiProviderEmailContent(String apiName, String receiverName) {

        return "<p>Dear "+receiverName+",</p>" +
                "    <br>" +
                "    <p style='margin-left: pxs'>This is to notify you that we have published and API called "+apiName+".</p>" +
                "    <br>" +
                "    <p>Thanks</p>";

    }
}
