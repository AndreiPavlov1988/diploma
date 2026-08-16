package com.skypro.diploma.mapper;

import com.skypro.diploma.dto.comment.CommentDto;
import com.skypro.diploma.dto.comment.CreateCommentReq;
import com.skypro.diploma.entity.Comment;
import com.skypro.diploma.entity.User;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 18.0.2.1 (Oracle Corporation)"
)
@Component
public class CommentMapperImpl implements CommentMapper {

    private final DateTimeFormatter dateTimeFormatter_yyyy_MM_dd_HH_mm_ss_11333195168 = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );

    @Override
    public CommentDto toDto(Comment comment) {
        if ( comment == null ) {
            return null;
        }

        CommentDto commentDto = new CommentDto();

        commentDto.setAuthor( commentAuthorId( comment ) );
        commentDto.setAuthorName( mapUserToAuthorName( comment.getAuthor() ) );
        if ( comment.getCreatedAt() != null ) {
            commentDto.setCreatedAt( dateTimeFormatter_yyyy_MM_dd_HH_mm_ss_11333195168.format( comment.getCreatedAt() ) );
        }
        commentDto.setId( comment.getId() );
        commentDto.setText( comment.getText() );

        return commentDto;
    }

    @Override
    public Comment toEntity(CreateCommentReq createCommentReq) {
        if ( createCommentReq == null ) {
            return null;
        }

        Comment.CommentBuilder comment = Comment.builder();

        comment.text( createCommentReq.getText() );

        return comment.build();
    }

    @Override
    public void updateCommentFromDto(CreateCommentReq createCommentReq, Comment comment) {
        if ( createCommentReq == null ) {
            return;
        }

        if ( createCommentReq.getText() != null ) {
            comment.setText( createCommentReq.getText() );
        }
    }

    @Override
    public List<CommentDto> toDtoList(List<Comment> comments) {
        if ( comments == null ) {
            return null;
        }

        List<CommentDto> list = new ArrayList<CommentDto>( comments.size() );
        for ( Comment comment : comments ) {
            list.add( toDto( comment ) );
        }

        return list;
    }

    private Long commentAuthorId(Comment comment) {
        if ( comment == null ) {
            return null;
        }
        User author = comment.getAuthor();
        if ( author == null ) {
            return null;
        }
        Long id = author.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
