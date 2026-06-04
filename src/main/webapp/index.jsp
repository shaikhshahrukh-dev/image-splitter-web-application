<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="java.net.URLEncoder,java.util.List,com.pack1.ImageSplitterUtil"%>
<%
    String imgName = (String) request.getAttribute(ImageSplitterUtil.ATTR_UPLOADED_IMAGE_NAME);
    String originalName = (String) request.getAttribute(ImageSplitterUtil.ATTR_UPLOADED_ORIGINAL_NAME);
    Integer width = (Integer) request.getAttribute(ImageSplitterUtil.ATTR_WIDTH);
    Integer height = (Integer) request.getAttribute(ImageSplitterUtil.ATTR_HEIGHT);
    Integer pages = (Integer) request.getAttribute(ImageSplitterUtil.ATTR_PAGES);
    String errorMessage = (String) request.getAttribute(ImageSplitterUtil.ATTR_ERROR_MESSAGE);
    boolean sliced = Boolean.TRUE.equals(request.getAttribute(ImageSplitterUtil.ATTR_SLICED));
    List<String> sliceFiles = ImageSplitterUtil.copyStringList(request.getAttribute(ImageSplitterUtil.ATTR_SLICE_FILES));
    String outputFolder = (String) request.getAttribute(ImageSplitterUtil.ATTR_OUTPUT_FOLDER);

    String pagePreset = (String) request.getAttribute("settingPagePreset");
    String orientation = (String) request.getAttribute("settingOrientation");
    Number customWidthNumber = (Number) request.getAttribute("settingCustomWidthMm");
    Number customHeightNumber = (Number) request.getAttribute("settingCustomHeightMm");
    Number marginNumber = (Number) request.getAttribute("settingMarginMm");
    Number overlapNumber = (Number) request.getAttribute("settingOverlapMm");
    String pageLabel = (String) request.getAttribute("settingPageLabel");
    Number pageWidthNumber = (Number) request.getAttribute("settingPageWidthMm");
    Number pageHeightNumber = (Number) request.getAttribute("settingPageHeightMm");
    Number printableWidthNumber = (Number) request.getAttribute("settingPrintableWidthMm");
    Number printableHeightNumber = (Number) request.getAttribute("settingPrintableHeightMm");

    if (imgName == null) {
        imgName = (String) session.getAttribute(ImageSplitterUtil.ATTR_UPLOADED_IMAGE_NAME);
    }
    if (originalName == null) {
        originalName = (String) session.getAttribute(ImageSplitterUtil.ATTR_UPLOADED_ORIGINAL_NAME);
    }
    if (width == null) {
        width = (Integer) session.getAttribute(ImageSplitterUtil.ATTR_WIDTH);
    }
    if (height == null) {
        height = (Integer) session.getAttribute(ImageSplitterUtil.ATTR_HEIGHT);
    }
    if (pages == null) {
        pages = (Integer) session.getAttribute(ImageSplitterUtil.ATTR_PAGES);
    }
    if (sliceFiles.isEmpty()) {
        sliceFiles = ImageSplitterUtil.copyStringList(session.getAttribute(ImageSplitterUtil.SESSION_SLICE_FILES));
    }
    if (outputFolder == null) {
        outputFolder = (String) session.getAttribute(ImageSplitterUtil.SESSION_OUTPUT_FOLDER);
    }
    if (!sliced && !sliceFiles.isEmpty()) {
        sliced = true;
    }

    if (pagePreset == null) {
        pagePreset = (String) session.getAttribute("settingPagePreset");
    }
    if (orientation == null) {
        orientation = (String) session.getAttribute("settingOrientation");
    }
    if (customWidthNumber == null) {
        customWidthNumber = (Number) session.getAttribute("settingCustomWidthMm");
    }
    if (customHeightNumber == null) {
        customHeightNumber = (Number) session.getAttribute("settingCustomHeightMm");
    }
    if (marginNumber == null) {
        marginNumber = (Number) session.getAttribute("settingMarginMm");
    }
    if (overlapNumber == null) {
        overlapNumber = (Number) session.getAttribute("settingOverlapMm");
    }
    if (pageLabel == null) {
        pageLabel = (String) session.getAttribute("settingPageLabel");
    }
    if (pageWidthNumber == null) {
        pageWidthNumber = (Number) session.getAttribute("settingPageWidthMm");
    }
    if (pageHeightNumber == null) {
        pageHeightNumber = (Number) session.getAttribute("settingPageHeightMm");
    }
    if (printableWidthNumber == null) {
        printableWidthNumber = (Number) session.getAttribute("settingPrintableWidthMm");
    }
    if (printableHeightNumber == null) {
        printableHeightNumber = (Number) session.getAttribute("settingPrintableHeightMm");
    }

    if (pagePreset == null || pagePreset.trim().isEmpty()) {
        pagePreset = "A4";
    }
    if (orientation == null || orientation.trim().isEmpty()) {
        orientation = "PORTRAIT";
    }

    double customWidthMm = customWidthNumber != null ? customWidthNumber.doubleValue() : 210.0;
    double customHeightMm = customHeightNumber != null ? customHeightNumber.doubleValue() : 297.0;
    double marginMm = marginNumber != null ? marginNumber.doubleValue() : 0.0;
    double overlapMm = overlapNumber != null ? overlapNumber.doubleValue() : 0.0;
    double pageWidthMm = pageWidthNumber != null ? pageWidthNumber.doubleValue() : 210.0;
    double pageHeightMm = pageHeightNumber != null ? pageHeightNumber.doubleValue() : 297.0;
    double printableWidthMm = printableWidthNumber != null ? printableWidthNumber.doubleValue() : 210.0;
    double printableHeightMm = printableHeightNumber != null ? printableHeightNumber.doubleValue() : 297.0;

    if (pageLabel == null || pageLabel.trim().isEmpty()) {
        String presetLabel = "LETTER".equals(pagePreset) ? "Letter" : ("CUSTOM".equals(pagePreset) ? "Custom" : "A4");
        String orientationLabel = "LANDSCAPE".equals(orientation) ? "Landscape" : "Portrait";
        pageLabel = presetLabel + " " + orientationLabel;
    }

    boolean hasImage = imgName != null && !imgName.trim().isEmpty();
    boolean hasOutput = sliced && !sliceFiles.isEmpty() && outputFolder != null && !outputFolder.trim().isEmpty();
    String safeOriginalName = ImageSplitterUtil.escapeHtml(originalName != null ? originalName : "No image selected");
    String safePageLabel = ImageSplitterUtil.escapeHtml(pageLabel);
    String pageWidthLabel = ImageSplitterUtil.formatOneDecimal(pageWidthMm);
    String pageHeightLabel = ImageSplitterUtil.formatOneDecimal(pageHeightMm);
    String printableWidthLabel = ImageSplitterUtil.formatOneDecimal(printableWidthMm);
    String printableHeightLabel = ImageSplitterUtil.formatOneDecimal(printableHeightMm);
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Image Splitter</title>
<style>
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@600;700;800&family=Rajdhani:wght@500;600;700&display=swap');

:root {
    --bg-deep: #04111f;
    --panel: rgba(7, 20, 33, 0.82);
    --line: rgba(134, 223, 255, 0.18);
    --accent: #7ee7ff;
    --accent-strong: #2cd8ff;
    --warn: #ff7a59;
    --text-main: #eef8ff;
    --text-soft: #9fc7dd;
    --text-muted: #6f93a7;
    --success: #a9ffcc;
    --shadow: 0 30px 80px rgba(0, 0, 0, 0.38);
    --radius: 28px;
}

* {
    box-sizing: border-box;
}

html,
body {
    margin: 0;
    min-height: 100%;
}

body {
    font-family: 'Rajdhani', sans-serif;
    color: var(--text-main);
    background:
        radial-gradient(circle at top left, rgba(44, 216, 255, 0.22), transparent 28%),
        radial-gradient(circle at 85% 15%, rgba(255, 112, 72, 0.18), transparent 22%),
        linear-gradient(135deg, rgba(3, 11, 20, 0.9), rgba(5, 23, 41, 0.86)),
        url('images/5509798.jpg') center/cover fixed;
    position: relative;
    overflow-x: hidden;
}

body::before,
body::after {
    content: "";
    position: fixed;
    inset: 0;
    pointer-events: none;
}

body::before {
    background:
        linear-gradient(90deg, transparent 0, transparent calc(100% - 1px), rgba(126, 231, 255, 0.05) calc(100% - 1px)),
        linear-gradient(transparent 0, transparent calc(100% - 1px), rgba(126, 231, 255, 0.05) calc(100% - 1px));
    background-size: 120px 120px;
    mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.16), rgba(0, 0, 0, 0.55));
}

body::after {
    background: linear-gradient(180deg, rgba(4, 17, 31, 0), rgba(4, 17, 31, 0.78));
}

.page-shell {
    position: relative;
    z-index: 1;
    width: min(1440px, calc(100% - 40px));
    margin: 0 auto;
    padding: 40px 0 54px;
}

.hero {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    gap: 28px;
    margin-bottom: 28px;
}

.hero-copy {
    max-width: 580px;
}

.eyebrow {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    padding: 10px 16px;
    border-radius: 999px;
    border: 1px solid rgba(126, 231, 255, 0.2);
    background: rgba(7, 18, 31, 0.5);
    color: var(--accent);
    font-size: 15px;
    font-weight: 700;
    letter-spacing: 0.24em;
    text-transform: uppercase;
}

.eyebrow::before {
    content: "";
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: var(--accent-strong);
    box-shadow: 0 0 18px rgba(44, 216, 255, 0.8);
}

h1 {
    margin: 18px 0 10px;
    font-family: 'Orbitron', sans-serif;
    font-size: clamp(2.5rem, 4vw, 4.8rem);
    line-height: 1;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    text-shadow: 0 0 25px rgba(44, 216, 255, 0.25);
}

.hero p {
    margin: 0;
    color: var(--text-soft);
    font-size: 1.2rem;
    line-height: 1.45;
}

.error-banner {
    margin-bottom: 22px;
    padding: 16px 20px;
    border: 1px solid rgba(255, 122, 89, 0.35);
    border-radius: 18px;
    background: rgba(71, 20, 14, 0.72);
    color: #ffd6cd;
    font-size: 1.05rem;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.22);
}

.dashboard {
    display: grid;
    grid-template-columns: minmax(280px, 0.96fr) minmax(320px, 1.12fr) minmax(340px, 1.18fr);
    gap: 22px;
}

.panel {
    position: relative;
    display: flex;
    flex-direction: column;
    min-height: 720px;
    padding: 26px;
    border-radius: var(--radius);
    border: 1px solid var(--line);
    background:
        linear-gradient(180deg, rgba(255, 255, 255, 0.02), rgba(255, 255, 255, 0)),
        var(--panel);
    box-shadow: var(--shadow);
    backdrop-filter: blur(12px);
    overflow: hidden;
}

.panel::before {
    content: "";
    position: absolute;
    inset: 0 auto auto 0;
    width: 100%;
    height: 3px;
    background: linear-gradient(90deg, rgba(44, 216, 255, 0), rgba(44, 216, 255, 0.88), rgba(255, 122, 89, 0.35));
}

.panel::after {
    content: "";
    position: absolute;
    top: -40%;
    right: -20%;
    width: 220px;
    height: 220px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(44, 216, 255, 0.16), transparent 70%);
}

.panel-head,
.panel-body {
    position: relative;
    z-index: 1;
}

.panel-head {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 16px;
    margin-bottom: 22px;
}

.panel-label {
    margin: 0 0 6px;
    color: var(--accent);
    font-family: 'Orbitron', sans-serif;
    font-size: 0.84rem;
    letter-spacing: 0.22em;
    text-transform: uppercase;
}

.panel-title {
    margin: 0;
    font-size: 1.85rem;
    font-weight: 700;
}

.panel-copy {
    margin: 10px 0 0;
    color: var(--text-soft);
    font-size: 1rem;
    line-height: 1.45;
}

.status-pill {
    flex-shrink: 0;
    padding: 10px 14px;
    border-radius: 999px;
    border: 1px solid rgba(126, 231, 255, 0.18);
    background: rgba(6, 15, 26, 0.72);
    color: var(--text-soft);
    font-size: 0.95rem;
    font-weight: 700;
}

.status-pill.ready {
    color: var(--success);
    border-color: rgba(169, 255, 204, 0.26);
}

.status-pill.idle {
    color: var(--text-muted);
}

.preview-stage {
    position: relative;
    display: grid;
    place-items: center;
    min-height: 360px;
    margin-bottom: 22px;
    padding: 22px;
    border-radius: 24px;
    border: 1px solid rgba(126, 231, 255, 0.14);
    background:
        linear-gradient(135deg, rgba(9, 24, 39, 0.94), rgba(6, 17, 28, 0.75)),
        radial-gradient(circle at top right, rgba(44, 216, 255, 0.12), transparent 34%);
    overflow: hidden;
}

.preview-stage::before {
    content: "";
    position: absolute;
    inset: 14px;
    border: 1px solid rgba(126, 231, 255, 0.08);
    border-radius: 18px;
}

.preview-media {
    position: relative;
    z-index: 1;
    width: min(100%, 320px);
    max-height: 420px;
    object-fit: contain;
    border-radius: 20px;
    box-shadow: 0 18px 60px rgba(0, 0, 0, 0.42);
}

.preview-empty {
    position: relative;
    z-index: 1;
    width: min(100%, 320px);
    aspect-ratio: 4 / 5;
    border-radius: 22px;
    border: 1px dashed rgba(126, 231, 255, 0.24);
    display: grid;
    place-items: center;
    padding: 24px;
    text-align: center;
    color: var(--text-muted);
    background: rgba(4, 12, 21, 0.46);
}

.file-name {
    margin: 0 0 18px;
    color: var(--text-soft);
    font-size: 1.05rem;
    word-break: break-word;
}

.metric-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 14px;
}

.metric-card,
.info-card,
.empty-card,
.result-banner,
.download-toolbar {
    padding: 18px;
    border-radius: 20px;
    border: 1px solid rgba(126, 231, 255, 0.12);
    background: rgba(9, 24, 39, 0.62);
}

.metric-card span,
.info-card span.label-chip {
    display: block;
    color: var(--text-muted);
    font-size: 0.95rem;
    text-transform: uppercase;
    letter-spacing: 0.14em;
}

.metric-card strong {
    display: block;
    margin-top: 10px;
    color: var(--text-main);
    font-family: 'Orbitron', sans-serif;
    font-size: 1.45rem;
    line-height: 1.2;
}

.stack {
    display: flex;
    flex-direction: column;
    gap: 16px;
    flex: 1;
}

.info-card strong,
.empty-card strong,
.result-banner strong {
    display: block;
    margin-bottom: 8px;
    font-size: 1.14rem;
}

.info-card p,
.empty-card p,
.result-banner p,
.helper-text {
    margin: 0;
    color: var(--text-soft);
    line-height: 1.45;
}

.upload-form,
.split-form {
    margin: 0;
}

.file-input {
    display: none;
}

.action-button,
.download-link {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    min-height: 58px;
    padding: 14px 18px;
    border: none;
    border-radius: 18px;
    cursor: pointer;
    text-decoration: none;
    font-family: 'Orbitron', sans-serif;
    font-size: 0.92rem;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.action-button.primary {
    color: #04111f;
    background: linear-gradient(135deg, #92f4ff, #2cd8ff 65%, #1cc0ef);
    box-shadow: 0 16px 30px rgba(44, 216, 255, 0.28);
}

.action-button.secondary {
    color: #fff4ef;
    background: linear-gradient(135deg, #ff8f73, #ff633f 60%, #ff4f2f);
    box-shadow: 0 16px 28px rgba(255, 99, 63, 0.24);
}

.download-link {
    color: var(--text-main);
    background: rgba(7, 20, 33, 0.78);
    border: 1px solid rgba(126, 231, 255, 0.18);
}

.download-link.zip-link {
    color: #04111f;
    background: linear-gradient(135deg, #95f6c2, #53ebb0 60%, #2fd9a0);
    border-color: rgba(149, 246, 194, 0.32);
}

.download-link.pdf-link {
    color: #fff4ef;
    background: linear-gradient(135deg, rgba(255, 143, 115, 0.34), rgba(255, 79, 47, 0.42));
    border-color: rgba(255, 122, 89, 0.38);
}

.action-button:hover,
.download-link:hover {
    transform: translateY(-2px);
}

.settings-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
}

.field {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.field label {
    color: var(--text-soft);
    font-size: 0.98rem;
    font-weight: 700;
}

.field input,
.field select {
    width: 100%;
    min-height: 48px;
    padding: 10px 12px;
    border-radius: 14px;
    border: 1px solid rgba(126, 231, 255, 0.14);
    background: rgba(6, 17, 28, 0.9);
    color: var(--text-main);
    font: inherit;
}

.field input:focus,
.field select:focus {
    outline: none;
    border-color: rgba(126, 231, 255, 0.48);
    box-shadow: 0 0 0 3px rgba(44, 216, 255, 0.12);
}

.field-wide {
    grid-column: 1 / -1;
}

.settings-summary {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
}

.summary-pill {
    padding: 14px 16px;
    border-radius: 16px;
    background: rgba(6, 17, 28, 0.72);
    border: 1px solid rgba(126, 231, 255, 0.1);
}

.summary-pill small {
    display: block;
    color: var(--text-muted);
    text-transform: uppercase;
    letter-spacing: 0.12em;
}

.summary-pill strong {
    display: block;
    margin-top: 8px;
    font-size: 1.05rem;
}

.result-banner {
    border-color: rgba(169, 255, 204, 0.16);
    background: rgba(12, 44, 32, 0.45);
}

.result-banner strong {
    color: var(--success);
}

.download-toolbar {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
}

.thumb-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
}

.thumb-card {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 14px;
    border-radius: 20px;
    border: 1px solid rgba(126, 231, 255, 0.12);
    background: rgba(9, 24, 39, 0.66);
}

.thumb-stage {
    display: grid;
    place-items: center;
    min-height: 220px;
    padding: 14px;
    border-radius: 18px;
    background: rgba(5, 15, 24, 0.9);
    border: 1px solid rgba(126, 231, 255, 0.08);
}

.thumb-stage img {
    max-width: 100%;
    max-height: 220px;
    border-radius: 12px;
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.34);
}

.thumb-meta small {
    display: block;
    color: var(--text-muted);
    text-transform: uppercase;
    letter-spacing: 0.12em;
}

.thumb-meta strong {
    display: block;
    margin: 6px 0 4px;
    font-size: 1.08rem;
}

.thumb-meta span {
    color: var(--text-soft);
}

.thumb-card .download-link {
    margin-top: auto;
}

.helper-text {
    color: var(--text-muted);
}

.page-settings-badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 10px 14px;
    border-radius: 999px;
    border: 1px solid rgba(126, 231, 255, 0.18);
    background: rgba(6, 17, 28, 0.78);
    color: var(--accent);
    font-weight: 700;
}

@media (max-width: 1220px) {
    .dashboard {
        grid-template-columns: 1fr 1fr;
    }

    .panel.output-panel {
        grid-column: 1 / -1;
        min-height: 0;
    }
}

@media (max-width: 860px) {
    .page-shell {
        width: min(100% - 24px, 100%);
        padding-top: 26px;
        padding-bottom: 32px;
    }

    .hero {
        flex-direction: column;
        align-items: flex-start;
    }

    .dashboard {
        grid-template-columns: 1fr;
    }

    .panel {
        min-height: 0;
        padding: 20px;
    }

    .metric-grid,
    .settings-grid,
    .settings-summary,
    .download-toolbar,
    .thumb-grid {
        grid-template-columns: 1fr;
    }
}
</style>
</head>
<body>

<div class="page-shell">
    <div class="hero">
        <div>
            <div class="eyebrow">A4 and custom page workflow</div>
            <h1>Image Splitter</h1>
            <p>Upload one large image, fine-tune the page setup, preview every page, then export PNGs, ZIP, or a single PDF.</p>
        </div>
        <div class="hero-copy">
            <p>The splitter now estimates page count live, supports A4, Letter, and custom sizes, and turns each result into a print-ready page preview with margins and overlap built in.</p>
        </div>
    </div>

    <% if (errorMessage != null && !errorMessage.trim().isEmpty()) { %>
        <div class="error-banner"><strong>Processing error:</strong> <%= ImageSplitterUtil.escapeHtml(errorMessage) %></div>
    <% } %>

    <div class="dashboard">
        <section class="panel preview-panel">
            <div class="panel-head">
                <div>
                    <p class="panel-label">Preview</p>
                    <h2 class="panel-title">Source Image</h2>
                    <p class="panel-copy">Check the uploaded artwork and confirm how many pages the current setup will need.</p>
                </div>
                <div class="status-pill <%= hasImage ? "ready" : "idle" %>"><%= hasImage ? "Image loaded" : "Waiting for upload" %></div>
            </div>

            <div class="preview-stage">
                <% if (hasImage) { %>
                    <img class="preview-media" src="uploads/<%= imgName %>" alt="Uploaded preview">
                <% } else { %>
                    <div class="preview-empty">
                        <div>
                            <strong>No image yet</strong>
                            <p>Choose a file to inspect its size and unlock the page setup tools.</p>
                        </div>
                    </div>
                <% } %>
            </div>

            <p class="file-name"><strong>File:</strong> <%= safeOriginalName %></p>

            <div class="metric-grid">
                <div class="metric-card">
                    <span>Width</span>
                    <strong><%= width != null ? width + " px" : "--" %></strong>
                </div>
                <div class="metric-card">
                    <span>Height</span>
                    <strong><%= height != null ? height + " px" : "--" %></strong>
                </div>
                <div class="metric-card">
                    <span>Pages</span>
                    <strong id="estimatedPages"><%= pages != null ? pages : "--" %></strong>
                </div>
            </div>

            <div class="stack" style="margin-top: 16px;">
                <div class="info-card">
                    <span class="label-chip">Current page setup</span>
                    <strong id="pageSetupLabel"><%= safePageLabel %></strong>
                    <p id="printableAreaText">Page <%= pageWidthLabel %> x <%= pageHeightLabel %> mm with <%= printableWidthLabel %> x <%= printableHeightLabel %> mm printable area.</p>
                </div>
                <div class="info-card">
                    <span class="label-chip">Live estimate</span>
                    <strong id="estimateHeadline"><%= pages != null ? pages + " pages expected" : "Upload an image to calculate pages" %></strong>
                    <p id="estimateNote">Margin <%= ImageSplitterUtil.formatOneDecimal(marginMm) %> mm and overlap <%= ImageSplitterUtil.formatOneDecimal(overlapMm) %> mm are included in the count.</p>
                </div>
            </div>
        </section>

        <section class="panel controls-panel">
            <div class="panel-head">
                <div>
                    <p class="panel-label">Actions</p>
                    <h2 class="panel-title">Configure And Split</h2>
                    <p class="panel-copy">Upload first, then set the page size, margins, and overlap before generating the page set.</p>
                </div>
                <div class="page-settings-badge" id="settingsBadge"><%= safePageLabel %></div>
            </div>

            <div class="stack">
                <div class="info-card">
                    <strong>Step 1: Upload an image</strong>
                    <p>Supported image files are validated before the preview is shown.</p>
                </div>

                <form id="uploadForm" class="upload-form" action="ImageInfoServlet" method="post" enctype="multipart/form-data">
                    <input class="file-input" type="file" id="fileChooser" name="imageFile" accept="image/*" required>
                    <button type="button" class="action-button primary" onclick="document.getElementById('fileChooser').click();">Choose Image</button>
                </form>

                <p class="helper-text">Selecting a file uploads it immediately and refreshes the left panel.</p>

                <div class="info-card">
                    <strong>Step 2: Choose page settings</strong>
                    <p>These settings control the page size, white margins, and extra overlap between slices.</p>
                </div>

                <form class="split-form stack" action="SliceServlet" method="post">
                    <input type="hidden" name="imageName" value="<%= hasImage ? imgName : "" %>">

                    <div class="settings-grid">
                        <div class="field">
                            <label for="pagePreset">Page preset</label>
                            <select id="pagePreset" name="pagePreset">
                                <option value="A4" <%= "A4".equals(pagePreset) ? "selected" : "" %>>A4</option>
                                <option value="LETTER" <%= "LETTER".equals(pagePreset) ? "selected" : "" %>>Letter</option>
                                <option value="CUSTOM" <%= "CUSTOM".equals(pagePreset) ? "selected" : "" %>>Custom</option>
                            </select>
                        </div>

                        <div class="field">
                            <label for="orientation">Orientation</label>
                            <select id="orientation" name="orientation">
                                <option value="PORTRAIT" <%= "PORTRAIT".equals(orientation) ? "selected" : "" %>>Portrait</option>
                                <option value="LANDSCAPE" <%= "LANDSCAPE".equals(orientation) ? "selected" : "" %>>Landscape</option>
                            </select>
                        </div>

                        <div class="field field-wide" id="customFields" style="<%= "CUSTOM".equals(pagePreset) ? "" : "display:none;" %>">
                            <div class="settings-grid">
                                <div class="field">
                                    <label for="customWidthMm">Custom width (mm)</label>
                                    <input type="number" id="customWidthMm" name="customWidthMm" min="10" step="0.1" value="<%= ImageSplitterUtil.formatOneDecimal(customWidthMm) %>">
                                </div>
                                <div class="field">
                                    <label for="customHeightMm">Custom height (mm)</label>
                                    <input type="number" id="customHeightMm" name="customHeightMm" min="10" step="0.1" value="<%= ImageSplitterUtil.formatOneDecimal(customHeightMm) %>">
                                </div>
                            </div>
                        </div>

                        <div class="field">
                            <label for="marginMm">Margin (mm)</label>
                            <input type="number" id="marginMm" name="marginMm" min="0" step="0.1" value="<%= ImageSplitterUtil.formatOneDecimal(marginMm) %>">
                        </div>

                        <div class="field">
                            <label for="overlapMm">Overlap (mm)</label>
                            <input type="number" id="overlapMm" name="overlapMm" min="0" step="0.1" value="<%= ImageSplitterUtil.formatOneDecimal(overlapMm) %>">
                        </div>
                    </div>

                    <div class="settings-summary">
                        <div class="summary-pill">
                            <small>Page Size</small>
                            <strong id="pageSizeText"><%= pageWidthLabel %> x <%= pageHeightLabel %> mm</strong>
                        </div>
                        <div class="summary-pill">
                            <small>Printable Area</small>
                            <strong id="printAreaSummary"><%= printableWidthLabel %> x <%= printableHeightLabel %> mm</strong>
                        </div>
                    </div>

                    <% if (hasImage) { %>
                        <button type="submit" class="action-button secondary">Create Page Set</button>
                        <p class="helper-text">The split will generate page-sized PNG previews plus ZIP and PDF downloads.</p>
                    <% } else { %>
                        <div class="empty-card">
                            <strong>Split step locked</strong>
                            <p>Upload an image first, then the page generation button will appear here.</p>
                        </div>
                    <% } %>
                </form>

                <% if (hasOutput) { %>
                    <div class="result-banner">
                        <strong>Page set ready</strong>
                        <p><%= sliceFiles.size() %> printable pages were generated using <%= safePageLabel %> settings.</p>
                    </div>
                <% } %>
            </div>
        </section>

        <section class="panel output-panel">
            <div class="panel-head">
                <div>
                    <p class="panel-label">Output</p>
                    <h2 class="panel-title">Page Previews</h2>
                    <p class="panel-copy">Review the rendered pages, then download individual PNGs, a ZIP package, or a full PDF.</p>
                </div>
                <div class="status-pill <%= hasOutput ? "ready" : "idle" %>">
                    <%= hasOutput ? sliceFiles.size() + " pages ready" : "No output yet" %>
                </div>
            </div>

            <div class="stack">
                <% if (hasOutput) { %>
                    <div class="download-toolbar">
                        <a class="download-link zip-link" href="ZipServlet">Download ZIP</a>
                        <a class="download-link pdf-link" href="PdfServlet">Download PDF</a>
                    </div>

                    <div class="thumb-grid">
                        <%
                            for (int index = 0; index < sliceFiles.size(); index++) {
                                String fileName = sliceFiles.get(index);
                        %>
                            <article class="thumb-card">
                                <div class="thumb-stage">
                                    <img src="output/<%= outputFolder %>/<%= fileName %>" alt="Page preview <%= index + 1 %>">
                                </div>
                                <div class="thumb-meta">
                                    <small>Page <%= index + 1 %></small>
                                    <strong><%= safePageLabel %></strong>
                                    <span><%= pageWidthLabel %> x <%= pageHeightLabel %> mm page</span>
                                </div>
                                <a class="download-link" href="DownloadServlet?file=<%= URLEncoder.encode(fileName, "UTF-8") %>">Download PNG</a>
                            </article>
                        <% } %>
                    </div>
                <% } else { %>
                    <div class="empty-card">
                        <strong>No pages generated yet</strong>
                        <p>Once you run the split, this panel will show thumbnail previews for every page plus PDF and ZIP export options.</p>
                    </div>
                <% } %>
            </div>
        </section>
    </div>
</div>

<script>
var sourceWidth = <%= width != null ? width.intValue() : 0 %>;
var sourceHeight = <%= height != null ? height.intValue() : 0 %>;

function mm(value) {
    return Number.isFinite(value) ? value.toFixed(1) + " mm" : "--";
}

function readNumber(id, fallbackValue) {
    var value = parseFloat(document.getElementById(id).value);
    return Number.isFinite(value) ? value : fallbackValue;
}

function resolvePageSize() {
    var preset = document.getElementById("pagePreset").value;
    var orientation = document.getElementById("orientation").value;
    var widthMm = 210.0;
    var heightMm = 297.0;

    if (preset === "LETTER") {
        widthMm = 215.9;
        heightMm = 279.4;
    } else if (preset === "CUSTOM") {
        widthMm = readNumber("customWidthMm", 210.0);
        heightMm = readNumber("customHeightMm", 297.0);
    }

    if (orientation === "LANDSCAPE") {
        var temp = widthMm;
        widthMm = heightMm;
        heightMm = temp;
    }

    return { preset: preset, orientation: orientation, widthMm: widthMm, heightMm: heightMm };
}

function updateEstimate() {
    var page = resolvePageSize();
    var marginMm = Math.max(0, readNumber("marginMm", 0));
    var overlapMm = Math.max(0, readNumber("overlapMm", 0));
    var printableWidth = page.widthMm - (marginMm * 2);
    var printableHeight = page.heightMm - (marginMm * 2);

    document.getElementById("customFields").style.display = page.preset === "CUSTOM" ? "" : "display:none;";

    var settingsLabel = (page.preset === "LETTER" ? "Letter" : (page.preset === "CUSTOM" ? "Custom" : "A4"))
        + " "
        + (page.orientation === "LANDSCAPE" ? "Landscape" : "Portrait");

    document.getElementById("settingsBadge").textContent = settingsLabel;
    document.getElementById("pageSetupLabel").textContent = settingsLabel;
    document.getElementById("pageSizeText").textContent = mm(page.widthMm) + " x " + mm(page.heightMm);

    if (printableWidth <= 0 || printableHeight <= 0) {
        document.getElementById("printAreaSummary").textContent = "Invalid margin";
        document.getElementById("printableAreaText").textContent = "Reduce the margin so the printable area stays positive.";
        document.getElementById("estimatedPages").textContent = "--";
        document.getElementById("estimateHeadline").textContent = "Fix the page settings to continue";
        document.getElementById("estimateNote").textContent = "Margins must leave some printable area on the page.";
        return;
    }

    document.getElementById("printAreaSummary").textContent = mm(printableWidth) + " x " + mm(printableHeight);
    document.getElementById("printableAreaText").textContent =
        "Page " + mm(page.widthMm) + " x " + mm(page.heightMm) + " with " + mm(printableWidth) + " x " + mm(printableHeight) + " printable area.";

    if (sourceWidth <= 0 || sourceHeight <= 0) {
        document.getElementById("estimatedPages").textContent = "--";
        document.getElementById("estimateHeadline").textContent = "Upload an image to calculate pages";
        document.getElementById("estimateNote").textContent =
            "Margin " + mm(marginMm) + " and overlap " + mm(overlapMm) + " will be applied when the image is ready.";
        return;
    }

    var scalePxPerMm = sourceWidth / printableWidth;
    var contentHeightPx = Math.max(1, Math.round(printableHeight * scalePxPerMm));
    var overlapPx = Math.max(0, Math.round(overlapMm * scalePxPerMm));

    if (overlapPx >= contentHeightPx) {
        document.getElementById("estimatedPages").textContent = "--";
        document.getElementById("estimateHeadline").textContent = "Overlap is too large";
        document.getElementById("estimateNote").textContent = "Choose an overlap smaller than the printable page height.";
        return;
    }

    var stepPx = Math.max(1, contentHeightPx - overlapPx);
    var totalPages = sourceHeight <= contentHeightPx
        ? 1
        : 1 + Math.ceil((sourceHeight - contentHeightPx) / stepPx);

    document.getElementById("estimatedPages").textContent = String(totalPages);
    document.getElementById("estimateHeadline").textContent = totalPages + (totalPages === 1 ? " page expected" : " pages expected");
    document.getElementById("estimateNote").textContent =
        "Margin " + mm(marginMm) + " and overlap " + mm(overlapMm) + " are included in this count.";
}

document.getElementById("fileChooser").addEventListener("change", function () {
    if (this.files && this.files.length > 0) {
        document.getElementById("uploadForm").submit();
    }
});

["pagePreset", "orientation", "customWidthMm", "customHeightMm", "marginMm", "overlapMm"].forEach(function (id) {
    var element = document.getElementById(id);
    if (element) {
        element.addEventListener("input", updateEstimate);
        element.addEventListener("change", updateEstimate);
    }
});

updateEstimate();
</script>

</body>
</html>
