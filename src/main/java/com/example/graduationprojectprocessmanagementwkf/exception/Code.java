package com.example.graduationprojectprocessmanagementwkf.exception;

import lombok.*;

@Getter
public enum Code {
    LOGIN_ERROR(400, "用户名密码错误"),
    BAD_REQUEST(400, "请求错误"),
    UNAUTHORIZED(401, "未登录"),
    TOKEN_EXPIRED(403, "过期请重新登录"),
    FORBIDDEN(403, "无权限"),
    FILE_UPLOAD_ERROR(400, "文件上传失败");
    public static final int ERROR = 400;
    private final int code;
    private final String message;

    Code(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
