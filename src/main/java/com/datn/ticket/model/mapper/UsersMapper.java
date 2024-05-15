package com.datn.ticket.model.mapper;

import com.datn.ticket.model.Users;
import com.datn.ticket.model.dto.UsersDTO;

public class UsersMapper {
    public static UsersDTO toUsersDTO(Users users){
        UsersDTO usersDTO = new UsersDTO();
        usersDTO.setName(users.getName());
        usersDTO.setAddress(users.getAddress());
        usersDTO.setPhone(users.getPhone());
        usersDTO.setAge(users.getAge());
        usersDTO.setPoint(users.getPoint());

        return usersDTO;
    }
}
