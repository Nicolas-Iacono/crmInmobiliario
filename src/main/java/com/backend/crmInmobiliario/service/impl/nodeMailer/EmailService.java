package com.backend.crmInmobiliario.service.impl.nodeMailer;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarConfirmacionRegistro(String nombre, String email) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Confirmación de registro - Tuinmo");
            helper.setFrom("Tuinmo <no-reply@tuinmo.com>");

            String htmlContent = generarHtmlConfirmacion(nombre);
            helper.setText(htmlContent, true);

            mailSender.send(mensaje);

        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo de confirmación", e);
        }
    }

    private String generarHtmlConfirmacion(String nombre) {
        return """
        <html>
          <head>
            <style>
              body {
                font-family: 'Poppins', Arial, sans-serif;
                background-color: #f6f6f6;
                margin: 0;
                padding: 0;
              }
              .container {
                max-width: 600px;
                margin: 40px auto;
                background-color: #ffffff;
                border-radius: 16px;
                box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                overflow: hidden;
              }
              .header {
                background-color: #8D2EE6;
                color: white;
                text-align: center;
                padding: 30px 20px;
              }
              .header img {
                width: 90px;
                margin-bottom: 15px;
              }
              .content {
                padding: 30px;
                text-align: center;
                color: #333;
              }
              .content h2 {
                color: #8D2EE6;
                margin-bottom: 10px;
              }
              .footer {
                background-color: #f0f0f0;
                text-align: center;
                padding: 15px;
                font-size: 13px;
                color: #777;
              }
              .button {
                display: inline-block;
                background-color: #8D2EE6;
                color: white;
                text-decoration: none;
                padding: 12px 24px;
                border-radius: 8px;
                margin-top: 20px;
                font-weight: 600;
              }
            </style>
          </head>
          <body>
            <div class="container">
              <div class="header">
                <img src="https://tuinmo.net/logoInmo512.png" alt="Tuinmo Logo">
                <h1>¡Te damos la bienvenida!</h1>
              </div>
              <div class="content">
                <h2>Hola, %s 👋</h2>
                <p>Tu cuenta fue creada correctamente en <strong>Tuinmo</strong>.</p>
                <p>Ya podés ingresar con tu usuario y comenzar a gestionar tu inmobiliaria desde tu celular.</p>
                <a href="https://tuinmo.net/login" class="button">Ingresar a mi cuenta</a>
              </div>
              <div class="footer">
                <p>© 2025 Tuinmo. Todos los derechos reservados.</p>
              </div>
            </div>
          </body>
        </html>
        """.formatted(nombre);
    }
}
