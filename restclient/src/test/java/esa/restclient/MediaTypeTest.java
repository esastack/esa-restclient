package esa.restclient;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MediaTypeTest {

    @Test
    void testValueOf() {
        MediaType mediaType = MediaType.valueOf(MediaType.APPLICATION_JSON_UTF8_VALUE);
        assertEquals(StandardCharsets.UTF_8, mediaType.charset());
        assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, mediaType.value());
        assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, mediaType.toString());
    }

}
