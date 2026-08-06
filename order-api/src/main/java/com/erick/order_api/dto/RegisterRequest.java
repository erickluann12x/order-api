package com.erick.order_api.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "O usuário é obrigatório")
    @Size(
            min = 3,
            max = 50,
            message = "O usuário deve possuir entre 3 e 50 caracteres"
    )
    private String username;

    @NotBlank(message = "A senha é obrigatória")
    @Size(
            min = 8,
            max = 72,
            message = "A senha deve possuir entre 8 e 72 caracteres"
    )
    private String password;
}
