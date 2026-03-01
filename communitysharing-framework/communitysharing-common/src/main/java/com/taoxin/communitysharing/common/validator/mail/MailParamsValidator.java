package com.taoxin.communitysharing.common.validator.mail;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MailParamsValidator implements ConstraintValidator<MailParams, String> {
    @Override
    public void initialize(MailParams constraintAnnotation) {
        // TODO: 验证邮箱参数
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // TODO: 验证邮箱参数
        return value != null && value.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    }
}
