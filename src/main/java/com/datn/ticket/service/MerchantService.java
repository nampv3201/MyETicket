package com.datn.ticket.service;

import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.dto.EventStatisticDTO;
import com.datn.ticket.model.dto.StatisticsDetail;

import java.text.ParseException;
import java.util.List;

public interface MerchantService {
    void updateMerchant(Merchants merchants);
    List<EventStatisticDTO> getStatistics() throws ParseException;
    List<StatisticsDetail> getStatisticsByEvent(int eventId) throws ParseException;

    Merchants myInfor();
}
