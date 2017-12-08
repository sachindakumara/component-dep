package org.workflow.core.service;

import com.wso2telco.core.dbutils.exception.BusinessException;
import com.wso2telco.core.dbutils.model.UserProfileDTO;
import com.wso2telco.core.dbutils.util.ApprovalRequest;
import com.wso2telco.core.dbutils.util.AssignRequest;
import com.wso2telco.core.dbutils.util.Callback;
import org.apache.commons.logging.Log;
import org.workflow.core.activity.ActivityClientFactory;
import org.workflow.core.activity.ProcessSearchRequest;
import org.workflow.core.activity.RestClient;
import org.workflow.core.activity.TaskAssignRequest;
import org.workflow.core.execption.WorkflowExtensionException;
import org.workflow.core.model.*;
import org.workflow.core.util.AppVariable;
import org.workflow.core.util.DeploymentTypes;
import org.workflow.core.util.Messages;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class AbsractQueryBuilder implements WorkFlowProcessor {

    protected Log log;
    protected DeploymentTypes depType;
    static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    static final String MONTH_FORMAT = "MMM";


    protected abstract String getProcessDefinitionKey();

    protected abstract DeploymentTypes getDeployementType();

    protected abstract Callback buildMyTaskResponse(final TaskSearchDTO searchDTO, final TaskList taskList,
                                              final UserProfileDTO userProfile) throws BusinessException;

    protected abstract Callback buildAllTaskResponse(final TaskSearchDTO searchDTO, final TaskList taskList,
                                              final UserProfileDTO userProfile) throws BusinessException;

    protected abstract Callback getHistoricalData(String user, List<Range> months, List<String> xAxisLabels) throws BusinessException;

    protected abstract Callback buildApprovalRequest(final ApprovalRequest approvalRequest) throws BusinessException;

    public Callback searchPending(TaskSearchDTO searchDTO, final UserProfileDTO userProfile) throws BusinessException {
        ProcessSearchRequest processRequest = buildSearchRequest(searchDTO, userProfile);
        processRequest.setCandidateGroup("admin");
        TaskList taskList = executeRequest(processRequest);
        return buildMyTaskResponse(searchDTO, taskList, userProfile);

    }

    @Override
    public Callback searchPending(TaskSearchDTO searchDTO, UserProfileDTO userProfile, String assigenee) throws BusinessException {
        ProcessSearchRequest processRequest = buildSearchRequest(searchDTO, userProfile);
        processRequest.setAssignee(assigenee);
        TaskList taskList = executeRequest(processRequest);
        return buildAllTaskResponse(searchDTO, taskList, userProfile);
    }

    public TaskList executeRequest(ProcessSearchRequest processRequest) throws BusinessException {
        RestClient activityClient = ActivityClientFactory.getInstance().getClient(getProcessDefinitionKey());
        TaskList taskList = null;
        try {
            taskList = activityClient.getTasks(processRequest);

            for (Task task : taskList.getData()) {
                TaskVariableResponse[] vars = activityClient.getVariables(String.valueOf(task.getId()));
                task.setVariables(vars);
            }

        } catch (WorkflowExtensionException e) {
            log.error("", e);
            throw new BusinessException(e);
        }

        return taskList;
    }

    @Override
    public ProcessSearchRequest buildSearchRequest(TaskSearchDTO searchDTO, final UserProfileDTO userProfile) throws BusinessException {
        ProcessSearchRequest request = new ProcessSearchRequest();
        request.setSize(searchDTO.getBatchSize());
        request.setStart(searchDTO.getStart());
        request.setSort(searchDTO.getSortBy());
        request.setProcessDefinitionKey(getProcessDefinitionKey());

        String filterStr = searchDTO.getFilterBy();
        /**
         * if the request need to be filtered the string must be formated as
         * filterby:value,filterby2:value
         */
        if (filterStr != null && !filterStr.trim().isEmpty()) {
            /**
             * split the multiple filter criteria by ,
             */
            final String[] filterCritias = filterStr.split(",");
            for (String critira : filterCritias) {
                /**
                 * split the criteria by : to separate out the name and value ,
                 */
                String[] critiraarry = critira.split(":");
                /**
                 * validate name and value. Both should not be null. and filer name should be
                 * defined at the filter map .if not ignore adding.
                 */
                if (critiraarry.length == 2 && !critiraarry[0].trim().isEmpty() && !critiraarry[1].trim().isEmpty()
                        && getFilterMap().containsKey(critiraarry[0].trim())) {
                    /**
                     * add process variable ,
                     *
                     */

                    Variable var = new Variable(getFilterMap().get(critiraarry[0]), critiraarry[1]);
                    request.addProcessVariable(var);
                }
            }
        }

        return request;
    }

    protected Map<String, String> getFilterMap() {
        Map<String, String> filter = new HashMap<String, String>();
        filter.put("name", AppVariable.NAME.key());
        filter.put("applicationname", AppVariable.NAME.key());
        filter.put("appname", AppVariable.NAME.key());
        filter.put("tier", AppVariable.TIER.key());
        filter.put("createdby", AppVariable.USERNAME.key());
        filter.put("owner", AppVariable.USERNAME.key());
        return filter;
    }

    @Override
    public Callback getGraphData(UserProfileDTO userProfile) throws BusinessException {

        DateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        DateFormat monthFormat = new SimpleDateFormat(MONTH_FORMAT, Locale.ENGLISH);

        List<Range> months = new ArrayList();
        List<String> xAxisLabels = new ArrayList();

        for (int i = -5; i < 1; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, i);
            calendar.set(Calendar.DATE, 1);
            calendar.set(Calendar.HOUR, -12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            Date start = calendar.getTime();

            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.DATE, -1);
            calendar.set(Calendar.HOUR, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);

            Date stop = calendar.getTime();

            months.add(new Range().setStart(simpleDateFormat.format(start)).setEnd(simpleDateFormat.format(stop)));
            xAxisLabels.add(monthFormat.format(stop));
        }

        return getHistoricalData(userProfile.getUserName(), months, xAxisLabels);

    }

    @Override
    public Callback approveTask(ApprovalRequest approvalRequest) throws BusinessException {
        return buildApprovalRequest(approvalRequest);
    }

    @Override
    public Callback assignTask(AssignRequest assignRequest) throws BusinessException {
        String assignee = "admin";
        TaskAssignRequest request = new TaskAssignRequest();
        request.setAction("claim");
        request.setAssignee(assignee.toLowerCase());
        RestClient activityClient = ActivityClientFactory.getInstance().getClient(getProcessDefinitionKey());
        try {
            activityClient.assignTask(assignRequest.getTaskId(), request);
            return new Callback().setPayload(null).setSuccess(true).setMessage(Messages.TASK_APPROVAL_SUCCESS.getValue());
        } catch (WorkflowExtensionException e) {
            log.error("", e);
            return new Callback().setPayload(null).setSuccess(false).setMessage(Messages.TASK_APPROVAL_FAILED.getValue());
        }
    }
}
