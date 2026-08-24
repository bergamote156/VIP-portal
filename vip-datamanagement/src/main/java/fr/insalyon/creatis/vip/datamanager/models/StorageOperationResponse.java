package fr.insalyon.creatis.vip.datamanager.models;

import com.fasterxml.jackson.annotation.JsonView;
import fr.insalyon.creatis.vip.core.server.inter.DataViews;

@JsonView(DataViews.User.class)
public class StorageOperationResponse {

    private String operationId;
    private String status;

    public StorageOperationResponse() {}

    public StorageOperationResponse(String operationId, String status) {
        this.operationId = operationId;
        this.status = status;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
