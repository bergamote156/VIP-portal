package fr.insalyon.creatis.vip.publication.models;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.gwt.user.client.rpc.IsSerializable;


public enum PublicationType implements IsSerializable {

    ConferenceArticle("Article In Conference Proceedings"),
    Journal("Journal Article"),
    BookChapter("Book Chapter"),
    Other("Other");

    private final String label;

    PublicationType(String label) {
        this.label = label;
    }

    @JsonValue
    @Override
    public String toString() {
        return label;
    }

    public static PublicationType fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        for (PublicationType type : values()) {
            if (type.label.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown publication type: " + value);
    }
}