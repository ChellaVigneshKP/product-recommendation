package com.bigdata.ecom.gateway.config;

import com.bigdata.ecom.gateway.exception.TokenExpiredException;
import com.bigdata.ecom.gateway.util.JwtUtil;
import com.bigdata.ecom.gateway.util.RouteValidator;
import com.nimbusds.jwt.JWTClaimsSet;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final JwtUtil jweUtil;

    public AuthenticationFilter(JwtUtil jweUtil) {
        super(Config.class);
        this.jweUtil = jweUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestPath = request.getURI().getPath();
            if (RouteValidator.getIsSecured().test(request)) {
                HttpCookie jweTokenCookie = request.getCookies().getFirst("token");
                String jweToken = jweTokenCookie != null ? jweTokenCookie.getValue() : null;
                if (jweToken == null) {
                    return handleException(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "Missing authorization cookie");
                }else{
                    logger.info("JWE Token: {}", jweToken);
                }
                try {
                    JWTClaimsSet claims = jweUtil.validateToken(jweToken);
                    String username = claims.getSubject();
                    String email = (String) claims.getClaim("emailId");
                    String userId = String.valueOf(claims.getClaim("userId"));
                    String[] clientIpAddresses = extractClientIpAddresses(request);
                    String ipAddressV4 = clientIpAddresses[0];
                    String ipAddressV6 = clientIpAddresses[1];
                    logger.info("Request from IPv4: {}, IPv6: {} to Path: {}", ipAddressV4, ipAddressV6, requestPath);
                    logger.info("Request from IPv4: {}, IPv6: {}, Username: {}, Email: {}, UserId: {}", ipAddressV4, ipAddressV6, username, email, userId);
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .headers(httpHeaders -> {
                                httpHeaders.remove("X-User-Name");
                                httpHeaders.remove("X-User-Email");
                                httpHeaders.remove("X-User-Id");
                            })
                            .header("X-User-Name", username)
                            .header("X-User-Email", email)
                            .header("X-User-Id", userId)
                            .header("X-User-IP", ipAddressV4)
                            .header("X-User-IP-V6", ipAddressV6)
                            .build();
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                } catch (TokenExpiredException e) {
                    return handleException(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "Token has expired");
                } catch (Exception e) {
                    return handleException(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "Invalid JWE token");
                }
            } else {
                String[] clientIpAddresses = extractClientIpAddresses(request);
                String ipAddressV4 = clientIpAddresses[0];
                String ipAddressV6 = clientIpAddresses[1];
                logger.info("Request to unsecured path from IPv4: {}, IPv6: {} to Path: {}", ipAddressV4, ipAddressV6, requestPath);
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-IP", ipAddressV4)  // Add client IPv4
                        .header("X-User-IP-V6", ipAddressV6)  // Add client IPv6
                        .build();
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }
        };
    }

    private String[] extractClientIpAddresses(ServerHttpRequest request) {
        String ipAddressV4 = "Unknown";
        String ipAddressV6 = "Unknown";
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            String[] ipAddresses = xForwardedFor.split(",");
            String clientIp = ipAddresses[0].trim();
            try {
                InetAddress inetAddress = InetAddress.getByName(clientIp);
                if (inetAddress instanceof java.net.Inet4Address) {
                    ipAddressV4 = inetAddress.getHostAddress();
                } else if (inetAddress instanceof java.net.Inet6Address) {
                    ipAddressV6 = inetAddress.getHostAddress();
                    if (ipAddressV6.startsWith("::ffff:")) {
                        ipAddressV4 = ipAddressV6.substring(7);
                    }
                }
            } catch (Exception e) {
                logger.error("Error parsing IP address from X-Forwarded-For: {}", e.getMessage());
            }
        } else {
            InetSocketAddress remoteAddress = request.getRemoteAddress();
            if (remoteAddress != null) {
                InetAddress inetAddress = remoteAddress.getAddress();
                if (inetAddress instanceof java.net.Inet4Address) {
                    ipAddressV4 = inetAddress.getHostAddress();
                } else if (inetAddress instanceof java.net.Inet6Address) {
                    ipAddressV6 = inetAddress.getHostAddress();
                    if (ipAddressV6.startsWith("::ffff:")) {
                        ipAddressV4 = ipAddressV6.substring(7);
                    }
                }
            }
        }
        return new String[]{ipAddressV4, ipAddressV6};
    }

    private Mono<Void> handleException(ServerHttpResponse response, HttpStatus status, String message) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String responseBody = "{\"error\": \"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        private boolean enableLogging; // Example property to enable logging
        private String customHeader;   // Example property for a custom header

        // Constructor
        public Config(boolean enableLogging, String customHeader) {
            this.enableLogging = enableLogging;
            this.customHeader = customHeader;
        }

        // Getters and setters
        public boolean isEnableLogging() {
            return enableLogging;
        }

        public void setEnableLogging(boolean enableLogging) {
            this.enableLogging = enableLogging;
        }

        public String getCustomHeader() {
            return customHeader;
        }

        public void setCustomHeader(String customHeader) {
            this.customHeader = customHeader;
        }
    }
}