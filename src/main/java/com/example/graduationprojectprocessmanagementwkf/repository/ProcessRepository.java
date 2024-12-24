package com.example.graduationprojectprocessmanagementwkf.repository;

import org.springframework.data.r2dbc.repository.Query;
import com.example.graduationprojectprocessmanagementwkf.dox.Process;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProcessRepository extends ReactiveCrudRepository<Process,String> {

    @Query("""
select * from process p where p.department_id = :departmentId
""")
    Flux<Process> findByDepartmentId(String departmentId);

    @Query("""
select * from process p where p.id = :id and p.department_id = :departmentId
""")
    Mono<Process> findByIdAndDepartmentId(String id, String departmentId);

    @Query("""
delete from process p where p.id = :id and p.department_id = :departmentId
""")
    Mono<Void> deleteByIdAndDepartmentId(String id, String departmentId);


}
