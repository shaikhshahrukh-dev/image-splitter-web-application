package com.pack1;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

@WebServlet("/PdfServlet")
public class PdfServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final double POINTS_PER_MM = 72.0 / 25.4;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        List<String> currentSliceFiles = session == null
                ? ImageSplitterUtil.copyStringList(null)
                : ImageSplitterUtil.copyStringList(session.getAttribute(ImageSplitterUtil.SESSION_SLICE_FILES));
        String outputFolderName = session == null ? null
                : (String) session.getAttribute(ImageSplitterUtil.SESSION_OUTPUT_FOLDER);

        if (currentSliceFiles.isEmpty() || !ImageSplitterUtil.isSimpleFileName(outputFolderName)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Split an image before downloading a PDF.");
            return;
        }

        File outputRoot = ImageSplitterUtil.getOrCreateStorageDirectory(request.getServletContext(), "output");
        File outputFolder = new File(outputRoot, outputFolderName);

        SliceSettings settings = SliceSettings.fromSessionOrDefault(session);
        float pageWidthPoints = (float) (settings.getPageWidthMm() * POINTS_PER_MM);
        float pageHeightPoints = (float) (settings.getPageHeightMm() * POINTS_PER_MM);
        PDRectangle pageSize = new PDRectangle(pageWidthPoints, pageHeightPoints);

        String pdfFileName = session == null ? null
                : (String) session.getAttribute(ImageSplitterUtil.SESSION_PDF_NAME);
        if (pdfFileName == null || pdfFileName.trim().isEmpty()) {
            pdfFileName = "image_pages.pdf";
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + pdfFileName + "\"");

        try (PDDocument document = new PDDocument()) {
            int pagesAdded = 0;
            for (String fileName : currentSliceFiles) {
                if (!ImageSplitterUtil.isSimpleFileName(fileName)) {
                    continue;
                }

                File file = new File(outputFolder, fileName);
                if (!file.isFile()) {
                    continue;
                }

                BufferedImage pageImage = ImageIO.read(file);
                if (pageImage == null) {
                    continue;
                }

                PDPage page = new PDPage(pageSize);
                document.addPage(page);
                pagesAdded++;

                PDImageXObject image = LosslessFactory.createFromImage(document, pageImage);
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.drawImage(image, 0, 0, pageSize.getWidth(), pageSize.getHeight());
                }
            }

            if (pagesAdded == 0) {
                response.reset();
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No generated pages are available for PDF export.");
                return;
            }

            document.save(response.getOutputStream());
        }
    }
}
