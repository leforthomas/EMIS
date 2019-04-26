package com.geocento.webapps.earthimages.emis.application.share;

import java.io.Serializable;
import java.util.List;

public class FileDTO implements Serializable {

    String name;
    Long sizeInBytes;
    // only for directories
    List<FileDTO> files;
    private String path;

    public FileDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public List<FileDTO> getFiles() {
        return files;
    }

    public void setFiles(List<FileDTO> files) {
        this.files = files;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
