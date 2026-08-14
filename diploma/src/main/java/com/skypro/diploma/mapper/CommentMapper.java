package com.skypro.diploma.mapper;

import com.skypro.diploma.dto.comment.CommentDto;
import com.skypro.diploma.dto.comment.CreateCommentReq;
import com.skypro.diploma.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CommentMapper {

    // Entity -> CommentDto
    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "authorName", source = "author")
    CommentDto toDto(Comment comment);

    // CreateCommentReq -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)        // ← ИСПРАВЛЕНО: active вместо isActive
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "ad", ignore = true)
    Comment toEntity(CreateCommentReq createCommentReq);

    // CreateCommentReq -> Entity (обновление)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)        // ← ИСПРАВЛЕНО: active вместо isActive
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "ad", ignore = true)
    void updateCommentFromDto(CreateCommentReq createCommentReq, @MappingTarget Comment comment);

    // Список Entity -> список DTO
    List<CommentDto> toDtoList(List<Comment> comments);
}
