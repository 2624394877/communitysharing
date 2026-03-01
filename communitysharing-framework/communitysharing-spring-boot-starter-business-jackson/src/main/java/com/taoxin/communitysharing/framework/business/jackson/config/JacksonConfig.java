package com.taoxin.communitysharing.framework.business.jackson.config;

import com.taoxin.communitysharing.common.constant.DateConstants;
import com.taoxin.communitysharing.framework.business.jackson.serializer.MultipartFileSerializer;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.YearMonthDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.YearMonthSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@AutoConfiguration
public class JacksonConfig {
    /**
     * 创建ObjectMapper对象
     * @return
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 忽略未知属性
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // 忽略空对象

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 忽略空对象

        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置时区为上海时区

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateConstants.LOCAL_DATE_TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DateConstants.LOCAL_DATE_TIME_PATTERN))); // 支持LocalDateTime
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DateConstants.LOCAL_DATE_PATTERN)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DateConstants.LOCAL_DATE_PATTERN))); // 支持LocalDate
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DateConstants.LOCAL_TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DateConstants.LOCAL_TIME_PATTERN))); // 支持LocalTime
        javaTimeModule.addSerializer(YearMonth.class, new YearMonthSerializer(DateTimeFormatter.ofPattern(DateConstants.YEAR_MONTH_PATTERN)));
        javaTimeModule.addDeserializer(YearMonth.class, new YearMonthDeserializer(DateTimeFormatter.ofPattern(DateConstants.YEAR_MONTH_PATTERN))); // 支持YearMonth
        objectMapper.registerModule(javaTimeModule); // 添加时间模块

        SimpleModule simpleModule = new SimpleModule(); // 创建SimpleModule对象
        simpleModule.addSerializer(MultipartFile.class, new MultipartFileSerializer()); // 添加MultipartFile序列化器
        objectMapper.registerModule(simpleModule); // 注册SimpleModule对象
        JsonUtil.init(objectMapper); // 传入ObjectMapper对象
        return objectMapper;
    }
}
