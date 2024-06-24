package io.quarkus.ts.http.restclient.reactive.multipart;

import java.util.List;
import java.util.Map;

public class Item {
    public final String name;
    public final long size;
    public final String charset;
    public final String fileContent;

    public String getFileContent() {
        return fileContent;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getCharset() {
        return charset;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isFileItem() {
        return isFileItem;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public final String fileName;
    public final boolean isFileItem;
    public final Map<String, List<String>> headers;
    public boolean fileItem;

    public Item(String name, long size, String charset, String fileName, boolean isFileItem,
            Map<String, List<String>> headers, boolean fileItem, String fileContent) {
        this.name = name;
        this.size = size;
        this.charset = charset;
        this.fileName = fileName;
        this.isFileItem = isFileItem;
        this.headers = headers;
        this.fileItem = fileItem;
        this.fileContent = fileContent;
    }
}
