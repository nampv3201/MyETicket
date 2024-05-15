package com.datn.ticket.service;

import com.datn.ticket.model.Accounts;
import com.datn.ticket.model.Roles;
import com.datn.ticket.model.dto.request.IntrospectRequest;
import com.datn.ticket.model.dto.response.AuthenticationResponse;
import com.datn.ticket.model.dto.response.IntrospectResponse;
import com.nimbusds.jose.JOSEException;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.util.List;

public interface AccountService {

    void newAccount(Accounts account, List<Integer> roleID);
    List<Accounts> getAccount();
    ResponseEntity<Object> getByID(Integer id);

    Accounts getUsername(String username);

    ResponseEntity<AuthenticationResponse> signIn(String username, String password, int role);

    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;

    ResponseEntity<Object> disableAccount(Integer id);
    ResponseEntity<Object> enableAccount(Integer id);

    Roles getRole(Integer id);


}
