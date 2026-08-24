package fr.insalyon.creatis.vip.datamanager.models;

import com.fasterxml.jackson.annotation.JsonView;
import fr.insalyon.creatis.vip.core.server.inter.DataViews;

@JsonView(DataViews.User.class)
public class StorageDownloadRequest {

    private String path;

    public StorageDownloadRequest() {}

    public StorageDownloadRequest(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
