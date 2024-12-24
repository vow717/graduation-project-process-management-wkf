package com.example.graduationprojectprocessmanagementwkf.exception.filter;

import com.example.graduationprojectprocessmanagementwkf.exception.Code;
import com.example.graduationprojectprocessmanagementwkf.exception.component.JWTComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.util.List;

@Order(1)
@Component
@Slf4j
@RequiredArgsConstructor
public class LoginFilter implements WebFilter {
    private final PathPattern path=new PathPatternParser().parse("/api/**");
    private final List<PathPattern> excludes=List.of(new PathPatternParser().parse("/api/login"));
    private final JWTComponent jwtComponent;
    private final ResponseHelper responseHelper;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain){
        for(PathPattern p:excludes){
            if(p.matches(exchange.getRequest().getPath().pathWithinApplication())){
                return chain.filter(exchange);
            }
        }
        if(!path.matches(exchange.getRequest().getPath().pathWithinApplication())){
            return responseHelper.response(Code.BAD_REQUEST,exchange);
        }
        String token=exchange.getRequest().getHeaders().getFirst("token");
        return jwtComponent.decode(token)
                .flatMap(d->{
                    exchange.getAttributes().put("uid",d.getClaim("uid").asString());
                    exchange.getAttributes().put("role",d.getClaim("role").asString());
                    return chain.filter(exchange);
                });
    }
}
