package esa.restclient.core.request;

public class DefaultEntityRequest extends DefaultExecutableRequest implements EntityRequest {
    private volatile Object entity;

    DefaultEntityRequest(DefaultExecutableRequest executableRequest, Object entity) {
        super(executableRequest);
        this.entity = entity;
    }

    @Override
    public Object entity() {
        return entity;
    }

    @Override
    protected Object getBodyObj() {
        return entity;
    }
}
