package com.taoxin.communitysharing.auth.smtp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component // 标记为组件
@Slf4j
public class EmailSmtpHelper {
    @Resource
    private EmailProperties emailProperties;
    @Resource
    private JavaMailSender javaMailSender;

    public boolean sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailProperties.getUsername());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            javaMailSender.send(message);
            log.info("邮件发送成功, 收件人: {}, 主题: {}, 内容: {}", to, subject, text);
            return true;
        } catch (MailException e) {
            log.warn("{} 邮件发送失败: {}",emailProperties.getUsername(), e.getMessage());
            return false;
        }
    }
}
