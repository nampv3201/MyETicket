package com.datn.ticket.util;

import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.qrcode.QRCodeReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class QRCodeService {

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public static String generateQRCode(String text) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
        byte[] pngData = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(pngData);
    }

    public static String decodeQRCode(byte[] qrCodeImage) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(qrCodeImage);
        BufferedImage bufferedImage = ImageIO.read(bis);
        if (bufferedImage == null) {
            throw new Exception("Could not decode image");
        }
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        QRCodeReader qrCodeReader = new QRCodeReader();
        Result result = qrCodeReader.decode(bitmap);
        return result.getText();
    }

    @Async
    public void sendQR(String QRCode, String email, List<Object[]> eventMail) throws MessagingException, UnsupportedEncodingException {
        for(Object[] row : eventMail){
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);


            helper.setFrom(new InternetAddress(fromEmail, "ETicket"));
            helper.setTo(email);
            helper.setSubject((String) row[1]);
            StringBuilder txtMail = new StringBuilder();
            txtMail.append(String.format("Bạn đã đặt vé cho sự kiện: %s.Trong đó bao gồm:\n", row[1]));
            String[] type = row[3].toString().split(",");
            for(String txt : type){
                txtMail.append(String.format("  %s\n", txt));
            }
            txtMail.append(String.format("Chi tiết sự kiện: http://localhost:8080/home/%s", row[0]));
            message.setText(txtMail.toString());

            byte[] qrCodeBytes = Base64.getDecoder().decode(QRCode);
            InputStreamSource qrCodeSource = new ByteArrayResource(qrCodeBytes);
            helper.addAttachment("QRCode.png", qrCodeSource);

            emailSender.send(message);
        }

    }
}
