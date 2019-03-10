package com.xhinliang.jugg.util;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;

/**
 * Json Mapper Static Utils
 */
public final class JsonMapperUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.disable(FAIL_ON_UNKNOWN_PROPERTIES);
        MAPPER.registerModule(new ProtobufModule());
        MAPPER.registerModule(new GuavaModule());
        MAPPER.registerModule(new ParameterNamesModule());
    }

    public static String toPrettyJson(@Nullable Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
