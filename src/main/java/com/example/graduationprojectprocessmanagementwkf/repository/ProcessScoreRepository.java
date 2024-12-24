package com.example.graduationprojectprocessmanagementwkf.repository;

import com.example.graduationprojectprocessmanagementwkf.dox.ProcessScore;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProcessScoreRepository extends ReactiveCrudRepository<ProcessScore, String> {


//    @Query("""
//    select
//                    ps.id as id,
//                     ps.student_id as student_id,
//                     ps.process_id as process_id,
//                     ps.detail as detail,
//                     ps.teacher_id as teacher_id
//     from process_score ps ,user u where ps.id=:pid and u.group_number=:groupNumber and u.id=ps.student_id
//    """)

    @Query("""
            select ps.id as id,
                ps.student_id as student_id,
                ps.process_id as process_id,
                ps.teacher_id as teacher_id,
                ps.detail as detail
            from process_score ps left join user u
            on ps.student_id=u.id
            where u.group_number=:groupNumber;
            """)
    Flux<ProcessScore> findByGroupNumber(int groupNumber);

@Query("""
            select ps.id as id,
                ps.student_id as student_id,
                ps.process_id as process_id,
                ps.teacher_id as teacher_id,
                ps.detail as detail
            from process_score ps ,user u
            where ps.student_id=u.id and u.group_number=:groupNumber and ps.process_id=:pid;
            """)
    Flux<ProcessScore> findByPidAndGroupNumber(String pid, int groupNumber);


    @Query("""
                select  ps.id as id,
                     ps.student_id as student_id,
                    ps.process_id as process_id,
                    ps.teacher_id as teacher_id,
                    ps.detail as detail
                from process_score ps,user u where ps.student_id=u.id and u.student->'$.teacherId'=:tid and ps.process_id=:pid
""")
    Flux<ProcessScore> findByPidAndTid(String pid, String tid);

    @Modifying
    @Query("""
    update process_score ps set ps.detail=:detail where ps.id=:psId
    """)
    Mono<Void>updateDetail(String psId,String detail);
}
