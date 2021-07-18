package top.meethigher.distributedhazelcastlock.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * JacksonUtil
 *
 * @author kit chen
 * @github https://github.com/meethigher
 * @blog https://meethigher.top
 * @time 2021/7/17
 */
public final class JacksonUtil {

    public static ObjectMapper mapper;

    /**
     * 使用泛型方法，把json字符串转换为相应的JavaBean对象。
     * (1)转换为普通JavaBean：readValue(json,Student.class)
     * (2)转换为List,如List<Student>,将第二个参数传递为Student
     * [].class.然后使用Arrays.asList();方法把得到的数组转换为特定类型的List
     *
     * @param jsonStr
     * @param valueType
     * @return
     */
    public static <T> T readValue(String jsonStr, Class<T> valueType) {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }

        try {
            return mapper.readValue(jsonStr, valueType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * json数组转List
     *
     * @param jsonStr
     * @param valueTypeRef
     * @return
     */
    public static <T> T readValue(String jsonStr, TypeReference<T> valueTypeRef) {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }

        try {
            return mapper.readValue(jsonStr, valueTypeRef);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 将字符串转list对象
     *
     * @param <T>
     * @param jsonStrs
     * @param cls
     * @return
     */
    public static <T> List<T> readValues(String jsonStrs, Class<T> cls) {
        ObjectMapper mapper = new ObjectMapper();
        List<T> objList = null;
        try {
            JavaType t = mapper.getTypeFactory().constructParametricType(
                    List.class, cls);
            objList = mapper.readValue(jsonStrs, t);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objList;
    }

    /**
     * 把JavaBean转换为json字符串
     *
     * @param object
     * @return
     */
    public static String toJSon(Object object) {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }

        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String jsonNodeToString(JsonNode jsonNode) throws JsonProcessingException {
        return mapper.writeValueAsString(jsonNode);
    }

    public static JsonNode toJsonNode(String string) {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }

        try {
            return mapper.readTree(string);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JsonNode toJsonNode(Object o) {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }

        try {
            return mapper.readTree(toJSon(o));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> jsonNodeToMap(JsonNode jsonNode) {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }

        try {
            Map<String, Object> result = mapper.convertValue(jsonNode, new TypeReference<Map<String, Object>>() {
            });
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}