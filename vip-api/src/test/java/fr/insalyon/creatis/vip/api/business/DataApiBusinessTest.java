package fr.insalyon.creatis.vip.api.business;

import static fr.insalyon.creatis.vip.api.data.UserTestUtils.baseUser2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insalyon.creatis.vip.api.model.UploadData;
import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.server.business.Server;
import fr.insalyon.creatis.vip.datamanager.server.business.LFCPermissionBusiness;
import fr.insalyon.creatis.vip.datamanager.server.business.StorageBusiness;

public class DataApiBusinessTest {

    @Test
    public void testBase64Decoder(@TempDir Path tempDir) throws IOException, VipException {

        // Prepare
        LFCPermissionBusiness lfcPermissionBusiness = Mockito.mock(LFCPermissionBusiness.class);
        StorageBusiness storageBusiness = Mockito.mock(StorageBusiness.class);
        Server server = Mockito.mock(Server.class);

        String lfcParentPath = "/vip/Home";
        String lfcPath = lfcParentPath + "/test_uploaded.txt";
        File uploadDataFile = new ClassPathResource("jsonObjects/uploadData_1.json").getFile();
        UploadData uploadData = new ObjectMapper().readValue(uploadDataFile, UploadData.class);

        // Configure
        when(lfcPermissionBusiness.isLFCPathAllowed(baseUser2, lfcPath, LFCPermissionBusiness.LFCAccessType.UPLOAD, true)).thenReturn(true);
        when(storageBusiness.doesPathExist(lfcParentPath)).thenReturn(true);

        // Doing it
        DataApiBusiness sut = new DataApiBusiness(storageBusiness);
        sut.setServer(server);
        sut.setUserSupplier(() -> baseUser2);
        sut.uploadCustomData(lfcPath, uploadData);

        // Verify
        verify(storageBusiness).uploadBase64File(eq(lfcPath), eq(uploadData.getBase64Content()));
        assertThat(uploadData.getBase64Content().isEmpty(), is(false));
    }
}
