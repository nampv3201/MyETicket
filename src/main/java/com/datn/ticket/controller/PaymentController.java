package com.datn.ticket.controller;

import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.dto.request.PaymentRequest;
import com.datn.ticket.model.dto.response.ApiResponse;
import com.datn.ticket.configuration.VNPayConfig;
import com.datn.ticket.model.dto.response.PaymentResponse;
import com.datn.ticket.service.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.datn.ticket.configuration.VNPayConfig.vnp_Command;
import static com.datn.ticket.configuration.VNPayConfig.vnp_Version;

@RestController
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {

    @Autowired
    UserService userService;
    @Autowired
    private HttpSession session;
    @Autowired
    private HttpServletRequest servletRequest;

    @GetMapping("/create_payment")
    public ApiResponse<?> createPayment(@RequestBody PaymentRequest request) throws UnsupportedEncodingException {

        String orderType = "other";
        String bankCode = "NCB";

        String vnp_TxnRef = VNPayConfig.getRandomNumber(8);

        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", "100000000");
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", request.getCartId().toString());
        vnp_Params.put("vnp_OrderType", orderType);

//        String locate = req.getParameter("language");
        String locate = "vn";
        if (locate != null && !locate.isEmpty()) {
            vnp_Params.put("vnp_Locale", locate);
        } else {
            vnp_Params.put("vnp_Locale", "vn");
        }
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl + "?userId=" + userService.myInfor().getId());
        vnp_Params.put("vnp_IpAddr", "vnp_IpAddr");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 10);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        vnp_Params.put("vnp_Inv_Customer", String.valueOf(userService.myInfor().getId()));

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
        com.google.gson.JsonObject job = new JsonObject();
        job.addProperty("code", "00");
        job.addProperty("message", "success");
        job.addProperty("data", paymentUrl);
        Gson gson = new Gson();
//        resp.getWriter().write(gson.toJson(job));
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForEntity(paymentUrl, String.class);
        return ApiResponse.builder().message("Tạo yêu cầu thành công")
                .result(paymentUrl).build();

    }

    @GetMapping("/infor")
    public ApiResponse<?> getInforPayment(@RequestParam("vnp_OrderInfo") String vnp_OrderInfo,
                                          @RequestParam("vnp_Amount") double amount,
                                          @RequestParam("vnp_BankTranNo") String bankTranNo,
                                          @RequestParam("vnp_PayDate") String paymentDate,
                                          @RequestParam("vnp_ResponseCode") String responseCode,
                                          @RequestParam("userId") int uId) throws UnsupportedEncodingException {

        try {
            // Chuyển đổi chuỗi JSON thành một mảng hoặc danh sách
            Gson gson = new Gson();
            Integer[] array = gson.fromJson(URLDecoder.decode(vnp_OrderInfo, StandardCharsets.UTF_8), Integer[].class);
            List<Integer> cartIds = Arrays.asList(array);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            Date dateTime = Date.from(LocalDateTime.parse(paymentDate, formatter).atZone(java.time.ZoneId.systemDefault()).toInstant());

            PaymentResponse response = new PaymentResponse();
            response.setCartIds(cartIds);
            response.setBankTranNo(bankTranNo);
            response.setAmount(amount);
            response.setPaymentDate(dateTime);
            response.setResponseCode(responseCode);
            response.setUId(uId);
//            try{
                userService.payment(response);
                session.invalidate();
                return ApiResponse.<PaymentResponse>builder().result(response).build();
//            }catch (Exception e){
//                session.invalidate();
//                return ApiResponse.builder().message(e.getMessage()).build();
//            }
        } catch (JsonSyntaxException e) {
            session.invalidate();
            return ApiResponse.builder().message("Outer" + e.getMessage()).build();
        }
    }
}
