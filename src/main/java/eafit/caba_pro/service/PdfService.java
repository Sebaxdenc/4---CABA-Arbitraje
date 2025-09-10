package eafit.caba_pro.service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import eafit.caba_pro.model.Liquidacion;

@Service
public class PdfService {
    SpringTemplateEngine templateEngine;

    public PdfService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generarPdfDesdeLiquidacion(Liquidacion liquidacion) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 1. Renderizar HTML con Thymeleaf
            Context context = new Context();
            context.setVariable("liquidacion", liquidacion);
            String html = templateEngine.process("pdf/liquidacion", context);

            // 2. Convertir HTML a PDF
            try (OutputStream os = baos) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, null);
                builder.toStream(os);
                builder.run();
            }

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
    }
}
