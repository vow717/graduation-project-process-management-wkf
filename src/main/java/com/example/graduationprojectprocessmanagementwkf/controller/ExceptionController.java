package com.example.graduationprojectprocessmanagementwkf.controller;

import com.example.graduationprojectprocessmanagementwkf.exception.Code;
import com.example.graduationprojectprocessmanagementwkf.exception.XException;
import com.example.graduationprojectprocessmanagementwkf.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.UncategorizedR2dbcException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(XException.class)
    public Mono<ResultVO> handleException(XException e){
        if(e.getCode()!=null){
            return Mono.just(ResultVO.error(e.getCode()));
        }
        return Mono.just(ResultVO.error(e.getCodeNum(),e.getMessage()));
    }

    //抄老师的,处理未知异常
    @ExceptionHandler(Exception.class)
    public Mono<ResultVO> handleException(Exception exception) {
        return Mono.just(ResultVO.error(Code.BAD_REQUEST.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(UncategorizedR2dbcException.class)
    public Mono<ResultVO> handelUncategorizedR2dbcException(UncategorizedR2dbcException exception) {
        return Mono.just(ResultVO.error(Code.BAD_REQUEST.getCode(), "唯一约束冲突！" + exception.getMessage()));
    }
}
