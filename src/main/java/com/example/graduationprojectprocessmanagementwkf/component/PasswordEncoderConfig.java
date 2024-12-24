package com.example.graduationprojectprocessmanagementwkf.component;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder getPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
    //这个类是一个配置类，这个类的作用是配置一个PasswordEncoder的Bean
    //BCryptPasswordEncoder是一个实现了PasswordEncoder接口的类
    //这个类的作用是对密码进行加密
    //用法:
    //在需要用到它的时候,比如在service层的类中,注入这个类
    //@Autowired
    //private PasswordEncoder encoder
    //然后调用encodedPassword=encoder.encode("password")就可以对密码进行加密
    //并且encoder.matches("password",encodedPassword)可以用来验证密码是否正确
}

