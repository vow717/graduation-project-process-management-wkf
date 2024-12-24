package com.example.graduationprojectprocessmanagementwkf.filter;

import com.example.graduationprojectprocessmanagementwkf.dox.User;
import com.example.graduationprojectprocessmanagementwkf.exception.Code;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

@Slf4j
@Order(2)
@RequiredArgsConstructor
@Component
public class AdminFilter implements WebFilter {

    private final PathPattern includes = new PathPatternParser().parse("/api/admin/**");
    private final ResponseHelper responseHelper;

    @NonNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (includes.matches(request.getPath().pathWithinApplication())) {
            String role = (String) exchange.getAttributes().get("role");
            //log.info("role:{}", role);
            if (User.ROLE_ADMIN.equals(role)) {
                return chain.filter(exchange);
            }
            return responseHelper.response(Code.FORBIDDEN, exchange);
        }
        return chain.filter(exchange);
    }
}
