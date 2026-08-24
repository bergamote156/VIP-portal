package fr.insalyon.creatis.vip.core.server.business.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insalyon.creatis.vip.core.client.DefaultError;
import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.models.Group;
import fr.insalyon.creatis.vip.core.models.User;

/**
 * TODO : add log (errors or warn or info when throwing an exception)
 */

@Service
public class CorePermissions {
    private final Supplier<User> uSupplier;

    @Autowired
    public CorePermissions(Supplier<User> uSupplier) {
        this.uSupplier = uSupplier;
    }

    public void filter(Consumer<PermissionChain> conditions) throws VipException {
        PermissionChain chain = new PermissionChain(uSupplier.get().getLevel());

        conditions.accept(chain);
        chain.filter();
    }

    public void checkOnlyUserPrivateGroups(Set<Group> groupsToCheck) throws VipException {
        User user = uSupplier.get();
        Set<Group> userGroups = user.getGroups();

        if (groupsToCheck == null) {
            return;
        }
        for (Group group : groupsToCheck) {
            // check ONLY user groups and ONLY privates groups
            if ( ! userGroups.contains(group) || group.isPublicGroup()) {
                throw new VipException(DefaultError.ACCESS_DENIED);
            }
        }
    }

    public Set<Group> filterOnlyUserGroups(Set<Group> toFilter) {
        User user = uSupplier.get();
        Set<Group> result;
        Set<Group> userGroups = (user == null) ? null : user.getGroups();

        if (user == null) {
            result = toFilter.stream().filter( g -> g.isPublicGroup()).collect(Collectors.toSet());
        } else if (user.isSystemAdministrator()) {
            result = new HashSet<>(toFilter);
        } else {
            result = toFilter.stream().filter( g -> userGroups.contains(g)).collect(Collectors.toSet());
        }
        return result;
    }

    public <T> List<T> filterOnlySame(List<T> a, List<T> b) {
        List<T> result = new ArrayList<>(a);

        result.retainAll(b);
        return result;
    }

    public <T> void checkItemInList(T item, List<T> list) throws VipException {
        if ( ! list.contains(item)) {
            throw new VipException(DefaultError.ACCESS_DENIED);
        }
    }

    public <T> void checkUnchanged(T a, T b) throws VipException {
        if (!a.equals(b)) {
            throw new VipException(DefaultError.ACCESS_DENIED); 
        }
    }

    public <T> T shouldExist(T a) throws VipException {
        return shouldExist(a, Object.class, "unknown");
    }

    public <T> T shouldExist(T a, Class<?> type, String name) throws VipException {
        if (a == null) {
            throw new VipException(DefaultError.NOT_FOUND, type.getSimpleName(), name);
        } else {
            return a;
        }
    }
}
