package com.example.graduationprojectprocessmanagementwkf.controller;

import com.example.graduationprojectprocessmanagementwkf.dox.User;
import com.example.graduationprojectprocessmanagementwkf.service.UserService;
import com.example.graduationprojectprocessmanagementwkf.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/")
public class UserController {
    private final UserService userService;


    @PostMapping("password")
    public Mono<ResultVO> changePassword(@RequestAttribute String uid,@RequestBody User user){
        return userService.changeMyPassword(uid,user.getPassword())
                .thenReturn(ResultVO.ok());
    }

    @GetMapping("processes")
    public Mono<ResultVO> listProcesses(@RequestAttribute String departmentId){
        return userService.listProcesses(departmentId)
                .map(ResultVO::success);
    }
}
