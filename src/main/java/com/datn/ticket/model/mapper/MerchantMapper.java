package com.datn.ticket.model.mapper;

import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.dto.MerchantsDTO;

public class MerchantMapper {
    public static MerchantsDTO merchantsDTO(Merchants merchants){
        MerchantsDTO tmp = new MerchantsDTO();
        tmp.setName(merchants.getName());
        tmp.setAddress(merchants.getAddress());
        tmp.setDescription(merchants.getDescription());
        tmp.setLicense(merchants.getLicense());
        tmp.setPhone(merchants.getPhone());
        return tmp;
    }
}
