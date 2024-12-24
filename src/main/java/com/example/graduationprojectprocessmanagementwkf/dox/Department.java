package com.example.graduationprojectprocessmanagementwkf.dox;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {
    @Id
    @CreatedBy
    private String id;
    private String name;
    @ReadOnlyProperty
    @JsonIgnore
    private LocalDateTime insertTime;
    @ReadOnlyProperty
    @JsonIgnore
    private LocalDateTime updateTime;
    //JsonIgnore注释用于在序列化和反序列化时忽略属性，因为这个属性是只读的，所以不需要序列化和反序列化
    //相比于ReadOnlyProperty注释，JsonIgnore注释更加灵活，可以在序列化和反序列化时都忽略属性
}