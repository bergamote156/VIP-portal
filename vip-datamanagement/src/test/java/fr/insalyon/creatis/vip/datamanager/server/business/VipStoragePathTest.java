package fr.insalyon.creatis.vip.datamanager.server.business;

import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.models.User;
import fr.insalyon.creatis.vip.datamanager.models.VipStoragePath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class VipStoragePathTest {

    @Test
    public void testVipPathStorageNormalization() throws VipException {
        // nothing to do
        assertVipPath("/vip/Home/dir", "/vip/Home/dir");
        // managed ..
        assertVipPath("/vip/Home/somewhere/../dir", "/vip/Home/dir");
        // handle several slashes and trailing slashes
        assertVipPath("/vip///Home/dir/", "/vip/Home/dir");
        assertInvalidVipPath("/vip/../../../dir");
        User user = new User();
        user.setFolder("test-user");
        System.out.println(new VipStoragePath(user, Path.of("/vip/Home/coucou.txt"), "/users", "/groups", "/voroot"));
        System.out.println(new VipStoragePath(user, Path.of("/vip/Home"), "/users", "/groups", "/voroot"));
    }

    public void assertVipPath(String original, String expected) throws VipException {
        User user = new User();
        user.setFolder("test-user");

        Assertions.assertEquals(
            new VipStoragePath(user, Path.of(original), "/users", "/groups", "/voroot").getVipPathString(),
                expected
        );
    }

    public void assertInvalidVipPath(String original) {
        User user = new User();
        user.setFolder("test-user");

        Assertions.assertThrows(
                VipException.class,
                () -> new VipStoragePath(user, Path.of(original), "/users", "/groups", "/voroot")
        );
    }

}
