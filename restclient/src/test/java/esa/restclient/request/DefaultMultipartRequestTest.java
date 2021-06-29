package esa.restclient.request;

import esa.commons.http.HttpVersion;
import esa.restclient.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMultipartRequestTest {
    private final static String httpUrl = "http://localhost:8080";

    @Test
    void testBasicFunction() {
        MultipartRequest multipartRequest = RestClient.ofDefault().post(httpUrl).multipart();
        DefaultHttpRequestTest.testHeaderOperate(multipartRequest);
        DefaultHttpRequestTest.testHeadersOperate(multipartRequest);
        DefaultHttpRequestTest.testContentTypeOperate(multipartRequest);
        DefaultHttpRequestTest.testAcceptOperate(multipartRequest);
        DefaultHttpRequestTest.testHttpVersion(multipartRequest, multipartRequest.version());
        DefaultExecutableRequestTest.testPropertyOperate(multipartRequest);
        DefaultHttpRequestTest.testCookieOperate(multipartRequest);
        DefaultHttpRequestTest.testParamOperate(multipartRequest);
        MultipartRequest multipartRequest1 = RestClient.ofDefault().post(httpUrl).multipart();
        DefaultHttpRequestTest.testParamsOperate(multipartRequest1);
        multipartRequest.disableExpectContinue();
        assertFalse(multipartRequest.isUseExpectContinue());
        multipartRequest.enableUriEncode();
        assertTrue(multipartRequest.uriEncode());
        assertThrows(IllegalArgumentException.class, () -> multipartRequest.readTimeout(-1));
        assertThrows(IllegalArgumentException.class, () -> multipartRequest.maxRedirects(-1));
        assertThrows(IllegalArgumentException.class, () -> multipartRequest.maxRetries(-1));
        assertThrows(IllegalArgumentException.class, () -> multipartRequest.maxRetries(0));
        multipartRequest.readTimeout(5);
        multipartRequest.maxRedirects(6);
        multipartRequest.maxRetries(7);
        assertEquals(5, multipartRequest.readTimeout());
        assertEquals(6, multipartRequest.maxRedirects());
        assertEquals(7, multipartRequest.maxRetries());
    }

    @Test
    void testMultiAttrsSet() {
        String name1 = "name1";
        String name2 = "name2";
        String name3 = "name3";
        String value1 = "value1";
        String value2 = "value2";
        String value3 = "value3";
        RestClientBuilder builder = RestClient.create();
        builder.version(HttpVersion.HTTP_1_0);
        MultipartRequest multipartRequest = RestClient.ofDefault().post(httpUrl).multipart();
        multipartRequest.attr(name3, null);
        multipartRequest.attr(null, value3);
        assertEquals(0, multipartRequest.multipartItems().size());
        assertThrows(UnsupportedOperationException.class, () -> multipartRequest.multipartItems().add(new DefaultMultipartItem(new ContentDisposition.MultipartContentDisposition(name1, null),
                MediaType.TEXT_PLAIN, null, value1)));
        multipartRequest.attr(name1, value1);
        assertEquals(1, multipartRequest.multipartItems().size());
        MultipartItem expectMultipartItem = new DefaultMultipartItem(new ContentDisposition.MultipartContentDisposition(name1, null),
                MediaType.TEXT_PLAIN, null, value1);

        isMultipartItemEqual(expectMultipartItem, multipartRequest.multipartItems().get(0));

        multipartRequest.attr(name1, value2);
        assertEquals(2, multipartRequest.multipartItems().size());
        assertThrows(UnsupportedOperationException.class, () -> multipartRequest.multipartItems().add(new DefaultMultipartItem(new ContentDisposition.MultipartContentDisposition(name1, null),
                MediaType.TEXT_PLAIN, null, value1)));
        expectMultipartItem = new DefaultMultipartItem(new ContentDisposition.MultipartContentDisposition(name1, null),
                MediaType.TEXT_PLAIN, null, value2);
        isMultipartItemEqual(expectMultipartItem, multipartRequest.multipartItems().get(1));

        multipartRequest.attr(name2, value2, MediaType.APPLICATION_OCTET_STREAM);
        assertEquals(3, multipartRequest.multipartItems().size());
        expectMultipartItem = new DefaultMultipartItem(new ContentDisposition.MultipartContentDisposition(name2, null),
                MediaType.APPLICATION_OCTET_STREAM, null, value2);
        isMultipartItemEqual(expectMultipartItem, multipartRequest.multipartItems().get(2));

        multipartRequest.attr(name2, value2, MediaType.APPLICATION_OCTET_STREAM, "aaa");
        assertEquals(4, multipartRequest.multipartItems().size());
        expectMultipartItem = new DefaultMultipartItem(new ContentDisposition.MultipartContentDisposition(name2, null),
                MediaType.APPLICATION_OCTET_STREAM, "aaa", value2);
        isMultipartItemEqual(expectMultipartItem, multipartRequest.multipartItems().get(3));

        File file = new File("aaa");
        multipartRequest.file(name1, file);
        assertEquals(5, multipartRequest.multipartItems().size());
        expectMultipartItem = new DefaultMultipartItem(new ContentDisposition.MultipartContentDisposition(name1, file.getName()),
                MediaType.APPLICATION_OCTET_STREAM, null, file);
        isMultipartItemEqual(expectMultipartItem, multipartRequest.multipartItems().get(4));

        multipartRequest.file(name2, file, MediaType.APPLICATION_JSON);
        assertEquals(6, multipartRequest.multipartItems().size());
        expectMultipartItem = new DefaultMultipartItem(new ContentDisposition.MultipartContentDisposition(name2, file.getName()),
                MediaType.APPLICATION_JSON, null, file);
        isMultipartItemEqual(expectMultipartItem, multipartRequest.multipartItems().get(5));

        multipartRequest.file(name2, "bbb", file, MediaType.APPLICATION_JSON, "aaa");
        assertEquals(7, multipartRequest.multipartItems().size());
        expectMultipartItem = new DefaultMultipartItem(new ContentDisposition.MultipartContentDisposition(name2, "bbb"),
                MediaType.APPLICATION_JSON, "aaa", file);
        isMultipartItemEqual(expectMultipartItem, multipartRequest.multipartItems().get(6));

        multipartRequest.file(name2, "bbb", file, MediaType.APPLICATION_JSON);
        assertEquals(8, multipartRequest.multipartItems().size());
        expectMultipartItem = new DefaultMultipartItem(new ContentDisposition.MultipartContentDisposition(name2, "bbb"),
                MediaType.APPLICATION_JSON, null, file);
        isMultipartItemEqual(expectMultipartItem, multipartRequest.multipartItems().get(7));
    }

    private void isMultipartItemEqual(MultipartItem expectMultipartItem, MultipartItem actualMultipartItem) {
        if (expectMultipartItem == null) {
            assertNull(actualMultipartItem);
            return;
        }
        assertNotNull(actualMultipartItem);
        assertEquals(expectMultipartItem.attribute(), actualMultipartItem.attribute());
        assertEquals(expectMultipartItem.contentTransferEncoding(), actualMultipartItem.contentTransferEncoding());
        Assertions.assertEquals(expectMultipartItem.contentType(), actualMultipartItem.contentType());
        assertEquals(expectMultipartItem.contentDisposition().name(), actualMultipartItem.contentDisposition().name());
        assertEquals(expectMultipartItem.contentDisposition().fileName(), actualMultipartItem.contentDisposition().fileName());
        assertEquals(expectMultipartItem.contentDisposition().type(), actualMultipartItem.contentDisposition().type());
    }

}
