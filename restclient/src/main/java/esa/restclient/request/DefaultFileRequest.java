package esa.restclient.request;

import java.io.File;

public class DefaultFileRequest extends DefaultExecutableRequest implements FileRequest {
    private volatile File file;


    DefaultFileRequest(DefaultExecutableRequest executableRequest, File file) {
        super(executableRequest);
        this.file = file;
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public Object bodyEntity() {
        return file;
    }
}
