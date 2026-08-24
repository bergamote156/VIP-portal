package fr.insalyon.creatis.vip.datamanager.models;

import fr.insalyon.creatis.vip.core.client.VipError;

/**
 * Error codes for storage operations.
 */
public enum DataManagementError implements VipError {

    INVALID_STORAGE_PATH(4000, "The provided path ({}) is invalid because : {}", 2),
    STORAGE_PERMISSION_ERROR(4001, "Permission denied for '{}' : '{}'", 2, 403),
    RESOURCE_NOT_FOUND_ERROR(4002, "File or directory not found: {}", 1, 404),
    INVALID_OPERATION(4003, "Issue with operation {}, {}", 2),
    INVALID_DIRECTORY_LISTING(4004, "Error listing directory {} : {}", 2),
    INVALID_DIRECTORY_CREATION(4005, "Error creating directory {} : {}", 2),
    OPERATION_PERMISSION_ERROR(4006, "Permission denied for operation {}", 1, 403),
    INVALID_DOWNLOAD(4007, "Error downloading {} : {}", 2),
    INVALID_UPLOAD(4008, "Error uploading to {} : {}", 2),
    FILE_TOO_BIG(4009, "File too big. Max size : {}", 1);

    private final Integer code;
    private final String message;
    private final Integer expectedParams;
    private final Integer httpCode;

    DataManagementError(Integer code, String message, Integer expectedParams, Integer httpCode) {
        this.code = code;
        this.message = message;
        this.expectedParams = expectedParams;
        this.httpCode = httpCode;
    }

    DataManagementError(Integer code, String message, Integer expectedParams) {
        this(code, message, expectedParams, 400);
    }

    @Override public Integer getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public Integer getExpectedParameters() { return expectedParams; }
    @Override public Integer getHttpCode() { return httpCode; }
}
