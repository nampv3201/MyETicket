package com.datn.ticket.model.mapper;

import com.datn.ticket.model.Users;
import com.datn.ticket.model.dto.UsersDTO;
import com.datn.ticket.model.dto.response.UserInforResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface UsersMapper {
    UsersMapper INSTANCE = Mappers.getMapper(UsersMapper.class);
    @Mapping(source = "name", target = "name")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "age", target = "age")
    @Mapping(source = "point", target = "point")
    UserInforResponse toUserDto(Users users);
}
