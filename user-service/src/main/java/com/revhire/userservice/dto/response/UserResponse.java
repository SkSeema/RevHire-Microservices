package com.revhire.userservice.dto.response;

import com.revhire.userservice.model.User.Role;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private Boolean status;
}
