package com.pack1;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/SliceServlet")
public class SliceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        SliceSettings sessionSettings = SliceSettings.fromSessionOrDefault(session);
        SliceSettings settings = SliceSettings.fromRequest(req, sessionSettings);
        settings.applyToRequest(req);

        List<String> validationErrors = settings.validate();
        if (!validationErrors.isEmpty()) {
            forwardWithError(req, res, validationErrors.get(0));
            return;
        }

        String imageName = req.getParameter("imageName");
        if (!ImageSplitterUtil.isSimpleFileName(imageName)) {
            forwardWithError(req, res, "The selected image could not be processed.");
            return;
        }

        String storedImageName = (String) session.getAttribute(ImageSplitterUtil.ATTR_UPLOADED_IMAGE_NAME);
        if (storedImageName == null || !storedImageName.equals(imageName)) {
            forwardWithError(req, res, "Upload an image again before splitting.");
            return;
        }

        File uploadDir = ImageSplitterUtil.getOrCreateStorageDirectory(req.getServletContext(), "uploads");
        File originalFile = new File(uploadDir, imageName);
        if (!originalFile.isFile()) {
            forwardWithError(req, res, "The uploaded image could not be found on the server.");
            return;
        }

        BufferedImage image = ImageIO.read(originalFile);
        if (image == null) {
            forwardWithError(req, res, "The uploaded file is not a readable image.");
            return;
        }

        SliceLayout layout;
        try {
            layout = SliceLayout.fromImage(image.getWidth(), image.getHeight(), settings);
        } catch (IllegalArgumentException ex) {
            forwardWithError(req, res, ex.getMessage());
            return;
        }

        String originalFileName = (String) session.getAttribute(ImageSplitterUtil.ATTR_UPLOADED_ORIGINAL_NAME);
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            originalFileName = imageName;
        }

        File outputRoot = ImageSplitterUtil.getOrCreateStorageDirectory(req.getServletContext(), "output");
        String outputFolderName = ImageSplitterUtil.buildJobFolderName();
        File outputFolder = new File(outputRoot, outputFolderName);
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            forwardWithError(req, res, "The output folder could not be created.");
            return;
        }

        List<String> sliceFiles = new ArrayList<String>();
        for (int pageIndex = 0; pageIndex < layout.getTotalPages(); pageIndex++) {
            String sliceName = ImageSplitterUtil.buildPageStorageName(pageIndex + 1, layout.getTotalPages());
            File outputFile = new File(outputFolder, sliceName);
            BufferedImage pageImage = layout.renderPageImage(image, pageIndex);
            ImageIO.write(pageImage, "png", outputFile);
            sliceFiles.add(sliceName);
        }

        settings.storeInSession(session);

        String downloadBase = ImageSplitterUtil.buildDownloadBaseName(originalFileName);
        session.setAttribute(ImageSplitterUtil.SESSION_SLICE_FILES, new ArrayList<String>(sliceFiles));
        session.setAttribute(ImageSplitterUtil.SESSION_OUTPUT_FOLDER, outputFolderName);
        session.setAttribute(ImageSplitterUtil.SESSION_ZIP_NAME, ImageSplitterUtil.buildZipFileName(originalFileName));
        session.setAttribute(ImageSplitterUtil.SESSION_PDF_NAME, ImageSplitterUtil.buildPdfFileName(originalFileName));
        session.setAttribute(ImageSplitterUtil.SESSION_DOWNLOAD_BASE, downloadBase);

        req.setAttribute(ImageSplitterUtil.ATTR_SLICE_FILES, sliceFiles);
        req.setAttribute(ImageSplitterUtil.ATTR_SLICED, Boolean.TRUE);
        req.setAttribute(ImageSplitterUtil.ATTR_OUTPUT_FOLDER, outputFolderName);
        ImageSplitterUtil.populateImageAttributes(req, imageName, originalFileName, image.getWidth(),
                image.getHeight(), layout.getTotalPages());
        ImageSplitterUtil.storeImageAttributes(session, imageName, originalFileName, image.getWidth(),
                image.getHeight(), layout.getTotalPages());

        req.getRequestDispatcher("index.jsp").forward(req, res);
    }

    private void forwardWithError(HttpServletRequest req, HttpServletResponse res, String message)
            throws ServletException, IOException {

        req.setAttribute(ImageSplitterUtil.ATTR_ERROR_MESSAGE, message);
        if (req.getAttribute(SliceSettings.ATTR_SETTINGS) == null) {
            SliceSettings.fromSessionOrDefault(req.getSession()).applyToRequest(req);
        }
        ImageSplitterUtil.copyImageAttributesFromSession(req);
        ImageSplitterUtil.copyCurrentSliceStateFromSession(req);
        req.getRequestDispatcher("index.jsp").forward(req, res);
    }
}
