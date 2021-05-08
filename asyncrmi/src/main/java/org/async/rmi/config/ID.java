package org.async.rmi.config;

import java.io.File;

/**
 * Created by Barak Bar Orion
 * 12/12/14.
 */
public class ID {
    private File key;
    private File certificate;

    public ID() {
    }

    public ID(File key, File certificate) {
        this.key = key;
        this.certificate = certificate;
    }

    public void setKey(File key) {
        this.key = key;
    }

    public void setCertificate(File certificate) {
        this.certificate = certificate;
    }

    public File getKey() {
        return key;
    }

    public File getCertificate() {
        return certificate;
    }
}


