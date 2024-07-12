package com.datn.ticket.controller;

import com.datn.ticket.dto.request.CreatePaymentRequest;
import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.Cart;
import com.datn.ticket.dto.request.AddToCartRequest;
import com.datn.ticket.dto.request.DirectPaymentRequest;
import com.datn.ticket.dto.request.PaymentRequest;
import com.datn.ticket.dto.response.ApiResponse;
import com.datn.ticket.configuration.VNPayConfig;
import com.datn.ticket.dto.response.PaymentResponse;
import com.datn.ticket.service.EventService;
import com.datn.ticket.service.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.datn.ticket.configuration.VNPayConfig.vnp_Command;
import static com.datn.ticket.configuration.VNPayConfig.vnp_Version;

@RestController
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {

    @Autowired
    UserService userService;

    @Autowired
    EventService eventService;

    @PostMapping("/direct_payment")
    public ApiResponse<?> directPayment(@RequestBody DirectPaymentRequest requests) throws UnsupportedEncodingException {
        List<Integer> cartIds = new ArrayList<>();
        double totalCost = 0;

        List<Cart> carts = new ArrayList<>();
        for(AddToCartRequest request : requests.getCartId()) {
            Cart cart = new Cart();
            cart.setCreateTickets(eventService.getTicketType(request.getCreateTicketId()));
            cart.setQuantity(request.getQuantity());
            carts.add(cart);
        }
        try{
            cartIds = userService.directOrder(carts);
            for(int cartId : cartIds){
                if(!userService.checkQuantity(cartId).getMessage().equals("OK")){
                    return ApiResponse.builder()
                            .message(userService.checkQuantity(cartId).getMessage())
                            .build();
                }
                totalCost += userService.getSingleCart(cartId).getCost();
            }
            String paymentUrl = configPayment(totalCost, cartIds, requests.getMethodId(), requests.getEmail());
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForEntity(paymentUrl, String.class);
            return ApiResponse.builder().result(paymentUrl)
                    .build();

        }catch (Exception e){
            userService.removeFromCart(cartIds);
            return ApiResponse.builder()
                    .message(e.getMessage())
                    .build();
        }
    }
    @PostMapping("/create_payment")
    public ApiResponse<?> createPayment(@RequestBody PaymentRequest request) throws UnsupportedEncodingException {

        double totalCost = 0;
        for(int i : request.getCartId()){
            if(!userService.checkQuantity(i).getMessage().equals("OK")){
                return ApiResponse.builder()
                       .message("Số lượng còn lại không đủ")
                       .build();
            }
            totalCost += userService.getSingleCart(i).getCost();
        }

        String paymentUrl = configPayment(totalCost, request.getCartId(), request.getMethod(), request.getEmail());
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForEntity(paymentUrl, String.class);
        return ApiResponse.builder().message("Tạo yêu cầu thành công")
                .result(paymentUrl).build();

    }

    @GetMapping("/infor-update")
    public ApiResponse getInforPayment(@RequestBody PaymentResponse response) throws UnsupportedEncodingException {

        try {
            String pResponse = userService.payment(response);
            return ApiResponse.builder().message(pResponse).build();
        }catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @GetMapping("/vnpay_response")
    public ResponseEntity<Map<String, Object>> handleVnpayIpn(@RequestParam Map<String, String> allParams) {
        String secureHash = allParams.get("vnp_SecureHash");
        String orderId = allParams.get("vnp_TxnRef");
        String rspCode = allParams.get("vnp_ResponseCode");
        log.info(secureHash);

        Gson gson = new Gson();
        Integer[] array = gson.fromJson(URLDecoder.decode(allParams.get("cartId"), StandardCharsets.UTF_8), Integer[].class);
        List<Integer> cartIds = Arrays.asList(array);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime dateTime = LocalDateTime.parse(allParams.get("vnp_PayDate"), formatter);
        String amount = allParams.get("vnp_Amount");

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String paymentTime = dateTime.format(outputFormatter);
        String email = allParams.get("email");

        Map<String, String> sortedParams = new TreeMap<>(allParams);
        String signData = sortedParams.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));


        String signed = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, signData);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("amount", amount);
        response.put("paymentTime", dateTime);
        response.put("cartId", cartIds.toString());
        response.put("email", email);

        if ("00".equals(rspCode)) {
            response.put("status", "Thanh toán thành công");
        } else {
            response.put("status", "Thanh toán thất bại");
        }

        return ResponseEntity.ok(response);
    }
    public String configPayment(double totalCost, List<Integer> cartId, int paymentMethod, String email) throws UnsupportedEncodingException {
            String orderType = "Other";
            String bankCode = "NCB";

//            String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
            UUID vnp_TxnRef = VNPayConfig.getId();

            String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.format("%.0f", totalCost*100));
            vnp_Params.put("vnp_CurrCode", "VND");

            if (bankCode != null && !bankCode.isEmpty()) {
                vnp_Params.put("vnp_BankCode", bankCode);
            }
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef.toString());
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + vnp_TxnRef);
            vnp_Params.put("vnp_OrderType", orderType);

//        String locate = req.getParameter("language");
            String locate = "vn";
            if (locate != null && !locate.isEmpty()) {
                vnp_Params.put("vnp_Locale", locate);
            } else {
                vnp_Params.put("vnp_Locale", "vn");
            }
            vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl +
                    "?cartId=" + URLEncoder.encode(cartId.toString(), StandardCharsets.UTF_8.toString()) +
                    "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8.toString()));
            vnp_Params.put("vnp_IpAddr", "vnp_IpAddr");

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
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

            log.info(hashData.toString());
            String queryUrl = query.toString();
            String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
            userService.createPayment(CreatePaymentRequest.builder()
                    .paymentId(vnp_TxnRef.toString())
                    .amount(BigDecimal.valueOf(totalCost)).build());

            return paymentUrl;
        }
}
