package com.backend.crmInmobiliario.service.impl.imgAPdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfService {

    public byte[] convertirImagenesAPdf(List<MultipartFile> imagenes) throws Exception {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        for (MultipartFile imgFile : imagenes) {

            String contentType = imgFile.getContentType();
            if (contentType == null || !contentType.startsWith("image")) {
                throw new IllegalArgumentException("Solo se aceptan imágenes");
            }

            Image img = Image.getInstance(imgFile.getBytes());
            img.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
            img.setAlignment(Element.ALIGN_CENTER);

            document.add(img);
            document.newPage();
        }

        document.close();
        return baos.toByteArray();
    }
}