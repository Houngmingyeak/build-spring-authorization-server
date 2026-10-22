package co.istad.springauthorizationserver.config;

import co.istad.springauthorizationserver.entity.Role;
import co.istad.springauthorizationserver.entity.User;
import co.istad.springauthorizationserver.repository.RoleRepository;
import co.istad.springauthorizationserver.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisteredClientRepository registeredClientRepository;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           RegisteredClientRepository registeredClientRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public void run(String... args) {

        // ===== បង្កើត user admin (បើមិនទាន់មាន) =====
        if (!userRepository.existsByUsername("admin")) {

            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new IllegalStateException(
                            "ROLE_ADMIN not found. Check the Flyway V1 migration."));
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new IllegalStateException(
                            "ROLE_USER not found. Check the Flyway V1 migration."));

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("1234"));
            admin.getRoles().add(adminRole);
            admin.getRoles().add(userRole);

            userRepository.save(admin);
        }

        // ===== បង្កើត client public-client (បើមិនទាន់មាន) =====
        if (registeredClientRepository.findByClientId("public-client") == null) {

            RegisteredClient publicClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("public-client")
                    .clientName("Public Client")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://127.0.0.1:4200")
                    .redirectUri("https://oauth.pstmn.io/v1/callback")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(true)
                            .requireProofKey(true)
                            .build())
                    .build();

            registeredClientRepository.save(publicClient);
        }

        // ===== ថ្មី ===== បង្កើត client confidential-client (បើមិនទាន់មាន)
        if (registeredClientRepository.findByClientId("confidential-client") == null) {

            RegisteredClient confidentialClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("confidential-client")
                    .clientName("Confidential Client")
                    // secret ត្រូវ hash ដោយ BCrypt មុនទុកក្នុង database (ពេលវាយក្នុង Postman ប្រើ client-secret-123)
                    .clientSecret(passwordEncoder.encode("client-secret-123"))
                    // អនុញ្ញាតទាំង ២ របៀបផ្ញើ secret: Basic header និង body
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    // authorization_code សម្រាប់ login, refresh_token សម្រាប់ស្នើ access token ថ្មី
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("https://oauth.pstmn.io/v1/callback")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(true)
                            .requireProofKey(true)
                            .build())
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofMinutes(5))
                            .refreshTokenTimeToLive(Duration.ofDays(7))
                            .reuseRefreshTokens(false)
                            .build())
                    .build();

            registeredClientRepository.save(confidentialClient);
        }
    }
}