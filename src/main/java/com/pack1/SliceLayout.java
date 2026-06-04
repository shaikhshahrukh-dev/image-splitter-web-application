package com.pack1;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class SliceLayout {

    private final SliceSettings settings;
    private final int sourceWidthPx;
    private final int sourceHeightPx;
    private final int pageWidthPx;
    private final int pageHeightPx;
    private final int contentHeightPx;
    private final int overlapPx;
    private final int stepPx;
    private final int totalPages;
    private final int marginLeftPx;
    private final int marginTopPx;

    private SliceLayout(SliceSettings settings, int sourceWidthPx, int sourceHeightPx, int pageWidthPx,
            int pageHeightPx, int contentHeightPx, int overlapPx, int stepPx, int totalPages,
            int marginLeftPx, int marginTopPx) {

        this.settings = settings;
        this.sourceWidthPx = sourceWidthPx;
        this.sourceHeightPx = sourceHeightPx;
        this.pageWidthPx = pageWidthPx;
        this.pageHeightPx = pageHeightPx;
        this.contentHeightPx = contentHeightPx;
        this.overlapPx = overlapPx;
        this.stepPx = stepPx;
        this.totalPages = totalPages;
        this.marginLeftPx = marginLeftPx;
        this.marginTopPx = marginTopPx;
    }

    public static SliceLayout fromImage(int sourceWidthPx, int sourceHeightPx, SliceSettings settings) {
        if (sourceWidthPx <= 0 || sourceHeightPx <= 0) {
            throw new IllegalArgumentException("The uploaded image must have a valid size.");
        }

        java.util.List<String> errors = settings.validate();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.get(0));
        }

        double printableWidthMm = settings.getPrintableWidthMm();
        double printableHeightMm = settings.getPrintableHeightMm();
        double scalePxPerMm = sourceWidthPx / printableWidthMm;

        int pageWidthPx = Math.max(sourceWidthPx, (int) Math.round(settings.getPageWidthMm() * scalePxPerMm));
        int pageHeightPx = Math.max(1, (int) Math.round(settings.getPageHeightMm() * scalePxPerMm));
        int contentHeightPx = Math.max(1, (int) Math.round(printableHeightMm * scalePxPerMm));
        int overlapPx = Math.max(0, (int) Math.round(settings.getOverlapMm() * scalePxPerMm));
        if (overlapPx >= contentHeightPx) {
            throw new IllegalArgumentException("Overlap must be smaller than the printable page height.");
        }

        int stepPx = Math.max(1, contentHeightPx - overlapPx);
        int totalPages = sourceHeightPx <= contentHeightPx
                ? 1
                : 1 + (int) Math.ceil((sourceHeightPx - contentHeightPx) / (double) stepPx);

        int marginLeftPx = Math.max(0, (pageWidthPx - sourceWidthPx) / 2);
        int marginTopPx = Math.max(0, (pageHeightPx - contentHeightPx) / 2);

        return new SliceLayout(settings.copy(), sourceWidthPx, sourceHeightPx, pageWidthPx, pageHeightPx,
                contentHeightPx, overlapPx, stepPx, totalPages, marginLeftPx, marginTopPx);
    }

    public BufferedImage renderPageImage(BufferedImage sourceImage, int pageIndex) {
        int startY = getStartYForPage(pageIndex);
        int sliceHeight = getSliceHeightForPage(pageIndex);
        BufferedImage slice = sourceImage.getSubimage(0, startY, sourceWidthPx, sliceHeight);

        BufferedImage page = new BufferedImage(pageWidthPx, pageHeightPx, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = page.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, pageWidthPx, pageHeightPx);

            graphics.drawImage(slice, marginLeftPx, marginTopPx, null);

            graphics.setColor(new Color(230, 238, 246));
            graphics.drawRect(Math.max(0, marginLeftPx - 1), Math.max(0, marginTopPx - 1), sourceWidthPx + 1,
                    Math.max(0, contentHeightPx) + 1);
        } finally {
            graphics.dispose();
        }

        return page;
    }

    public int getStartYForPage(int pageIndex) {
        return pageIndex * stepPx;
    }

    public int getSliceHeightForPage(int pageIndex) {
        int startY = getStartYForPage(pageIndex);
        return Math.min(contentHeightPx, sourceHeightPx - startY);
    }

    public SliceSettings getSettings() {
        return settings.copy();
    }

    public int getPageWidthPx() {
        return pageWidthPx;
    }

    public int getPageHeightPx() {
        return pageHeightPx;
    }

    public int getContentHeightPx() {
        return contentHeightPx;
    }

    public int getOverlapPx() {
        return overlapPx;
    }

    public int getStepPx() {
        return stepPx;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
