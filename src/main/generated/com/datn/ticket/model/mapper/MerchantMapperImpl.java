package com.datn.ticket.model.mapper;

import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.dto.response.MerchantsResponse;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-05-24T16:27:53+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 19.0.1 (Oracle Corporation)"
)
public class MerchantMapperImpl implements MerchantMapper {

    @Override
    public MerchantsResponse merchantsDTO(Merchants merchants) {
        if ( merchants == null ) {
            return null;
        }

        MerchantsResponse.MerchantsResponseBuilder merchantsResponse = MerchantsResponse.builder();

        merchantsResponse.id( merchants.getId() );
        merchantsResponse.name( merchants.getName() );
        merchantsResponse.address( merchants.getAddress() );
        merchantsResponse.phone( merchants.getPhone() );
        merchantsResponse.license( merchants.getLicense() );
        merchantsResponse.description( merchants.getDescription() );

        return merchantsResponse.build();
    }
}
