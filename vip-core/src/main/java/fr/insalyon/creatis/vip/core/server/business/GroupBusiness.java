package fr.insalyon.creatis.vip.core.server.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.insalyon.creatis.grida.client.GRIDAClient;
import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.core.client.DefaultError;
import fr.insalyon.creatis.grida.client.GRIDAPoolClient;
import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.client.view.user.UserLevel;
import fr.insalyon.creatis.vip.core.models.Group;
import fr.insalyon.creatis.vip.core.models.GroupType;
import fr.insalyon.creatis.vip.core.models.User;
import fr.insalyon.creatis.vip.core.server.business.base.CommonBusiness;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import fr.insalyon.creatis.vip.core.server.dao.GroupDAO;
import fr.insalyon.creatis.vip.core.server.dao.UsersGroupsDAO;
import fr.insalyon.creatis.vip.core.server.inter.annotations.VIPExternalSafe;
import fr.insalyon.creatis.vip.core.server.model.PrecisePage;

@Service
@Transactional
public class GroupBusiness extends CommonBusiness {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Server server;
    private final GroupDAO groupDAO;
    private final GRIDAClient gridaClient;
    private final GRIDAPoolClient gridaPoolClient;
    private final UsersGroupsDAO usersGroupsDAO;

    @Autowired
    public GroupBusiness(GRIDAClient gridaClient, GRIDAPoolClient gridaPoolClient, GroupDAO groupDAO, Server server, UsersGroupsDAO usersGroupsDAO) {
        this.server = server;
        this.groupDAO = groupDAO;
        this.gridaClient = gridaClient;
        this.gridaPoolClient = gridaPoolClient;
        this.usersGroupsDAO = usersGroupsDAO;
    }

    @VIPExternalSafe
    public void add(Group group) throws VipException {
        permissions.filter((chain) -> chain.admin());

        try {
            checkAuto(group);
            gridaClient.createFolder(
                    server.getDataManagerGroupsHome(),
                    group.getName().replaceAll(" ", "_"));
            
            groupDAO.add(group);
        } catch (GRIDAClientException ex) {
            logger.error("Error adding group : {}", group.getName(), ex);
            throw new VipException(ex);
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    @VIPExternalSafe
    public void remove(String groupName) throws VipException {
        permissions.filter((chain) -> chain.admin());

        try {
            gridaPoolClient.delete(server.getDataManagerGroupsHome() + "/"
                    + groupName.replaceAll(" ", "_"), getUser().getFullName());
            groupDAO.remove(groupName);
        } catch (GRIDAClientException ex) {
            logger.error("Error removing group : {}", groupName, ex);
            throw new VipException(ex);
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    @VIPExternalSafe
    public void update(String name, Group group) throws VipException {
        permissions.filter((chain) -> chain.admin());

        try {
            checkAuto(group);
            if ( ! name.equals(group.getName())) {
                gridaClient.rename(
                        server.getDataManagerGroupsHome() + "/" + name.replaceAll(" ", "_"),
                        server.getDataManagerGroupsHome() + "/" + group.getName().replaceAll(" ", "_"));
            }
            groupDAO.update(name, group);
        } catch (GRIDAClientException ex) {
            logger.error("Error updating group : {}", name, ex);
            throw new VipException(ex);
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    @VIPExternalSafe
    public List<Group> get(boolean onlyApplications, boolean onlyResources) throws VipException {
        try {
            if (onlyApplications && onlyResources) {
                // Both filters requested = error
                throw new VipException(DefaultError.BAD_PARAMETERS, "onlyApplications and onlyResources cannot be both true!");
            }
            List<Group> groups;
            if (getUserLevel().equals(UserLevel.Administrator)) {
                groups = groupDAO.get();
            } else {
                groups = Stream.concat(
                    getOrLoadUserGroups(getUser()).stream(),
                    groupDAO.get().stream().filter(Group::isPublicGroup)
                ).distinct().toList();
            }
            if (onlyApplications) {
                groups = groups.stream().filter((g) -> g.getType().equals(GroupType.APPLICATION)).toList();
            }
            if (onlyResources) {
                groups = groups.stream().filter((g) -> g.getType().equals(GroupType.RESOURCE)).toList();
            }
            return groups;
        } catch (DAOException ex) {
            logger.error("Error retrieving groups", ex);
            throw new VipException(ex);
        }
    }

    @VIPExternalSafe
    public List<Group> get() throws VipException {
        return get(false, false);
    }

    @VIPExternalSafe
    public PrecisePage<Group> get(boolean onlyApplications, boolean onlyResources, int offset, int quantity) throws VipException {
        return pageBuilder.doPrecise(offset, quantity, get(onlyApplications, onlyResources));
    }

    @VIPExternalSafe
    public List<Group> searchGroups(String query, int limit) throws VipException {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        int safeLimit = Math.max(1, Math.min(limit, 50));
        String normalized = query.trim().toLowerCase();

        return get(false, false).stream()
                .filter((group) -> group.getName() != null && group.getName().toLowerCase().contains(normalized))
                .limit(safeLimit)
                .collect(Collectors.toList());
    }

    @VIPExternalSafe
    public Group get(String groupName) throws VipException {
        if (groupName == null) {
            return null;
        }
        return get().stream()
                .filter(g -> groupName.equals(g.getName()))
                .findAny().orElse(null);
    }

    public Group getByName(String groupName) throws VipException {
        if (groupName == null) {
            return null;
        }
        try {
            return groupDAO.getByName(groupName);
        } catch (DAOException e) {
            throw new VipException(e);
        }
    }

    public List<Group> getPublic() throws VipException {
        return groupDAO.get().stream()
            .filter((g) -> g.isPublicGroup())
            .collect(Collectors.toList());
    }

    public List<Group> getByType(GroupType type) throws VipException {
        try {
            return groupDAO.getByType(type);
        } catch (DAOException e) {
            throw new VipException(e);
        }
    }

    public List<Group> getByApplication(String appName) throws VipException {
        try {
            return groupDAO.getByApplication(appName);
        } catch (DAOException e) {
            throw new VipException(e);
        }
    }

    public Set<Group> getByResource(String ressourceName) throws VipException {
        try {
            return groupDAO.getByRessource(ressourceName);
        } catch (DAOException e) {
            throw new VipException(e);
        }
    }

    public void checkAuto(Group group) throws VipException {
        if (group.isAuto()) {
            Group existing = getByType(group.getType()).stream().filter((g) -> g.isAuto()).findFirst().orElse(null);

            if ( ! group.isPublicGroup()) {
                throw new VipException("You can only create public auto groups!");
            } else if (existing != null && ! existing.getName().equals(group.getName())) {
                throw new VipException("You can't have multiples auto groups of the same type!");
            }
        }
    }

    public String getWarningSameVisibility(Set<String> groupNames) throws VipException {
        List<Group> groups = new ArrayList<>();

        for (String name : groupNames) {
            groups.add(get(name));
        }

        if (groups.stream().map(Group::isPublicGroup).toList().stream().distinct().count() > 1) {
            return "Be careful: the groups that you have chosen do not have the same visibility!";
        } else {
            return null;
        }
    }

    public Set<Group> getOrLoadUserGroups(User user) throws VipException {
        if (user.getGroups() == null || user.getGroups().isEmpty()) {
            user.setGroups(usersGroupsDAO.getUserGroups(user.getEmail()));
        }
        return user.getGroups();
    }
}