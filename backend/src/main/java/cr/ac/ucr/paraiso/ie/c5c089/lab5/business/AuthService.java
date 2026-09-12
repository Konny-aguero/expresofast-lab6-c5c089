package cr.ac.ucr.paraiso.ie.c5c089.lab5.business;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.AuthResponseDTO;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponseDTO autenticarUsuario(AuthRequestDTO authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generarToken(authentication);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .collect(Collectors.toList());

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(jwt);
        response.setExpirationTime(tokenProvider.obtenerExpiracion(jwt));
        response.setUsername(authRequest.getUsername());
        response.setRoles(roles);
        return response;
    }
}