package com.example.graduationprojectprocessmanagementwkf.controller;


import com.example.graduationprojectprocessmanagementwkf.exception.Code;
import com.example.graduationprojectprocessmanagementwkf.component.JWTComponent;
import com.example.graduationprojectprocessmanagementwkf.dox.User;
import com.example.graduationprojectprocessmanagementwkf.exception.XException;
import com.example.graduationprojectprocessmanagementwkf.service.UserService;
import com.example.graduationprojectprocessmanagementwkf.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
public class LoginController {
    private final UserService userService;
    private final PasswordEncoder encoder;
    private final JWTComponent jwtComponent;

    @PostMapping("login")
    public Mono<ResultVO> login(@RequestBody User user, ServerHttpResponse response){
        return userService.findByNumber(user.getNumber())
                .switchIfEmpty(Mono.error(XException.builder().message("用户不存在").codeNum(Code.ERROR).build()))
                .map(u->{
                    if(encoder.matches(user.getPassword(),u.getPassword())) {
                        Map<String, Object> tokenM = new HashMap<>();
                        tokenM.put("uid", u.getId());
                        tokenM.put("role", u.getRole());
                        tokenM.put("departmentId", u.getDepartmentId());
                        if (u.getGroupNumber() != null) {
                            tokenM.put("groupNumber", u.getGroupNumber());
                        }
                        String token = jwtComponent.encode(tokenM);

                        response.getHeaders().add("token", token);
                        response.getHeaders().add("role", u.getRole());

                        return ResultVO.success(u);
                    }else {
                        return ResultVO.error(Code.LOGIN_ERROR);
                    }
                });

    }


}
