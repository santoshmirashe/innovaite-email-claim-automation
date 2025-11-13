package com.sapiens.innovate.vo;

import com.sapiens.innovate.entity.InnovaiteClaimUsers;

public class UserMapper {

    public static UserDTO toDTO(InnovaiteClaimUsers user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(String.valueOf(user.getRole()));
        return dto;
    }
}

