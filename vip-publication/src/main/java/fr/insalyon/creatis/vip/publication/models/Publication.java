package fr.insalyon.creatis.vip.publication.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.gwt.user.client.rpc.IsSerializable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import fr.insalyon.creatis.vip.core.server.inter.DataViews;

/**
 *
 * @author Nouha Boujelben
 */
@JsonView(DataViews.User.class)
public class Publication implements IsSerializable {
    

    private Long id;
    @NotBlank
    private String title;
    @NotBlank
    private String date;
    private String doi;
    @NotBlank
    private String authors;
    @NotNull
    private PublicationType type;
    @NotBlank
    private String typeName;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String vipAuthor;
    @NotBlank
    private String vipApplication;

    public Publication() {
    }
    

    public Publication(Long id, String title, String date, String doi, String authors, PublicationType type, String typeName, String vipAuthor, String vipApplication) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.doi = doi;
        this.authors = authors;
        this.type = type;
        this.typeName = typeName;
        this.vipAuthor = vipAuthor;
        this.vipApplication = vipApplication;
    }
    
    public Publication( Long id,String title, String date, String doi, String authors, String type, String typeName, String vipApplication) {
        this.id=id;
        this.title = title;
        this.date = date;
        this.doi = doi;
        this.authors = authors;
        this.type = PublicationType.fromValue(type);
        this.typeName = typeName;
        this.vipApplication = vipApplication;
    }
    
    public Publication(String title, String date, String doi, String authors, String type, String typeName,String vipAuthor, String vipApplication) {
        this.title = title;
        this.date = date;
        this.doi = doi;
        this.authors = authors;
        this.type = PublicationType.fromValue(type);
        this.typeName = typeName;
        this.vipAuthor=vipAuthor;
        this.vipApplication = vipApplication;
    }

    public Publication(Long id, String title, String date, String doi, String authors, PublicationType type, String typeName, String vipApplication) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.doi = doi;
        this.authors = authors;
        this.type = type;
        this.typeName = typeName;
        this.vipApplication = vipApplication;
    }

    public Publication(String title, String date, String doi, String authors, PublicationType type, String typeName, String vipAuthor, String vipApplication) {
        this.title = title;
        this.date = date;
        this.doi = doi;
        this.authors = authors;
        this.type = type;
        this.typeName = typeName;
        this.vipAuthor = vipAuthor;
        this.vipApplication = vipApplication;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getType() {
        return type == null ? null : type.toString();
    }

    public PublicationType getTypeEnum() {
        return type;
    }

    public void setType(String type) {
        this.type = PublicationType.fromValue(type);
    }

    public void setType(PublicationType type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getVipAuthor() {
        return vipAuthor;
    }

    public void setVipAuthor(String vipAuthor) {
        this.vipAuthor = vipAuthor;
    }

    public String getVipApplication() {
        return vipApplication;
    }

    public void setVipApplication(String vipApplication) {
        this.vipApplication = vipApplication;
    }



}
