package com.example.graduationprojectprocessmanagementwkf.service;

import com.example.graduationprojectprocessmanagementwkf.component.JWTComponent;
import com.example.graduationprojectprocessmanagementwkf.dox.Department;
import com.example.graduationprojectprocessmanagementwkf.dox.User;
import com.example.graduationprojectprocessmanagementwkf.exception.Code;
import com.example.graduationprojectprocessmanagementwkf.exception.XException;
import com.example.graduationprojectprocessmanagementwkf.repository.DepartmentRepository;
import com.example.graduationprojectprocessmanagementwkf.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;


    @Transactional
    public Mono<Void> addDepartment(String name){
        return departmentRepository.findByName(name)
                .flatMap(d->Mono.error(XException.builder().codeNum(Code.ERROR).message("部门已存在").build()))
                .switchIfEmpty(Mono.defer(()->{
                    Department department=Department.builder()
                            .name(name)
                            .build();
                    return departmentRepository.save(department);
                })).then();
                /*
                .flatMap(d->{
                    if(d!=null){
                        log.error("部门已存在");
                        return Mono.error(XException.builder().message("部门已存在").codeNum(Code.ERROR).build());
                    }else {
                        Department department=Department.builder()
                                .name(name)
                                .build();
                        return departmentRepository.save(department);                    }
                }).then();
                这样好像不行，不知道为什么？
                */

    }

    @Transactional
    public Mono<Void> deleteDepartment(String id){
        return departmentRepository.deleteById(id).then();
    }

    public Mono<List<Department>> getAllDepartments(){
        return departmentRepository.findAll().collectList();
    }


    @Transactional
    public Mono<Void> addTeacher(User user){
        return userRepository.findByNumber(user.getNumber())
                .flatMap(u->Mono.error(XException.builder().codeNum(Code.ERROR).message("用户已存在").build()))
                .switchIfEmpty(Mono.defer(()->{
                    user.setRole(User.ROLE_TEACHER);
                    user.setPassword(encoder.encode(user.getNumber()));
                    return userRepository.save(user);
                })).then();
    }

    @Transactional
    public Mono<Void> deleteTeacher(String id){
        return userRepository.deleteTeacherById(id).then();
    }
    public Mono<List<User>> getTeachersByDepartmentId(String did){
        return userRepository.findTeachersByDepartmentId(did,User.ROLE_TEACHER).collectList();
    }

    public Mono<Void> resetPassword(String uid){
        return userRepository.findById(uid)
                .switchIfEmpty(Mono.error(XException.builder().codeNum(Code.ERROR).message("用户不存在").build()))
                .flatMap(u->{
                    u.setPassword(encoder.encode(u.getNumber()));
                    return userRepository.save(u);
                }).then();
    }

    @Transactional
    public Mono<Void> addTeachers(List<User> users,String departmentId){
        for(User u:users){
            u.setPassword(encoder.encode(u.getNumber()));
            u.setDepartmentId(departmentId);
            u.setRole(User.ROLE_TEACHER);
        }
        return userRepository.saveAll(users).then();
    }
}
