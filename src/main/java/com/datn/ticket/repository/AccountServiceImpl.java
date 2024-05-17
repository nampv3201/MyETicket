package com.datn.ticket.repository;

import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.*;

import java.text.ParseException;
import java.util.StringJoiner;
import java.util.UUID;
import com.datn.ticket.model.dto.AccountsDTO;
import com.datn.ticket.model.dto.request.IntrospectRequest;
import com.datn.ticket.model.dto.response.AccountResponse;
import com.datn.ticket.model.dto.response.ApiResponse;
import com.datn.ticket.model.dto.response.AuthenticationResponse;
import com.datn.ticket.model.dto.response.IntrospectResponse;
import com.datn.ticket.model.mapper.AccountMapper;
import com.datn.ticket.service.AccountService;
import com.datn.ticket.util.CurrentAccount;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.util.CollectionUtils;

import javax.naming.Context;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AccountServiceImpl implements AccountService {

    private final EntityManager manager;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @Autowired
    public AccountServiceImpl(EntityManager manager) {
        this.manager = manager;
    }

    @Override
    @Transactional
    public void newAccount(Accounts account, List<Integer> roleID) {
        manager.persist(account);
        List<Roles> aRoles;
        for(int id : roleID){
            if(id == 2){
                Users u = new Users();
                u.setPoint(0);
                u.setAge(-1);
                u.setStatus(1);
                u.setAccounts(account);
                manager.persist(u);
            } else if (id == 3) {
                Merchants m = new Merchants();
                m.setAccounts(account);
                manager.persist(m);
            }
            Roles r = getRole(id);
            AccountRole ar = new AccountRole();
            ar.setAccounts(account);
            ar.setRoles(r);
            manager.persist(ar);
        }
    }

    @Override
    public List<AccountResponse> getAccount() {
        Query query = manager.createNativeQuery("select a.id, a.username, a.status, " +
                "GROUP_CONCAT(DISTINCT r.role_name ORDER BY r.role_name ASC SEPARATOR ', ') " +
                "from account a " +
                "join account_has_role ar on a.id = ar.Account_id " +
                "join role r on ar.role_id = r.id group by a.id", AccountResponse.class);
        List<AccountResponse> accounts = query.getResultList();
        return accounts;
    }

    @Override
    public AccountResponse getByID(Integer id) {
        Query query = manager.createNativeQuery("select a.id, a.username, a.status, " +
                "GROUP_CONCAT(DISTINCT r.role_name ORDER BY r.role_name ASC SEPARATOR ', ') " +
                "from account a " +
                "join account_has_role ar on a.id = ar.Account_id " +
                "join role r on ar.role_id = r.id where a.id = :id", AccountResponse.class);
        query.setParameter("id", id);
        try{
            return (AccountResponse) query.getSingleResult();
        }catch (NoResultException e) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
    }

    @Override
    public Accounts getUsername(String username) {
        try {
            TypedQuery<Accounts> query = manager.createQuery("Select a from Accounts a where a.username = :username", Accounts.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public ResponseEntity<AuthenticationResponse> signIn(String username, String password, int role) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        Query customQuery = manager.createQuery("Select ar.accounts, ar.roles, a.username, a.status from AccountRole ar" +
                " join ar.accounts a where a.username = :username");
        customQuery.setParameter("username", username);
        ArrayList<Integer> roles = new ArrayList<>();
        try{
            List<Object[]> results = customQuery.getResultList();
            Accounts a = new Accounts();
            a = (Accounts) results.get(0)[0];
            if(!passwordEncoder.matches(password, a.getPassword())){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AuthenticationResponse.builder().token(null).authenticated(false).build());
            }
            for(Object[] r : results){
                Roles mrole = new Roles();
                mrole = (Roles) r[1];
                roles.add(mrole.getId());
            }
            a.setRoles(roles);

            if(a.getRoles().contains(role) && (Integer) results.get(0)[3] == 1){
                CurrentAccount currentAccount = CurrentAccount.getInstance();
                currentAccount.setAccounts(a);
                currentAccount.setUsername((String) results.get(0)[2]);
                currentAccount.setStatus((Integer) results.get(0)[3]);

                var token = generateToken(a);
                return ResponseEntity.ok(AuthenticationResponse.builder().token(token).authenticated(true).build());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AuthenticationResponse.builder().token(null).authenticated(false).build());
        }catch (NoResultException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AuthenticationResponse.builder().token(null).authenticated(false).build());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Object> disableAccount(Integer id) {
        Query query = manager.createQuery("Update Accounts a set a.status = 0 where a.id = :id");
        query.setParameter("id", id);
        try{
            query.executeUpdate();
            return ResponseEntity.ok("Disabled");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi xảy ra, vui lòng thử lại sau");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Object> enableAccount(Integer id) {
        Query query = manager.createQuery("Update Accounts a set a.status = 1 where a.id = :id");
        query.setParameter("id", id);
        try{
            query.executeUpdate();
            return ResponseEntity.ok("Enabled");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi xảy ra, vui lòng thử lại sau");
        }
    }

    @Override
    public Roles getRole(Integer id) {
        TypedQuery<Roles> query = manager.createQuery("Select r from Roles r where r.id = :id", Roles.class);
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    private String generateToken(Accounts accounts) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(accounts.getId()))
                .issuer("my ticket")
                .jwtID(UUID.randomUUID().toString())
                .claim("Scope", buildScope(accounts))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        var token = request.getToken();
        SignedJWT signedJWT = SignedJWT.parse(token);
        boolean isValid = true;

        try {
//            verifyToken(token, false);
            isValid = signedJWT.verify(verifier);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().validated(isValid).build();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        var verified = signedJWT.verify(verifier);

        return signedJWT;
    }

    private String buildScope(Accounts accounts){
        StringJoiner joiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(accounts.getRoles())){
            for(int r : accounts.getRoles()){
                joiner.add(getRole(r).getRole_name());
            }
        }

        return joiner.toString();
    }
}
