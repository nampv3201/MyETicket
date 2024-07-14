package com.datn.ticket;

import com.datn.ticket.configuration.VNPayConfig;
import com.google.gson.Gson;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class TicketApplicationTests {

	@Test
	public void contextLoads() {
		System.out.println(hmacSHA512(VNPayConfig.secretKey, "userId=12&cartId=[16]&vnp_Amount=139800000&vnp_BankCode=VNPAY&vnp_CardType=QRCODE&vnp_OrderInfo=1&vnp_PayDate=20240606154305&vnp_ResponseCode=15&vnp_TmnCode=A8S4PS9W&vnp_TransactionNo=0&vnp_TransactionStatus=02&vnp_TxnRef=13569912"));
	}

	public static String hmacSHA512(final String key, final String data) {
		try {

			if (key == null || data == null) {
				throw new NullPointerException();
			}
			final Mac hmac512 = Mac.getInstance("HmacSHA512");
			byte[] hmacKeyBytes = key.getBytes();
			final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
			hmac512.init(secretKey);
			byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
			byte[] result = hmac512.doFinal(dataBytes);
			StringBuilder sb = new StringBuilder(2 * result.length);
			for (byte b : result) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();

		} catch (Exception ex) {
			return "";
		}
	}

}
