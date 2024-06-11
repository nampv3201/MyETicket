package com.datn.ticket.repository;

import com.datn.ticket.dto.response.ApiResponse;
import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.*;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import com.datn.ticket.dto.request.IntrospectRequest;
import com.datn.ticket.dto.request.LogoutRequest;
import com.datn.ticket.dto.request.RefreshRequest;
import com.datn.ticket.dto.response.AuthenticationResponse;
import com.datn.ticket.dto.response.IntrospectResponse;
import com.datn.ticket.service.AccountService;
import com.datn.ticket.util.EmailUtil;
import com.twilio.Twilio;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Repository
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final EntityManager manager;

    @Autowired
    JavaMailSender mailSender;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    // Email
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public AccountServiceImpl(EntityManager manager) {
        this.manager = manager;
//        Twilio.init(twilioId, twilioToken);
    }

    @Override
    @Transactional
    public void newAccount(Accounts account, List<String> roles) {
        manager.persist(account);
        List<Roles> aRoles;
        for(String role : roles){
            if(role.toUpperCase(Locale.ROOT).equals("USER")){
                Users u = new Users();
                u.setPoint(0);
                u.setAge(-1);
                u.setAccounts(account);
                manager.persist(u);
            } else if (role.toUpperCase(Locale.ROOT).equals("MERCHANT")) {
                Merchants m = new Merchants();
                m.setAccounts(account);
                manager.persist(m);
            }
            manager.createNativeQuery("insert into account_has_role (`Account_id`, `role_id`, `status`) " +
                    "values (:accountId, (select r.id from role r where r.role_name = :roleName), 1)")
                    .setParameter("accountId", account.getId())
                    .setParameter("roleName", role.toUpperCase(Locale.ROOT))
                    .executeUpdate();
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
    public Accounts getEmail(String email) {
        try {
            TypedQuery<Accounts> query = manager.createQuery("Select a from Accounts a where a.email = :email", Accounts.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public ResponseEntity<AuthenticationResponse> signIn(String username, String password, String role) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        Query customQuery = manager.createNativeQuery("select a.id, a.username, a.password, a.create_at, ar.role_id, ar.status " +
                " from account a" +
                " join account_has_role ar on ar.Account_id = a.id " +
                "join role r on ar.role_id = r.id " +
                "where a.username = :username and r.role_name = :role");
        customQuery.setParameter("username", username);
        customQuery.setParameter("role", role.toUpperCase(Locale.ROOT));
        ArrayList<Integer> roles = new ArrayList<>();
        try{
            List<Object[]> results = customQuery.getResultList();
            Accounts a = new Accounts();
            a.setId((Integer) results.get(0)[0]);
            a.setUsername((String) results.get(0)[1]);
            a.setPassword((String) results.get(0)[2]);
            a.setCreateAt((Date) results.get(0)[3]);
            if(!passwordEncoder.matches(password, a.getPassword())){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AuthenticationResponse.builder().token(null).authenticated(false).build());
            }
            for(Object[] r : results){
                roles.add((Integer) results.get(0)[4]);
            }
            a.setRoles(roles);
            if(Byte.toUnsignedInt((Byte) results.get(0)[5]) == 1){
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
    @Transactional
    public ApiResponse<?> changePassword(String oldPassword, String newPassword) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        try {
            Accounts a = (Accounts) manager.createNativeQuery("Select * from Account a where a.id = :id", Accounts.class)
                    .setParameter("id", SecurityContextHolder.getContext().getAuthentication().getName())
                    .getSingleResult();
            log.info("input: {}", passwordEncoder.encode(oldPassword));
            log.info("db: {}", a.getPassword());
            if (passwordEncoder.matches(oldPassword, a.getPassword())) {
                a.setPassword(passwordEncoder.encode(newPassword));
                manager.createNativeQuery("update Account a set a.password = :password where a.id = :id")
                        .setParameter("password", a.getPassword())
                        .setParameter("id", SecurityContextHolder.getContext().getAuthentication().getName())
                        .executeUpdate();
            }else{
                return ApiResponse.builder().message("Mật khẩu cũ không chính xác").build();
            }
            return ApiResponse.builder().message("Đổi mật khẩu thành công").build();
        }catch (NoResultException ne){
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> verifyOTP(String email, String OTP) {
        try {
            ForgotPassword myPw = (ForgotPassword) manager.createNativeQuery("select * from forgotpassword fp " +
                            "join account a on fp.Account_Id = a.id " +
                            "where a.email = :email", ForgotPassword.class)
                    .setParameter("email", email)
                    .getSingleResult();

            if(!myPw.getOTP().equals(OTP)){
                return ApiResponse.builder().message("OTP không khớp").build();
            }
            if(myPw.getExpirationTime().before(Date.from(LocalDateTime.now()
                    .atZone(ZoneId.systemDefault()).toInstant()))){
                return ApiResponse.builder().message("OTP hết hiệu lực").build();
            }
            manager.createNativeQuery("update forgotpassword set `expirationTime` = :expirationTime " +
                            "where Account_Id = :accountId")
                    .setParameter("accountId", getEmail(email).getId())
                    .setParameter("expirationTime", LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .executeUpdate();

            return ApiResponse.builder().message("True").build();
        }catch(Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> resetPassword(String email, String newPassword) {
        try{
            manager.createNativeQuery("update account set `password` = :password " +
                            "where email = :email")
                    .setParameter("email", email)
                    .setParameter("password",  newPassword)
                    .executeUpdate();

            return ApiResponse.builder().message("Thành công").build();
        }catch (Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public Roles getRole(Integer id) {
        TypedQuery<Roles> query = manager.createQuery("Select r from Roles r where r.id = :id", Roles.class);
        query.setParameter("id", id);
        return query.getSingleResult();
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

    @Override
    @Transactional
    public String sendOTP(String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        String OTP = genOTP();
        EmailUtil content = new EmailUtil("Quên mật khẩu", "Mã OTP của bạn là: " + OTP
            + "\nVui lòng không tiết lộ mã OTP này cho b kỳ ai." +
                "\nNếu bạn không gửi yêu cầu này, vui lòng bỏ qua tin nhắn");

        try{
            manager.createNativeQuery("insert into forgotpassword (`Account_Id`, `OTP`, `expirationTime`) " +
                    "values (:accountId, :otp, :expirationTime) " +
                    "ON DUPLICATE KEY  " +
                    "UPDATE `OTP` = :otp, `expirationTime` = :expirationTime")
                    .setParameter("accountId", getEmail(email).getId())
                    .setParameter("otp", OTP)
                    .setParameter("expirationTime", LocalDateTime.now().plusMinutes(5)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .executeUpdate();

            message.setFrom(fromEmail);
            message.setSubject(content.getSubject());
            message.setText(content.getMessage());
            message.setTo(email);
            mailSender.send(message);
        }catch(NoResultException e){
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }catch(Exception e){
            log.error(Arrays.toString(e.getStackTrace()));
            throw new AppException((ErrorCode.UNCATEGORIZED_EXCEPTION));
        }

        return "Gửi thành công";
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

    private String buildScope(Accounts accounts){
        StringJoiner joiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(accounts.getRoles())){
            for(int r : accounts.getRoles()){
                joiner.add(getRole(r).getRole_name());
            }
        }

        return joiner.toString();
    }

    private String genOTP(){
        SecureRandom random = new SecureRandom();

        // Tạo một StringBuilder để xây dựng OTP
        StringBuilder otp = new StringBuilder();

        // Tạo mã OTP với độ dài đã cho
        for (int i = 0; i < 6; i++) {
            otp.append(random.nextInt(10)); // Số ngẫu nhiên từ 0-9
        }

        return otp.toString();
    }
}
