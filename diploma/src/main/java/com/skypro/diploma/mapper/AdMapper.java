package com.skypro.diploma.mapper;

import com.skypro.diploma.dto.ad.AdDto;
import com.skypro.diploma.dto.ad.CreateAdReq;
import com.skypro.diploma.dto.ad.FullAdDto;
import com.skypro.diploma.dto.ad.UpdateAdReq;
import com.skypro.diploma.entity.Ad;
import com.skypro.diploma.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AdMapper {

    // Метод для красивого вывода "Имя Фамилия"
    @Named("userToAuthorName")
    default String mapUserToAuthorName(User user) {
        if (user == null) return null;
        return user.getFirstName() + " " + user.getLastName();
    }

    // Entity -> AdDto
    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "image", source = "imagePath")
    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
    AdDto toDto(Ad ad);

    // Entity -> FullAdDto (с именем автора)
    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "authorName", source = "author", qualifiedByName = "userToAuthorName")
    @Mapping(target = "image", source = "imagePath")
    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
    FullAdDto toFullAdDto(Ad ad);

    // CreateAdReq -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imagePath", ignore = true) // imagePath мы обычно сохраняем отдельно через MultipartFile
    @Mapping(target = "imageData", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "author", ignore = true) // Автора проставляем вручную из SecurityContext в сервисе
    @Mapping(target = "comments", ignore = true)
    Ad toEntity(CreateAdReq createAdReq);

    // UpdateAdReq -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "imageData", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "comments", ignore = true)
    void updateAdFromDto(UpdateAdReq updateAdReq, @MappingTarget Ad ad);

    List<AdDto> toDtoList(List<Ad> ads);
}