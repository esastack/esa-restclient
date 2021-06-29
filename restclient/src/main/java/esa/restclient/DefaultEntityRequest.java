package esa.restclient;

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
    public Object bodyEntity() {
        return entity;
    }

}
