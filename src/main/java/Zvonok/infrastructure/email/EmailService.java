package Zvonok.infrastructure.email;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async("emailTaskExecutor")
    public void sendOtpEmail(String toEmail, String otpCode) {
        log.info("Async thread: {}", Thread.currentThread());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            message.setFrom("Trevoo <sashashevcov199929@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject("Код подтверждения - Trevoo");
            helper.setText(buildOtpEmailHtml(otpCode), true);

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildOtpEmailHtml(String otpCode) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Trevoo - Подтверждение email</title>
                </head>
                <body style="margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;background:#1a1614;color:#f5f2ed;line-height:1.6;">
                
                    <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background:#1a1614;min-height:100vh;">
                        <tr>
                            <td align="center" valign="top" style="padding:40px 20px;">
                
                                <!-- Карточка -->
                                <table width="100%%" style="max-width:600px;background:#221d1a;border-radius:20px;border:1px solid #3a322d;box-shadow:0 4px 20px rgba(0,0,0,0.4);">
                
                                    <!-- Логотип + заголовок -->
                                    <tr>
                                        <td style="padding:40px 40px 20px;text-align:center;">
                                            <h1 style="margin:0;font-size:36px;font-weight:700;color:#ff7b67;letter-spacing:-1px;">Trevoo</h1>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:0 40px 24px;text-align:center;">
                                            <h2 style="margin:0;font-size:24px;font-weight:600;color:#f5f2ed;">Подтверждение email</h2>
                                        </td>
                                    </tr>
                
                                    <!-- Основной текст -->
                                    <tr>
                                        <td style="padding:0 40px 20px;text-align:center;">
                                            <p style="margin:0;font-size:16px;color:#d0c4b8;">
                                                Здравствуйте!<br>
                                                Введите этот код в приложение Trevoo:
                                            </p>
                                        </td>
                                    </tr>
                
                                    <!-- Блок с кодом — самый важный -->
                                    <tr>
                                        <td style="padding:0 40px 32px;">
                                            <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                                                <tr>
                                                    <td style="background:#1f1a17;border:2px solid #3a322d;border-radius:16px;padding:28px;text-align:center;">
                                                        <div style="font-family:'Courier New',monospace;font-size:48px;font-weight:bold;letter-spacing:12px;color:#ff7b67;text-shadow:0 0 12px rgba(255,123,103,0.4);">
                                                            %s
                                                        </div>
                                                        <p style="margin:16px 0 0;font-size:14px;color:#b8a798;">
                
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                
                                    <!-- Срок действия -->
                                    <tr>
                                        <td style="padding:0 40px 24px;text-align:center;">
                                            <p style="margin:0;font-size:14px;color:#b8a798;">
                                                ⏰ Код действителен <strong>5 минут</strong>
                                            </p>
                                        </td>
                                    </tr>
                
                                    <!-- Безопасность -->
                                    <tr>
                                        <td style="padding:0 40px 32px;">
                                            <table width="100%%" style="background:#2a2420;border-radius:12px;border-left:4px solid #ff7b67;padding:20px;">
                                                <tr>
                                                    <td>
                                                        <p style="margin:0 0 8px;font-size:14px;font-weight:600;color:#f5f2ed;">🔒 Безопасность</p>
                                                        <p style="margin:0;font-size:13px;color:#b8a798;line-height:1.5;">
                                                            Никому не сообщайте этот код. Мы никогда не запрашиваем его по телефону или в чате.
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                
                                    <!-- Футер -->
                                    <tr>
                                        <td style="padding:32px 40px;text-align:center;font-size:13px;color:#b8a798;">
                                            Если вы не регистрировались — просто проигнорируйте письмо.<br>
                                            <span style="color:#5fbf9b;margin-top:8px;display:block;">Trevoo — Всегда на созвоне</span>
                                            <br>
                                            © 2026 Trevoo. Все права защищены.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, otpCode);
    }

    // =============================== НОВЫЙ МЕТОД ДЛЯ ВОССТАНОВЛЕНИЯ ПАРОЛЯ ===============================
    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(String toEmail, String resetLink) {   // ← можно добавить String token, если хочешь код
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            message.setFrom("Trevoo <sashashevcov199929@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject("Восстановление пароля - Trevoo");


            helper.setText(buildPasswordResetEmailHtml(resetLink), true);

            mailSender.send(message);
            log.info("✅ Password reset email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("❌ Failed to send password reset email to: {}", toEmail, e);

        }
    }

    private String buildPasswordResetEmailHtml(String resetLink) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Trevoo - Восстановление пароля</title>
                </head>
                <body style="margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;background:#1a1614;color:#f5f2ed;line-height:1.6;">
                
                    <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background:#1a1614;min-height:100vh;">
                        <tr>
                            <td align="center" valign="top" style="padding:40px 20px;">
                
                                <!-- Карточка -->
                                <table width="100%%" style="max-width:600px;background:#221d1a;border-radius:20px;border:1px solid #3a322d;box-shadow:0 4px 20px rgba(0,0,0,0.4);">
                
                                    <!-- Логотип + заголовок -->
                                    <tr>
                                        <td style="padding:40px 40px 20px;text-align:center;">
                                            <h1 style="margin:0;font-size:36px;font-weight:700;color:#ff7b67;letter-spacing:-1px;">Trevoo</h1>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:0 40px 24px;text-align:center;">
                                            <h2 style="margin:0;font-size:24px;font-weight:600;color:#f5f2ed;">Восстановление пароля</h2>
                                        </td>
                                    </tr>
                
                                    <!-- Основной текст -->
                                    <tr>
                                        <td style="padding:0 40px 20px;text-align:center;">
                                            <p style="margin:0;font-size:16px;color:#d0c4b8;">
                                                Здравствуйте!<br>
                                                Мы получили запрос на сброс пароля для вашего аккаунта Trevoo.
                                            </p>
                                        </td>
                                    </tr>
                
                                    <!-- Блок с кнопкой -->
                                    <tr>
                                        <td style="padding:0 40px 32px;">
                                            <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                                                <tr>
                                                    <td style="background:#1f1a17;border:2px solid #3a322d;border-radius:16px;padding:28px;text-align:center;">
                
                                                        <!-- Кнопка сброса пароля -->
                                                        <a href="%s" style="display:inline-block;background:#ff7b67;color:#221d1a;font-size:18px;font-weight:600;text-decoration:none;padding:16px 32px;border-radius:10px;margin-bottom:20px;box-shadow:0 4px 0 #b34433;transition:all 0.2s ease;">
                                                             Сбросить пароль
                                                        </a>
                
                                                        <p style="margin:20px 0 0;font-size:14px;color:#b8a798;">
                                                            Нажмите на кнопку выше, чтобы задать новый пароль.<br>
                                                            Ссылка действительна 15 минут.
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                
                                    <!-- Срок действия -->
                                    <tr>
                                        <td style="padding:0 40px 24px;text-align:center;">
                                            <p style="margin:0;font-size:14px;color:#b8a798;">
                                                ⏰ Ссылка действительна <strong>15 минут</strong>
                                            </p>
                                        </td>
                                    </tr>
                
                                    <!-- Если не запрашивали -->
                                    <tr>
                                        <td style="padding:0 40px 32px;">
                                            <table width="100%%" style="background:#2a2420;border-radius:12px;border-left:4px solid #ff7b67;padding:20px;">
                                                <tr>
                                                    <td>
                                                        <p style="margin:0 0 8px;font-size:14px;font-weight:600;color:#f5f2ed;">⚠️ Если это были не вы</p>
                                                        <p style="margin:0;font-size:13px;color:#b8a798;line-height:1.5;">
                                                            Если вы не запрашивали сброс пароля — просто проигнорируйте это письмо.<br>
                                                            Ваш пароль останется без изменений.
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                
                                    <!-- Футер -->
                                    <tr>
                                        <td style="padding:32px 40px;text-align:center;font-size:13px;color:#b8a798;">
                                            <span style="color:#5fbf9b;margin-bottom:8px;display:block;">Trevoo — Всегда на созвоне</span>
                                            © 2026 Trevoo. Все права защищены.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, resetLink);
    }
}
