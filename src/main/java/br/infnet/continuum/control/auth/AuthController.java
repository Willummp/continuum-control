package br.infnet.continuum.control.auth;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UsuarioRepository usuarios;
    private final JwtService jwt;
    private final StringRedisTemplate redis;

    public AuthController(AuthenticationManager authManager, UsuarioRepository usuarios,
                          JwtService jwt, StringRedisTemplate redis) {
        this.authManager = authManager;
        this.usuarios = usuarios;
        this.jwt = jwt;
        this.redis = redis;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        Usuario u = usuarios.findByUsername(auth.getName()).orElseThrow();
        String jti = UUID.randomUUID().toString();
        String token = jwt.generate(u.getUsername(), u.getPerfil().name(), jti);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "perfil", u.getPerfil().name(),
                "username", u.getUsername()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        if (authentication != null && authentication.getDetails() instanceof String jti) {
            redis.opsForValue().set("jwt:blacklist:" + jti, "1",
                    Duration.ofMillis(jwt.getExpirationMs()));
        }
        return ResponseEntity.noContent().build();
    }
}
