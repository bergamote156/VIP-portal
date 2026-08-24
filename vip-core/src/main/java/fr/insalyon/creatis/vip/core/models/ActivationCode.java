package fr.insalyon.creatis.vip.core.models;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.gwt.user.client.rpc.IsSerializable;
import fr.insalyon.creatis.vip.core.server.inter.DataViews;

import jakarta.validation.constraints.NotBlank;

@JsonView(DataViews.User.class)
public class ActivationCode implements IsSerializable {

    @NotBlank
    private String code;

    public ActivationCode() {
    }

    public ActivationCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
