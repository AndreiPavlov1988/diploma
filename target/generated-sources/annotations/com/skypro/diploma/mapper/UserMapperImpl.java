package com.skypro.diploma.mapper;

import com.skypro.diploma.dto.user.RegisterReq;
import com.skypro.diploma.dto.user.UpdateUserReq;
import com.skypro.diploma.dto.user.UserDto;
import com.skypro.diploma.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 18.0.2.1 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDto userDto = new UserDto();

        userDto.setImage( user.getImagePath() );
        userDto.setId( user.getId() );
        userDto.setUsername( user.getUsername() );
        userDto.setFirstName( user.getFirstName() );
        userDto.setLastName( user.getLastName() );
        userDto.setPhone( user.getPhone() );
        userDto.setRole( user.getRole() );

        return userDto;
    }

    @Override
    public User toEntity(RegisterReq registerReq) {
        if ( registerReq == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.username( registerReq.getUsername() );
        user.password( registerReq.getPassword() );
        user.firstName( registerReq.getFirstName() );
        user.lastName( registerReq.getLastName() );
        user.phone( registerReq.getPhone() );
        user.role( registerReq.getRole() );

        return user.build();
    }

    @Override
    public void updateUserFromDto(UpdateUserReq updateUserReq, User user) {
        if ( updateUserReq == null ) {
            return;
        }

        if ( updateUserReq.getFirstName() != null ) {
            user.setFirstName( updateUserReq.getFirstName() );
        }
        if ( updateUserReq.getLastName() != null ) {
            user.setLastName( updateUserReq.getLastName() );
        }
        if ( updateUserReq.getPhone() != null ) {
            user.setPhone( updateUserReq.getPhone() );
        }
    }

    @Override
    public List<UserDto> toDtoList(List<User> users) {
        if ( users == null ) {
            return null;
        }

        List<UserDto> list = new ArrayList<UserDto>( users.size() );
        for ( User user : users ) {
            list.add( toDto( user ) );
        }

        return list;
    }
}
