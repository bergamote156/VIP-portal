package fr.insalyon.creatis.vip.application.models;

import fr.insalyon.creatis.vip.core.models.User;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Used for the CARMIN API where inputs can be a list of input maps
 */
public class CarminWorkflow extends Workflow {

    private List<Map<String,WorkflowInput>> inputsMapsList;

    public CarminWorkflow(String workflowName, String applicationName, String applicationVersion) {
        super(workflowName, applicationName, applicationVersion);
    }

    public CarminWorkflow(String id, String workflowName, String applicationName, String applicationVersion,
                          User user, String status, Date startDate, Date endDate,
                          String engineEndpoint, String tags) {
        super(id, workflowName, applicationName, applicationVersion, user, status, startDate, endDate, engineEndpoint, tags);
    }

    public List<Map<String, WorkflowInput>> getInputsMapsList() {
        return inputsMapsList;
    }

    public void setInputsMapsList(List<Map<String, WorkflowInput>> inputsMapsList) {
        this.inputsMapsList = inputsMapsList;
    }
}
