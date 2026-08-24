package fr.insalyon.creatis.vip.core.models;

import com.fasterxml.jackson.annotation.JsonView;

import fr.insalyon.creatis.vip.core.server.inter.DataViews;
import jakarta.validation.constraints.NotNull;

@JsonView(DataViews.User.class)
public class UserAndPassword {

    @NotNull
    public User user;
    @NotNull
    public String password;
    public String comment;
}
