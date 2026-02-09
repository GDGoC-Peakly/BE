package com.example.peakly.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailVerifyMailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${auth.email-verify.link-base-url}")
    private String linkBaseUrl;

    public void sendVerifyMail(String toEmail, String rawToken) {
        String link = buildVerifyLink(rawToken);

        String subject = "[Peakly] 이메일 인증을 완료해 주세요";
        String text = """
                안녕하세요! Peakly 이메일 인증 안내드립니다.

                아래 링크를 눌러 이메일 인증을 완료해 주세요:
                %s

                만약 본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.
                """.formatted(link);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    private String buildVerifyLink(String rawToken) {
        // linkBaseUrl에 이미 ?가 포함될 수도 있으니 안전하게 처리
        String sep = linkBaseUrl.contains("?") ? "&" : "?";
        return linkBaseUrl + sep + "token=" + rawToken;
    }
}
