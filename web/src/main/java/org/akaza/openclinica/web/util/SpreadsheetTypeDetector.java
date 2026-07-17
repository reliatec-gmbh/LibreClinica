package org.akaza.openclinica.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Determines the true spreadsheet file type (.xls, .xlsx, .ods) from file
 * content (magic bytes / zip container structure) instead of trusting the file
 * name, which LibreClinica has historically hard-coded to ".xls".
 */
public final class SpreadsheetTypeDetector {

	// Supported extensions, in lookup priority order, for locating a saved
	// CRF version file whose extension is no longer assumed to be ".xls".
	public static final String[] SUPPORTED_EXTENSIONS = { ".xls", ".xlsx", ".ods" };

	private static final byte[] OLE2_MAGIC = { (byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, (byte) 0x1A, (byte) 0xE1 };
	private static final byte[] ZIP_MAGIC = { 0x50, 0x4B, 0x03, 0x04 };

	private SpreadsheetTypeDetector() {
	}

	/** Returns ".xls", ".xlsx" or ".ods" based on file content. */
	public static String detectExtension(Path file) throws IOException {
		byte[] header = readHeader(file, 8);

		if (startsWith(header, OLE2_MAGIC)) {
			return ".xls";
		}
		if (startsWith(header, ZIP_MAGIC)) {
			return detectZipBasedExtension(file);
		}
		throw new IOException("Unrecognized spreadsheet file format: " + file.getFileName());
	}

	private static String detectZipBasedExtension(Path file) throws IOException {
		try (ZipFile zip = new ZipFile(file.toFile())) {
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
				if ("mimetype".equals(name)) {
					// ODS packages carry a dedicated "mimetype" entry
					return ".ods";
				}
				if (name.startsWith("xl/")) {
					// XLSX packages carry an "xl/" content directory
					return ".xlsx";
				}
			}
		}
		// ZIP container but neither marker found - default to xlsx
		return ".xlsx";
	}

	private static byte[] readHeader(Path file, int length) throws IOException {
		byte[] buffer = new byte[length];
		try (InputStream in = Files.newInputStream(file)) {
			if (in.read(buffer) < length) {
				throw new IOException("File too small to determine type: " + file.getFileName());
			}
		}
		return buffer;
	}

	private static boolean startsWith(byte[] data, byte[] prefix) {
		if (data.length < prefix.length) {
			return false;
		}
		for (int i = 0; i < prefix.length; i++) {
			if (data[i] != prefix[i]) {
				return false;
			}
		}
		return true;
	}

	/** Maps a detected extension to the correct HTTP content type for download. */
	public static String contentTypeFor(String extension) {
		switch (extension) {
		case ".xlsx":
			return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		case ".ods":
			return "application/vnd.oasis.opendocument.spreadsheet";
		case ".xls":
		default:
			return "application/vnd.ms-excel";
		}
	}
}