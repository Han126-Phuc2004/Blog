package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    private String username;
    private String role;
    private String message;
    
    public LoginResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.message = "Đăng nhập thành công";
    }
}

