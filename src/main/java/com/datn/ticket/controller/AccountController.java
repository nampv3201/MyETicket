package com.datn.ticket.controller;


import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.Accounts;
import com.datn.ticket.model.Roles;
import com.datn.ticket.model.dto.request.IntrospectRequest;
import com.datn.ticket.model.dto.request.LoginRequest;
import com.datn.ticket.model.dto.request.SignUpRequest;
import com.datn.ticket.model.dto.response.ApiResponse;
import com.datn.ticket.model.dto.response.AuthenticationResponse;
import com.datn.ticket.model.dto.response.IntrospectResponse;
import com.datn.ticket.service.AccountService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/account")
@Tag(name = "Account Controller")
public class AccountController {
    private final AccountService accountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Lấy danh sách tất cả các tài khoản")
    @GetMapping("/getAccount")
    public List<Accounts> getAccount(){
        return accountService.getAccount();
    }

    @Operation(summary = "Lấy 1 tài khoản cụ thể")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable("id") Integer id){
        return accountService.getByID(id);
    }

    @Operation(summary = "Đăng ký tài khoản mới")
    @PostMapping("/sign-up")
    public ApiResponse<?> newAccount(@RequestBody SignUpRequest request) throws ParseException {
        if(accountService.getUsername(request.getUsername()) != null){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        Accounts a = new Accounts();
        a.setUsername(request.getUsername());
        a.setPassword(passwordEncoder.encode(request.getPassword()));
        a.setStatus(1);
        try{
            accountService.newAccount(a, request.getRole());
        }catch(Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        return ApiResponse.builder().message("Đăng ký thành công").build();
    }

    @Operation(summary = "Đăng nhập")
    @PostMapping("/sign-in")
    public ResponseEntity<AuthenticationResponse> signIn(@RequestBody LoginRequest request){
        return accountService.signIn(request.getUsername(), request.getPassword(), request.getRole());
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = accountService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @Operation(summary = "Disable tài khoản")
    @PutMapping("/disable/{id}")
    public void disableAccount(@PathVariable("id") int id){
        accountService.disableAccount(id);
    }

    @Operation(summary = "Enable tài khoản")
    @PutMapping("/enable/{id}")
    public void enableAccount(@PathVariable("id") int id){
        accountService.enableAccount(id);
    }
}
