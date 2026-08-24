package fr.insalyon.creatis.vip.social.models;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import fr.insalyon.creatis.vip.core.models.User;
import fr.insalyon.creatis.vip.core.server.inter.DataViews;

import java.util.Date;
import jakarta.validation.constraints.NotBlank;

/**
 *
 * @author Rafael Silva
 */
@JsonView(DataViews.User.class)
public class Message implements IsSerializable {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private User sender;
    private User[] receivers;
    @NotBlank
    private String title;
    @NotBlank
    private String message;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String posted;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date postedDate;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean read;

    public Message() {
    }

    public Message(long id, User sender, User receiver, String title, String message,
            String posted, Date postedDate, boolean read) {

        this(id, sender, new User[]{receiver}, title, message, posted, postedDate, read);
    }

    public Message(long id, User sender, User[] receivers, String title, String message, String posted, Date postedDate, boolean read) {
        this.id = id;
        this.sender = sender;
        this.receivers = receivers;
        this.title = title;
        this.message = message;
        this.posted = posted;
        this.postedDate = postedDate;
        this.read = read;
    }

    public long getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getPosted() {
        return posted;
    }

    public boolean isRead() {
        return read;
    }

    public String getTitle() {
        return title;
    }

    public User[] getReceivers() {
        return receivers;
    }

    public Date getPostedDate() {
        return postedDate;
    }
}
