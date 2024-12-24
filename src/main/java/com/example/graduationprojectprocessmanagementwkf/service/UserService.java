package com.example.graduationprojectprocessmanagementwkf.service;

import com.example.graduationprojectprocessmanagementwkf.dox.User;
import com.example.graduationprojectprocessmanagementwkf.dox.Process;
import com.example.graduationprojectprocessmanagementwkf.repository.ProcessRepository;
import com.example.graduationprojectprocessmanagementwkf.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final ProcessRepository processRepository;
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
}
