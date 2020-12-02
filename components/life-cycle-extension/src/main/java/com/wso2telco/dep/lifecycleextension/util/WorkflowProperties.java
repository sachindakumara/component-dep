package com.wso2telco.dep.lifecycleextension.util;

import com.wso2telco.core.dbutils.fileutils.PropertyFileReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class WorkflowProperties {

    private static HashMap<String, String> propertiesMap = null;
    private static final Log log = LogFactory.getLog(WorkflowProperties.class);

    public static Properties loadWorkflowProperties(){
        Properties props;
        props = PropertyFileReader.getFileReader().getProperties(Constants.WORKFLOW_PROPERTIES_FILE);
        return props;
    }

    public static Map<String, String> loadWorkflowPropertiesFromXML() {
        if (propertiesMap == null) {
            try {
                propertiesMap = new HashMap<String, String>();

                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                String carbonHome = System.getProperty("carbon.home");
                String workflowPropertiesFile = carbonHome + "/repository/conf/" + Constants.WORKFLOW_PROPERTIES_XML_FILE;

                Document document = builder.parse(new File(workflowPropertiesFile));
                Element rootElement = document.getDocumentElement();

                NodeList nodeList = rootElement.getElementsByTagName("Property");
                if (nodeList != null && nodeList.getLength() > 0) {
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        String nodeName = node.getAttributes().getNamedItem("name").getNodeValue();
                        if (nodeName.equalsIgnoreCase(Constants.SERVICE_HOST)
                                || nodeName.equalsIgnoreCase(Constants.KEY_WORKFLOW_EMAIL_NOTIFICATION_HOST)
                                || nodeName.equalsIgnoreCase(Constants.KEY_WORKFLOW_EMAIL_NOTIFICATION_FROM_ADDRESS)
                                || nodeName.equalsIgnoreCase(Constants.KEY_WORKFLOW_EMAIL_NOTIFICATION_FROM_PASSWORD)
                                || nodeName.equalsIgnoreCase(Constants.PUBLISHER_ROLE_START_WITH)
                                || nodeName.equalsIgnoreCase(Constants.PUBLISHER_ROLE_END_WITH)
                                || nodeName.equalsIgnoreCase(Constants.MANDATE_SERVICE_HOST)) {
                            String value = ((Element)node).getTextContent();
                            propertiesMap.put(nodeName, value);
                        }
                    }
                }
            } catch (Exception e) {
                String errorMessage = "Error in WorkflowProperties.loadWorkflowPropertiesFromXML";
                log.error(errorMessage, e);
            }
        }
        return propertiesMap;
    }
}
