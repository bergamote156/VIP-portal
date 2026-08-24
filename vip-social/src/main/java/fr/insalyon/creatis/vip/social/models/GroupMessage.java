package fr.insalyon.creatis.vip.social.models;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import com.google.gwt.user.client.rpc.IsSerializable;

import fr.insalyon.creatis.vip.core.models.User;
import fr.insalyon.creatis.vip.core.server.inter.DataViews;

/**
 *
 * @author Rafael Silva
 */
@JsonView(DataViews.User.class)
public class GroupMessage implements IsSerializable {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private User sender;
    @NotBlank
    private String groupName;
    @NotBlank
    private String title;
    @NotBlank
    private String message;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String posted;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date postedDate;

    public GroupMessage() {
    }

    public GroupMessage(long id, User sender, String groupName, String title, 
            String message, String posted, Date postedDate) {

        this.id = id;
        this.sender = sender;
        this.groupName = groupName;
        this.title = title;
        this.message = message;
        this.posted = posted;
        this.postedDate = postedDate;
    }

    public String getGroupName() {
        return groupName;
    }

    public long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getPosted() {
        return posted;
    }

    public Date getPostedDate() {
        return postedDate;
    }

    public User getSender() {
        return sender;
    }

    public String getTitle() {
        return title;
    }
}
