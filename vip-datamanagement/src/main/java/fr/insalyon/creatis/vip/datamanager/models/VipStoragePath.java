package fr.insalyon.creatis.vip.datamanager.models;

import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.models.User;
import fr.insalyon.creatis.vip.datamanager.client.DataManagerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class VipStoragePath {

    private static final Logger logger = LoggerFactory.getLogger(VipStoragePath.class);

    private final User user;

    private final String usersHome;

    private final String groupsHome;

    private final String voRoot;

    private final Path vipPath;
    private String groupName;
    private Path relativePath;

    private final Path realPath;

    private VipPathType type;

    private enum VipPathType {
        VIP_ROOT,
        USERS_HOME,
        GROUP,
        USERS_FOLDER,
        VO_ROOT
    }

    public boolean isRootPath() {
        return type == VipPathType.VIP_ROOT;
    }

    public boolean isHomePath() {
        return type == VipPathType.USERS_HOME;
    }

    public boolean isGroupPath() {
        return type == VipPathType.GROUP;
    }

    public boolean impossibleToRemove() {
        return vipPath.getNameCount() <= 2;
    }

    public boolean isAdminArea() {
        return type == VipPathType.USERS_FOLDER || type == VipPathType.VO_ROOT;
    }

    public String getGroupName() {
        return groupName;
    }

    public Path getRelativePath() {
        return relativePath;
    }

    public VipStoragePath(User user, Path vipPath, String usersHome, String groupsHome, String voRoot) throws VipException {
        this.user = user;
        this.usersHome = usersHome;
        this.groupsHome = groupsHome;
        this.voRoot = voRoot;
        this.vipPath = vipPath.normalize().toAbsolutePath();

        if ( ! this.vipPath.startsWith(DataManagerConstants.ROOT)) {
            logger.info("invalid storage path, does not begin with /vip : {}", this.vipPath);
            throw new VipException(DataManagementError.INVALID_STORAGE_PATH, this.vipPath, "must start with /vip");
        }
        determinePathElements();
        // convert to realPath
        realPath = convertToRealPath(this.vipPath);
    }

    private void determinePathElements() throws VipException {
        if (vipPath.getNameCount() < 1) {
            logger.info("invalid storage path : {}", vipPath);
            throw new VipException(DataManagementError.INVALID_STORAGE_PATH, vipPath, "must start with '/vip");
        }
        if (vipPath.getNameCount() == 1) {
            type = VipPathType.VIP_ROOT;
            return;
        }
        String secondFolder = vipPath.getName(1).toString();
        if (secondFolder.equals(DataManagerConstants.USERS_HOME)) {
            setAndCheckRelativePath();
            type = VipPathType.USERS_HOME;
        }
        else if (secondFolder.endsWith(DataManagerConstants.GROUP_APPEND)) {
            groupName = secondFolder.substring(0, secondFolder.length()-DataManagerConstants.GROUP_APPEND.length());
            setAndCheckRelativePath();
            type = VipPathType.GROUP;
        }
        else if (secondFolder.equals(DataManagerConstants.USERS_FOLDER)) {
            type = VipPathType.USERS_FOLDER;
        }
        else if (secondFolder.equals(DataManagerConstants.VO_ROOT_FOLDER)) {
            type = VipPathType.VO_ROOT;
        }
        else {
            logger.info("invalid storage path : {}", vipPath);
            throw new VipException(DataManagementError.INVALID_STORAGE_PATH, vipPath, "must start with /vip/Home or /vip/xxx (Group)");
        }
    }

    private void setAndCheckRelativePath() throws VipException {
        if (vipPath.getNameCount() <= 2) {
            relativePath = Path.of("");
            return;
        }
        relativePath = vipPath.subpath(2, vipPath.getNameCount());
        // Allow alphanumeric, dot, dash, underscore, space and parentheses in path parts
        for (Path part : relativePath) {
            String partStr = part.toString();
            if (!partStr.matches(DataManagerConstants.VALID_PATH_CHARS)) {
                throw new VipException(DataManagementError.INVALID_STORAGE_PATH, vipPath, "must not contain invalid character");
            }
        }
    }

    public Path convertToRealPath(Path vipPath) throws VipException {
        Path rootPath;
        switch (type) {
            case VIP_ROOT -> rootPath = Path.of(DataManagerConstants.ROOT);
            case USERS_HOME -> rootPath = Path.of(usersHome).resolve(resolveUserFolder());
            case GROUP -> {
                rootPath = Path.of(groupsHome).resolve(groupName.replace(" ", "_"));
            }
            case USERS_FOLDER -> rootPath = Path.of(usersHome);
            case VO_ROOT -> rootPath = Path.of(voRoot);
            default -> throw new IllegalArgumentException("Unsupported vip path prefix: " + vipPath.getName(1).toString());
        }

        Path result = rootPath;
        for (int i = 2; i < vipPath.getNameCount(); i++) {
            result = result.resolve(vipPath.getName(i).toString());
        }

        return result.normalize();
    }

    private String resolveUserFolder() throws VipException {
        if (user == null || user.getFolder() == null || user.getFolder().isBlank()) {
            throw new VipException(DataManagementError.INVALID_STORAGE_PATH, "User folder is not defined");
        }
        return user.getFolder();
    }

    public String getVipPathString() {
        return vipPath.toString();
    }

    public Path getVipPath() {
        return vipPath;
    }

    public String getRealPathString() {
        return realPath.toString();
    }

    public Path getRealPath() {
        return realPath;
    }

    @Override
    public String toString() {
        return vipPath.toString();
    }
}
