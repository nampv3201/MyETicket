package com.datn.ticket.model.mapper;

import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.dto.response.MerchantsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface MerchantMapper {
    MerchantMapper INSTANCE = Mappers.getMapper(MerchantMapper.class);
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "license", target = "license")
    @Mapping(source = "description", target = "description")
    MerchantsResponse merchantsDTO(Merchants merchants);
}
