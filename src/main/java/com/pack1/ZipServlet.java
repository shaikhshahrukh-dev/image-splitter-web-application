package com.pack1;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/ZipServlet")
public class ZipServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int BUFFER_SIZE = 8192;

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
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Split an image before downloading a ZIP.");
            return;
        }

        File outputRoot = ImageSplitterUtil.getOrCreateStorageDirectory(request.getServletContext(), "output");
        File outputFolder = new File(outputRoot, outputFolderName);
        List<File> filesToZip = new ArrayList<File>();
        for (String fileName : currentSliceFiles) {
            if (!ImageSplitterUtil.isSimpleFileName(fileName)) {
                continue;
            }

            File file = new File(outputFolder, fileName);
            if (file.isFile()) {
                filesToZip.add(file);
            }
        }

        if (filesToZip.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No generated pages are available to zip.");
            return;
        }

        String zipFileName = session == null ? null
                : (String) session.getAttribute(ImageSplitterUtil.SESSION_ZIP_NAME);
        if (zipFileName == null || zipFileName.trim().isEmpty()) {
            zipFileName = "image_pages.zip";
        }

        String downloadBase = session == null ? null
                : (String) session.getAttribute(ImageSplitterUtil.SESSION_DOWNLOAD_BASE);

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");

        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            byte[] buffer = new byte[BUFFER_SIZE];

            for (int index = 0; index < filesToZip.size(); index++) {
                File file = filesToZip.get(index);
                String entryName = ImageSplitterUtil.buildPageDownloadName(downloadBase, index + 1, filesToZip.size());
                zipOut.putNextEntry(new ZipEntry(entryName));

                try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        zipOut.write(buffer, 0, length);
                    }
                }

                zipOut.closeEntry();
            }
        }
    }
}
