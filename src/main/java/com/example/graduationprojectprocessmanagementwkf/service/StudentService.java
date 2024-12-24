package com.example.graduationprojectprocessmanagementwkf.service;

import com.example.graduationprojectprocessmanagementwkf.dox.ProcessFile;
import com.example.graduationprojectprocessmanagementwkf.repository.ProcessFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentService {
    private final ProcessFileRepository processFileRepository;

    @Transactional
    public Mono<Void> addProcessFile(ProcessFile processFile){
        return processFileRepository.findByProcessIdAndStudentIdAndNumber(processFile.getProcessId(), processFile.getStudentId(), processFile.getNumber())
                .flatMap(p -> {
                    p.setDetail(processFile.getDetail());
                    return processFileRepository.save(p);
                })
                .switchIfEmpty(processFileRepository.save(processFile)).then();
    }

    public Mono<List<ProcessFile>> listProcessFiles(String pid, String sid) {
        return processFileRepository.findByStudentId(pid, sid).collectList();
    }
}
