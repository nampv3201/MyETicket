package com.datn.ticket.service;

import com.datn.ticket.dto.request.SignUpRequest;
import com.datn.ticket.dto.response.ApiResponse;
import com.datn.ticket.model.Accounts;
import com.datn.ticket.model.Roles;
import com.datn.ticket.dto.request.IntrospectRequest;
import com.datn.ticket.dto.request.LogoutRequest;
import com.datn.ticket.dto.request.RefreshRequest;
import com.datn.ticket.dto.response.AuthenticationResponse;
import com.datn.ticket.dto.response.IntrospectResponse;
import com.nimbusds.jose.JOSEException;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.util.List;

public interface AccountService {

    void newAccount(Accounts account, SignUpRequest request);
    Accounts getUsername(String username);
    Accounts getEmail(String email);

    boolean checkLicense(String license);
    ResponseEntity<AuthenticationResponse> signIn(String username, String password);
    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;
    Roles getRole(Integer id);
    void logout(LogoutRequest request) throws ParseException, JOSEException;
    AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;
    String sendOTP(String email);
    ApiResponse<?> verifyOTP(String email, String OTP);
    ApiResponse<?> changePassword(String oldPassword, String newPassword);
    ApiResponse<?> resetPassword(String email, String newPassword);

}
