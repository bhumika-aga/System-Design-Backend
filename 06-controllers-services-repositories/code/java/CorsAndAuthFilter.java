// System Design - Backend
// Chapter 06, Controllers, Services & Repositories -> 12 Common middlewares
// Java 21 / Spring Boot 3.3

package com.example.layers.filter;

// CORS is configuration, not a filter you write by hand.
@Configuration
class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("https://app.example.com");
    }
}

// Auth: verify, then put the identity somewhere the rest of the
// request can reach it. In Spring that place is the SecurityContext,
// not a bag of loose request attributes.
@Component
class AuthFilter extends OncePerRequestFilter {
    
    private final TokenVerifier tokens;
    
    AuthFilter(TokenVerifier tokens) {
        this.tokens = tokens;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
        throws ServletException, IOException {
        
        try {
            AppUser user = tokens.verify(
                request.getHeader(HttpHeaders.AUTHORIZATION));
            
            var authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.authorities());
            SecurityContextHolder.getContext()
                .setAuthentication(authentication);
            
        } catch (BadCredentialsException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return; // 401, and stop
        }
        
        chain.doFilter(request, response);
    }
}
