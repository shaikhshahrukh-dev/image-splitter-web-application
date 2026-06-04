package com.pack1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public final class ImageSplitterUtil {

    public static final String ATTR_UPLOADED_IMAGE_NAME = "uploadedImageName";
    public static final String ATTR_UPLOADED_ORIGINAL_NAME = "uploadedOriginalName";
    public static final String ATTR_WIDTH = "width";
    public static final String ATTR_HEIGHT = "height";
    public static final String ATTR_PAGES = "pages";
    public static final String ATTR_SLICED = "sliced";
    public static final String ATTR_SLICE_FILES = "sliceFiles";
    public static final String ATTR_ERROR_MESSAGE = "errorMessage";
    public static final String ATTR_OUTPUT_FOLDER = "outputFolder";

    public static final String SESSION_SLICE_FILES = "currentSliceFiles";
    public static final String SESSION_OUTPUT_FOLDER = "currentOutputFolder";
    public static final String SESSION_ZIP_NAME = "currentZipName";
    public static final String SESSION_PDF_NAME = "currentPdfName";
    public static final String SESSION_DOWNLOAD_BASE = "currentDownloadBase";

    private ImageSplitterUtil() {
    }

    public static File getOrCreateStorageDirectory(ServletContext context, String directoryName)
            throws IOException {

        String rootPath = context.getRealPath("/");
        if (rootPath == null) {
            throw new IOException("Application storage path is not available on this server.");
        }

        File directory = new File(rootPath, directoryName);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create storage directory: " + directoryName);
        }

        return directory;
    }

    public static String extractClientFileName(String submittedFileName) {
        if (submittedFileName == null) {
            return "";
        }

        String normalized = submittedFileName.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        return lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
    }

    public static String buildStoredUploadFileName(String submittedFileName) {
        String clientFileName = extractClientFileName(submittedFileName);
        String baseName = sanitizeBaseName(removeExtension(clientFileName));
        if (baseName.isEmpty()) {
            baseName = "image";
        }

        String uniqueToken = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return baseName + "_" + uniqueToken + ".png";
    }

    public static String buildJobFolderName() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String buildPageStorageName(int index, int totalPages) {
        int digits = Math.max(2, String.valueOf(totalPages).length());
        return String.format("page_%0" + digits + "d.png", Integer.valueOf(index));
    }

    public static String buildPageDownloadName(String downloadBase, int index, int totalPages) {
        String base = sanitizeBaseName(downloadBase);
        if (base.isEmpty()) {
            base = "image";
        }

        int digits = Math.max(2, String.valueOf(totalPages).length());
        return String.format("%s_page_%0" + digits + "d.png", base, Integer.valueOf(index));
    }

    public static String removeExtension(String fileName) {
        if (fileName == null) {
            return "";
        }

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot <= 0) {
            return fileName;
        }

        return fileName.substring(0, lastDot);
    }

    public static String sanitizeBaseName(String value) {
        if (value == null) {
            return "";
        }

        String sanitized = value.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
        sanitized = sanitized.replaceAll("_+", "_");
        return sanitized.replaceAll("^[_\\.]+|[_\\.]+$", "");
    }

    public static String buildDownloadBaseName(String originalFileName) {
        String baseName = sanitizeBaseName(removeExtension(extractClientFileName(originalFileName)));
        return baseName.isEmpty() ? "image" : baseName;
    }

    public static void populateImageAttributes(HttpServletRequest request, String storedImageName,
            String originalFileName, int width, int height, int pages) {

        request.setAttribute(ATTR_UPLOADED_IMAGE_NAME, storedImageName);
        request.setAttribute(ATTR_UPLOADED_ORIGINAL_NAME, originalFileName);
        request.setAttribute(ATTR_WIDTH, Integer.valueOf(width));
        request.setAttribute(ATTR_HEIGHT, Integer.valueOf(height));
        request.setAttribute(ATTR_PAGES, Integer.valueOf(pages));
    }

    public static void storeImageAttributes(HttpSession session, String storedImageName,
            String originalFileName, int width, int height, int pages) {

        session.setAttribute(ATTR_UPLOADED_IMAGE_NAME, storedImageName);
        session.setAttribute(ATTR_UPLOADED_ORIGINAL_NAME, originalFileName);
        session.setAttribute(ATTR_WIDTH, Integer.valueOf(width));
        session.setAttribute(ATTR_HEIGHT, Integer.valueOf(height));
        session.setAttribute(ATTR_PAGES, Integer.valueOf(pages));
    }

    public static void copyImageAttributesFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        copySessionAttribute(session, request, ATTR_UPLOADED_IMAGE_NAME);
        copySessionAttribute(session, request, ATTR_UPLOADED_ORIGINAL_NAME);
        copySessionAttribute(session, request, ATTR_WIDTH);
        copySessionAttribute(session, request, ATTR_HEIGHT);
        copySessionAttribute(session, request, ATTR_PAGES);
    }

    public static void copyCurrentSliceStateFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        List<String> sliceFiles = copyStringList(session.getAttribute(SESSION_SLICE_FILES));
        if (!sliceFiles.isEmpty()) {
            request.setAttribute(ATTR_SLICE_FILES, sliceFiles);
            request.setAttribute(ATTR_SLICED, Boolean.TRUE);
        }

        Object outputFolder = session.getAttribute(SESSION_OUTPUT_FOLDER);
        if (outputFolder instanceof String) {
            request.setAttribute(ATTR_OUTPUT_FOLDER, outputFolder);
        }
    }

    private static void copySessionAttribute(HttpSession session, HttpServletRequest request,
            String attributeName) {

        Object value = session.getAttribute(attributeName);
        if (value != null) {
            request.setAttribute(attributeName, value);
        }
    }

    public static void clearCurrentSliceState(HttpSession session) {
        session.removeAttribute(SESSION_SLICE_FILES);
        session.removeAttribute(SESSION_OUTPUT_FOLDER);
        session.removeAttribute(SESSION_ZIP_NAME);
        session.removeAttribute(SESSION_PDF_NAME);
        session.removeAttribute(SESSION_DOWNLOAD_BASE);
    }

    public static List<String> copyStringList(Object value) {
        List<String> result = new ArrayList<String>();
        if (!(value instanceof Collection<?>)) {
            return result;
        }

        for (Object item : (Collection<?>) value) {
            if (item instanceof String) {
                result.add((String) item);
            }
        }

        return result;
    }

    public static int findFileIndex(List<String> files, String fileName) {
        if (files == null || fileName == null) {
            return -1;
        }

        for (int i = 0; i < files.size(); i++) {
            if (fileName.equals(files.get(i))) {
                return i;
            }
        }

        return -1;
    }

    public static String buildZipFileName(String originalFileName) {
        return buildDownloadBaseName(originalFileName) + "_pages.zip";
    }

    public static String buildPdfFileName(String originalFileName) {
        return buildDownloadBaseName(originalFileName) + "_pages.pdf";
    }

    public static boolean isSimpleFileName(String fileName) {
        return fileName != null && fileName.matches("[A-Za-z0-9._-]+");
    }

    public static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder(value.length());
        for (char ch : value.toCharArray()) {
            switch (ch) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#39;");
                    break;
                default:
                    escaped.append(ch);
                    break;
            }
        }

        return escaped.toString();
    }

    public static String formatOneDecimal(double value) {
        return String.format(java.util.Locale.US, "%.1f", Double.valueOf(value));
    }
}
