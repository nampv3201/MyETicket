package com.datn.ticket.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentUpdateRequest {
    String cartId;
    String email;
    Double vnp_Amount;
    String vnp_BankCode;
    String vnp_BankTranNo;
    String vnp_CardType;
    String vnp_OrderInfo;
    String vnp_PayDate;
    String vnp_ResponseCode;
    String vnp_TmnCode;
    String vnp_TransactionNo;
    String vnp_TransactionStatus;
    String vnp_TxnRef;
    String vnp_SecureHash;
}
