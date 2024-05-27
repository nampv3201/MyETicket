package com.datn.ticket.repository;

import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.*;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import com.datn.ticket.model.dto.AccountsDTO;
import com.datn.ticket.model.dto.request.IntrospectRequest;
import com.datn.ticket.model.dto.request.LogoutRequest;
import com.datn.ticket.model.dto.request.RefreshRequest;
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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Repository
@Slf4j
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
        Query customQuery = manager.createQuery("Select ar.accounts, ar.roles, a.username, ar.status from AccountRole ar" +
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @Transactional
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidateToken invalidateToken = new InvalidateToken();
            invalidateToken.setId(jit);
            invalidateToken.setExpiryTime(expiryTime);

            manager.persist(invalidateToken);
        } catch (AppException exception){
            log.info("Token already expired");
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
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(accounts))
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
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().validated(isValid).build();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                .toInstant().plus(30, ChronoUnit.MINUTES).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if(!(verified && expiryTime.after(new Date()))){
            throw  new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if(manager.createQuery("Select it from InvalidateToken it where it.id = :token", InvalidateToken.class)
                .setParameter("token", signedJWT.getJWTClaimsSet().getJWTID())
                .getResultList().size() > 0){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    @Override
    @Transactional
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidateToken invalidatedToken =
                InvalidateToken.builder().id(jit).expiryTime(expiryTime).build();

        manager.persist(invalidatedToken);

        var accountId = signedJWT.getJWTClaimsSet().getSubject();

        Query customQuery = manager.createQuery("Select ar.accounts, ar.roles from AccountRole ar" +
                " join ar.accounts a where a.id = :accountId");
        customQuery.setParameter("accountId", accountId);
        ArrayList<Integer> roles = new ArrayList<>();

        List<Object[]> results = customQuery.getResultList();
        Accounts a = new Accounts();
        a = (Accounts) results.get(0)[0];

        for(Object[] r : results){
            Roles mrole = new Roles();
            mrole = (Roles) r[1];
            roles.add(mrole.getId());
        }
        a.setRoles(roles);

        var token = generateToken(a);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
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
