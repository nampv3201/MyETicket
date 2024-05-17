package com.datn.ticket.model.mapper;

import com.datn.ticket.model.Accounts;
import com.datn.ticket.model.dto.AccountsDTO;
import com.datn.ticket.model.dto.response.AccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountMapper {
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);
}
