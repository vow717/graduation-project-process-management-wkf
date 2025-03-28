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
public class ProcessFile {
    @Id
    @CreatedBy
    private String id;
    private String detail;
    private String studentId;
    private String processId;
    private Integer number; //文件序号, 用于区分同一个学生上传的多个文件,一个processId老师可能要求学生上传多个文件
    private String fileHash;//md5文件hash,用于判断文件是否重复上传来实现秒传功能
    @ReadOnlyProperty
    @JsonIgnore
    private LocalDateTime insertTime;
    @ReadOnlyProperty
    @JsonIgnore
    private LocalDateTime updateTime;
}