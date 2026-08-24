package fr.insalyon.creatis.vip.core.server.business.base;

import java.util.ArrayList;
import java.util.List;

import fr.insalyon.creatis.vip.core.client.DefaultError;
import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.client.view.user.UserLevel;
import fr.insalyon.creatis.vip.core.server.inter.CheckedRunnable;

public class PermissionChain {

    public record Scope(UserLevel level, CheckedRunnable<VipException> runnable) {}

    private final UserLevel userLevel;
    private final List<PermissionChain.Scope> items;

    public PermissionChain(UserLevel userLevel) {
        this.userLevel = userLevel;
        this.items = new ArrayList<>();
    }

    private PermissionChain chain(CheckedRunnable<VipException> action, UserLevel required) {
        items.add(new Scope(required, action));
        return this;
    }

    // perform chain filtering (like ResponsabilityChain pattern)
    // if user do not enter in any filter, it will be rejected
    public void filter() throws VipException {
        for (PermissionChain.Scope scope : items) {
            if (userLevel.equals(scope.level)) {
                if (scope.runnable != null) {
                    scope.runnable.run();
                }
                return;
            }
        }
        throw new VipException(DefaultError.ACCESS_DENIED);
    }

    public PermissionChain user() {
        return user(null);
    }

    public PermissionChain user(CheckedRunnable<VipException> action) {
        chain(action, UserLevel.Beginner);
        chain(action, UserLevel.Advanced);
        return this; 
    }

    public PermissionChain developer() {
        return developer(null);
    }

    public PermissionChain developer(CheckedRunnable<VipException> action) {
        chain(action, UserLevel.Developer);
        return this;
    }

    public PermissionChain admin() {
        return admin(null);
    }

    public PermissionChain admin(CheckedRunnable<VipException> action) {
        chain(action, UserLevel.Administrator);
        return this;
    }
}
