package com.bigdata.ecom.gateway.util;

import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    public JWTClaimsSet validateToken(String token) throws Exception {
        SignedJWT signedJWT = (SignedJWT) JWTParser.parse(token);
        signedJWT.verify(new MACVerifier(secret));
        logger.info("JWT token validated");
        return signedJWT.getJWTClaimsSet();
    }
}
