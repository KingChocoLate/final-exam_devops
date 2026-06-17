package net.orderzone.idcard.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.model.Template;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PDFExportService {

    private final PhotoStorageService photoStorageService;
    private final QRCodeService qrCodeService;
    private final BarcodeService barcodeService;

    public byte[] exportSingle(Profile profile) throws Exception {
        Document doc = new Document(PageSize.A6.rotate(), 20, 20, 20, 20);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, baos);
        doc.open();
        addCard(doc, profile);
        doc.close();
        return baos.toByteArray();
    }

    public byte[] exportBatch(List<Profile> profiles) throws Exception {
        Document doc = new Document(PageSize.A6.rotate(), 20, 20, 20, 20);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, baos);
        doc.open();
        for (int i = 0; i < profiles.size(); i++) {
            if (i > 0) doc.newPage();
            addCard(doc, profiles.get(i));
        }
        doc.close();
        return baos.toByteArray();
    }

    private void addCard(Document doc, Profile profile) throws Exception {
        Template tmpl = profile.getTemplate();
        String primaryHex = tmpl != null ? tmpl.getPrimaryColor() : "#1d4ed8";
        String orgName = tmpl != null && tmpl.getOrganizationName() != null
                ? tmpl.getOrganizationName() : "ID CARD";

        BaseColor primaryColor = hexToColor(primaryHex);

        // Header
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE);
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell hCell = new PdfPCell(new Phrase(orgName + " — "
                + profile.getType().name() + " ID", titleFont));
        hCell.setBackgroundColor(primaryColor);
        hCell.setPadding(10);
        hCell.setBorder(Rectangle.NO_BORDER);
        hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(hCell);
        doc.add(header);
        doc.add(Chunk.NEWLINE);

        // Body
        PdfPTable body = new PdfPTable(3);
        body.setWidthPercentage(100);
        body.setWidths(new float[]{2f, 3f, 2f});

        // Photo
        PdfPCell photoCell = new PdfPCell();
        photoCell.setBorder(Rectangle.NO_BORDER);
        photoCell.setPadding(5);
        if (profile.hasPhoto()) {
            try {
                byte[] photoBytes = photoStorageService.load(profile.getPhotoFileName());
                Image img = Image.getInstance(photoBytes);
                img.scaleToFit(80, 100);
                photoCell.addElement(img);
            } catch (Exception ignored) {
                photoCell.addElement(new Phrase("No Photo"));
            }
        }
        body.addCell(photoCell);

        // Info
        Font nameFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 8);
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPadding(5);
        infoCell.addElement(new Phrase(profile.getFullName(), nameFont));
        infoCell.addElement(new Phrase("ID: " + profile.getRegistrationNumber(), smallFont));
        infoCell.addElement(new Phrase("Dept: " + s(profile.getDepartment()), smallFont));
        infoCell.addElement(new Phrase("Title: " + s(profile.getTitle()), smallFont));
        infoCell.addElement(new Phrase("Email: " + s(profile.getEmail()), smallFont));
        if (profile.getExpiryDate() != null)
            infoCell.addElement(new Phrase("Expires: " + profile.getExpiryDate(), smallFont));
        body.addCell(infoCell);

        // QR
        PdfPCell qrCell = new PdfPCell();
        qrCell.setBorder(Rectangle.NO_BORDER);
        qrCell.setPadding(5);
        qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        String verifyUrl = "http://localhost:8080/profiles/verify/" + profile.getUuid();
        byte[] qrBytes = qrCodeService.generateQRCode(verifyUrl, 150, 150);
        Image qrImg = Image.getInstance(qrBytes);
        qrImg.scaleToFit(70, 70);
        qrCell.addElement(qrImg);
        body.addCell(qrCell);

        doc.add(body);

        // Barcode
        doc.add(Chunk.NEWLINE);
        byte[] barcodeBytes = barcodeService.generateBarcode(
                profile.getRegistrationNumber(), profile.getBarcodeType());
        Image barImg = Image.getInstance(barcodeBytes);
        barImg.scaleToFit(200, 35);
        barImg.setAlignment(Element.ALIGN_CENTER);
        doc.add(barImg);
    }

    private String s(String v) { return v != null ? v : "—"; }

    private BaseColor hexToColor(String hex) {
        try {
            hex = hex.replace("#", "");
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return new BaseColor(r, g, b);
        } catch (Exception e) {
            return new BaseColor(29, 78, 216);
        }
    }
}