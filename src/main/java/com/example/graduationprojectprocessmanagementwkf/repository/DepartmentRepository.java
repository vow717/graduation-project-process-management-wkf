package com.example.graduationprojectprocessmanagementwkf.repository;

import com.example.graduationprojectprocessmanagementwkf.dox.Department;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface DepartmentRepository extends ReactiveCrudRepository<Department, String> {

    @Query("""
            select * from department d where d.name = :name
            """)
    Mono<Department> findByName(String name);

    @Query("""
            select * from department d where d.id = :id
            """)
    Mono<Department> findById(String id);

    @Modifying
    @Query("""
            delete from department d where d.id = :id
            """)
    Mono<Void> deleteById(String id);


}
