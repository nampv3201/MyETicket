package com.datn.ticket.service;

import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.dto.EventStatisticDTO;
import com.datn.ticket.model.dto.StatisticsDetail;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface MerchantService {
    void updateMerchant(int id, Map<String, Object> input);
    Merchants getMerchantInfor(Integer id);
    List<Merchants> getListMerchants();

    List<EventStatisticDTO> getStatistics(int merchantID) throws ParseException;
    List<StatisticsDetail> getStatisticsByEvent(int eventId) throws ParseException;

}
