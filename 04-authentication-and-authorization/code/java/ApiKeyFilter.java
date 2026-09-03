// System Design - Backend
// Chapter 04, Authentication & Authorization -> 10 API key authentication
// Java 21 / Spring Boot 3.3 / Spring Security 6

// Store only the HASH of an issued key, exactly as with a password.
// On each call, hash what was presented and compare in constant time.
@Component
class ApiKeyFilter extends OncePerRequestFilter {
    
    private final ApiClientRepository clients;
    
    ApiKeyFilter(ApiClientRepository clients) {
        this.clients = clients;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
        throws ServletException, IOException {
        
        String presented = request.getHeader("X-API-Key");
        if (presented == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "authentication failed");
            return;
        }
        
        String hash = sha256Hex(presented);
        Optional<ApiClient> client = clients.findByKeyHash(hash);
        
        // MessageDigest.isEqual is constant-time: it never returns
        // early on the first differing byte, so the response time
        // leaks nothing about how much of the key was correct.
        if (client.isEmpty() || !MessageDigest.isEqual(
            hash.getBytes(StandardCharsets.UTF_8),
            client.get().keyHash().getBytes(StandardCharsets.UTF_8))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "authentication failed");
            return;
        }
        
        // check scopes, quota and expiry here
        chain.doFilter(request, response);
    }
}
