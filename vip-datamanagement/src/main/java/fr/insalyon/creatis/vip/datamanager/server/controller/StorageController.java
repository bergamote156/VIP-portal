package fr.insalyon.creatis.vip.datamanager.server.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonView;

import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.server.inter.DataViews;
import fr.insalyon.creatis.vip.datamanager.models.Data;
import fr.insalyon.creatis.vip.datamanager.models.DataManagementError;
import fr.insalyon.creatis.vip.datamanager.models.PoolOperation;
import fr.insalyon.creatis.vip.datamanager.models.StorageDownloadRequest;
import fr.insalyon.creatis.vip.datamanager.server.business.StorageBusiness;

@RestController
@RequestMapping("/storage")
public class StorageController {

    private static final Logger logger = LoggerFactory.getLogger(StorageController.class);

    private final StorageBusiness storageBusiness;
    @Autowired
    public StorageController(StorageBusiness storageBusiness) {
        this.storageBusiness = storageBusiness;
    }

    @GetMapping(value = {"/directories", "/directories/{*path}"})
    public List<Data> listDirectory(
            @PathVariable(required = false) String path,
            @RequestParam(defaultValue = "false") boolean refresh) throws VipException {
        // TODO : handle refresh
        String resolvedPath = resolveStoragePath(path);
        return storageBusiness.listDir(resolvedPath);
    }

    private String resolveStoragePath(String path) throws VipException {
        if (path == null || path.isBlank()) {
            logger.info("null or empty path provided in StorageController : {}", path);
            throw new VipException(DataManagementError.INVALID_STORAGE_PATH, path, "must start with '/vip");
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    // GET /storage/<path>
    @GetMapping(value = {"", "/", "/{*path}"})
    public Data getPathMetadata(@PathVariable(required = false) String path) throws VipException {
        throw new VipException("Not implemented");
    }

    @JsonView(DataViews.User.class)
    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PoolOperation uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestPart("destination") String destination) throws VipException {
        String resolvedPath = resolveStoragePath(destination);
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new VipException(DataManagementError.INVALID_UPLOAD, resolvedPath, "Upload file name is required");
        }

        try {
            String operationId = storageBusiness.submitUploadFromInputStream(
                    destination,
                    file.getOriginalFilename(),
                    file.getInputStream());
            return new PoolOperation(operationId, PoolOperation.Status.Queued);
        } catch (IOException e) {
            throw new VipException(e);
        }
    }

    @PostMapping(value = "/directories/{*path}")
    @ResponseStatus(HttpStatus.CREATED)
    public void createDirectory(@PathVariable String path) throws VipException {
        String fullPath = resolveStoragePath(path);
        storageBusiness.createDirectory(fullPath);
    }

    @JsonView(DataViews.User.class)
    @PostMapping(value = "/downloads")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PoolOperation submitDownload(
            @RequestBody @NotNull StorageDownloadRequest request) throws VipException {
        if (request == null || request.getPath() == null || request.getPath().isBlank()) {
            logger.info("null or empty path provided in StorageController");
            throw new VipException(DataManagementError.INVALID_STORAGE_PATH,
                    request == null ? null : request.getPath(),
                    "must start with '/vip");
        }

        String operationId = storageBusiness.submitDownload(request.getPath());
        return new PoolOperation(operationId, PoolOperation.Status.Queued);
    }

    @JsonView(DataViews.User.class)
    @GetMapping(value = "/operations/{operationId}")
    public PoolOperation getOperationStatus(@PathVariable String operationId) throws VipException {
        return storageBusiness.getOperation(operationId);
    }

    @GetMapping(value = "/downloads/{operationId}/content")
    public ResponseEntity<Resource> getDownloadContent(@PathVariable String operationId) throws VipException {
        File file = storageBusiness.getDownloadFileIfReady(operationId);

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.getName())
                                .build()
                                .toString())
                .body(resource);
    }

    @DeleteMapping(value = "/{*path}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(@PathVariable String path) throws VipException {
        String resolvedPath = resolveStoragePath(path);
        storageBusiness.deletePath(resolvedPath);
    }
}
