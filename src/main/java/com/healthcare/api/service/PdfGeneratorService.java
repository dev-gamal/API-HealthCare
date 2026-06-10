package com.healthcare.api.service;

import com.healthcare.api.dto.MedicalFileResponseDTO;
import com.healthcare.api.dto.AppointmentResponseDTO;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfGeneratorService {
    public ByteArrayInputStream generateMedicalFilePdf(MedicalFileResponseDTO medicalFile) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.BLUE);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);

            Paragraph title = new Paragraph("Dossier Médical - HealthCare+", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);

            document.add(new Paragraph("Informations du Patient", sectionFont));
            document.add(new Paragraph("Nom complet : " + medicalFile.getPatientCompleteName(), textFont));
            document.add(new Paragraph("ID Patient : " + medicalFile.getPatientId(), textFont));
            document.add(new Paragraph("Dossier créé le : " + medicalFile.getCreationDate(), textFont));

            Paragraph space = new Paragraph(" ");
            space.setSpacingAfter(20);
            document.add(space);

            document.add(new Paragraph("Détails Cliniques", sectionFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2); // Tableau à 2 colonnes
            table.setWidthPercentage(100);
            table.setWidths(new int[]{1, 3});

            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);

            PdfPCell hcell1 = new PdfPCell(new Phrase("Rubrique", tableHeaderFont));
            hcell1.setBackgroundColor(Color.GRAY);
            hcell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            hcell1.setPadding(8);
            table.addCell(hcell1);

            PdfPCell hcell2 = new PdfPCell(new Phrase("Description", tableHeaderFont));
            hcell2.setBackgroundColor(Color.GRAY);
            hcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            hcell2.setPadding(8);
            table.addCell(hcell2);

            table.addCell(createCell("Diagnostic", true));
            table.addCell(createCell(medicalFile.getDiagnosis() != null ? medicalFile.getDiagnosis() : "Non renseigné", false));

            table.addCell(createCell("Observations", true));
            table.addCell(createCell(medicalFile.getObservation() != null ? medicalFile.getObservation() : "Non renseigné", false));

            document.add(table);

            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du fichier PDF", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generatePatientAppointmentsPdf(List<AppointmentResponseDTO> appointments, String patientName) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLUE);
            Paragraph title = new Paragraph("Historique des Rendez-vous", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
            document.add(new Paragraph("Patient : " + patientName, subtitleFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{3, 3, 3, 2});

            String[] headers = {"Date", "Médecin", "Spécialité", "Statut"};
            for (String header : headers) {
                PdfPCell cell = createCell(header, true);
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (AppointmentResponseDTO app : appointments) {
                String dateFormatee = app.getAppointmentDate() != null ? app.getAppointmentDate().format(formatter) : "N/A";
                table.addCell(createCell(dateFormatee, false));
                table.addCell(createCell(app.getDoctorName(), false));
                table.addCell(createCell(app.getDoctorSpecialty(), false));
                table.addCell(createCell(app.getStatus().name(), false));
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du PDF des rendez-vous", e);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generateSimpleReportPdf(long totalPatients, long totalDoctors, long totalAppointments) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.RED);
            Paragraph title = new Paragraph("Rapport d'Activité - HealthCare+", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);

            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Color.BLACK);
            document.add(new Paragraph("Date d'édition : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), textFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(80);

            table.addCell(createCell("Total des Patients enregistrés", true));
            table.addCell(createCell(String.valueOf(totalPatients), false));

            table.addCell(createCell("Total des Médecins inscrits", true));
            table.addCell(createCell(String.valueOf(totalDoctors), false));

            table.addCell(createCell("Total des Rendez-vous planifiés", true));
            table.addCell(createCell(String.valueOf(totalAppointments), false));

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du rapport", e);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    private PdfPCell createCell(String content, boolean isHeader) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 12);
        if (isHeader) {
            font.setStyle(Font.BOLD);
        }
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setPadding(8);
        return cell;
    }
}