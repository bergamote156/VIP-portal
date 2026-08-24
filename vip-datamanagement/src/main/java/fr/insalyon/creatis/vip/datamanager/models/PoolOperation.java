package fr.insalyon.creatis.vip.datamanager.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.Date;

import fr.insalyon.creatis.vip.core.server.inter.DataViews;

/**
 *
 * @author Rafael Ferreira da Silva
 */
public class PoolOperation implements IsSerializable {

    public static enum Type {

        Download, Upload, Delete
    };

    public static enum Status {

        Queued, Running, Done, Failed, Rescheduled
    };
    @JsonView(DataViews.User.class)
    private String id;
    @JsonView(DataViews.Developer.class)
    private Date registration;
    @JsonView(DataViews.Developer.class)
    private String parsedRegistration;
    @JsonView(DataViews.Developer.class)
    private String source;
    @JsonView(DataViews.Developer.class)
    private String dest;
    @JsonView(DataViews.Developer.class)
    private Type type;
    @JsonView(DataViews.User.class)
    private Status status;
    @JsonView(DataViews.Developer.class)
    private String user;
    @JsonView(DataViews.Developer.class)
    private int progress;

    public PoolOperation() {
    }

    public PoolOperation(String id, Status status) {
        this.id = id;
        this.status = status;
    }

    /**
     * 
     * @param id
     * @param registration
     * @param source
     * @param dest
     * @param type
     * @param status
     * @param user
     */
    public PoolOperation(String id, Date registration, String parsedResgistration,
            String source, String dest, Type type, Status status, String user, 
            int progress) {

        this.id = id;
        this.registration = registration;
        this.parsedRegistration = parsedResgistration;
        this.source = source;
        this.dest = dest;
        this.type = type;
        this.status = status;
        this.user = user;
        this.progress = progress;
    }

    public String getDest() {
        return dest;
    }

    @JsonProperty("operationId")
    @JsonView(DataViews.User.class)
    public String getId() {
        return id;
    }

    @JsonView(DataViews.Developer.class)
    public Date getRegistration() {
        return registration;
    }

    @JsonView(DataViews.Developer.class)
    public String getSource() {
        return source;
    }

    @JsonView(DataViews.User.class)
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }

    @JsonView(DataViews.Developer.class)
    public Type getType() {
        return type;
    }

    @JsonView(DataViews.Developer.class)
    public String getUser() {
        return user;
    }

    @JsonView(DataViews.Developer.class)
    public String getParsedRegistration() {
        return parsedRegistration;
    }

    @JsonView(DataViews.Developer.class)
    public int getProgress() {
        return progress;
    }
}
