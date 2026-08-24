package fr.insalyon.creatis.vip.core.integrationtest.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import fr.insalyon.creatis.vip.core.client.DefaultError;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants.GROUP_ROLE;
import fr.insalyon.creatis.vip.core.client.view.user.UserLevel;
import fr.insalyon.creatis.vip.core.integrationtest.BaseInternalApiSpringIT;
import fr.insalyon.creatis.vip.core.models.Group;
import fr.insalyon.creatis.vip.core.models.User;
import fr.insalyon.creatis.vip.core.server.dao.UsersGroupsDAO;

public class GroupControllerIT extends BaseInternalApiSpringIT {

    private User adminUser;
    private User developperUser;
    private User basicUser;

    @Autowired
    private UsersGroupsDAO usersGroupsDAO;

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        adminUser = createUser(emailUser1, UserLevel.Administrator);
        developperUser = createUser(emailUser2, UserLevel.Developer);
        basicUser = createUser(emailUser3, UserLevel.Beginner);
    }

    @Test
    public void add() throws Exception {
        Group group = new Group("test");

        // basic user (=forbidden);
        mockMvc.perform(post("/internal/groups")
                .with(getUserSecurityMock(basicUser))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(group)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(DefaultError.ACCESS_DENIED.getCode()));

        // developer (=forbidden)
        mockMvc.perform(post("/internal/groups")
                .with(getUserSecurityMock(developperUser))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(group)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(DefaultError.ACCESS_DENIED.getCode()));

        // admin (=ok)
        mockMvc.perform(post("/internal/groups")
                .with(getUserSecurityMock(adminUser))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(group)))
                .andExpect(status().isOk());
    }

    @Test
    public void update() throws Exception {
        Group group = new Group("test");

        // create first
        mockMvc.perform(post("/internal/groups")
                .with(getUserSecurityMock(adminUser)).with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(group)))
                .andExpect(status().isOk());

        // basic user (=forbidden);
        mockMvc.perform(put("/internal/groups/" + group.getName())
                .with(getUserSecurityMock(basicUser))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(group)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(DefaultError.ACCESS_DENIED.getCode()));

        // developer (=forbidden)
        mockMvc.perform(put("/internal/groups/" + group.getName())
                .with(getUserSecurityMock(developperUser))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(group)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(DefaultError.ACCESS_DENIED.getCode()));

        // admin (=ok)
        mockMvc.perform(put("/internal/groups/" + group.getName())
                .with(getUserSecurityMock(adminUser))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(group)))
                .andExpect(status().isOk());

        // admin but not matching
        mockMvc.perform(put("/internal/groups/random")
                .with(getUserSecurityMock(adminUser))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(group)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(DefaultError.BAD_INPUT_FIELD.getCode()));
    }

    @Test
    public void remove() throws Exception {
        Group group = new Group("test");

        // create first
        mockMvc.perform(post("/internal/groups")
                .with(getUserSecurityMock(adminUser)).with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(group)))
                .andExpect(status().isOk());

        // basic user (=forbidden);
        mockMvc.perform(delete("/internal/groups/" + group.getName())
                .with(getUserSecurityMock(basicUser))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(DefaultError.ACCESS_DENIED.getCode()));

        // developer (=forbidden)
        mockMvc.perform(delete("/internal/groups/" + group.getName())
                .with(getUserSecurityMock(developperUser))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(DefaultError.ACCESS_DENIED.getCode()));

        // admin (=ok)
        mockMvc.perform(delete("/internal/groups/" + group.getName())
                .with(getUserSecurityMock(adminUser)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        // admin but not existing existing
        mockMvc.perform(delete("/internal/groups/" + group.getName())
                .with(getUserSecurityMock(adminUser))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(DefaultError.NOT_FOUND.getCode()));
    }

    // getList can test both `get(string)` and `get`
    // since `get(name)` is based on `get` in business layer
    @Test
    public void getList() throws Exception {
        Group group = new Group("test");
        Group group2 = new Group("test2");
        Group auto = new Group("auto");
        auto.setAuto(true);

        asAdminContext(() -> {
            groupBusiness.add(group);
            groupBusiness.add(group2);
            groupBusiness.add(auto);
        });

        // user in group + auto
        // developer in group2 + auto
        // admin in group + group2 + auto
        usersGroupsDAO.add(basicUser.getEmail(), group.getName(), GROUP_ROLE.User);
        usersGroupsDAO.add(developperUser.getEmail(), group2.getName(), GROUP_ROLE.User);

        // basic user
        mockMvc.perform(get("/internal/groups")
                .with(getUserSecurityMock(basicUser))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[*].name")
                        .value(containsInAnyOrder(group.getName(), group2.getName(), auto.getName())));

        // developer
        mockMvc.perform(get("/internal/groups")
                .with(getUserSecurityMock(developperUser))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[*].name")
                        .value(containsInAnyOrder(group.getName(), group2.getName(), auto.getName())));

        // admin
        mockMvc.perform(get("/internal/groups")
                .with(getUserSecurityMock(adminUser))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[*].name")
                        .value(containsInAnyOrder(group.getName(), group2.getName(), auto.getName())));
    }
}
