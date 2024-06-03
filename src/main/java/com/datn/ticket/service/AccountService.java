package com.datn.ticket.service;

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

    void newAccount(Accounts account, List<Integer> roleID);
    Accounts getUsername(String username);
    ResponseEntity<AuthenticationResponse> signIn(String username, String password, int role);
    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;
    Roles getRole(Integer id);
    void logout(LogoutRequest request) throws ParseException, JOSEException;
    AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;

}
