package fr.insalyon.creatis.vip.datamanager.integrationtest;

import fr.insalyon.creatis.vip.core.integrationtest.BaseInternalApiSpringIT;
import fr.insalyon.creatis.vip.core.models.Group;
import fr.insalyon.creatis.vip.core.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DirectoryListIT extends BaseInternalApiSpringIT {

    /**
     * when listing a directory.
     * Needs :
     * - a pathinfo on the path
     * - a listing on the path
     */

    private User basicUser;
    private Group groupTest1;

    @Autowired StorageTestConfigurer utils;

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        asAdminContext(() -> {
            groupTest1 = createGroup("groupTest1");
            createGroup("groupTest2");
            basicUser = createUserInGroup(emailUser1, groupTest1.getName());
        });
    }
    // Listing on Root
    @Test
    public void testDirectoryListingForRoot() throws Exception {
        mockMvc.perform(get("/internal/storage/directories/vip")
                        .with(getUserSecurityMock(basicUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Home"))
                .andExpect(jsonPath("$[0].type").value("folder"))
                .andExpect(jsonPath("$[1].name").value("groupTest1 (group)"))
                .andExpect(jsonPath("$[1].type").value("folder"));
    }

    // OK cases in Home
    @Test
    public void testDirectoryListingInHome() throws Exception {
        String[] elements = {"fileTest1", "folderTest1", "fileTest2"};
        utils.configureFolderForUser(basicUser, "", elements);

        mockMvc.perform(get("/internal/storage/directories/vip/Home")
                        .with(getUserSecurityMock(basicUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpectAll(utils.getDatasMatcher(elements));

        Mockito.reset(gridaClient);
        utils.configureFolderForUser(basicUser, "path/to/user/dir", elements);

        mockMvc.perform(get("/internal/storage/directories/vip/Home/path/to/user/dir")
                        .with(getUserSecurityMock(basicUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpectAll(utils.getDatasMatcher(elements));
    }

    // Ok cases in group
    @Test
    public void testDirectoryListingInGroup() throws Exception {
        String[] elements = {"fileTest1", "folderTest1", "fileTest2"};
        utils.configureFolderInGroup(groupTest1, "", elements);

        mockMvc.perform(get("/internal/storage/directories/vip/groupTest1 (group)")
                        .with(getUserSecurityMock(basicUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpectAll(utils.getDatasMatcher(elements));

        Mockito.reset(gridaClient);
        utils.configureFolderInGroup(groupTest1, "path/to/group/dir", "fileTest1", "folderTest1", "fileTest2");

        mockMvc.perform(get("/internal/storage/directories/vip/groupTest1 (group)/path/to/group/dir")
                        .with(getUserSecurityMock(basicUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpectAll(utils.getDatasMatcher(elements));
    }

    // In unauthorized group
    @Test
    public void testDirectoryListingInWrongGroup() throws Exception {
        mockMvc.perform(get("/internal/storage/directories/vip/groupTest2 (group)")
                        .with(getUserSecurityMock(basicUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(4001));
    }

    // Listing admin-only stuff
    @Test
    public void testAdminOnlyDirectoryListing() throws Exception {
        String[] elements = {"fileTest1", "folderTest1", "fileTest2"};
        utils.configureFolderForUser(basicUser, "", elements);

        // OK for admin
        mockMvc.perform(get("/internal/storage/directories/vip/Users/" + basicUser.getFolder())
                        .with(getUserSecurityMock(getAdminUser())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpectAll(utils.getDatasMatcher(elements));

        Mockito.reset(gridaClient);

        // FAIL for basic user
        mockMvc.perform(get("/internal/storage/directories/vip/Users/ANY_STUFF_MUST_FAIL")
                        .with(getUserSecurityMock(basicUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(4001));
    }

    @Test
    public void testErrorCases() throws Exception {
        /*
        Error cases :
        - dir does not exist
        - listing on a file
        - listing on NOT /vip/...
        - listing on /vip/WRONGGROUP
        - listing with forbidden ..
     */
        // dir does not exist
        utils.configureNonExistingElementForUser(basicUser, "path/not/exist");

        mockMvc.perform(get("/internal/storage/directories/vip/Home/path/not/exist")
                        .with(getUserSecurityMock(basicUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(4002));

        Mockito.reset(gridaClient);

        // trying to list on a file
        utils.configureFileForUser(basicUser, "path/to/file.txt");

        mockMvc.perform(get("/internal/storage/directories/vip/Home/path/to/file.txt")
                        .with(getUserSecurityMock(basicUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(4004));

        Mockito.reset(gridaClient);

        // trying to list on path not starting with vip
        mockMvc.perform(get("/internal/storage/directories/NOTVIP/stuff")
                        .with(getUserSecurityMock(basicUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(4000));

        // trying to list on unknown root folder
        mockMvc.perform(get("/internal/storage/directories/vip/NOTAGROUP")
                        .with(getUserSecurityMock(basicUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(4000));

        // trying to list on unknown group
        mockMvc.perform(get("/internal/storage/directories/vip/UNKNOWNGROUP (group)")
                        .with(getUserSecurityMock(basicUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(4001));

        // trying to hack with ..
        mockMvc.perform(get("/internal/storage/directories/vip/../../file_to_hack.txt")
                        .with(getUserSecurityMock(basicUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(9000));
    }

}
