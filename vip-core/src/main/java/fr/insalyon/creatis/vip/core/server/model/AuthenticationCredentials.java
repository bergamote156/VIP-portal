package fr.insalyon.creatis.vip.core.server.model;

import com.fasterxml.jackson.annotation.JsonView;

import fr.insalyon.creatis.vip.core.server.inter.DataViews;
import jakarta.validation.constraints.NotNull;

@JsonView(DataViews.User.class)
public class AuthenticationCredentials {

    private String username;
    private String password;

    @NotNull
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
