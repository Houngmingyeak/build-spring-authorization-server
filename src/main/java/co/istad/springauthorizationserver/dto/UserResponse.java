package co.istad.springauthorizationserver.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private List<String> roles;
    private LocalDateTime createdAt;
}