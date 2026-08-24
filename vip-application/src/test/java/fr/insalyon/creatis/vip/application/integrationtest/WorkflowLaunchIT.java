package fr.insalyon.creatis.vip.application.integrationtest;

import fr.insalyon.creatis.vip.application.client.view.ApplicationError;
import fr.insalyon.creatis.vip.application.client.view.monitor.WorkflowStatus;
import fr.insalyon.creatis.vip.application.models.CarminWorkflow;
import fr.insalyon.creatis.vip.application.models.Workflow;
import fr.insalyon.creatis.vip.application.models.WorkflowInput;
import fr.insalyon.creatis.vip.core.client.DefaultError;
import fr.insalyon.creatis.vip.core.client.VipError;
import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.integrationtest.WithMockAdmin;
import fr.insalyon.creatis.vip.core.models.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class WorkflowLaunchIT extends BaseApplicationSpringIT {

    private Workflow getBasicGoodWorkflow() {
        return new Workflow(
                "Test-workflow-name",
                "test-app-name",
                "test-app-version");
    }

    @Test
    @Disabled
    public void launchOk() throws Exception {
        AtomicReference<User> user = new AtomicReference<>();
        asAdminContext(() -> {
            String boutiques = getResourceFromClasspath("FreeSurfer-Recon-all_v731.json").getContentAsString(StandardCharsets.UTF_8);
            applicationTestConfigurer.configureAnAppVersion("test-app-name", "test-app-version", boutiques, "test-group");
            user.set(createUserInGroup(emailUser1, "test-group"));
        });
        Workflow workflow = getBasicGoodWorkflow();
        Map<String, WorkflowInput> inputs = new HashMap<>();
        inputs.put("results-directory", WorkflowInput.ofValue("/some/where"));
        workflow.setInputs(inputs);
        withUserContext(user.get(), () -> {
            workflowLaunchBusiness.launch(workflow);
        });
        // verify web service call
        // verify return workflows
        // verify database
    }

    @Test
    @WithMockAdmin
    public void testMalformedWorkflowsInputs() throws Exception {
        // if workflow ok, then we do as if there are too many workflows already running
        Mockito.when(workflowDAO.getNumberOfRunning(Mockito.anyString())).thenReturn(100L);

        // by default, missing inputs
        Workflow workflow = getBasicGoodWorkflow();
        assertVipException(DefaultError.BAD_INPUT_FIELD, () -> workflowLaunchBusiness.launch(workflow));
        // then missing results-directory
        Map<String, WorkflowInput> inputs = new HashMap<>();
        workflow.setInputs(inputs);
        assertVipException(ApplicationError.INVALID_WORKFLOW_INPUT, () -> workflowLaunchBusiness.launch(workflow));
        // ok if results-directory
        inputs.put("results-directory", WorkflowInput.ofValue("/some/where"));
        assertVipException(ApplicationError.USER_MAX_EXECS, () -> workflowLaunchBusiness.launch(workflow));

        // with carmin workflows
        CarminWorkflow carminWorkflow = new CarminWorkflow(workflow.getWorkflowName(), workflow.getApplicationName(), workflow.getApplicationVersion());
        // missing inputs
        assertVipException(DefaultError.BAD_INPUT_FIELD, () -> workflowLaunchBusiness.launch(carminWorkflow));
        // then empty
        List<Map<String, WorkflowInput>> inputsList = new ArrayList<>();
        carminWorkflow.setInputsMapsList(inputsList);
        assertVipException(DefaultError.BAD_INPUT_FIELD, () -> workflowLaunchBusiness.launch(carminWorkflow));
        // then missing res-directory
        inputsList.add(new HashMap<>());
        inputsList.add(new HashMap<>());
        inputsList.getFirst().put("results-directory", WorkflowInput.ofValue("/some/where"));
        assertVipException(ApplicationError.INVALID_WORKFLOW_INPUT, () -> workflowLaunchBusiness.launch(carminWorkflow));
        // ok if results-directory
        inputsList.getLast().put("results-directory", WorkflowInput.ofValue("/some/where"));
        assertVipException(ApplicationError.USER_MAX_EXECS, () -> workflowLaunchBusiness.launch(carminWorkflow));


        // id / userId / status / wk fame / app / app version
    }

    @Test
    @WithMockAdmin
    public void testMalformedWorkflows() throws Exception {
        // if workflow ok, then we do as if there are too many workflows already running
        Mockito.when(workflowDAO.getNumberOfRunning(Mockito.anyString())).thenReturn(100L);

        // make it work
        Workflow workflow = getBasicGoodWorkflow();
        Map<String, WorkflowInput> inputs = new HashMap<>();
        workflow.setInputs(inputs);
        inputs.put("results-directory", WorkflowInput.ofValue("/some/where"));
        assertVipException(ApplicationError.USER_MAX_EXECS, () -> workflowLaunchBusiness.launch(workflow));

        // wk fame / app / app version
        workflow.setId("test-id");
        assertVipException(DefaultError.BAD_INPUT_FIELD, () -> workflowLaunchBusiness.launch(workflow));
        workflow.setId(null);
        // userId
        workflow.setUser(new User("id", "", ""));
        assertVipException(DefaultError.BAD_INPUT_FIELD, () -> workflowLaunchBusiness.launch(workflow));
        workflow.setUser(null);
        // status
        workflow.setStatus(WorkflowStatus.Running);
        assertVipException(DefaultError.BAD_INPUT_FIELD, () -> workflowLaunchBusiness.launch(workflow));
        workflow.setStatus(null);
        // workflow name
        workflow.setWorkflowName(null);
        assertVipException(DefaultError.BAD_INPUT_FIELD, () -> workflowLaunchBusiness.launch(workflow));
        workflow.setWorkflowName("test-worfklow-name");
        // app name
        workflow.setApplicationName(null);
        assertVipException(DefaultError.BAD_INPUT_FIELD, () -> workflowLaunchBusiness.launch(workflow));
        workflow.setApplicationName("test-app-name");
        // app version
        workflow.setApplicationVersion(null);
        assertVipException(DefaultError.BAD_INPUT_FIELD, () -> workflowLaunchBusiness.launch(workflow));
    }

    private void assertVipException(VipError vipError, Executable executable) {
        VipException vipException = Assertions.assertThrows(VipException.class, executable);
        Assertions.assertEquals(vipError, vipException.getVipError());
    }
}
