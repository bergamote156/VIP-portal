package fr.insalyon.creatis.vip.datamanager.server.business;

import fr.insalyon.creatis.vip.core.client.VipError;
import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.models.Group;
import fr.insalyon.creatis.vip.core.models.User;
import fr.insalyon.creatis.vip.core.server.business.base.CommonBusiness;
import fr.insalyon.creatis.vip.core.server.dao.GroupDAO;
import fr.insalyon.creatis.vip.core.server.inter.annotations.VIPExternalSafe;
import fr.insalyon.creatis.vip.datamanager.models.Data;
import fr.insalyon.creatis.vip.datamanager.models.DataManagementError;
import fr.insalyon.creatis.vip.datamanager.models.PoolOperation;
import fr.insalyon.creatis.vip.datamanager.models.VipStoragePath;
import fr.insalyon.creatis.vip.datamanager.server.DataManagerUtil;
import fr.insalyon.creatis.vip.datamanager.server.business.LFCPermissionBusiness.LFCAccessType;
import jakarta.annotation.PostConstruct;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static fr.insalyon.creatis.vip.datamanager.client.DataManagerConstants.GROUP_APPEND;
import static fr.insalyon.creatis.vip.datamanager.client.DataManagerConstants.USERS_HOME;

@Service
@Transactional
public class StorageBusiness extends CommonBusiness {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final LFCBusiness lfcBusiness;
    private final LFCPermissionBusiness lfcPermissionBusiness;
    private final GroupDAO groupDAO;
    private final TransferPoolBusiness transferPoolBusiness;
    private final DataManagerBusiness dataManagerBusiness;
    private final VipStoragePathFactory vipStoragePathFactory;
    private ScheduledExecutorService scheduler;


    @Autowired
    public StorageBusiness(
            LFCBusiness lfcBusiness,
            LFCPermissionBusiness lfcPermissionBusiness,
            GroupDAO groupDAO,
            TransferPoolBusiness transferPoolBusiness,
            DataManagerBusiness dataManagerBusiness,
            VipStoragePathFactory vipStoragePathFactory) {
        this.lfcBusiness = lfcBusiness;
        this.lfcPermissionBusiness = lfcPermissionBusiness;
        this.groupDAO = groupDAO;
        this.transferPoolBusiness = transferPoolBusiness;
        this.dataManagerBusiness = dataManagerBusiness;
        this.vipStoragePathFactory = vipStoragePathFactory;
    }

    @PostConstruct
    private void initScheduler() {
        // cannot be done in constructor because server is autowired in setter, so later
        int parallelDownloadsNb = server.getApiParallelDownloadNb();
        logger.info("Declaring threads for {} synchronous downloads and uploads", parallelDownloadsNb);
        // 2 threads are needed for every download
        this.scheduler = Executors.newScheduledThreadPool(2 * parallelDownloadsNb);
    }

    @VIPExternalSafe
    public List<Data> listDir(String pathString) throws VipException {
        return listDir(pathString, true);
    }

    @VIPExternalSafe
    public List<Data> listDir(String pathString, boolean refresh) throws VipException {
        Optional<PathInfo> pathInfo = listDirOrGetPathInfo(pathString, refresh, true);
        if (pathInfo.isEmpty()) {
            logger.error("{} lists a non-existing path ({})", getUserEmail(), pathString);
            throw new VipException(DataManagementError.RESOURCE_NOT_FOUND_ERROR, pathString);
        }
        return pathInfo.get().info;
    }

    @VIPExternalSafe
    public Optional<PathInfo> listDirOrGetPathInfo(String pathString, boolean refresh) throws VipException {
        return listDirOrGetPathInfo(pathString, refresh, false);
    }

    public static class PathInfo {
        public Data.Type type;
        public List<Data> info;

        public PathInfo(Data.Type type, List<Data> info) {
            this.type = type;
            this.info = info;
        }
    }

    private Optional<PathInfo> listDirOrGetPathInfo(String pathString, boolean refresh, boolean onlyDir) throws VipException {
        VipStoragePath path = vipStoragePathFactory.create(pathString);
        lfcPermissionBusiness.checkLFCPathPermission(path, LFCAccessType.READ);

        if (path.isRootPath()) {
            List<Data> list = getRootDirectoriesName().stream()
                    .map(name -> new Data(name, Data.Type.folder, null))
                    .toList();
            return Optional.of(new PathInfo(Data.Type.folder, list));
        }
        Optional<Data.Type> type = lfcBusiness.getPathInfo(path);
        if (type.isEmpty()) {
            return Optional.empty();
        }
        if (onlyDir && type.get() == Data.Type.file) {
            logger.error("{} lists to list {}, but is a file :", getUserEmail(), pathString);
            throw new VipException(DataManagementError.INVALID_DIRECTORY_LISTING, pathString, "Path is a file");
        }
        return Optional.of(new PathInfo(type.get(), lfcBusiness.listDir(path, refresh)));
    }

    @VIPExternalSafe
    public List<String> getRootDirectoriesName() throws VipException {
        List<String> rootDir = new ArrayList<>();
        rootDir.add(USERS_HOME);

        List<Group> groups;
        if (getUser().isSystemAdministrator()) {
            groups = groupDAO.get();
        } else {
            groups = new ArrayList<>(getUser().getGroups());
        }

        for (Group group : groups) {
            logger.debug("Adding group {} to root dir", group.getName());
            rootDir.add(group.getName() + GROUP_APPEND);
        }
        return rootDir;
    }

    @VIPExternalSafe
    public boolean doesPathExist(String path) throws VipException {
        return getPathInfo(path).isPresent();
    }

    @VIPExternalSafe
    public Optional<Data.Type> getPathInfo(String stringPath) throws VipException {
        VipStoragePath path = vipStoragePathFactory.create(stringPath);
        lfcPermissionBusiness.checkLFCPathPermission(path, LFCAccessType.READ);

        if (path.isRootPath()) {
            return Optional.of(Data.Type.folder);
        }
        return lfcBusiness.getPathInfo(path);
    }

    @VIPExternalSafe
    public Long getModificationDate(String stringPath) throws VipException {
        VipStoragePath path = vipStoragePathFactory.create(stringPath);
        lfcPermissionBusiness.checkLFCPathPermission(path, LFCAccessType.READ);

        if (path.isRootPath()) {
            return null;
        }
        if ( ! lfcBusiness.exists(path)) {
            logger.info("{} trying to get modification date of a non-existing resource : {}", getUserEmail(), path);
            throw new VipException(DataManagementError.RESOURCE_NOT_FOUND_ERROR, path);
        }
        return lfcBusiness.getModificationDate(path);
    }

    @VIPExternalSafe
    public void deletePath(String pathString) throws VipException {
        VipStoragePath path = vipStoragePathFactory.create(pathString);
        lfcPermissionBusiness.checkLFCPathPermission(path, LFCAccessType.DELETE);

        if ( ! lfcBusiness.exists(path)) {
            logger.info("{} trying to delete a non-existing resource : {}", getUserEmail(), path);
            throw new VipException(DataManagementError.RESOURCE_NOT_FOUND_ERROR, path);
        }
        transferPoolBusiness.delete(path);
    }

    @VIPExternalSafe
    public void createDirectory(String pathString) throws VipException {
        VipStoragePath path = vipStoragePathFactory.create(pathString);
        lfcPermissionBusiness.checkLFCPathPermission(path, LFCAccessType.UPLOAD);

        checkUploadIsPossible(path, "Creating a directory", DataManagementError.INVALID_DIRECTORY_CREATION);

        lfcBusiness.createDir(path);
    }

    @VIPExternalSafe
    public String submitDownload(String pathString) throws VipException {
        return submitDownload(pathString, false);
    }

    @VIPExternalSafe
    public File downloadAndWait(String stringPath) throws VipException {
        String operationId = submitDownload(stringPath, true);
        waitForOperationOrTimeout(operationId);
        PoolOperation operation = transferPoolBusiness.getDownloadPoolOperation(operationId);
        return buildFileFromPoolOperation(operation);
    }

    @VIPExternalSafe
    private String submitDownload(String pathString, boolean onlyFileWithMaxSize) throws VipException {
        VipStoragePath path = vipStoragePathFactory.create(pathString);
        lfcPermissionBusiness.checkLFCPathPermission(path, LFCAccessType.READ);

        if (path.isRootPath()) {
            logger.error("{} trying to download /vip", getUserEmail());
            throw new VipException(DataManagementError.STORAGE_PERMISSION_ERROR, path, "Cannot download /vip");
        }
        Optional<Data.Type> pathInfo = lfcBusiness.getPathInfo(path);
        if (pathInfo.isEmpty()) {
            logger.error("{} trying to download not existing element {}", getUserEmail(), path);
            throw new VipException(DataManagementError.RESOURCE_NOT_FOUND_ERROR, path);
        }
        if (onlyFileWithMaxSize) {
            if (pathInfo.get().equals(Data.Type.folder)) {
                logger.error("{} trying to download a folder {}", getUserEmail(), path);
                throw new VipException(DataManagementError.INVALID_DOWNLOAD, path, "Cannot download a folder");
            }
            // path exists and is a file: check its size
            List<Data> fileData = lfcBusiness.listDir(path, true);
            long fileSize = fileData.getFirst().getLength();
            long maxSize = server.getCarminApiDataTransfertMaxSize();
            if (fileSize > maxSize) {
                logger.error("{} trying to download a file too big {} ( {} > max {})",
                        getUserEmail(), path, fileSize, maxSize);
                throw new VipException(DataManagementError.INVALID_DOWNLOAD, path, "File too big");
            }
        }
        return transferPoolBusiness.downloadFile(path);
    }

    @VIPExternalSafe
    public PoolOperation getOperation(String operationId) throws VipException {
        PoolOperation operation = transferPoolBusiness.getOperationById(
                operationId,
                getUser().getFolder());
        ensureOperationOwnership(operation, operationId);
        return operation;
    }

    @VIPExternalSafe
    public File getDownloadFileIfReady(String operationId) throws VipException {
        PoolOperation operation = transferPoolBusiness.getDownloadPoolOperation(operationId);
        ensureOperationOwnership(operation, operationId);

        if ( ! PoolOperation.Status.Done.equals(operation.getStatus())) {
            logger.info("{} trying to download an unfinished operation {}", getUserEmail(), operationId);
            throw new VipException(DataManagementError.INVALID_OPERATION, operationId, "Operation not finished");
        }

        return buildFileFromPoolOperation(operation);
    }

    private void ensureOperationOwnership(PoolOperation operation, String operationId) throws VipException {
        String currentUserEmail = getUserEmail();
        if (operation.getUser() == null || ! operation.getUser().equals(currentUserEmail)) {
            logger.error("Operation {} does not belong to current user {} but to {}",
                    operationId, currentUserEmail, operation.getUser());
            throw new VipException(DataManagementError.OPERATION_PERMISSION_ERROR, operationId);
        }
    }

    private File buildFileFromPoolOperation(PoolOperation operation) throws VipException {
        File file = new File(operation.getDest());
        if (file.isDirectory()) {
            file = new File(operation.getDest() + "/"
                    + FilenameUtils.getName(operation.getSource()));
        }
        if ( ! file.exists()) {
            logger.error("{} trying to download a non-existing-file {}. Operation : {}", getUserEmail(), file, operation.getId());
            throw new VipException(DataManagementError.INVALID_OPERATION, operation.getId(), "File not available");
        }
        return file;
    }

    @VIPExternalSafe
    public String submitUploadFromInputStream(
            String destinationDir,
            String filename,
            InputStream inputStream) throws VipException {
        String cleanFileName = DataManagerUtil.getCleanFilename(filename);
        String destination = Path.of(destinationDir, cleanFileName).toString();
        Optional<String> operationId = uploadFromFromInputStream(destination, inputStream, false, false);
        return operationId.orElseThrow(() -> {
            logger.error("Invalid upload for {} without operationId : {}", getUserEmail(), destination);
            return new VipException("Error during upload upload");
        });
    }

    @VIPExternalSafe
    public void uploadFileOrCreateDirFromInputStream(String pathString, InputStream is)
            throws VipException {
        uploadFromFromInputStream(pathString, is, true, true);
    }

    @VIPExternalSafe
    public void uploadBase64File(String pathString, String base64Content) throws VipException {
        double maxSize = server.getCarminApiDataTransfertMaxSize() * 1.33;
        if (base64Content.length() > maxSize) {
            logger.error("Trying to upload base64 that is too big ({} > max {} bytes (max + 33% for base64))",
                    base64Content.length(), maxSize);
            throw new VipException(DataManagementError.FILE_TOO_BIG, maxSize);
        }
        InputStream base64InputStream = getBase64InputStream(base64Content);
        uploadFromFromInputStream(pathString, base64InputStream, false, true);
    }

    // ################## UPLOAD stuff

    private Optional<String> uploadFromFromInputStream(
            String pathString, InputStream is, boolean createDirIfEmpty, boolean waitForOperationToFinish)
            throws VipException {
        VipStoragePath destinationPath = vipStoragePathFactory.create(pathString);
        lfcPermissionBusiness.checkLFCPathPermission(destinationPath, LFCAccessType.UPLOAD);

        VipStoragePath destinationDirPath = vipStoragePathFactory.create(destinationPath.getVipPath().getParent());
        checkUploadIsPossible(destinationDirPath, destinationPath, "uploading", DataManagementError.INVALID_UPLOAD);

        String localPath = buildUniqueLocalUploadPath(destinationPath.getVipPath().getFileName().toString());
        boolean isFileEmpty = saveInputStreamToFile(is, localPath);
        if (isFileEmpty && createDirIfEmpty) {
            logger.debug("no content in upload, creating dir : {}", pathString);
            lfcBusiness.createDir(destinationPath);
            return Optional.empty();
        }
        else if (isFileEmpty) {
            logger.error("'{}' uploading empty file to '{}'", getUserEmail(), destinationPath);
            throw new VipException(DataManagementError.INVALID_UPLOAD, destinationPath, "File must not be empty");
        }
        // file not empty : upload
        String operationId = transferPoolBusiness.uploadFile(localPath, destinationDirPath);
        if (waitForOperationToFinish) {
            waitForOperationOrTimeout(operationId);
        }
        return Optional.of(operationId);
    }

    private InputStream getBase64InputStream(String base64Content) throws VipException {
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            InputStream inputStream = ReaderInputStream.builder()
                    .setReader(new StringReader(base64Content))
                    .setCharset(StandardCharsets.UTF_8)
                    .get();
            return decoder.wrap(inputStream);
        } catch (IOException e) {
            logger.error("Error getting base64 input stream", e);
            throw new VipException("Error writing base64 file", e);
        }
    }

    private boolean saveInputStreamToFile(InputStream is, String path) throws VipException {
        Long maxSize = server.getCarminApiDataTransfertMaxSize();
        long totalBytesRead = 0;

        try (OutputStream fos = Files.newOutputStream(Paths.get(path))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            boolean isFileEmpty = true;
            while ((bytesRead = is.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                if (maxSize != null && maxSize > 0 && totalBytesRead > maxSize) {
                    logger.error("Trying to upload a file too big ({} bytes, max {} bytes)",
                            totalBytesRead, maxSize);
                    throw new VipException(DataManagementError.FILE_TOO_BIG, maxSize);
                }
                isFileEmpty = false;
                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
            return isFileEmpty;
        } catch (VipException e) {
            cleanupLocalUploadFile(path);
            throw e;
        } catch (IOException e) {
            logger.error("IO Error storing file {}", path, e);
            throw new VipException("Upload error", e);
        }
    }

    private void cleanupLocalUploadFile(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException cleanupException) {
            logger.warn("Could not cleanup temporary upload file {}", path, cleanupException);
        }
    }

    private String buildUniqueLocalUploadPath(String cleanFileName) {
        // DataManagerBusiness.getUploadRootDirectory(false) creates un unique folder
        return Path.of(dataManagerBusiness.getUploadRootDirectory(false),cleanFileName).toString();
    }

    private void checkUploadIsPossible(VipStoragePath destination, String actionLabel, VipError vipError) throws VipException {
        checkUploadIsPossible(null, destination, actionLabel, vipError);
    }

    private void checkUploadIsPossible(VipStoragePath destinationDir, VipStoragePath destination, String actionLabel, VipError vipError) throws VipException {
        if (destinationDir == null) {
            destinationDir = vipStoragePathFactory.create(destination.getVipPath().getParent());
        }
        if (destinationDir.isRootPath()) {
            logger.info("'{}' {} impossible in  /vip '{}'", getUserEmail(), actionLabel, destination);
            throw new VipException(vipError, destination, "Parent directory does not exist");
        }
        Optional<Data.Type> parentInfo = lfcBusiness.getPathInfo(destinationDir);
        if (parentInfo.isEmpty()) {
            logger.info("'{}' {} in a non-existing folder '{}'", getUserEmail(), actionLabel, destination);
            throw new VipException(vipError, destination, "Parent directory does not exist");
        }
        if ( ! parentInfo.get().equals(Data.Type.folder)) {
            logger.info("'{}' {} but the parent is a file '{}'", getUserEmail(), actionLabel, destination);
            throw new VipException(vipError, destination, "Parent directory is a file");
        }

        Optional<Data.Type> newDirInfo = lfcBusiness.getPathInfo(destination);
        if (newDirInfo.isPresent()) {
            logger.info("'{}' {} to '{}' but it already exists", getUserEmail(), actionLabel, destination);
            throw new VipException(vipError, destination, "A file or directory already exists");
        }
    }

    // ############### Async stuff

    private void waitForOperationOrTimeout(String operationId)
            throws VipException {
        Callable<Boolean> isDownloadOverCall = () -> isOperationOver(operationId);

        Callable<Boolean> waitForDownloadCall = () -> {
            while (true) {
                Future<Boolean> isDownloadOverFuture =
                        scheduler.schedule(isDownloadOverCall, getRetryDelay(), TimeUnit.SECONDS);
                if (isDownloadOverFuture.get()) {
                    return true;
                }
            }
        };

        Future<Boolean> completionFuture = scheduler.submit(waitForDownloadCall);
        timeoutOperationCompletionFuture(operationId, completionFuture, getTimeout());
    }

    private boolean isOperationOver(String operationId)
            throws VipException {
        PoolOperation.Status operationStatus = transferPoolBusiness.getOperationStatus(operationId);

        return switch (operationStatus) {
            case Queued, Running -> {
                logger.debug("status of operation {} : {}", operationId, operationStatus);
                yield false;
            }
            case Done -> true;
            default -> {
                logger.error("IO LFC Operation failed : {} : {}", operationId, operationStatus);
                throw new VipException("IO LFC Operation operation failed");
            }
        };
    }

    private void timeoutOperationCompletionFuture(
            String operationId,
            Future<Boolean> completionFuture, int timeoutInSeconds) throws VipException {
        try {
            completionFuture.get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Waiting for operation completion interrupted : {}", operationId, e);
            throw new VipException("Waiting for operation completion interrupted", e);
        } catch (ExecutionException e) {
            logger.error("Error waiting for operation completion : {}", operationId, e);
            throw new VipException("Error waiting for operation completion", e);
        } catch (TimeoutException e) {
            completionFuture.cancel(true);
            logger.error("Timeout operation completion : {}", operationId, e);
            throw new VipException("Aborting operation : too long", e);
        }
    }

    private Integer getRetryDelay() {
        return server.getCarminApiDownloadRetryInSeconds();
    }

    private Integer getTimeout() {
        return server.getCarminApiDownloadTimeoutInSeconds();
    }
}