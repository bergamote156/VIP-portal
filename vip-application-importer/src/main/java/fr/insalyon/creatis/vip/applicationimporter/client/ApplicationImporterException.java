package fr.insalyon.creatis.vip.applicationimporter.client;

import fr.insalyon.creatis.vip.core.client.VipException;

public class ApplicationImporterException extends VipException {

    public ApplicationImporterException() {
    }

    public ApplicationImporterException(String message) {
        super(message);
    }

    public ApplicationImporterException(Throwable thrwbl) {
        super(thrwbl);
    }

    public ApplicationImporterException(String message, Throwable parent) {
        super(message, parent);
    }
}
