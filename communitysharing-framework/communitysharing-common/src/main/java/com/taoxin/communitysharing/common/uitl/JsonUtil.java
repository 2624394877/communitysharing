package com.taoxin.communitysharing.common.uitl;

import cn.hutool.core.lang.TypeReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON工具类，提供对象与JSON字符串之间的转换功能
 * @objectMapper 对象映射器 作用：将JSON字符串转换为对象，将对象转换为JSON字符串
 */
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper(); // 对象映射器

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 忽略未知属性
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // 忽略空对象
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(new JavaTimeModule()); // 添加时间模块
    }

    /**
     * 初始化对象映射器
     * @param ObjectMapper
     */
    public static void init(ObjectMapper ObjectMapper) {
        objectMapper = ObjectMapper;
    }

    /**
     * 将对象转换为JSON字符串
     * @param obj 待转换的对象
     * @return JSON字符串
     * @SneakyThrows 忽略方法抛出的异常
     */   
    @SneakyThrows
    public static String toJsonString(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * 将JSON字符串转换为对象
     * @param json JSON字符串
     * @param clazz 目标对象类型
     * @return 目标对象
     * @param <T> 目标对象类型
     */
    @SneakyThrows
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) { // JSON字符串为空
            return null;
        }
        return objectMapper.readValue(json, clazz); // 将JSON字符串转换为对象
    }

    /**

     将 JSON 字符串解析为指定键和值类型的 {@code Map<K, V>}。

     <p>示例：
     <pre>
     String json = "{"1": {"name":"a"}, "2": {"name":"b"}}";

     Map<Integer, User> map = parseMap(json, Integer.class, User.class);

     </pre>

     @param jsonStr 要解析的 JSON 字符串，不能为空或无效的 JSON

     @param keyClass Map 的键类型（例如 {@code String.class}、{@code Integer.class}）

     @param valueClass Map 的值类型（用于将每个值转换为目标类型）

     @param <K> 键的泛型类型

     @param <V> 值的泛型类型

     @return 按给定泛型类型解析后的 {@code Map<K, V>}（当输入为空或为空对象时，Jackson 可能返回空 Map 或抛出异常，取决于配置）

     @throws Exception 当 JSON 解析失败或类型转换错误时抛出（建议在调用处改为更具体的异常处理或封装为运行时异常）
     */
    public static <K, V> Map<K, V> parseMap(String jsonStr, Class<K> keyClass, Class<V> valueClass) throws Exception {
        // 创建 TypeReference，指定泛型类型
        TypeReference<Map<K, V>> typeRef = new TypeReference<Map<K, V>>() {
        };

        // 将 JSON 字符串转换为 Map
        return objectMapper.readValue(jsonStr, objectMapper.getTypeFactory().constructMapType(Map.class, keyClass, valueClass));
    }


    /**
     * 将 JSON 字符串解析为指定元素类型的 {@code List<T>}。
     * @param jsonStr 要解析的 JSON 字符串，不能为空或无效的 JSON
     * @param clazz 列表元素的类型（例如 {@code User.class}）
     * @return 按给定元素类型解析后的 {@code List<T>}（当输入为空或空数组时，Jackson 可能返回空 List 或抛出异常，取决于配置）
     * @param <T> 列表元素的泛型类型
     * @throws Exception 当 JSON 解析失败或类型转换错误时抛出（建议在调用处改为更具体的异常处理或封装为运行时异常）
     */
    public static <T> List<T> parseList(String jsonStr, Class<T> clazz) throws Exception {
        // 使用 TypeReference 指定 List<T> 的泛型类型
        TypeReference<List<T>> typeRef = new TypeReference<List<T>>() {
        };
        // 将json转换为列表
        return objectMapper.readValue(jsonStr, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    /**
     * 将 JSON 字符串解析为指定元素类型的 Set<T>。
     * @param jsonStr 要解析的 JSON 字符串，不能为空或无效的 JSON
     * @param clazz Set 元素的类型（例如 {@code User.class}）
     * @return 按给定元素类型解析后的 Set<T>（当输入为空或空数组时，Jackson 可能返回 空Set 或抛出异常，取决于配置）
     * @param <T> Set 元素的泛型类型
     * @throws Exception 当 JSON 解析失败或类型转换错误时抛出（建议在调用处改为更具体的异常处理或封装为运行时异常）
     */
    public static <T> Set<T> parseSet(String jsonStr, Class<T> clazz) throws Exception {
        // 使用 TypeReference 创建 Set<T> 的类型引用
        return objectMapper.readValue(jsonStr, new com.fasterxml.jackson.core.type.TypeReference<Set<T>>() {
            @Override
            public Type getType() {
                return objectMapper.getTypeFactory().constructCollectionType(Set.class, clazz);
            }
        });
    }
}
