package esa.restclient.core.request;

public class DefaultEntityRequest extends DefaultExecutableRequest implements EntityRequest {
    private volatile Object entity;

    DefaultEntityRequest(ExecutableRequest executableRequest, Object entity) {
        super(executableRequest);
        this.entity = entity;
    }

    @Override
    public Object entity() {
        return entity;
    }
}
