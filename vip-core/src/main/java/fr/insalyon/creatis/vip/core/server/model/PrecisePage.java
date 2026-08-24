package fr.insalyon.creatis.vip.core.server.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;

import fr.insalyon.creatis.vip.core.server.inter.DataViews;

@JsonView(DataViews.User.class)
public class PrecisePage<T> {

    public List<T> data;
    public int total;
}
