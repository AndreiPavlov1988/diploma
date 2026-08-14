package com.skypro.diploma.mapper;

import com.skypro.diploma.dto.comment.CommentDto;
import com.skypro.diploma.dto.comment.CreateCommentReq;
import com.skypro.diploma.entity.Comment;
import com.skypro.diploma.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CommentMapper {

    // Метод для преобразования User в String (имя автора)
    default String mapUserToAuthorName(User user) {
        if (user == null) {
            return null;
        }
        return user.getFirstName() + " " + user.getLastName();
    }

    // Entity -> CommentDto
    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "authorName", expression = "java(mapUserToAuthorName(comment.getAuthor()))")
    CommentDto toDto(Comment comment);

    // CreateCommentReq -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "ad", ignore = true)
    Comment toEntity(CreateCommentReq createCommentReq);

    // CreateCommentReq -> Entity (обновление)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "ad", ignore = true)
    void updateCommentFromDto(CreateCommentReq createCommentReq, @MappingTarget Comment comment);

    // Список Entity -> список DTO
    List<CommentDto> toDtoList(List<Comment> comments);
}
