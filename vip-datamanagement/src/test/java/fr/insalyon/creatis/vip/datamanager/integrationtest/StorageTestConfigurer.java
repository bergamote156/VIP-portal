package fr.insalyon.creatis.vip.datamanager.integrationtest;

import fr.insalyon.creatis.grida.client.GRIDAClient;
import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.grida.client.GRIDAPoolClient;
import fr.insalyon.creatis.grida.common.bean.GridData;
import fr.insalyon.creatis.grida.common.bean.GridPathInfo;
import fr.insalyon.creatis.grida.common.bean.Operation;
import fr.insalyon.creatis.vip.core.integrationtest.ServerMockConfig;
import fr.insalyon.creatis.vip.core.integrationtest.TestConfigurer;
import fr.insalyon.creatis.vip.core.models.Group;
import fr.insalyon.creatis.vip.core.models.User;
import fr.insalyon.creatis.vip.datamanager.client.DataManagerConstants;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultMatcher;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Component
public class StorageTestConfigurer {

    @Autowired GRIDAClient gridaClient;
    @Autowired GRIDAPoolClient gridaPoolClient;

    // ################## GENERAL stuff ################

    public Path getRemotePathForUser(User user, String elementPath) {
        return Path.of(ServerMockConfig.TEST_USERS_ROOT,  user.getFolder(), elementPath);
    }

    // ################## FOLDER CONFIG ##################

    // configure the grida client mock to answer for getPathInfo and getFolderData on a specific path inside a user folder
    // elements must begin with "folder" or "file"
    public void configureFolderForUser(User user, String folder, String... elements) throws Exception {
        Path path = Path.of(ServerMockConfig.TEST_USERS_ROOT,  user.getFolder(), folder);
        configureFolder(path.toAbsolutePath().toString(), elements);
    }

    // configure the grida client mock to answer for getPathInfo and getFolderData on a specific path inside a group folder
    // elements must begin with "folder" or "file"
    public void configureFolderInGroup(Group group, String folder, String... elements) throws Exception {
        Path path = Path.of(ServerMockConfig.TEST_GROUP_ROOT, group.getName(), folder);
        configureFolder(path.toAbsolutePath().toString(), elements);
    }

    // configure the grida client mock to answer for getPathInfo and getFolderData on a specific path
    // elements must begin with "folder" or "file"
    public void configureFolder(String path, String... elements) throws Exception {

        Mockito.when(gridaClient.getPathInfo(path))
                .thenReturn(new GridPathInfo(true, GridData.Type.Folder))
                .thenThrow(new RuntimeException("Should not be called another time"));

        if (elements == null || (elements.length == 1 && elements[0] == null)) {
            // should not configure elements
            return;
        }

        if ( ! Arrays.stream(elements).allMatch(e -> e.startsWith("file") || e.startsWith("folder"))) {
            throw new RuntimeException("Bad configurations : elements must begin with 'file' or 'folder'");
        }

        Mockito.when(gridaClient.getFolderData(path, true))
                .thenReturn(Arrays.stream(elements).map(e -> new GridData(e, e.startsWith("folder") ? GridData.Type.Folder : GridData.Type.File, "")).collect(Collectors.toList()))
                .thenThrow(new RuntimeException("Should not be called another time"));
    }

    // ############### FILE CONFIG ##################

    public void configureFileForUser(User user, String filePath) throws Exception {
        Path path = Path.of(ServerMockConfig.TEST_USERS_ROOT, user.getFolder(), filePath);
        configureFile(path.toAbsolutePath().toString());
    }

    public void configureFileForGroup(Group group, String filePath) throws Exception {
        Path path = Path.of(ServerMockConfig.TEST_GROUP_ROOT, group.getName(), filePath);
        configureFile(path.toAbsolutePath().toString());
    }

    public void configureFile(String path) throws Exception {
        Mockito.when(gridaClient.getPathInfo(path))
                .thenReturn(new GridPathInfo(true, GridData.Type.File))
                .thenThrow(new RuntimeException("Should not be called another time"));
    }

    // ############# NON-EXISTING STUFF CONFIG ################

    public void configureNonExistingElementForUser(User user, String elementPath) throws Exception {
        Path path = Path.of(ServerMockConfig.TEST_USERS_ROOT,  user.getFolder(), elementPath);
        configureNonExistingElement(path.toAbsolutePath().toString());
    }

    public void configureNonExistingElementForGroup(Group group, String elementPath) throws Exception {
        Path path = Path.of(ServerMockConfig.TEST_GROUP_ROOT, group.getName(), elementPath);
        configureNonExistingElement(path.toAbsolutePath().toString());
    }

    public void configureNonExistingElement(String path) throws GRIDAClientException {
        Mockito.when(gridaClient.getPathInfo(path))
                .thenReturn(new GridPathInfo(false, null))
                .thenThrow(new RuntimeException("Should not be called another time"));
    }

    // ################# Operations ###########################

    public void configureOperationForElementInHome(User user, String elementPath, String operationId) throws GRIDAClientException {
        Path remotePath = Path.of(ServerMockConfig.TEST_USERS_ROOT,  user.getFolder(), elementPath);
        configureOperationForPath(user, remotePath, operationId);
    }

    public void configureOperationForElementInGroup(User user, Group group, String elementPath, String operationId) throws GRIDAClientException {
        Path remotePath = Path.of(ServerMockConfig.TEST_GROUP_ROOT, group.getName(), elementPath);
        configureOperationForPath(user, remotePath, operationId);
    }

    public void configureOperationForPath(User user, Path path, String operationId) throws GRIDAClientException {
        Path localPath = Path.of(ServerMockConfig.TEST_GRIDA_STORAGE_PATH, DataManagerConstants.DOWNLOAD_FOLDER, path.getParent().toString());
        Mockito.when(gridaPoolClient.downloadFile(path.toString(),
                        localPath.toString(),
                        user.getEmail()))
                .thenReturn(operationId)
                .thenThrow(new RuntimeException("Should not be called another time"));
    }

    public void configureOperation(User user, String operationId, Operation.Status operationStatus) throws GRIDAClientException {
        Operation operation = new Operation(
                operationId,
                "",
                "",
                Operation.Type.Download,
                user.getEmail(),
                "",
                0);
        operation.setStatus(operationStatus);
        Mockito.when(gridaPoolClient.getOperationById(operationId))
                .thenReturn(operation)
                .thenThrow(new RuntimeException("Should not be called another time"));
    }

    public void configureOperationWithContent(User user, String operationId, Path localPath) throws GRIDAClientException {
        Operation operation = new Operation(
                operationId,
                "",
                localPath.toString(),
                Operation.Type.Download,
                user.getEmail(),
                "",
                0);
        operation.setStatus(Operation.Status.Done);
        Mockito.when(gridaPoolClient.getOperationById(operationId))
                .thenReturn(operation)
                .thenThrow(new RuntimeException("Should not be called another time"));
    }

    // #################### VERIFICATION FOR CREATE FOLDER ################

    public void verifyCreateFolderForUser(User user, String folderPath, Integer times) throws GRIDAClientException {
        Path path = Path.of(ServerMockConfig.TEST_USERS_ROOT, user.getFolder(), folderPath);
        Mockito.verify(gridaClient, Mockito.times(times)).createFolder(path.getParent().toString(), path.getFileName().toString());
    }

    public void verifyCreateFolderForGroup(Group group, String folderPath, Integer times) throws GRIDAClientException {
        Path path = Path.of(ServerMockConfig.TEST_GROUP_ROOT, group.getName(), folderPath);
        Mockito.verify(gridaClient, Mockito.times(times)).createFolder(path.getParent().toString(), path.getFileName().toString());
    }

    public void verifyNoCreateFolder() throws GRIDAClientException {
        Mockito.verify(gridaClient, Mockito.never()).createFolder(Mockito.anyString(), Mockito.anyString());
    }

    // #################### VERIFICATION FOR DELETE ################

    public void verifyDeleteForUser(User user, String pathToDelete, Integer times) throws GRIDAClientException {
        Path path = Path.of(ServerMockConfig.TEST_USERS_ROOT, user.getFolder(), pathToDelete);
        Mockito.verify(gridaPoolClient, Mockito.times(times)).delete(path.toString(), user.getEmail());
    }

    public void verifyDeleteForGroup(Group group, User user, String pathToDelete, Integer times) throws GRIDAClientException {
        Path path = Path.of(ServerMockConfig.TEST_GROUP_ROOT, group.getName(), pathToDelete);
        Mockito.verify(gridaPoolClient, Mockito.times(times)).delete(path.toString(), user.getEmail());
    }

    public void verifyNoDelete() throws GRIDAClientException {
        Mockito.verify(gridaPoolClient, Mockito.never()).delete(Mockito.anyString(), Mockito.anyString());
    }

    // ###################### MATCHERS ###################

    // get the JSON matcher for the Data type
    public ResultMatcher[] getDatasMatcher(String... elements) {
        List<ResultMatcher> matchers = new ArrayList<>();
        matchers.add(jsonPath("$.length()").value(elements.length));
        for (int i=0; i<elements.length; i++) {
            matchers.add(jsonPath("$["+i+"].name").value(elements[i]));
            matchers.add(jsonPath("$["+i+"].type").value(elements[i].startsWith("file") ? "file" : "folder"));
        }
        return matchers.toArray(new ResultMatcher[0]);
    }

    // JSON Matcher for the Operation type

    public ResultMatcher[] getOperationMatcher(String operationId, String status) {
        List<ResultMatcher> matchers = new ArrayList<>();
        matchers.add(jsonPath("$.operationId").value(operationId));
        matchers.add(jsonPath("$.status").value(status));
        return matchers.toArray(new ResultMatcher[0]);
    }
}
