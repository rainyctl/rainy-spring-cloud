package cc.rainyctl.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// no registry required, apply automatically
@Slf4j
@Component
public class ResponseTimeGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String uri = request.getURI().toString();
        long start = System.currentTimeMillis();
        log.info("Request {} received at {}", uri, start);

        return chain.filter(exchange)
                .doFinally(signalType -> log.info("Request {} took {} ms", uri, System.currentTimeMillis() - start));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
