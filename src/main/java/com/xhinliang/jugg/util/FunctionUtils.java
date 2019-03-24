package com.xhinliang.jugg.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.xhinliang.jugg.exception.JuggRuntimeException;

/**
 * @author xhinliang
 */
public class FunctionUtils {

    private static final int MAX_JSON_LENGTH = 1000000;

    public static String getJsonLimited(Object object) {
        String json = JsonMapperUtils.toPrettyJson(object);
        if (json != null && json.length() >= MAX_JSON_LENGTH) {
            json = json.substring(0, MAX_JSON_LENGTH);
        }
        return json;
    }

    public static String exceptionToString(Exception e) {
        if (e instanceof JuggRuntimeException) {
            return e.getMessage();
        }
        return String.join("\n", ExceptionUtils.getRootCauseStackTrace(e));
    }

    public static File getTempFileFromInputStream(InputStream in) throws IOException {
        File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            // copy stream
            // CHECKSTYLE:OFF
            byte[] buffer = new byte[1024];
            // CHECKSTYLE:ON
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }
}
