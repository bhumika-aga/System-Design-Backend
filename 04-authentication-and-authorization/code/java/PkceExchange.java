// System Design - Backend
// Chapter 04, Authentication & Authorization -> 13 OAuth 2.0, PKCE
// Java 21 / Spring Boot 3.3 / Spring Security 6

@Service
class OAuthClient {
    
    // Build the PKCE pair before redirecting the user away.
    
    // Leg 2: exchange the code, sending code_verifier -- not a secret.
    TokenResponse exchange(String code, String verifier) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", "https://notes.app/callback");
        form.add("client_id", "note_app");
        form.add("code_verifier", verifier);
        
        return restClient.post()
                   .uri("https://auth.example/token")
                   .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                   .body(form)
                   .retrieve()
                   .body(TokenResponse.class);
    }
    // spring-boot-starter-oauth2-client performs this whole dance for
    // you, PKCE included, against any standard provider.
}

record Pkce(String verifier, String challenge) {
    
    static Pkce create() throws NoSuchAlgorithmException {
        byte[] bytes = new byte[32];
        SecureRandom.getInstanceStrong().nextBytes(bytes);
        
        String verifier = Base64.getUrlEncoder().withoutPadding()
                              .encodeToString(bytes);
        
        byte[] digest = MessageDigest.getInstance("SHA-256")
                            .digest(verifier.getBytes(StandardCharsets.US_ASCII));
        
        return new Pkce(verifier, Base64.getUrlEncoder().withoutPadding()
                                      .encodeToString(digest));
    }
}
