package com.example.graduationprojectprocessmanagementwkf.service;


import com.example.graduationprojectprocessmanagementwkf.dox.ProcessFile;
import com.example.graduationprojectprocessmanagementwkf.dox.ProcessScore;
import com.example.graduationprojectprocessmanagementwkf.dox.User;
import com.example.graduationprojectprocessmanagementwkf.dox.Process;
import com.example.graduationprojectprocessmanagementwkf.exception.Code;
import com.example.graduationprojectprocessmanagementwkf.exception.XException;
import com.example.graduationprojectprocessmanagementwkf.repository.ProcessFileRepository;
import com.example.graduationprojectprocessmanagementwkf.repository.ProcessRepository;
import com.example.graduationprojectprocessmanagementwkf.repository.ProcessScoreRepository;
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
public class TeacherService {
    private final UserRepository userRepository;
    private final ProcessRepository processRepository;
    private final ProcessScoreRepository processScoreRepository;
    private final ProcessFileRepository processFileRepository;
    private final PasswordEncoder encoder;


    //---------------------------------Student---------------------------------
    //获取指导的学生
    public Mono<List<User>> getTutorStudents(String tid,String departmentId){
        return userRepository.findStudentsByTutor(tid,departmentId,User.ROLE_STUDENT).collectList();

    }

    //获取同组评审的学生
    public Mono<List<User>> getGroupStudents(int groupNumber,String departmentId){
        return userRepository.findStudentsByGroup(groupNumber,departmentId,User.ROLE_STUDENT).collectList();
    }

    //修改学生
    @Transactional
    public Mono<Void> updateStudents(List<User> users){
        return Flux.fromIterable(users)
                .flatMap(u->userRepository.findByNumber(u.getNumber())
                        .flatMap(user->{
                            if(u.getGroupNumber()!=null){
                                user.setGroupNumber(u.getGroupNumber());
                            }
                            if(u.getStudent()!=null){
                                user.setStudent(u.getStudent());
                            }
                            return userRepository.save(user);
                        }))
                .then();
    }

    public Mono<List<User>> listStudents(String role,String departmentId){
        return userRepository.findByRoleAndDepartmentId(role,departmentId).collectList();
    }

    //添加一群学生
    @Transactional
    public Mono<Void> addStudents(List<User> users,String departmentId){
        for(User u:users){
            u.setPassword(encoder.encode(u.getNumber()));
            u.setDepartmentId(departmentId);
            u.setRole(User.ROLE_STUDENT);
        }
        return userRepository.saveAll(users).then();
    }

    //根据Number删除一个学生
    @Transactional
    public Mono<Void> deleteStudentByNumber(String number){
        return userRepository.deleteStudentByNumber(number);
    }

    @Transactional
    public Mono<Void> resetStudentPassword(String number){
        return userRepository.findByNumber(number)
                .flatMap(u->{
                    u.setPassword(encoder.encode(u.getNumber()));
                    return userRepository.save(u);
                }).then();
    }

    //---------------------------------Process---------------------------------

    @Transactional
    public Mono<Void> addProcess(Process process){
        return processRepository.save(process).then();
    }

    @Transactional
    public Mono<Void> deleteProcess(String pid,String departmentId){
        return processRepository.deleteByIdAndDepartmentId(pid, departmentId).then();
    }
    @Transactional
    public  Mono<Void> updateProcess(Process process ,String departmentId){
        return processRepository.findByIdAndDepartmentId(process.getId(), departmentId)
                .flatMap(p->{
                    p.setAuth(process.getAuth());
                    p.setItems(process.getItems());
                    p.setPoint(process.getPoint());
                    p.setName(process.getName());
                    if(process.getStudentAttach()!=null) {
                        p.setStudentAttach(process.getStudentAttach());
                    }
                    //这些都会有，因为更新的时候，是在一个模态框里面编辑，这些都是必有的
                    //如果有的没有会因为.ger不到报500错
                    return processRepository.save(p);
                }).then();
    }

    //---------------------------------ProcessScore------------------------------

    public Mono<List<ProcessScore>> listProcessScoresByGroupNumber(int groupNumber){
        return processScoreRepository.findByGroupNumber(groupNumber).collectList();
    }

    public Mono<List<ProcessScore>> listProcessScoresByPidAndGroupNumber(String pid,int groupNumber){
        return processScoreRepository.findByPidAndGroupNumber(pid,groupNumber).collectList();
    }
    public Mono<List<ProcessScore>> listProcessScoresByPidAndTid(String pid,String tid){
        return processScoreRepository.findByPidAndTid(pid,tid).collectList();
    }

    @Transactional
    public Mono<Void> updateProcessScore(ProcessScore processScore){
        //Mono.justOrEmpty()方法是用来判断一个对象是否为空，如果为空则返回一个空的Mono对象，否则返回一个包含这个对象的Mono对象
        log.debug("到了这里{}",processScore);
        return Mono.justOrEmpty(processScore.getId())
                //因为如果存在ps的id,那么就一定是前端修改某个学生评分，那么它的pid,sid,tid什么的看的是不变的，只会变detail
                .flatMap(psId -> processScoreRepository.updateDetail(psId, processScore.getDetail()))
                .switchIfEmpty(processScoreRepository.save(processScore).then())
                .then();
    }

    //---------------------------------ProcessFile-----------------------------
    public Mono<List<ProcessFile>> listProcessFilesByPidAndGroupNumber(String pid, int groupNumber){
        log.debug("ps到了这里{}",pid);
        return processFileRepository.findByGroup(pid,groupNumber).collectList();
    }
    public Mono<List<ProcessFile>> listProcessFilesByPidAndTid(String pid,String tid){
        return processFileRepository.findByTeacher(pid,tid).collectList();
    }
    //---------------------------------teacher---------------------------------
    public Mono<List<User>> getGroupTeachers(int groupNumber,String departmentId){
        return userRepository.findTeachersByGroup(groupNumber,departmentId,User.ROLE_TEACHER).collectList();
    }
    public  Mono<List<User>> listTeachers(String did){
        return userRepository.listTeachersByDepartmentId(User.ROLE_TEACHER,did).collectList();
    }

}
