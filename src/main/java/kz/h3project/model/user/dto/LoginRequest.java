package kz.h3project.model.user.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String username;
    private String password;
}
