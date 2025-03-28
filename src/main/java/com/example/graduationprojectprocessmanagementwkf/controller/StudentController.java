package com.example.graduationprojectprocessmanagementwkf.controller;

import com.example.graduationprojectprocessmanagementwkf.dox.ProcessFile;
import com.example.graduationprojectprocessmanagementwkf.exception.Code;
import com.example.graduationprojectprocessmanagementwkf.exception.XException;
import com.example.graduationprojectprocessmanagementwkf.service.StudentService;
import com.example.graduationprojectprocessmanagementwkf.vo.ResultVO;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/student/")
public class StudentController {
    private final StudentService studentService;
    @Value("${my.upload}")
    private String uploadDirectory;

    @GetMapping("processfiles/{pid}")
    public Mono<ResultVO> listProcessFiles(@PathVariable String pid, @RequestAttribute("uid") String sid) {
        return studentService.listProcessFiles(pid, sid)
                .map(ResultVO::success);
    }

    //单文件+签名x-token上传接口
    @PostMapping("upload/{pid}/numbers/{number}")
    public Mono<ResultVO> upload(@PathVariable String pid,
                                 @PathVariable int number,
                                 @RequestPart String pname,
                                 @RequestAttribute("uid") String sid,
                                 @RequestHeader(required = false) String xtoken,
                                 Mono<FilePart> file) {
        //如果xtoken为空，说明文件上传失败
        if(xtoken==null){
        return Mono.error(XException.builder().message("文件上传失败").build());
        }
        return file.flatMap(filePart->{
            String token=pid+pname+filePart.filename()+number;
            //对token进行base64编码，并且与xtoken进行比较，如果不相等，说明文件上传错误
            //一致时，才能说明这个文件上传请求是符合预期、未被篡改且来源可靠的。
            //如果两者不匹配，就有理由怀疑文件在传输过程中被修改、请求是伪造的或者客户端与服务端之间的交互出现了错误，所以判定为文件传输错误并拒绝此次上传操作。
            byte[] encode = Base64.getEncoder().encode(URLEncoder.encode(token, Charset.defaultCharset()).getBytes());
            var base64 = new String(encode).substring(0, 10);
            if (!base64.equals(xtoken)) {
                return Mono.error(XException.builder().message("文件上传错误").build());
            }
            ProcessFile pf = ProcessFile.builder()
                    .studentId(sid)
                    .processId(pid)
                    .number(number)
                    .detail(Path.of(pname).resolve(filePart.filename()).toString())
                    .build();
            Path p = Path.of(uploadDirectory).resolve(pname);

            //创建目录操作，如果目录不存在则创建，存在则不做操作
            return Mono.fromCallable(() -> Files.createDirectories(p))
                    .subscribeOn(Schedulers.boundedElastic()) //指定了这个操作在Schedulers.boundedElastic()调度器上执行.Schedulers.boundedElastic()是 Reactor 提供的一种弹性调度器，适合处理一些可能需要较长时间执行但又不会无限占用线程资源的任务，比如文件系统操作、数据库操作等，它可以根据系统负载动态调整线程资源分配。
                    .flatMap(path -> {
                        //将目录路径和上传文件的文件名拼接起来，得到完整的文件保存路径finPath
                        Path finPath = path.resolve(filePart.filename());
                        //将上传文件的内容从临时存储位置转移到最终确定的文件保存路径下，完成文件的保存操作。
                        //这里的transferTo方法是FilePart类提供的用于将文件内容实际保存到指定路径的方法，内部实现可能涉及到文件流的读写等底层操作。
                        return filePart.transferTo(finPath);
                    })
                    .then(studentService.addProcessFile(pf));
        })
                .then(studentService.listProcessFiles(pid, sid))
                .map(ResultVO::success)
                .onErrorResume(ex -> Mono.just(ResultVO.error(Code.ERROR, "文件上传错误！" + ex.getMessage())));

    }

    // 切片上传接口+hash验证
    @PostMapping("uploadByChunks")
    public Mono<ResultVO> uploadChunk(
            @RequestPart("chunk") FilePart chunk,
            @RequestPart("fileHash") String fileHash,
            @RequestPart("pname") String pname,
            @RequestPart("chunkIndex") String chunkIndex //这个chunkIndex是fileHash-index这种形式
            ) {
        log.debug("目前上传：chunkIndex: {}", chunkIndex);
        // 构建切片存储路径
        Path chunkDir = Paths.get(uploadDirectory, pname, fileHash);
        // 构建切片文件路径
        Path chunkPath = chunkDir.resolve(String.valueOf(chunkIndex));

        return Mono.fromRunnable(() -> {
                    try {
                        Files.createDirectories(chunkDir);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenMany(chunk.content())
                .flatMap(dataBuffer -> saveChunk(dataBuffer, chunkPath))
                .then(Mono.just(ResultVO.success("切片上传成功")));
    }

    // 合并切片并保存文件
    private Mono<Void> saveChunk(DataBuffer dataBuffer, Path chunkPath) {
        return Mono.fromRunnable(() -> {
            try (var outputStream = Files.newOutputStream(chunkPath)) {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                outputStream.write(bytes);
            } catch (IOException e) {
                throw new RuntimeException("切片保存失败", e);
            } finally {
                DataBufferUtils.release(dataBuffer);
            }
        });
    }

    // 完成上传接口（由前端在所有切片上传后调用）
    @PostMapping("complete/{pid}/numbers/{number}")
    public Mono<ResultVO> completeUpload(
            @PathVariable String pid,
            @PathVariable int number,
            @RequestPart("fileHash") String fileHash,
            @RequestPart("pname") String pname,
            @RequestPart("filename") String filename,
            @RequestAttribute("uid") String sid) {
        log.debug("completeUpload");
        // 构建临时切片目录和最终文件路径
        Path tempDir = Paths.get(uploadDirectory, pname, fileHash);
        Path finalPath = Paths.get(uploadDirectory, pname, filename);

        return Mono.just(tempDir)
                .filter(Files::exists)
                .switchIfEmpty(Mono.error(() -> XException.builder().codeNum(Code.ERROR).message("切片不完全").build()))
                .flatMap(dir -> Mono.fromRunnable(() -> {
                    try {
                        mergeChunks(dir, finalPath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .then(Mono.fromRunnable(() -> {
                    try {
                        Files.walk(tempDir)
                                .sorted(Comparator.reverseOrder())
                                .map(Path::toFile)
                                .forEach(java.io.File::delete);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .then(Mono.defer(() -> {
                    ProcessFile pf = ProcessFile.builder()
                            .studentId(sid)
                            .processId(pid)
                            .number(number)
                            .detail(Paths.get(pname, filename).toString())
                            .build();
                    return studentService.addProcessFile(pf);
                }))
                .thenReturn(ResultVO.success("文件上传完成"));
    }

    // 切片合并方法
    private void mergeChunks(Path tempDir, Path finalPath) throws IOException {
        Files.createDirectories(finalPath.getParent());
        try (var outputStream = Files.newOutputStream(finalPath)) {
            Files.list(tempDir)
                    .sorted(Comparator.comparingInt(path -> Integer.parseInt(path.getFileName().toString().split("-")[1])))
                    .forEach(chunkPath -> {
                        try (var inputStream = Files.newInputStream(chunkPath)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        } catch (IOException e) {
                            throw XException.builder().codeNum(Code.ERROR).message("切片合并失败").build();
                        }
                    });
        }
    }

}
