package com.example.graduationprojectprocessmanagementwkf.filter;

import com.example.graduationprojectprocessmanagementwkf.dox.User;
import com.example.graduationprojectprocessmanagementwkf.exception.Code;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

@Order(2)
@Component
@Slf4j
@RequiredArgsConstructor
public class TeacherFilter implements WebFilter {
    private final ResponseHelper responseHelper;
    PathPattern path=new PathPatternParser().parse("/api/teacher/**");

    @NonNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange,@NonNull WebFilterChain chain) {
        if(path.matches(exchange.getRequest().getPath().pathWithinApplication())){
            String role = (String) exchange.getAttributes().get("role");
            //log.info("role:{}", role);
            if (User.ROLE_TEACHER.equals(role)) {
                return chain.filter(exchange);
            }
            return responseHelper.response(Code.FORBIDDEN, exchange);
        }
        return chain.filter(exchange);
    }
}
