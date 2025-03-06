package com.example.graduationprojectprocessmanagementwkf.controller;

import com.example.graduationprojectprocessmanagementwkf.dox.User;
import com.example.graduationprojectprocessmanagementwkf.dox.UserAvatar;
import com.example.graduationprojectprocessmanagementwkf.service.UserService;
import com.example.graduationprojectprocessmanagementwkf.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/")
public class UserController {
    private final UserService userService;
    @Value("${my.upload}")
    private String uploadDir; // 注入上传路径

    @GetMapping("test")
    public Mono<ResultVO> test(){
        return Mono.just(ResultVO.success("test"));
    }

    @PostMapping("password")
    public Mono<ResultVO> changePassword(@RequestAttribute String uid,@RequestBody User user){
        return userService.changeMyPassword(uid,user.getPassword())
                .thenReturn(ResultVO.ok());
    }

    @GetMapping("processes")
    public Mono<ResultVO> listProcesses(@RequestAttribute String departmentId){
        return userService.listProcesses(departmentId)
                .map(ResultVO::success);
    }

    //上传头像，成功返回200
    @PostMapping("avatar")
    public Mono<ResultVO> uploadAvatar(@RequestAttribute String uid,Mono<FilePart> file){
        return file.flatMap(filePart->{
            String fileName = uid + filePart.filename();
            log.info("上传文件名:{}",fileName);
            Path p= Path.of(uploadDir).resolve("用户头像");
            UserAvatar userAvatar = UserAvatar.builder()
                    .userId(uid)
                    .avatar(fileName)
                    .build();

            return Mono.fromCallable(()->Files.createDirectories(p))
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMap(path->{
                        Path filePath = path.resolve(fileName);
                        return filePart.transferTo(filePath.toFile());
                    })
                    .then(userService.addAvatar(userAvatar))
                    .thenReturn(ResultVO.ok());
        });
    }

    private final DataBufferFactory factory = new DefaultDataBufferFactory();
    @GetMapping(value = "avatar")
    public Flux<DataBuffer> getAvatar(@RequestAttribute String uid, ServerHttpResponse response) throws IOException{

        log.info("1");
        return userService.getAvatar(uid)
                .map(ua->{
                    String pname=ua.getAvatar();
                    Path path = Path.of(uploadDir).resolve("用户头像").resolve(pname);
                    String name= URLEncoder.encode(path.getFileName().toString(), StandardCharsets.UTF_8);
                    HttpHeaders headers=response.getHeaders();
                    headers.set("filename", name);
                    try {
                        headers.setContentLength(Files.size(path));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    headers.setContentDisposition(ContentDisposition.attachment().build());
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    log.info("下载文件:{}",pname);
                    return DataBufferUtils.read(path, factory, 1024 * 8);
                })
                .flatMapMany(Flux::from);
    }
}
