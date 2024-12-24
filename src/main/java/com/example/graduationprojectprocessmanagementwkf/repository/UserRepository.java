package com.example.graduationprojectprocessmanagementwkf.repository;

import com.example.graduationprojectprocessmanagementwkf.dox.User;
import io.swagger.v3.oas.annotations.Webhook;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User,String> {

    @Query("""
select * from user u where u.role=:role and u.department_id=:departmentId
""")
    Flux<User> listTeachersByDepartmentId(String role,String departmentId);

    @Query("""
    select * from user u where u.number=:number
    """)
    Mono<User>findByNumber(String number);

    @Query("""
    select * from user u where u.number=:number and u.department_id=:departmentId
    """)
    Mono<User>findByNumberAndDepartmentId(String number,String departmentId);
    @Query("""
    select * from user u where u.id=:id
    """)
    Mono<User>findById(String id);
    @Query("""
    select * from user u where u.department_id = :departmentId AND u.role=:role
    """)
    Flux<User> findTeachersByDepartmentId(String departmentId, String role);

    @Modifying //这个注解表示这是一个更新操作
    @Query("""
delete from user u where u.id=:id
""")
    Mono<Void> deleteTeacherById(String id);

    @Modifying
    @Query("""
update user u set u.password=:password where u.id=:id
""")
    Mono<Void> changeMyPasswordById(String id, String password);

    @Query("""
select * from user u where u.student->'$.teacherId'=:tid and u.department_id=:departmentId and u.role=:role
""")
    Flux<User> findStudentsByTutor(String tid,String departmentId,String role);

    @Query("""
select * from user u where u.group_number=:groupNumber and u.role=:role and u.department_id=:departmentId
""")
    Flux<User> findStudentsByGroup(int groupNumber,String departmentId,String role);

    @Modifying
    @Query("""
delete from user u where u.number=:number
""")
    Mono<Void> deleteStudentByNumber(String number);

    @Query("""
select * from user u where u.role=:role and u.department_id=:departmentId
""")
    Flux<User> findByRoleAndDepartmentId(String role,String departmentId);

    @Query("""
    select * from user u where u.group_number=:groupNumber and u.role=:role and u.department_id=:departmentId
    """)
    Flux<User> findTeachersByGroup(int groupNumber, String departmentId, String role);
}
