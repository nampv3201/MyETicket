package com.datn.ticket.model.mapper;

import com.datn.ticket.model.Users;
import com.datn.ticket.model.dto.response.UserInforResponse;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-05-19T23:34:30+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 19.0.1 (Oracle Corporation)"
)
public class UsersMapperImpl implements UsersMapper {

    @Override
    public UserInforResponse toUserDto(Users users) {
        if ( users == null ) {
            return null;
        }

        UserInforResponse.UserInforResponseBuilder userInforResponse = UserInforResponse.builder();

        userInforResponse.name( users.getName() );
        userInforResponse.address( users.getAddress() );
        userInforResponse.phone( users.getPhone() );
        userInforResponse.age( users.getAge() );
        userInforResponse.point( users.getPoint() );

        return userInforResponse.build();
    }
}
