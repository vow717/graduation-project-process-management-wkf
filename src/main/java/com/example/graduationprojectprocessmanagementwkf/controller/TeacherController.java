package com.example.graduationprojectprocessmanagementwkf.controller;


import com.example.graduationprojectprocessmanagementwkf.dox.ProcessScore;
import com.example.graduationprojectprocessmanagementwkf.dox.User;
import com.example.graduationprojectprocessmanagementwkf.dox.Process;
import com.example.graduationprojectprocessmanagementwkf.service.TeacherService;
import com.example.graduationprojectprocessmanagementwkf.service.UserService;
import com.example.graduationprojectprocessmanagementwkf.vo.ResultVO;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/teacher/")
public class TeacherController {
    private final TeacherService teacherService;
    private final UserService userService;
    @Value("${my.upload}")
    private String uploadDirectory;

    @GetMapping("users/{number}")
    public Mono<ResultVO> getUserByNumber(@PathVariable String number,@RequestAttribute String departmentId){
        return userService.findByNumberAndDId(number,departmentId)
                .map(ResultVO::success);
    }
    //---------------------------------Student---------------------------------
    //获取本专业所有学生
    @GetMapping("students")
    public Mono<ResultVO> listAllStudents(@RequestAttribute String departmentId){
        return teacherService.listStudents(User.ROLE_STUDENT,departmentId)
                .map(ResultVO::success);
    }
    //重置学生密码
    @PutMapping("passwords/{number}")
    public Mono<ResultVO> resetStudentPassword(@PathVariable String number){
        return teacherService.resetStudentPassword(number)
                .thenReturn(ResultVO.ok());
    }

    //获取指导的学生
    @GetMapping("students/tutor")
    public Mono<ResultVO> getTutorStudents(@RequestAttribute String uid,@RequestAttribute String departmentId){
        return teacherService.getTutorStudents(uid,departmentId)
                .map(ResultVO::success);
    }
    //获取同组评审的学生
    @GetMapping("students/group")
    public Mono<ResultVO> getGroupStudents(@RequestAttribute int groupNumber,@RequestAttribute String departmentId){
        return teacherService.getGroupStudents(groupNumber,departmentId)
                .map(ResultVO::success);
    }

    //修改学生信息
    @PatchMapping("students")
    public Mono<ResultVO> updateStudents(@RequestBody List<User> users,@RequestAttribute String departmentId){
        return teacherService.updateStudents(users)
                .then(teacherService.listStudents(User.ROLE_STUDENT,departmentId))
                .map(ResultVO::success);
    }

    //添加一群学生
    @PostMapping("students")
    public Mono<ResultVO> Students(@RequestBody List<User> users, @RequestAttribute String departmentId){
        return teacherService.addStudents(users,departmentId)
                .then(teacherService.listStudents(User.ROLE_STUDENT,departmentId))
                .map(ResultVO::success);
    }

    //根据Number删除一个学生
    @DeleteMapping("deleteStudent/{number}")
    public Mono<ResultVO> deleteStudentByNumber(@PathVariable String number){
        return teacherService.deleteStudentByNumber(number)
                .thenReturn(ResultVO.ok());
    }

    //---------------------------------Process---------------------------------


    //创建一个Process,并返回所有Process
    @PostMapping("processes")
    public Mono<ResultVO> addProcess(@RequestBody Process process,@RequestAttribute String departmentId){
        process.setDepartmentId(departmentId);
        return teacherService.addProcess(process)
                .then(userService.listProcesses(departmentId))
                .map(ResultVO::success);
    }
    //更新一个Process
    @PatchMapping("processes")
    public Mono<ResultVO> updateProcess(@RequestBody Process process,@RequestAttribute String departmentId){
        return teacherService.updateProcess(process,departmentId)
                .then(userService.listProcesses(departmentId))
                .map(ResultVO::success);
    }

    //删除一个Process
    @DeleteMapping("processes/{pid}")
    public Mono<ResultVO> deleteProcess(@PathVariable String pid,@RequestAttribute String departmentId){
        return teacherService.deleteProcess(pid,departmentId)
                .then(userService.listProcesses(departmentId))
                .map(ResultVO::success);
    }


    //---------------------------------ProcessScore------------------------------

    @GetMapping("processscores/groups")
    public Mono<ResultVO> getGroupProcessScores(@RequestAttribute int groupNumber){
        return teacherService.listProcessScoresByGroupNumber(groupNumber)
                .map(ResultVO::success);
    }

    @PostMapping("processscores/types/{auth}")
    public Mono<ResultVO> addProcessScore(@RequestBody ProcessScore processScore,@PathVariable String auth,@RequestAttribute String uid,@RequestAttribute int groupNumber){
        var pid=processScore.getProcessId();//为什么不能用let而要用var:因为let是在一个作用域内，而var是在一个方法内,
        return teacherService.updateProcessScore(processScore)
                .then(Mono.defer(()->auth.equals(Process.REVIEW)
                                ?teacherService.listProcessScoresByPidAndGroupNumber(pid,groupNumber)
                                :teacherService.listProcessScoresByPidAndTid(pid,uid))
                        .map(ResultVO::success));
    }

    @GetMapping("processes/{pid}/types/{auth}")
    public Mono<ResultVO> getProcessScore(@PathVariable String pid,@PathVariable String auth,@RequestAttribute String uid,@RequestAttribute int groupNumber){
        return Mono.defer(()->auth.equals(Process.REVIEW)
                        ?teacherService.listProcessScoresByPidAndGroupNumber(pid,groupNumber)
                        :teacherService.listProcessScoresByPidAndTid(pid,uid))
                .map(ResultVO::success);
    }

    //---------------------------------ProcessFile------------------------------
    @GetMapping("processfiles/{pid}/types/{auth}")
    public Mono<ResultVO> getProcessFiles(@PathVariable String pid,@PathVariable String auth,@RequestAttribute String uid,@RequestAttribute int groupNumber){
        return Mono.defer(()->auth.equals(Process.REVIEW)
                        ?teacherService.listProcessFilesByPidAndGroupNumber(pid,groupNumber)
                        :teacherService.listProcessFilesByPidAndTid(pid,uid))
                .map(ResultVO::success);
    }

    //factory是用来创建DataBuffer的工厂类,可以用来创建DataBuffer对象，DataBuffer是Spring WebFlux框架中用来表示数据缓冲区的接口
    private final DataBufferFactory factory = new DefaultDataBufferFactory();
    @GetMapping("download/{pname}")
    public Flux<DataBuffer> download(@PathVariable String pname,ServerHttpResponse response)throws IOException{
        Path path= Path.of(uploadDirectory).resolve(pname);
        //这里是将文件名进行编码，因为文件名有中文，所以要进行编码,将其转换为UTF-8编码格式
        String name= URLEncoder.encode(path.getFileName().toString(), StandardCharsets.UTF_8);
        HttpHeaders headers=response.getHeaders();
        //将获取到的文件名设置到 HTTP 响应头的filename字段中。这个字段用于告知客户端接收到的文件的名称
        headers.set("filename", name);
        //将获取到的文件大小设置到 HTTP 响应头的Content-Length字段中。这个字段用于告知客户端即将接收的内容的长度，
        headers.setContentLength(Files.size(path));
        //这是 Spring 框架中用于构建Content-Disposition响应头的一种方式。attachment表示该响应的内容是作为附件提供的，而不是在浏览器中直接显示。
        headers.setContentDisposition(ContentDisposition.attachment().build());
        //将 HTTP 响应头的Content-Type字段设置为MediaType.APPLICATION_OCTET_STREAM。APPLICATION_OCTET_STREAM是一种通用的二进制流类型，表示响应的内容是原始的二进制数据
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        //DataBufferUtils.read()方法是用来读取文件内容并返回一个Flux<DataBuffer>对象的方法。这个方法会将文件内容读取到DataBuffer对象中，并将DataBuffer对象放入Flux流中
        return DataBufferUtils.read(path, factory, 1024 * 8);
    }

    //-------------------teacher-------------------
    //获取同组的老师
    @GetMapping("teachers/group")
    public Mono<ResultVO> getGroupTeachers(@RequestAttribute int groupNumber,@RequestAttribute String departmentId){
        return teacherService.getGroupTeachers(groupNumber,departmentId)
                .map(ResultVO::success);
    }

    //获取本专业所有老师
    @GetMapping("teachers")
    public Mono<ResultVO> getTeachers(@RequestAttribute String departmentId){
        return teacherService.listTeachers(departmentId)
                .map(ResultVO::success);
    }

}
