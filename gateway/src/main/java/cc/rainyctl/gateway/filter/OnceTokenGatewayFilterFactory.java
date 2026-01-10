package cc.rainyctl.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class OnceTokenGatewayFilterFactory extends AbstractNameValueGatewayFilterFactory {

    @Override
    public GatewayFilter apply(NameValueConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                // add a token to the response header
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    String tokenName = config.getName();
                    String value = config.getValue();
                    String token = null;
                    if ("uuid".equals(value)) {
                        token = UUID.randomUUID().toString();
                    } else if ("jwt".equals(value)) {
                        token = "aaa.bbb.ccc"; // close enough :)
                    } else {
                        token = "random-token";
                    }
                    exchange.getResponse().getHeaders().add(tokenName, token);
                }));
            }
        };
    }
}
