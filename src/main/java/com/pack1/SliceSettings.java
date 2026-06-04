package com.pack1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SliceSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ATTR_SETTINGS = "sliceSettings";
    public static final String SESSION_SETTINGS = "sliceSettings";
    public static final String ATTR_PAGE_PRESET = "settingPagePreset";
    public static final String ATTR_ORIENTATION = "settingOrientation";
    public static final String ATTR_CUSTOM_WIDTH_MM = "settingCustomWidthMm";
    public static final String ATTR_CUSTOM_HEIGHT_MM = "settingCustomHeightMm";
    public static final String ATTR_MARGIN_MM = "settingMarginMm";
    public static final String ATTR_OVERLAP_MM = "settingOverlapMm";
    public static final String ATTR_PAGE_LABEL = "settingPageLabel";
    public static final String ATTR_PAGE_WIDTH_MM = "settingPageWidthMm";
    public static final String ATTR_PAGE_HEIGHT_MM = "settingPageHeightMm";
    public static final String ATTR_PRINTABLE_WIDTH_MM = "settingPrintableWidthMm";
    public static final String ATTR_PRINTABLE_HEIGHT_MM = "settingPrintableHeightMm";

    public static final String PRESET_A4 = "A4";
    public static final String PRESET_LETTER = "LETTER";
    public static final String PRESET_CUSTOM = "CUSTOM";

    public static final String ORIENTATION_PORTRAIT = "PORTRAIT";
    public static final String ORIENTATION_LANDSCAPE = "LANDSCAPE";

    private static final double A4_WIDTH_MM = 210.0;
    private static final double A4_HEIGHT_MM = 297.0;
    private static final double LETTER_WIDTH_MM = 215.9;
    private static final double LETTER_HEIGHT_MM = 279.4;

    private static final double DEFAULT_CUSTOM_WIDTH_MM = 210.0;
    private static final double DEFAULT_CUSTOM_HEIGHT_MM = 297.0;
    private static final double DEFAULT_MARGIN_MM = 0.0;
    private static final double DEFAULT_OVERLAP_MM = 0.0;

    private String pagePreset;
    private String orientation;
    private double customWidthMm;
    private double customHeightMm;
    private double marginMm;
    private double overlapMm;

    public SliceSettings() {
        this.pagePreset = PRESET_A4;
        this.orientation = ORIENTATION_PORTRAIT;
        this.customWidthMm = DEFAULT_CUSTOM_WIDTH_MM;
        this.customHeightMm = DEFAULT_CUSTOM_HEIGHT_MM;
        this.marginMm = DEFAULT_MARGIN_MM;
        this.overlapMm = DEFAULT_OVERLAP_MM;
    }

    public static SliceSettings defaultSettings() {
        return new SliceSettings();
    }

    public static SliceSettings fromSessionOrDefault(HttpSession session) {
        if (session != null) {
            Object value = session.getAttribute(SESSION_SETTINGS);
            if (value instanceof SliceSettings) {
                return ((SliceSettings) value).copy();
            }
        }

        return defaultSettings();
    }

    public static SliceSettings fromRequest(HttpServletRequest request, SliceSettings fallback) {
        SliceSettings settings = fallback == null ? defaultSettings() : fallback.copy();

        String preset = request.getParameter("pagePreset");
        if (preset != null) {
            settings.setPagePreset(preset);
        }

        String orientation = request.getParameter("orientation");
        if (orientation != null) {
            settings.setOrientation(orientation);
        }

        settings.setCustomWidthMm(parseDouble(request.getParameter("customWidthMm"), settings.getCustomWidthMm()));
        settings.setCustomHeightMm(parseDouble(request.getParameter("customHeightMm"), settings.getCustomHeightMm()));
        settings.setMarginMm(parseDouble(request.getParameter("marginMm"), settings.getMarginMm()));
        settings.setOverlapMm(parseDouble(request.getParameter("overlapMm"), settings.getOverlapMm()));
        settings.normalize();
        return settings;
    }

    private static double parseDouble(String value, double defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public SliceSettings copy() {
        SliceSettings copy = new SliceSettings();
        copy.pagePreset = this.pagePreset;
        copy.orientation = this.orientation;
        copy.customWidthMm = this.customWidthMm;
        copy.customHeightMm = this.customHeightMm;
        copy.marginMm = this.marginMm;
        copy.overlapMm = this.overlapMm;
        return copy;
    }

    public void normalize() {
        pagePreset = normalizePreset(pagePreset);
        orientation = normalizeOrientation(orientation);
        customWidthMm = safeNumber(customWidthMm, DEFAULT_CUSTOM_WIDTH_MM);
        customHeightMm = safeNumber(customHeightMm, DEFAULT_CUSTOM_HEIGHT_MM);
        marginMm = safeNumber(marginMm, DEFAULT_MARGIN_MM);
        overlapMm = safeNumber(overlapMm, DEFAULT_OVERLAP_MM);
    }

    private static String normalizePreset(String value) {
        String normalized = value == null ? PRESET_A4 : value.trim().toUpperCase(Locale.ENGLISH);
        if (PRESET_A4.equals(normalized) || PRESET_LETTER.equals(normalized) || PRESET_CUSTOM.equals(normalized)) {
            return normalized;
        }

        return PRESET_A4;
    }

    private static String normalizeOrientation(String value) {
        String normalized = value == null ? ORIENTATION_PORTRAIT : value.trim().toUpperCase(Locale.ENGLISH);
        if (ORIENTATION_LANDSCAPE.equals(normalized)) {
            return ORIENTATION_LANDSCAPE;
        }

        return ORIENTATION_PORTRAIT;
    }

    private static double safeNumber(double value, double defaultValue) {
        return Double.isNaN(value) || Double.isInfinite(value) ? defaultValue : value;
    }

    public List<String> validate() {
        normalize();

        List<String> errors = new ArrayList<String>();

        double pageWidth = getPageWidthMm();
        double pageHeight = getPageHeightMm();
        if (pageWidth <= 0.0 || pageHeight <= 0.0) {
            errors.add("Choose a valid page size.");
            return errors;
        }

        if (marginMm < 0.0) {
            errors.add("Margin cannot be negative.");
        }

        if (overlapMm < 0.0) {
            errors.add("Overlap cannot be negative.");
        }

        if (PRESET_CUSTOM.equals(pagePreset)) {
            if (customWidthMm <= 10.0 || customHeightMm <= 10.0) {
                errors.add("Custom page width and height must be greater than 10 mm.");
            }
        }

        if (getPrintableWidthMm() <= 0.0 || getPrintableHeightMm() <= 0.0) {
            errors.add("Margin is too large for the selected page size.");
        } else if (overlapMm >= getPrintableHeightMm()) {
            errors.add("Overlap must be smaller than the printable page height.");
        }

        return errors;
    }

    public void applyToRequest(HttpServletRequest request) {
        SliceSettings copy = copy();
        request.setAttribute(ATTR_SETTINGS, copy);
        request.setAttribute(ATTR_PAGE_PRESET, copy.getPagePreset());
        request.setAttribute(ATTR_ORIENTATION, copy.getOrientation());
        request.setAttribute(ATTR_CUSTOM_WIDTH_MM, Double.valueOf(copy.getCustomWidthMm()));
        request.setAttribute(ATTR_CUSTOM_HEIGHT_MM, Double.valueOf(copy.getCustomHeightMm()));
        request.setAttribute(ATTR_MARGIN_MM, Double.valueOf(copy.getMarginMm()));
        request.setAttribute(ATTR_OVERLAP_MM, Double.valueOf(copy.getOverlapMm()));
        request.setAttribute(ATTR_PAGE_LABEL, copy.getPageLabel());
        request.setAttribute(ATTR_PAGE_WIDTH_MM, Double.valueOf(copy.getPageWidthMm()));
        request.setAttribute(ATTR_PAGE_HEIGHT_MM, Double.valueOf(copy.getPageHeightMm()));
        request.setAttribute(ATTR_PRINTABLE_WIDTH_MM, Double.valueOf(copy.getPrintableWidthMm()));
        request.setAttribute(ATTR_PRINTABLE_HEIGHT_MM, Double.valueOf(copy.getPrintableHeightMm()));
    }

    public void storeInSession(HttpSession session) {
        if (session != null) {
            SliceSettings copy = copy();
            session.setAttribute(SESSION_SETTINGS, copy);
            session.setAttribute(ATTR_PAGE_PRESET, copy.getPagePreset());
            session.setAttribute(ATTR_ORIENTATION, copy.getOrientation());
            session.setAttribute(ATTR_CUSTOM_WIDTH_MM, Double.valueOf(copy.getCustomWidthMm()));
            session.setAttribute(ATTR_CUSTOM_HEIGHT_MM, Double.valueOf(copy.getCustomHeightMm()));
            session.setAttribute(ATTR_MARGIN_MM, Double.valueOf(copy.getMarginMm()));
            session.setAttribute(ATTR_OVERLAP_MM, Double.valueOf(copy.getOverlapMm()));
            session.setAttribute(ATTR_PAGE_LABEL, copy.getPageLabel());
            session.setAttribute(ATTR_PAGE_WIDTH_MM, Double.valueOf(copy.getPageWidthMm()));
            session.setAttribute(ATTR_PAGE_HEIGHT_MM, Double.valueOf(copy.getPageHeightMm()));
            session.setAttribute(ATTR_PRINTABLE_WIDTH_MM, Double.valueOf(copy.getPrintableWidthMm()));
            session.setAttribute(ATTR_PRINTABLE_HEIGHT_MM, Double.valueOf(copy.getPrintableHeightMm()));
        }
    }

    public double getPageWidthMm() {
        double width = getBaseWidthMm();
        double height = getBaseHeightMm();
        return ORIENTATION_LANDSCAPE.equals(orientation) ? height : width;
    }

    public double getPageHeightMm() {
        double width = getBaseWidthMm();
        double height = getBaseHeightMm();
        return ORIENTATION_LANDSCAPE.equals(orientation) ? width : height;
    }

    public double getPrintableWidthMm() {
        return getPageWidthMm() - (marginMm * 2.0);
    }

    public double getPrintableHeightMm() {
        return getPageHeightMm() - (marginMm * 2.0);
    }

    public String getPageLabel() {
        String presetLabel = PRESET_LETTER.equals(pagePreset) ? "Letter"
                : PRESET_CUSTOM.equals(pagePreset) ? "Custom" : "A4";
        String orientationLabel = ORIENTATION_LANDSCAPE.equals(orientation) ? "Landscape" : "Portrait";
        return presetLabel + " " + orientationLabel;
    }

    private double getBaseWidthMm() {
        if (PRESET_CUSTOM.equals(pagePreset)) {
            return customWidthMm;
        }

        if (PRESET_LETTER.equals(pagePreset)) {
            return LETTER_WIDTH_MM;
        }

        return A4_WIDTH_MM;
    }

    private double getBaseHeightMm() {
        if (PRESET_CUSTOM.equals(pagePreset)) {
            return customHeightMm;
        }

        if (PRESET_LETTER.equals(pagePreset)) {
            return LETTER_HEIGHT_MM;
        }

        return A4_HEIGHT_MM;
    }

    public String getPagePreset() {
        return pagePreset;
    }

    public void setPagePreset(String pagePreset) {
        this.pagePreset = pagePreset;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public double getCustomWidthMm() {
        return customWidthMm;
    }

    public void setCustomWidthMm(double customWidthMm) {
        this.customWidthMm = customWidthMm;
    }

    public double getCustomHeightMm() {
        return customHeightMm;
    }

    public void setCustomHeightMm(double customHeightMm) {
        this.customHeightMm = customHeightMm;
    }

    public double getMarginMm() {
        return marginMm;
    }

    public void setMarginMm(double marginMm) {
        this.marginMm = marginMm;
    }

    public double getOverlapMm() {
        return overlapMm;
    }

    public void setOverlapMm(double overlapMm) {
        this.overlapMm = overlapMm;
    }
}
