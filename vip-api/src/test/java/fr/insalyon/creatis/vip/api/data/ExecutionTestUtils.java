package fr.insalyon.creatis.vip.api.data;

import fr.insalyon.creatis.vip.api.model.Execution;
import fr.insalyon.creatis.vip.api.model.ExecutionStatus;
import fr.insalyon.creatis.vip.api.tools.spring.JsonCustomObjectMatcher;
import fr.insalyon.creatis.vip.application.client.view.monitor.WorkflowStatus;
import fr.insalyon.creatis.vip.application.models.InOutData;
import fr.insalyon.creatis.vip.application.models.Workflow;

import org.hamcrest.Matcher;

import java.util.*;
import java.util.function.Function;

import static fr.insalyon.creatis.vip.api.tools.spring.JsonCustomObjectMatcher.jsonCorrespondsTo;

public class ExecutionTestUtils {

    public static final Map<String,Function> executionSuppliers;

    public static final Execution execution1,   execution2;
    public static final Workflow WORKFLOW_1, WORKFLOW_2;
    public static final List<InOutData> simulation1InData, simulation2InData;
    public static final List<InOutData> simulation1OutData, simulation2OutData;

    static {
        // TODO Test with int or float params
        WORKFLOW_1 = new Workflow("execId1", "Exec test 1", "pipelineTest1", "3",
                UserTestUtils.baseUser1.getFullName(), WorkflowStatus.Running.toString(),
                new GregorianCalendar(2016, 9, 2).getTime(), null,
                "engine 1", null);
        execution1 = getExecution(WORKFLOW_1, ExecutionStatus.RUNNING);
        execution1.setInputValuesForDisplay(new HashMap<String,Object>() {{
                                      put("param 1", "value 1");
                                      put("param 2", "42");
                                  }}
        );
        execution1.clearReturnedFiles();

        simulation1InData = Arrays.asList(
                new InOutData("value 1", "param 1", "String"),
                new InOutData("42", "param 2", "Integer"));
        simulation1OutData = Collections.emptyList();

        WORKFLOW_2 = new Workflow("execId2", "Exec test 2", "pipelineTest2", "4.2",
                UserTestUtils.baseUser1.getFullName(), WorkflowStatus.Completed.toString(),
                new GregorianCalendar(2016, 4, 29).getTime(), null,
                 "engine 1", null);
        execution2 = getExecution(WORKFLOW_2, ExecutionStatus.FINISHED);
        execution2.setInputValuesForDisplay(new HashMap<String,Object>() {{
                                      put("param2-1", "5.3");
                                  }}
        );

        execution2.setReturnedFiles(new HashMap<String,List<Object>>() {{
            put("param2-res", Collections.singletonList("/vip/Home/testFile1.xml"));
        }});
        simulation2InData = Collections.singletonList(
                new InOutData("5.3", "param2-1", "Float"));
        simulation2OutData = Collections.singletonList(
                new InOutData("/vip/Home/testFile1.xml", "param2-res", "URI"));

        executionSuppliers = getExecutionSuppliers();
    }

    private static Execution getExecution(Workflow workflow, ExecutionStatus executionStatus) {
        // TODO : startDate should be in seconds
        Object resultsDirectory = null;
        return new Execution(
            workflow.getID(),
            workflow.getWorkflowName(),
            workflow.getApplicationName() + "/" + workflow.getApplicationVersion(),
            0, executionStatus, null, null, workflow.getStartDate().getTime(), null,
            resultsDirectory);
    }

    public static Execution summariseExecution(Execution execution) {
        Execution newExecution = new Execution(
                execution.getIdentifier(),
                execution.getName(),
                execution.getPipelineIdentifier(),
                execution.getTimeout(),
                execution.getStatus(),
                execution.getStudyIdentifier(),
                execution.getErrorCode(),
                execution.getStartDate(),
                execution.getEndDate(),
                execution.getResultsLocation()
        );
        // strip input values (so do not add them);
        return newExecution;
    }

    public static Execution copyExecutionWithNewName(Execution execution, String newName) {
        Execution newExecution = new Execution(
                execution.getIdentifier(),
                newName,
                execution.getPipelineIdentifier(),
                execution.getTimeout(),
                execution.getStatus(),
                execution.getStudyIdentifier(),
                execution.getErrorCode(),
                execution.getStartDate(),
                execution.getEndDate(),
                execution.getResultsLocation()
        );
        // WARNING, do not copy input value and returned files objects
        newExecution.setInputValuesForDisplay(execution.getInputValuesForDisplay());
        newExecution.setReturnedFiles(execution.getReturnedFiles());
        return newExecution;
    }

    public static Workflow copySimulationWithNewName(Workflow simu, String newName) {
        Workflow newWorkflow = new Workflow(
                simu.getID(),
                newName,
                simu.getApplicationName(),
                simu.getApplicationVersion(),
                simu.getUserId(),
                simu.getStatus().toString(),
                simu.getStartDate(),
                simu.getEndDate(),
                simu.getEngineEndpoint(),
                null
        );
        return newWorkflow;
    }

    @SuppressWarnings("unchecked")
    public static Map<String,Function> getExecutionSuppliers() {
        return JsonCustomObjectMatcher.formatSuppliers(
                Arrays.asList(
                        "identifier", "name", "pipelineIdentifier", "timeout",
                        "status", "inputValues", "returnedFiles", "studyIdentifier",
                        "errorCode", "startDate", "endDate", "jobs"),
                Execution::getIdentifier,
                Execution::getName,
                Execution::getPipelineIdentifier,
                Execution::getTimeout,
                execution -> execution.getStatus().getRestLabel(),
                Execution::getInputValuesForDisplay,
                Execution::getReturnedFiles,
                Execution::getStudyIdentifier,
                Execution::getErrorCode,
                Execution::getStartDate,
                Execution::getEndDate,
                Execution::getJobs
        );
    }

    public static Matcher<Map<String,?>> jsonCorrespondsToExecution(Execution execution) {
        Map<Class, Map<String, Function>> suppliersRegistry = new HashMap<>();
        return jsonCorrespondsTo(execution, executionSuppliers, suppliersRegistry);
    }
}
