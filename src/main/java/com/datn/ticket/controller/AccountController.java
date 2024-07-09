package com.datn.ticket.controller;


import com.datn.ticket.dto.request.*;
import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.Accounts;
import com.datn.ticket.dto.response.ApiResponse;
import com.datn.ticket.dto.response.AuthenticationResponse;
import com.datn.ticket.dto.response.IntrospectResponse;
import com.datn.ticket.service.AccountService;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/account")
@Tag(name = "Account Controller")
@Slf4j
public class AccountController {
    private final AccountService accountService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private HttpServletRequest sessionnequest;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Đăng ký tài khoản mới")
    @PostMapping("/sign-up")
    public ApiResponse<?> newAccount(@RequestBody SignUpRequest request) throws ParseException {
        if(accountService.getUsername(request.getUsername()) != null){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        if(accountService.getEmail(request.getEmail()) != null){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if(request.getMerchantInfor() != null){
            if(!accountService.checkLicense(request.getMerchantInfor().getLicense())){
                throw new AppException(ErrorCode.LICENSE_HAS_REGISTERED);
            }
        }

        Accounts a = new Accounts();
        a.setUsername(request.getUsername());
        a.setPassword(passwordEncoder.encode(request.getPassword()));
        a.setEmail(request.getEmail());
        a.setCreateAt(java.sql.Date.valueOf(LocalDate.now()));
        try{
            accountService.newAccount(a, request);
        }catch(Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        return ApiResponse.builder().message("Đăng ký thành công").build();
    }


    @Operation(summary = "Đăng nhập")
    @PostMapping("/sign-in")
    public ResponseEntity<AuthenticationResponse> signIn(@RequestBody LoginRequest request){
        return accountService.signIn(request.getUsername(), request.getPassword());
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = accountService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @Operation(summary = "Đăng xuất")
    @PostMapping("/log-out")
    public void logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        accountService.logout(request);
    }

    @Operation(summary = "Refresh token")
    @PostMapping("/refresh-token")
    public AuthenticationResponse refreshToken(@RequestBody RefreshRequest request) throws ParseException, JOSEException {
        return accountService.refreshToken(request);
    }

    @Operation(summary = "Thay đổi mật khẩu")
    @PostMapping("/change-pasword")
    public ApiResponse<?> changePassword(@RequestBody ChangePasswordRequest request){
        return accountService.changePassword(request.getOldPassword(), request.getNewPassword());
    }

    @Operation(summary = "Quên mật khẩu")
    @PostMapping("/forgot_password")
    public ApiResponse<?> forgotPassword(@RequestBody Map<String, String> email) throws ParseException, JOSEException {
        if(accountService.getEmail(email.get("email")) == null){
            throw new AppException(ErrorCode.ITEM_NOT_EXIST);        }
        return ApiResponse.builder().message(accountService.sendOTP(email.get("email"))).build();
    }

    @Operation(summary = "Verify OTP")
    @PostMapping("/verifyOTP")
    public ApiResponse<?> verifyOTP(@RequestBody VerifyOTPRequest request) {
        HttpSession session = sessionnequest.getSession(true);
        session.setMaxInactiveInterval(3 * 60);

        session.setAttribute("email", request.getEmail());
        return accountService.verifyOTP(request.getEmail(), request.getOTP());
    }

    @Operation(summary = "resetPassword")
    @PostMapping("/resetPassword")
    public ApiResponse<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        HttpSession session = sessionnequest.getSession(false);
        if(session == null){
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }
        return accountService.resetPassword(session.getAttribute("email").toString(), passwordEncoder.encode(request.getPassword()));
    }
    
}
