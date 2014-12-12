package org.async.rmi.tls;

import java.io.File;

/**
 * Created by Barak Bar Orion
 * 12/12/14.
 */
public class JKS {
    private File jksFile;
    private String alias;
    private String filePassword;
    private String aliasPassword;

    public JKS() {
    }

    public JKS(File jksFile, String alias) {
        this.jksFile = jksFile;
        this.alias = alias;
    }

    public JKS(File jksFile, String alias, String filePassword, String aliasPassword) {
        this.jksFile = jksFile;
        this.alias = alias;
        this.filePassword = filePassword;
        this.aliasPassword = aliasPassword;
    }

    public File getJksFile() {
        return jksFile;
    }

    public void setJksFile(File jksFile) {
        this.jksFile = jksFile;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getFilePassword() {
        return filePassword;
    }

    public void setFilePassword(String filePassword) {
        this.filePassword = filePassword;
    }

    public String getAliasPassword() {
        return aliasPassword;
    }

    public void setAliasPassword(String aliasPassword) {
        this.aliasPassword = aliasPassword;
    }
}
