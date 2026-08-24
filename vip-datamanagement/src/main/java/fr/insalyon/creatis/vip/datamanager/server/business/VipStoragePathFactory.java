package fr.insalyon.creatis.vip.datamanager.server.business;

import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.models.User;
import fr.insalyon.creatis.vip.core.server.business.Server;
import fr.insalyon.creatis.vip.core.server.security.common.CurrentUserProvider;
import fr.insalyon.creatis.vip.datamanager.models.VipStoragePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class VipStoragePathFactory {

    private final Server server;
    private final CurrentUserProvider currentUserProvider;

    @Autowired
    public VipStoragePathFactory(Server server, CurrentUserProvider currentUserProvider) {
        this.server = server;
        this.currentUserProvider = currentUserProvider;
    }

    public VipStoragePath create(String first, String... more) throws VipException {
        return create(Path.of(first, more));
    }

    public VipStoragePath create(Path vipPath) throws VipException {
        return new VipStoragePath(
                currentUserProvider.get(),
                vipPath,
                server.getDataManagerUsersHome(),
                server.getDataManagerGroupsHome(),
                server.getVoRoot()
        );
    }
}
