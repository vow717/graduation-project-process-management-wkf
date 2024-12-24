package com.example.graduationprojectprocessmanagementwkf.controller;

import com.example.graduationprojectprocessmanagementwkf.dox.Department;
import com.example.graduationprojectprocessmanagementwkf.dox.User;
import com.example.graduationprojectprocessmanagementwkf.service.AdminService;
import com.example.graduationprojectprocessmanagementwkf.vo.ResultVO;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/")
public class AdminController {
    private final AdminService adminService;



    @Operation(summary = "重置密码")
    @PutMapping("password/{uid}")
    public Mono<ResultVO> resetPassword(@PathVariable String uid){
        return adminService.resetPassword(uid).thenReturn(ResultVO.ok());
    }
    //----------对部门department的相关操作----------

    @GetMapping("departments")
    public Mono<ResultVO> getDepartments(){
        return adminService.getAllDepartments()
                .map(ResultVO::success);
        //.map(ResultVO::success())相当于.map(d->ResultVO.success(d))
    }

    @PostMapping("departments")
    public Mono<ResultVO> addDepartment(@RequestBody Department department){
        return adminService.addDepartment(department.getName())
                .thenReturn(ResultVO.ok());
    }


    @DeleteMapping("deleteDepartment/{id}")
    public Mono<ResultVO> deleteDepartment(@PathVariable String id){
        return adminService.deleteDepartment(id)
                .thenReturn(ResultVO.ok());
    }


    //----------对老师teacher的相关操作----------

    @PostMapping("teachers/{did}")
    public Mono<ResultVO> addTeachers(@RequestBody List<User>users,@PathVariable String did){
        return adminService.addTeachers(users,did)
                .thenReturn(ResultVO.ok());
    }
    @PostMapping("addTeacher")
    public Mono<ResultVO> addTeacher(@RequestBody User user){
        return adminService.addTeacher(user)
                .thenReturn(ResultVO.ok());
    }

    @DeleteMapping("deleteTeacher/{id}")
    public Mono<ResultVO> deleteTeacher(@PathVariable String id){
        return adminService.deleteTeacher(id)
                .thenReturn(ResultVO.ok());
    }

    @GetMapping("getTeachers/{did}")
    public Mono<ResultVO> getTeachers(@PathVariable String did){
        return adminService.getTeachersByDepartmentId(did)
                .map(ResultVO::success);
    }
}
