package com.bigdata.ecom.products.util;

import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    public JWTClaimsSet validateToken(String token) throws Exception {
        SignedJWT signedJWT = (SignedJWT) JWTParser.parse(token);
        signedJWT.verify(new MACVerifier(secret));
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        if (claims.getExpirationTime() != null && claims.getExpirationTime().before(new Date())) {
            throw new Exception("Token has expired");
        }
        logger.info("JWT token validated");
        return claims;
    }
}

