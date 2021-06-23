package esa.restclient.core.request;

import java.io.File;

public class DefaultFileRequest extends DefaultExecutableRequest implements FileRequest {
    private volatile File file;


    DefaultFileRequest(ExecutableRequest executableRequest, File file) {
        super(executableRequest);
        this.file = file;
    }

    @Override
    public File file() {
        return file;
    }
}
