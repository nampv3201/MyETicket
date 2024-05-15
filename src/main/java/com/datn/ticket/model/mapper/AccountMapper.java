package com.datn.ticket.model.mapper;

import com.datn.ticket.model.Accounts;
import com.datn.ticket.model.dto.AccountsDTO;

public class AccountMapper {
    public static AccountsDTO accountsDTO(Accounts accounts){
        AccountsDTO acc = new AccountsDTO();
        acc.setUsername(accounts.getUsername());
        acc.setPassword(accounts.getPassword());
//        acc.setRole(accounts.getRole());

        return acc;
    }

}
