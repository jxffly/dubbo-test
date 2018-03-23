package com.fly.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;


/**
 * 战国 2017年09月11日16:43:35
 */
public class JsonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);


    private static final ObjectMapper mapper = new ObjectMapper();

    private static final ObjectMapper EXCLUDE_NULL_MAPPER = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        EXCLUDE_NULL_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        EXCLUDE_NULL_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        EXCLUDE_NULL_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);//默认不导出为空的字段

    }

    private JsonUtils() {
    }





    public static <T> T json2Bean(String jsonStr, Class<T> elementClasses) {
        if (String.class.equals(elementClasses)) {
            return (T) jsonStr;
        }
        try {
            return mapper.readValue(jsonStr, elementClasses);
        } catch (IOException e) {
            LOGGER.warn("json to bean error,source json:{} ", jsonStr, e);
            throw new RuntimeException(e);
        }
    }


    public static <T> ArrayList<T> json2ListBean(String jsonStr, Class<T> elementClasses) {
        try {
            return mapper.readValue(jsonStr, getCollectionType(ArrayList.class, elementClasses));
        } catch (IOException e) {
            LOGGER.warn("json2ListBean error message", e);
            throw new RuntimeException(e);
        }
    }


    private static JavaType getCollectionType(Class<?> collectionClass, Class<?> elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }


    public static void main(String[] args) {

    }


    public static String toJson(Object object) {
        try {
            if (object instanceof String) {
                return String.valueOf(object);
            }
            return EXCLUDE_NULL_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.warn("bean2Json ERROR ,object:{}", object, e);
            throw new RuntimeException(e);
        }

    }

}
