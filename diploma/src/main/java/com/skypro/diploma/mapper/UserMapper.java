package com.skypro.diploma.mapper;

import com.skypro.diploma.dto.user.RegisterReq;
import com.skypro.diploma.dto.user.UpdateUserReq;
import com.skypro.diploma.dto.user.UserDto;
import com.skypro.diploma.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    // Entity -> DTO
    @Mapping(target = "image", source = "imagePath")
    UserDto toDto(User user);

    // RegisterReq -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "ads", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "imageData", ignore = true)
    User toEntity(RegisterReq registerReq);

    // UpdateUserReq -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "ads", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "imageData", ignore = true)
    void updateUserFromDto(UpdateUserReq updateUserReq, @MappingTarget User user);

    // Список Entity -> список DTO
    List<UserDto> toDtoList(List<User> users);
}
