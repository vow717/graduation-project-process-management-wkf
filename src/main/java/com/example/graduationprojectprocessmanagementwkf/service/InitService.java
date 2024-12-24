package com.example.graduationprojectprocessmanagementwkf.service;

import com.example.graduationprojectprocessmanagementwkf.dox.User;
import com.example.graduationprojectprocessmanagementwkf.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public Mono<Void> init(){
        return userRepository.findByNumber("admin")
                .switchIfEmpty(Mono.defer(()-> {
                    User u= User.builder()
                            .number("admin")
                            .name("admin")
                            .departmentId("0")
                            .password(encoder.encode("admin"))
                            .role(User.ROLE_ADMIN)
                            .build();
                    return userRepository.save(u);
                })).then();
    }

}
