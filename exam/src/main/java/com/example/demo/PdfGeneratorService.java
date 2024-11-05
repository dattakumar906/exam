package com.example.demo;

import java.io.OutputStream;
import java.util.List;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Service
public class PdfGeneratorService {

    private final SpringTemplateEngine templateEngine;

    public PdfGeneratorService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void generatePdf(String templateName, List<ExamResult> examResults, OutputStream outputStream) throws Exception {
        Context context = new Context();
        context.setVariable("examResults", examResults);

        String htmlContent = templateEngine.process(templateName, context);
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream);
    }
}
