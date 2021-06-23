package esa.restclient.core.request;

import esa.commons.Checks;

public interface ContentDisposition {

    String type();

    String name();

    String fileName();

    static ContentDisposition multipartContentDisposition(String name) {
        return new MultipartContentDisposition(name, null);
    }

    static ContentDisposition multipartContentDisposition(String name, String fileName) {
        return new MultipartContentDisposition(name, fileName);
    }

    class MultipartContentDisposition implements ContentDisposition {
        private static final String FORM_DATA = "form-data";
        private final String name;
        private final String fileName;

        MultipartContentDisposition(String name, String fileName) {
            Checks.checkNotNull(name, "Name must be not null!");
            this.name = name;
            this.fileName = fileName;
        }

        @Override
        public String type() {
            return FORM_DATA;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String fileName() {
            return fileName;
        }
    }
}
