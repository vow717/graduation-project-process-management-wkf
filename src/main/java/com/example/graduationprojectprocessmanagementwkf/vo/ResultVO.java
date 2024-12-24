package com.example.graduationprojectprocessmanagementwkf.vo;

import com.example.graduationprojectprocessmanagementwkf.exception.Code;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResultVO {
    private int code;
    private String message;
    private Object data;

    private static final ResultVO EMPTY = ResultVO.builder().code(200).build();
    public static ResultVO ok() {
        return EMPTY;
    }

    public static ResultVO success(Object data) {
        return ResultVO.builder().code(200).data(data).build();
    }

    public static ResultVO error(int codeNum, String message) {
        return ResultVO.builder().code(codeNum).message(message).build();
    }
    public static ResultVO error(Code code) {
        return ResultVO.builder().code(code.getCode()).message(code.getMessage()).build();
    }

}
