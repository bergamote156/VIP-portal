package fr.insalyon.creatis.vip.datamanager.server.business;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import fr.insalyon.creatis.vip.core.server.business.base.CommonBusiness;
import fr.insalyon.creatis.vip.datamanager.models.VipStoragePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.insalyon.creatis.grida.client.GRIDAClient;
import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.grida.common.bean.GridData;
import fr.insalyon.creatis.grida.common.bean.GridPathInfo;
import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.models.User;
import fr.insalyon.creatis.vip.datamanager.client.view.DataManagerException;
import fr.insalyon.creatis.vip.datamanager.models.Data;

@Service
@Transactional
public class LFCBusiness extends CommonBusiness {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private GRIDAClient gridaClient;
    private LfcPathsBusiness lfcPathsBusiness;
    private VipStoragePathFactory vipStoragePathFactory;

    @Autowired
    public LFCBusiness(GRIDAClient gridaClient, LfcPathsBusiness lfcPathsBusiness,
                       VipStoragePathFactory vipStoragePathFactory) {
        this.gridaClient = gridaClient;
        this.lfcPathsBusiness = lfcPathsBusiness;
        this.vipStoragePathFactory = vipStoragePathFactory;
    }

    public List<Data> listDir(VipStoragePath path, boolean refresh) throws VipException {
        try {
            List<GridData> list = gridaClient.getFolderData(path.getRealPathString(), refresh);

            List<Data> dataList = new ArrayList<>();
            for (GridData data : list) {
                if (data.getType() == GridData.Type.Folder) {
                    dataList.add(new Data(data.getName(),
                            Data.Type.valueOf(data.getType().name().toLowerCase()),
                            data.getPermissions()));

                } else {
                    dataList.add(new Data(data.getName(),
                            Data.Type.valueOf(data.getType().name().toLowerCase()),
                            data.getLength(), data.getModificationDate(),
                            data.getReplicas(), data.getPermissions()));
                }
            }
            return dataList;

        } catch (GRIDAClientException ex) {
            logger.error("Error listing directory {} for {}", path, getUser(), ex);
            throw new VipException(ex);
        }
    }

    public void createDir(VipStoragePath path) throws VipException {
        Path realPath = path.getRealPath();
        try {
            gridaClient.createFolder(
                    realPath.getParent().toString(),
                    realPath.getFileName().toString());
        } catch (GRIDAClientException ex) {
            logger.error("Error creating directory {} for {}", path, getUser(),ex);
            throw new VipException(ex);
        }
    }

    public void rename(User user, String oldPath, String newPath, boolean extendPath)
            throws VipException {

        try {
            gridaClient.rename(
                    lfcPathsBusiness.parseBaseDir(user, oldPath),
                    lfcPathsBusiness.parseBaseDir(user, newPath));
        } catch (GRIDAClientException ex) {
            if (ex.getMessage().contains("Can not rename/move") && extendPath) {
                SimpleDateFormat sdf =
                        new SimpleDateFormat("-yyyy.MM.dd-HH.mm.ss");
                String newExtPath = newPath + sdf.format(new Date());
                rename(user, oldPath, newExtPath, false);
            } else {
                logger.error("Error renaming path {} to {} for {}",
                        oldPath, newPath, user,ex);
                throw new VipException(ex);
            }
        } catch (DataManagerException ex) {
            throw new VipException(ex);
        }
    }

    public void rename(
            User user, String baseDir, List<String> paths, String newBaseDir,
            boolean extendPath) throws VipException {

        for (String name : paths) {
            rename(user, baseDir + "/" + name, newBaseDir + "/" + name, extendPath);
        }
    }

    public boolean exists(User user, String path) throws VipException {
        return exists(vipStoragePathFactory.create(path));
    }

    public boolean exists(VipStoragePath path) throws VipException {
        try {
            return gridaClient.getPathInfo(path.getRealPathString()).exist();
        } catch (GRIDAClientException ex) {
            logger.error("Error checking file '{}' existence for {}", path, getUserEmail(), ex);
            throw new VipException(ex);
        }
    }

    public Optional<Data.Type> getPathInfo(VipStoragePath path) throws VipException {
        try {
            // convert GridPathInfo to an Optional<Data.Type> to avoid a new structure in vip.datamanager
            GridPathInfo pathInfo = gridaClient.getPathInfo(path.getRealPathString());
            if (pathInfo.exist()) {
                return Optional.of(Data.Type.valueOf(pathInfo.getType().name().toLowerCase()));
            }
            // create /vip/Home or /vip/xxx (group) if it does not exist
            else if (ensureHomeDirOrGroupDirExists(path)) {
                return Optional.of(Data.Type.folder);
            }
            else {
                return Optional.empty();
            }
        } catch (GRIDAClientException ex) {
            logger.error("Error getting path info {} for {}", path, userSupplier.get(), ex);
            throw new VipException(ex);
        }
    }

    private boolean ensureHomeDirOrGroupDirExists(VipStoragePath path) throws VipException {
        if ((path.isGroupPath() || path.isHomePath())
                && path.getRelativePath().toString().isEmpty()) {
            // a group dir or home dir does not exist, although it should. Correct that and create it
            createDir(path);
            return true;
        }
        return false;
    }

    public long getModificationDate(User user, String path) throws VipException {
        return getModificationDate(vipStoragePathFactory.create(path));
    }

    public long getModificationDate(VipStoragePath path) throws VipException {
        try {
            return gridaClient.getModificationDate(path.getRealPathString());
        } catch (GRIDAClientException ex) {
            logger.error("Error getting file {} modification date for {}", path, getUserEmail(), ex);
            throw new VipException(ex);
        }
    }

    public List<Long> getModificationDate(User user, List<String> paths)
            throws VipException {
        try {
            List<String> parsedPaths = new ArrayList<>();
            for (String path : paths) {
                parsedPaths.add(lfcPathsBusiness.parseBaseDir(user, path));
            }

            return gridaClient.getModificationDate(parsedPaths);
        } catch (GRIDAClientException ex) {
            logger.error("Error getting files {} modification dates for {}",
                    paths, user,ex);
            throw new VipException(ex);
        } catch (DataManagerException ex) {
            throw new VipException(ex);
        }
    }
}
