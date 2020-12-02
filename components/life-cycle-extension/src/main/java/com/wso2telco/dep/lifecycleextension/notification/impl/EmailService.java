package com.wso2telco.dep.lifecycleextension.notification.impl;

import com.wso2telco.dep.lifecycleextension.util.Constants;
import com.wso2telco.dep.lifecycleextension.util.WorkflowProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

public class EmailService {

    private static Log log = LogFactory.getLog(EmailService.class);


    public void sendEmail(final String emailAddress,final String subject,final String content) {
        new Thread() {
            @Override
            public void run() {

                Map<String, String> workflowProperties = WorkflowProperties.loadWorkflowPropertiesFromXML();
                String emailHost = workflowProperties.get(Constants.KEY_WORKFLOW_EMAIL_NOTIFICATION_HOST);
                String fromEmailAddress = workflowProperties.get(Constants.KEY_WORKFLOW_EMAIL_NOTIFICATION_FROM_ADDRESS);
                String fromEmailPassword = workflowProperties.get(Constants.KEY_WORKFLOW_EMAIL_NOTIFICATION_FROM_PASSWORD);

                Properties props = System.getProperties();
                props.put("mail.smtp.host", emailHost);
                props.put("mail.smtp.user", fromEmailAddress);
                props.put("mail.smtp.password", fromEmailPassword);
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.auth", "true");

                try {
                    Session session = Session.getDefaultInstance(props, null);
                    InternetAddress toAddress = new InternetAddress(emailAddress);

                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(fromEmailAddress));
                    message.addRecipient(Message.RecipientType.TO, toAddress);
                    message.setSubject(subject);
                    message.setContent(content, "text/html; charset=UTF-8");

                    Transport transport = session.getTransport("smtp");
                    transport.connect(emailHost, fromEmailAddress, fromEmailPassword);
                    transport.sendMessage(message, message.getAllRecipients());
                    transport.close();

                } catch (Exception e) {
                    log.error("Email sending failed. ", e);
                }
            }
        }.start();
    }
}
