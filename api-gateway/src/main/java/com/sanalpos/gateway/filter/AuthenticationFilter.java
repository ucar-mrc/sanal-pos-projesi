package com.sanalpos.gateway.filter;

import com.sanalpos.gateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;

    public AuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            // 1. İsteğin yolunu al
            String path = exchange.getRequest().getURI().getPath();

            // 2. Auth işlemleri (login/register) için token arama, direkt geçir
            if (path.startsWith("/api/v1/auth/")) {
                return chain.filter(exchange);
            }

            // 3. Diğer tüm istekler için Header'da Authorization var mı bak
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new RuntimeException("Yetkisiz erişim: Token bulunamadı!");
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

            // 4. Header "Bearer " ile başlıyor mu?
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7); // "Bearer " kısmını at
                try {
                    jwtUtil.validateToken(authHeader); // Token'ı doğrula
                } catch (Exception e) {
                    throw new RuntimeException("Yetkisiz erişim: Geçersiz token!");
                }
            } else {
                throw new RuntimeException("Yetkisiz erişim: Geçersiz token formatı!");
            }

            // Her şey yolundaysa isteği arkadaki servise ilet
            return chain.filter(exchange);
        });
    }

    public static class Config {
        // Konfigürasyon için gerekli (şimdilik boş)
    }
}