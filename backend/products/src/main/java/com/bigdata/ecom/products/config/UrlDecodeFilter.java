package com.bigdata.ecom.products.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class UrlDecodeFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            // Wrap the request to handle query parameter decoding
            HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(httpRequest) {
                @Override
                public String getQueryString() {
                    String query = super.getQueryString();
                    if (query != null) {
                        try {
                            return URLDecoder.decode(query, StandardCharsets.UTF_8);
                        } catch (IllegalArgumentException e) {
                            return query; // Return original if decoding fails
                        }
                    }
                    return null;
                }
            };
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
