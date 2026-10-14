package br.infnet.continuum.control.auth;

import br.infnet.continuum.control.mission.MissaoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("missionSecurity")
public class MissionSecurity {

    private final MissaoRepository missoes;
    private final UsuarioRepository usuarios;

    public MissionSecurity(MissaoRepository missoes, UsuarioRepository usuarios) {
        this.missoes = missoes;
        this.usuarios = usuarios;
    }

    public boolean canWrite(UUID missaoId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        boolean elevated = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_SUPERVISOR")
             || a.getAuthority().equals("ROLE_ADMINISTRADOR")
             || a.getAuthority().equals("ROLE_OPERADOR"));
        if (elevated) return true;
        // AGENTE: só se designado para a missão
        return usuarios.findByUsername(auth.getName())
                .map(u -> {
                    if (u.getAgente() == null) return false;
                    return missoes.findById(missaoId)
                            .map(m -> m.getAgentes().stream()
                                    .anyMatch(a -> a.getId().equals(u.getAgente().getId())))
                            .orElse(false);
                }).orElse(false);
    }
}
