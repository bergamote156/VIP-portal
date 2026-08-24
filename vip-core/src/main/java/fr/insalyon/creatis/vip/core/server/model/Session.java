package fr.insalyon.creatis.vip.core.server.model;

import com.fasterxml.jackson.annotation.JsonView;

import fr.insalyon.creatis.vip.core.client.view.user.UserLevel;
import fr.insalyon.creatis.vip.core.server.inter.DataViews;

@JsonView(DataViews.User.class)
public class Session {

    public String id;
    public String email;
    public String password;
    public UserLevel userlevel;
    public Boolean confirmed;
}
