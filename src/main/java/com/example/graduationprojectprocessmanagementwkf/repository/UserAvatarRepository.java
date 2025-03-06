package com.example.graduationprojectprocessmanagementwkf.repository;

import com.example.graduationprojectprocessmanagementwkf.dox.UserAvatar;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public interface UserAvatarRepository extends ReactiveCrudRepository<UserAvatar, String> {

    @Query("select * from user_avatar where user_id=:userId")
    Mono<UserAvatar> findByUserId(String userId);

}