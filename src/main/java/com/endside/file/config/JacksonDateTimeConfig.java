package com.endside.file.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonDateTimeConfig {

    private static final DateTimeFormatter LDT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter LD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Bean
    public JsonMapperBuilderCustomizer chalkDateTimeCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(LDT));
            module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(LDT));
            module.addSerializer(LocalDate.class, new LocalDateSerializer(LD));
            module.addDeserializer(LocalDate.class, new LocalDateDeserializer(LD));
            builder.addModule(module);
        };
    }
}
