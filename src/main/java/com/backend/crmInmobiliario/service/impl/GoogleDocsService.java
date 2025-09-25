package com.backend.crmInmobiliario.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class GoogleDocsService {

    // Recuperá refresh_token, clientId y clientSecret desde tu DB/config
    private GoogleCredential buildCredential(String accessToken, String refreshToken,
                                             String clientId, String clientSecret) throws Exception {
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets(clientId, clientSecret)
                .build();
        credential.setAccessToken(accessToken);
        credential.setRefreshToken(refreshToken);
        return credential;
    }

    public Map<String, String> createDocFromHtml(Long userId, String html,
                                                 String title,
                                                 String accessToken,
                                                 String refreshToken,
                                                 String clientId,
                                                 String clientSecret) throws Exception {

        var credential = buildCredential(accessToken, refreshToken, clientId, clientSecret);

        Drive drive = new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("crmInmobiliario").build();

        // 1) HTML temporal
        Path tmp = Files.createTempFile("contrato-", ".html");
        Files.writeString(tmp, html, StandardCharsets.UTF_8);

        // 2) Metadata pidiendo "Google Doc" como formato final
        File meta = new File();
        meta.setName(title);
        meta.setMimeType("application/vnd.google-apps.document");

        // 3) Subida con conversión (de text/html -> Google Docs)
        FileContent media = new FileContent("text/html", tmp.toFile());
        File file = drive.files().create(meta, media)
                .setFields("id, webViewLink, iconLink")
                .execute();

        Files.deleteIfExists(tmp);

        return Map.of(
                "id", file.getId(),
                "webViewLink", file.getWebViewLink()
        );
    }
}