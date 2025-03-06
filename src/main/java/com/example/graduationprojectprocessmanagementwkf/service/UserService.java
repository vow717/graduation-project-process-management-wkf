package com.example.graduationprojectprocessmanagementwkf.service;

import com.example.graduationprojectprocessmanagementwkf.dox.User;
import com.example.graduationprojectprocessmanagementwkf.dox.Process;
import com.example.graduationprojectprocessmanagementwkf.dox.UserAvatar;
import com.example.graduationprojectprocessmanagementwkf.exception.Code;
import com.example.graduationprojectprocessmanagementwkf.exception.XException;
import com.example.graduationprojectprocessmanagementwkf.repository.ProcessRepository;
import com.example.graduationprojectprocessmanagementwkf.repository.UserAvatarRepository;
import com.example.graduationprojectprocessmanagementwkf.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import reactor.core.publisher.Flux;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final ProcessRepository processRepository;
    private final UserAvatarRepository userAvatarRepository;
    @Value("${my.upload}")
    private String uploadDir; // 注入上传路径


    public Mono<User> findByNumber(String number){
        return userRepository.findByNumber(number);
    }
    public Mono<User> findByNumberAndDId(String number,String did){

        return userRepository.findByNumberAndDepartmentId(number,did);
    }
    @Transactional
    public Mono<Void> changeMyPassword(String uid, String password){
        //先String pswd=encoder.encode(password);好像会有问题？是因为这个方法是异步的吗？
        return userRepository.changeMyPasswordById(uid,encoder.encode(password));
    }

    public Mono<List<Process>> listProcesses(String departmentId){
        return processRepository.findByDepartmentId(departmentId)
                        .map(processes->{
                            return processes;
                        })
                .collectList();
    }



    public Mono<UserAvatar> getAvatar(String uid) {
        return  userAvatarRepository.findByUserId(uid);
    }

    @Transactional
    public Mono<Void> addAvatar(UserAvatar userAvatar) {
        log.info("addAvatar:{}",userAvatar);
        return userAvatarRepository.findByUserId(userAvatar.getUserId())
                .flatMap(ua->{
                    ua.setAvatar(userAvatar.getAvatar());
                    return userAvatarRepository.save(ua);
                })
                .switchIfEmpty(Mono.defer(()->userAvatarRepository.save(userAvatar)))
                .then();
    }
}
