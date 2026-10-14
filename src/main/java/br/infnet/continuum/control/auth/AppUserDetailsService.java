package br.infnet.continuum.control.auth;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public AppUserDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = usuarios.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        if (!u.isAtivo()) {
            throw new UsernameNotFoundException("Usuário inativo");
        }
        return User.withUsername(u.getUsername())
                .password(u.getPassword())
                .roles(u.getPerfil().name())
                .disabled(!u.isAtivo())
                .build();
    }
}
