package com.pack1;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/DownloadServlet")
public class DownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int BUFFER_SIZE = 8192;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        List<String> allowedFiles = session == null
                ? ImageSplitterUtil.copyStringList(null)
                : ImageSplitterUtil.copyStringList(session.getAttribute(ImageSplitterUtil.SESSION_SLICE_FILES));
        String outputFolderName = session == null ? null
                : (String) session.getAttribute(ImageSplitterUtil.SESSION_OUTPUT_FOLDER);

        String fileName = request.getParameter("file");
        int fileIndex = ImageSplitterUtil.findFileIndex(allowedFiles, fileName);
        if (!ImageSplitterUtil.isSimpleFileName(fileName) || fileIndex < 0
                || !ImageSplitterUtil.isSimpleFileName(outputFolderName)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested page is not available.");
            return;
        }

        File outputRoot = ImageSplitterUtil.getOrCreateStorageDirectory(request.getServletContext(), "output");
        File file = new File(new File(outputRoot, outputFolderName), fileName);
        if (!file.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested page could not be found.");
            return;
        }

        String downloadBase = session == null ? null
                : (String) session.getAttribute(ImageSplitterUtil.SESSION_DOWNLOAD_BASE);
        String downloadName = ImageSplitterUtil.buildPageDownloadName(downloadBase, fileIndex + 1, allowedFiles.size());

        String mimeType = request.getServletContext().getMimeType(file.getName());
        response.setContentType(mimeType != null ? mimeType : "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + downloadName + "\"");
        response.setContentLengthLong(file.length());

        try (InputStream in = new BufferedInputStream(new FileInputStream(file));
                OutputStream out = response.getOutputStream()) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
