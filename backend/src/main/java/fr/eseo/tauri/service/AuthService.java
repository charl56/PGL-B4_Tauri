package fr.eseo.tauri.service;

import fr.eseo.tauri.model.Project;
import fr.eseo.tauri.model.Role;
import fr.eseo.tauri.model.User;
import fr.eseo.tauri.model.enumeration.RoleType;
import fr.eseo.tauri.repository.ProjectRepository;
import fr.eseo.tauri.repository.RoleRepository;
import fr.eseo.tauri.repository.UserRepository;
import fr.eseo.tauri.security.AuthResponse;
import fr.eseo.tauri.security.JwtTokenUtil;
import fr.eseo.tauri.util.CustomLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProjectRepository projectRepository;

    public AuthResponse login(String email, String password) {
        try {
//            Authentication authentication = authenticate(email, password);  // Auth with LDAP
//            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

//          Check if user in DB
//            User user = userRepository.findByEmail(userDetails.getUsername())
            User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new SecurityException("Wrong credentials")); // User exist in LDAP, but not in DB

            String accessToken = jwtTokenUtil.generateAccessToken(user);
            CustomLogger.info("Access token generated for user " + user.id() + " : " + accessToken);
            Integer idProject = projectRepository.findFirstByActualTrue().map(Project::id).orElse(0);
            return new AuthResponse(user.id(), accessToken, idProject);
        } catch (Exception e){
            throw new SecurityException("Wrong credentials" + e.getMessage());
        }
    }

    public Authentication authenticate(String email, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
    }
}
