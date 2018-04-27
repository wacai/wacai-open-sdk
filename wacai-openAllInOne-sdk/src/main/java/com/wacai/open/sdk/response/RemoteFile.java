package com.wacai.open.sdk.response;

import java.io.Serializable;


public class RemoteFile implements Serializable{

    private String filename;

    private String originalName;

    private String namespace;

    private String secretKey;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    @Override
    public String toString() {
        return "RemoteFile{" +
                "filename='" + filename + '\'' +
                ", namespace='" + namespace + '\'' +
                ", secretKey='" + secretKey + '\'' +
                '}';
    }
}
