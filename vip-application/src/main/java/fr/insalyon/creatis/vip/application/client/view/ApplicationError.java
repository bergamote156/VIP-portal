package fr.insalyon.creatis.vip.application.client.view;

import fr.insalyon.creatis.vip.core.client.VipError;

public enum ApplicationError implements VipError {

    PLATFORM_MAX_EXECS(2000, "Max number of running executions reached on the platform.", 0),
    USER_MAX_EXECS(2001, "Max number of running executions reached.<br />You already have {} running executions.", 1),
    WRONG_APPLICATION_DESCRIPTOR(2002, "Error getting application descriptor for {}.", 1),
    ENGINE_SATURATED(2003, "Engine is saturated!", 0),
    LAUNCH_ERROR(2004, "Error launching execution, contact admins!", 0),
    BOUTIQUES_FILE_NOT_VALID(2005, "Boutiques file not valid: {}", 1),
    INVALID_WORKFLOW_INPUT(2006, "Invalid workflow value for input {} : {}", 2),
    WORKFLOW_LAUNCH_IMPOSSIBLE(2007, "The workflow could not be launched. Reason : {}", 1);

    private final String message;
    private final Integer code;
    private final Integer expectedParams;
    private final Integer httpCode;

    private ApplicationError(Integer code, String message, Integer expectedParams) {
        this.message = message;
        this.code = code;
        this.expectedParams = expectedParams;
        this.httpCode = 400;
    }

    @Override public Integer getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public Integer getExpectedParameters() { return expectedParams; }
    @Override public Integer getHttpCode() { return httpCode; }
}
