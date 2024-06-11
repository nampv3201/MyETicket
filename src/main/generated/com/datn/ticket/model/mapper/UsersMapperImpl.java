package com.datn.ticket.model.mapper;

import com.datn.ticket.dto.response.UserInforResponse;
import com.datn.ticket.model.Users;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-06-10T17:22:19+0700",
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
