package com.pack1;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@WebServlet("/ImageInfoServlet")
@MultipartConfig
public class ImageInfoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        SliceSettings settings = SliceSettings.fromSessionOrDefault(session);
        settings.storeInSession(session);
        settings.applyToRequest(req);

        Part filePart = req.getPart("imageFile");
        if (filePart == null || filePart.getSize() == 0) {
            forwardWithError(req, res, "Choose an image before starting.");
            return;
        }

        String originalFileName = ImageSplitterUtil.extractClientFileName(filePart.getSubmittedFileName());
        if (originalFileName.isEmpty()) {
            forwardWithError(req, res, "The uploaded file name could not be read.");
            return;
        }

        File uploadDir = ImageSplitterUtil.getOrCreateStorageDirectory(req.getServletContext(), "uploads");
        String storedFileName = ImageSplitterUtil.buildStoredUploadFileName(originalFileName);
        File savedFile = new File(uploadDir, storedFileName);

        BufferedImage image;
        try (InputStream inputStream = filePart.getInputStream()) {
            image = ImageIO.read(inputStream);
        }

        if (image == null) {
            forwardWithError(req, res, "That file is not a supported image.");
            return;
        }

        if (!ImageIO.write(image, "png", savedFile)) {
            forwardWithError(req, res, "The uploaded image could not be stored.");
            return;
        }

        SliceLayout layout;
        try {
            layout = SliceLayout.fromImage(image.getWidth(), image.getHeight(), settings);
        } catch (IllegalArgumentException ex) {
            forwardWithError(req, res, ex.getMessage());
            return;
        }

        ImageSplitterUtil.clearCurrentSliceState(session);
        ImageSplitterUtil.populateImageAttributes(req, storedFileName, originalFileName, image.getWidth(),
                image.getHeight(), layout.getTotalPages());
        ImageSplitterUtil.storeImageAttributes(session, storedFileName, originalFileName, image.getWidth(),
                image.getHeight(), layout.getTotalPages());

        req.getRequestDispatcher("index.jsp").forward(req, res);
    }

    private void forwardWithError(HttpServletRequest req, HttpServletResponse res, String message)
            throws ServletException, IOException {

        req.setAttribute(ImageSplitterUtil.ATTR_ERROR_MESSAGE, message);
        SliceSettings.fromSessionOrDefault(req.getSession()).applyToRequest(req);
        ImageSplitterUtil.copyImageAttributesFromSession(req);
        ImageSplitterUtil.copyCurrentSliceStateFromSession(req);
        req.getRequestDispatcher("index.jsp").forward(req, res);
    }
}
