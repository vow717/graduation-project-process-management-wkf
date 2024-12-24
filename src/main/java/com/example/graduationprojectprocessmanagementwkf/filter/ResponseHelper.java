package com.example.graduationprojectprocessmanagementwkf.filter;

import com.example.graduationprojectprocessmanagementwkf.exception.Code;
import com.example.graduationprojectprocessmanagementwkf.vo.ResultVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 复制老师的代码
 *
 * @SneakyThrows注解：
 * 这也是 Lombok 提供的一个注解，它的作用是将方法中可能抛出的异常自动包装成一个运行时异常抛出。在这个response方法中，它简化了对ObjectMapper.writeValueAsString方法可能抛出的异常处理，使得代码看起来更加简洁，无需显式地使用try-catch块来处理这些异常（但在实际运行时，如果出现问题，仍然会抛出异常，只是变成了运行时异常的形式）。
 * 构建响应数据：
 * 首先，通过objectMapper.writeValueAsString(ResultVO.error(code))将一个ResultVO对象（根据传入的Code参数构建的错误信息对象）转换为 JSON 格式的字符串。然后，使用getBytes(StandardCharsets.UTF_8)将这个 JSON 字符串转换为字节数组，以便后续作为响应体发送出去。
 * 配置响应头：
 * 获取当前请求对应的ServerHttpResponse对象，即ServerWebExchange中的响应部分，通过response.getHeaders().setContentType(MediaType.APPLICATION_JSON)设置响应头的Content-Type为APPLICATION_JSON，明确告知客户端返回的数据是 JSON 格式的。
 * 返回响应数据作为Mono<Void>：
 * 创建一个DataBuffer对象，通过response.bufferFactory().wrap(bytes)将前面准备好的字节数组包装成DataBuffer形式，以便能够在响应流中使用。然后，通过response.writeWith(Flux.just(wrap))将包含响应数据的DataBuffer以Flux（反应式流中的一种数据发布者形式）的形式写入到响应中，并最终返回一个Mono<Void>。这里的Mono<Void>表示一个异步操作的结果，它不返回具体的值（因为是写入响应的操作，重点在于完成写入过程，而不是返回某个具体的数据值），当写入操作完成后，这个Mono<Void>就完成了它的任务。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ResponseHelper {
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public Mono<Void> response(Code code, ServerWebExchange exchange) {
        byte[] bytes = objectMapper.writeValueAsString(ResultVO.error(code))
                .getBytes(StandardCharsets.UTF_8);
        ServerHttpResponse response = exchange.getResponse();
        DataBuffer wrap = response.bufferFactory().wrap(bytes);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response.writeWith(Flux.just(wrap));
    }
}
