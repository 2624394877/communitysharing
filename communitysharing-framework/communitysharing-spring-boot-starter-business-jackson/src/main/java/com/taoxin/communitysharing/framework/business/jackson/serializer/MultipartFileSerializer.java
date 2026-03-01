package com.taoxin.communitysharing.framework.business.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MultipartFileSerializer extends JsonSerializer<MultipartFile> {
    @Override
    public void serialize(MultipartFile value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeStartObject();
            gen.writeStringField("originalFilename", value.getOriginalFilename());
            gen.writeNumberField("size", value.getSize());
            gen.writeStringField("contentType", value.getContentType());
            gen.writeEndObject();
        }
    }
}
