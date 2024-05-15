package com.datn.ticket.controller;


import com.datn.ticket.model.Accounts;
import com.datn.ticket.model.Roles;
import com.datn.ticket.model.dto.request.IntrospectRequest;
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
    public String newAccount(@RequestBody String jsonMap) throws ParseException {
        Gson gson = new Gson();
        ArrayList<Integer> rolesID = new ArrayList<>();
        JsonElement root = gson.fromJson(jsonMap, JsonElement.class);

        if(root.isJsonObject()){
            JsonObject jsonObject = root.getAsJsonObject();
            if(accountService.getUsername(jsonObject.get("username").getAsString()) != null){
                return "Tài khoản đã tồn tại";
            }
            Accounts a = new Accounts();
            a.setUsername(jsonObject.get("username").getAsString());
            a.setPassword(passwordEncoder.encode(jsonObject.get("password").getAsString()));
            a.setStatus(1);

            JsonArray roleArray = jsonObject.getAsJsonArray("role");
            for(JsonElement r : roleArray){
                rolesID.add(r.getAsInt());
            }
            try{
                accountService.newAccount(a, rolesID);
            }catch(Exception e){
                return e.getMessage();
            }
        }
        return "Sign up successfully!";
    }

    @Operation(summary = "Đăng nhập")
    @PostMapping("/sign-in")
    public ResponseEntity<AuthenticationResponse> signIn(@RequestBody Map<String, Object> jsonMap){
        return accountService.signIn(jsonMap.get("username").toString(), jsonMap.get("password").toString(), (Integer) jsonMap.get("role"));
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
