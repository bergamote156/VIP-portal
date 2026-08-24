package fr.insalyon.creatis.vip.api.rest.itest.data;

import fr.insalyon.creatis.grida.common.bean.Operation;
import fr.insalyon.creatis.vip.api.rest.config.BaseRestApiSpringIT;
import fr.insalyon.creatis.vip.api.rest.mockconfig.DataConfigurator;
import fr.insalyon.creatis.vip.datamanager.integrationtest.StorageTestConfigurer;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.insalyon.creatis.vip.api.data.PathTestUtils.getAbsolutePath;
import static fr.insalyon.creatis.vip.api.data.PathTestUtils.testDir1;
import static fr.insalyon.creatis.vip.api.data.UserTestUtils.baseUser2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DataControllerIT extends BaseRestApiSpringIT  {

    @Autowired StorageTestConfigurer utils;

    protected void configureDataFS() throws Exception {
        DataConfigurator.configureFS(utils);
    }

    @Test
    public void shouldUploadFile(@TempDir Path tempDir) throws Exception {
        configureDataFS();
        String path =  getAbsolutePath(testDir1) + "/uploaded.txt";
        utils.configureNonExistingElementForUser(baseUser2, testDir1.getName() + "/uploaded.txt");
        byte[] fileContent = Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("testFile.txt").toURI()));
        String operationId = "testOpId";
        Operation donePoolOperation = new Operation(operationId, null, null, Operation.Type.Upload, baseUser2.getEmail(), null, 100);
        donePoolOperation.setStatus(Operation.Status.Done);
        Operation runningPoolOperation = new Operation(operationId, null, null, Operation.Type.Upload, baseUser2.getEmail(), null, 100);
        runningPoolOperation.setStatus(Operation.Status.Running);

        String remoteDirPath = utils.getRemotePathForUser(baseUser2, testDir1.getName()).toString();
        when (gridaPoolClient.uploadFile(
                anyString(),
                eq(remoteDirPath),
                eq(baseUser2.getEmail())))
                .thenReturn(operationId);
        when (gridaPoolClient.getOperationById(eq(operationId)))
                .thenReturn(runningPoolOperation, runningPoolOperation, donePoolOperation);

        when (server.getDataManagerPath()).thenReturn(tempDir.toString());
        when (server.getCarminApiDownloadRetryInSeconds()).thenReturn(1);
        when (server.getCarminApiDownloadTimeoutInSeconds()).thenReturn(1000);

        mockMvc.perform(
                        put("/rest/path" + path)
                                .content(fileContent).contentType(MediaType.TEXT_PLAIN)
                                .with(baseUser2()))
                .andDo(print())
                .andExpect(status().isCreated());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(gridaPoolClient).uploadFile(
                captor.capture(),
                eq(remoteDirPath),
                eq(baseUser2.getEmail()));
        String copiedFilePath = captor.getValue();
        File expectedFile = getResourceFromClasspath("testFile.txt").getFile();
        assertThat(
                FileUtils.contentEquals(expectedFile, new File(copiedFilePath)),
                Matchers.is(true));
        Assertions.assertTrue(copiedFilePath.startsWith(tempDir.resolve("uploads").toString()));
    }

}
