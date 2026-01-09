package com.liu.springbootdemo;

import com.liu.springbootdemo.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MailTest {
    @Autowired
    private EmailService emailService;

    @Test
    public void testSendSimpleEmail() {
        emailService.sendCode("todimstar@outlook.com", emailService.generateVerficationCode());
    }
}
