package com.example.graduationprojectprocessmanagementwkf.exception;

import lombok.*;

//正在生成 equals/hashCode 实现，但即使此类未扩展 java.lang.Object，也不调用超类。
//如果这是有意为之，请在您的类型中添加 '(callSuper=false)'。
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class XException extends RuntimeException {
    private int codeNum;
    private String message;
    private Code code;
}
