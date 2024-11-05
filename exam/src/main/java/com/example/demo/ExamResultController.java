package com.example.demo;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class ExamResultController {

    @Autowired
    private ExamResultRepository examResultRepository;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @GetMapping("/filtered-exam-results")
    public String getFilteredExamResults(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate,
            Model model) {

        List<ExamResult> filteredResults = examResultRepository.findByFilters(name, college, topic, examDate);
        model.addAttribute("examResults", filteredResults);
        model.addAttribute("name", name);
        model.addAttribute("college", college);
        model.addAttribute("topic", topic);
        model.addAttribute("examDate", examDate);
        return "examResults"; // Thymeleaf template name for displaying results
    }

    @GetMapping("/download-exam-results")
    public void downloadFilteredExamResults(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=filtered_exam_results.pdf");

        try (OutputStream out = response.getOutputStream()) {
            // Fetch data with applied filters
            List<ExamResult> filteredResults = examResultRepository.findByFilters(name, college, topic, examDate);

            // Log the number of records fetched
            System.out.println("Filtered results count: " + filteredResults.size());

            // Check if results are empty and handle appropriately
            if (filteredResults.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NO_CONTENT, "No data found for the provided filters.");
                return;
            }

            // Generate PDF with the fetched data
            pdfGeneratorService.generatePdf("examResultsPdf", filteredResults, out);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating PDF: " + e.getMessage());
        }
    }


}
