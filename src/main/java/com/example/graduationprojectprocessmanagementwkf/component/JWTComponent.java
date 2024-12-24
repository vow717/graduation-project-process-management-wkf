package com.example.graduationprojectprocessmanagementwkf.exception.component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.graduationprojectprocessmanagementwkf.exception.Code;
import com.example.graduationprojectprocessmanagementwkf.exception.XException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Component
public class JWTComponent {
    //私钥
    @Value("${my.secretkey}")
    private String secretKey;
    private Algorithm algorithm;//这是一个算法对象

    //组件初始化的时候，生成一个算法对象，无需反复生成
    //PostConstruct是一个注解，这个注解的作用是在这个组件初始化的时候调用这个方法
    @PostConstruct
    public void init(){
        algorithm=Algorithm.HMAC256(secretKey);//这个算法对象是用来生成JWT的
    }

    //这个方法是用来生成JWT的，传入一个map，这个map是JWT的payload部分
    public String encode(Map<String,Object> map){
        //1ds过期
        LocalDateTime time = LocalDateTime.now().plusDays(1);
        return JWT.create()
                .withPayload(map)
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(time.atZone(ZoneId.systemDefault()).toInstant()))
                .sign(algorithm);
    }
    //这个方法是用来解码JWT的，如果JWT过期或者签名不对，会抛出异常
    public Mono<DecodedJWT> decode(String token){
        try{
            return Mono.just(JWT.require(algorithm).build().verify(token));
        } catch (TokenExpiredException | SignatureVerificationException | JWTDecodeException e){
            if(e instanceof TokenExpiredException) {
                return Mono.error(XException.builder().code(Code.TOKEN_EXPIRED).build());
            }
            return  Mono.error(XException.builder().code(Code.FORBIDDEN).build());
        }
    }
}
