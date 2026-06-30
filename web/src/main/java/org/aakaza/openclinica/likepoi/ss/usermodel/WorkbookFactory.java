package org.aakaza.openclinica.likepoi.ss.usermodel;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This produces implementations of {@link Workbook}.<br>
 * So far we have implementations for xlsx with fastexel. See https://github.com/dhatim/fastexcel<br>
 * and ods with SODS See: https://github.com/miachm/SODS<br>
 */
public class WorkbookFactory {

	public final static Logger logger = LoggerFactory.getLogger(WorkbookFactory.class);

	public static Workbook create(File file) throws IOException {
		logger.debug("create(file = {})", file.getAbsolutePath());

		try (FileInputStream in = new FileInputStream(file)) {
			return create(in);
		}
	}

	public static Workbook create(InputStream inp) throws IOException {
		InputStream bis = inp.markSupported() ? inp : new BufferedInputStream(inp);
		bis.mark(1024);
		byte[] header = new byte[4];
		int read = bis.read(header);
		bis.reset(); //[80, 75, 3, 4]

		if (read >= 4 && header[0] == 0x50 && header[1] == 0x4B) {
			byte[] allBytes = readAllBytesJava8(bis);
			String text = new String(allBytes, 0, Math.min(allBytes.length, 500));

			if (text.contains("vnd.oasis.opendocument.spreadsheet")) {
				logger.debug("it is ods");
				return new OdsWorkbookImpl(new ByteArrayInputStream(allBytes));

			} else {
				logger.debug("it is xlsx");
				return new XlsxWorkbookImpl(new ByteArrayInputStream(allBytes));
			}
		}
		throw new IllegalArgumentException("Unbekanntes Dateiformat. Nur XLSX und ODS werden unterstützt.");
	}

	private static byte[] readAllBytesJava8(InputStream is) throws IOException {
		java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[1024];
		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}
		buffer.flush();
		return buffer.toByteArray();
	}
}

class XlsxWorkbookImpl implements Workbook {
	private final org.dhatim.fastexcel.reader.ReadableWorkbook delegate;

	public XlsxWorkbookImpl(InputStream in) throws IOException {
		this.delegate = new org.dhatim.fastexcel.reader.ReadableWorkbook(in);
	}

	@Override
	public Sheet getSheetAt(int index) {
		final org.dhatim.fastexcel.reader.Sheet fastexcelSheet = delegate.getSheet(index).get();

		final List<org.dhatim.fastexcel.reader.Row> cachedRows;
		try (Stream<org.dhatim.fastexcel.reader.Row> stream = fastexcelSheet.openStream()) {
			cachedRows = stream.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException("Fehler beim Vorladen der XLSX-Zeilen", e);
		}

		return new Sheet() {

			@Override
			public String getSheetName() {
				return fastexcelSheet.getName();
			}

			@Override
			public Row getRow(int rowIndex) {
				if (rowIndex < cachedRows.size()) {
					org.dhatim.fastexcel.reader.Row fastexcelRow = cachedRows.get(rowIndex);
					if (fastexcelRow.getCellCount() == 0)
						return null;
					return new XlsxRowImpl(fastexcelRow);
				}
				return null;
			}

			@Override
			public int getLastRowNum() {
				return cachedRows.isEmpty() ? 0 : cachedRows.size() - 1;
			}

			@Override
			public Iterator<Row> iterator() {
				List<Row> mappedRows = new ArrayList<>();
				for (org.dhatim.fastexcel.reader.Row r : cachedRows) {
					if (r.getCellCount() > 0) {
						mappedRows.add(new XlsxRowImpl(r));
					}
				}
				return mappedRows.iterator();
			}

			@Override
			public int getPhysicalNumberOfRows() {
				return cachedRows.size();
			}
		};
	}

	@Override
	public int getNumberOfSheets() {
		try (java.util.stream.Stream<org.dhatim.fastexcel.reader.Sheet> stream = delegate.getSheets()) {
			return (int) stream.count();
		}
	}

	@Override
	public String getSheetName(int index) {
		return delegate.getSheet(index).get().getName();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public Iterator<Sheet> iterator() {
		// Korrektur: Stream sauber in eine Liste mappen, damit der Iterator kompatibel
		// ist
		List<Sheet> sheets = new ArrayList<>();
		for (int i = 0; i < getNumberOfSheets(); i++) {
			sheets.add(getSheetAt(i));
		}
		return sheets.iterator();
	}
}

class XlsxRowImpl implements Row {
	private final org.dhatim.fastexcel.reader.Row delegate;

	public XlsxRowImpl(org.dhatim.fastexcel.reader.Row delegate) {
		this.delegate = delegate;
	}

	@Override
	public Cell getCell(int cellNum) {
		if (cellNum < 0 || cellNum >= delegate.getCellCount())
			return null;
		org.dhatim.fastexcel.reader.Cell cell = delegate.getCell(cellNum);
		if (cell == null || cell.getType() == org.dhatim.fastexcel.reader.CellType.EMPTY)
			return null;
		return new XlsxCellImpl(cell, delegate.getRowNum(), cellNum);
	}

	@Override
	public int getRowNum() {
		return delegate.getRowNum();
	}

	@Override
	public short getLastCellNum() {
		return (short) delegate.getCellCount();
	}

	@Override
	public Iterator<Cell> iterator() {
		List<Cell> cells = new ArrayList<>();
		for (int i = 0; i < delegate.getCellCount(); i++) {
			Cell c = getCell(i);
			if (c != null)
				cells.add(c);
		}
		return cells.iterator();
	}
}

class XlsxCellImpl implements Cell {
	private final org.dhatim.fastexcel.reader.Cell delegate;
	private final int r;
	private final int c;

	public XlsxCellImpl(org.dhatim.fastexcel.reader.Cell delegate, int r, int c) {
		this.delegate = delegate;
		this.r = r;
		this.c = c;
	}

	@Override
	public CellType getCellType() {
		if (delegate.getType() == org.dhatim.fastexcel.reader.CellType.NUMBER)
			return CellType.NUMERIC;
		if (delegate.getType() == org.dhatim.fastexcel.reader.CellType.BOOLEAN)
			return CellType.BOOLEAN;
		if (delegate.getType() == org.dhatim.fastexcel.reader.CellType.EMPTY)
			return CellType.BLANK;
		return CellType.STRING;
	}

	@Override
	public String getStringCellValue() {
		return delegate.getRawValue();
	}

	@Override
	public double getNumericCellValue() {
		try {
			return delegate.asNumber().doubleValue();
		} catch (Exception e) {
			return 0.0;
		}
	}

	@Override
	public boolean getBooleanCellValue() {
		try {
			return delegate.asBoolean();
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public int getRowIndex() {
		return r;
	}

	@Override
	public int getColumnIndex() {
		return c;
	}

	@Override
	public String getCellFormula() {
		// Gibt den rohen Text/Wert der Formelzelle zurück,
		// da fastexcel-reader die Formel selbst nicht extrahiert.
		return delegate.getRawValue() != null ? delegate.getRawValue() : "";
	}

	@Override
	public java.util.Date getDateCellValue() {
		try {
			// Excel speichert Datumsangaben als Fließkommazahl (Tage seit 30.12.1899)
			double excelTimestamp = getNumericCellValue();
			if (excelTimestamp == 0.0) {
				return null;
			}
			// Java 8 kompatible Umrechnung von Excel-Timestamp zu Java-Date
			long msSinceEpoch = (long) ((excelTimestamp - 25569.0) * 24.0 * 60.0 * 60.0 * 1000.0);
			return new java.util.Date(msSinceEpoch);
		} catch (Exception e) {
			return null;
		}
	}
}

class OdsWorkbookImpl implements Workbook {

	private static final Logger logger = LoggerFactory.getLogger(OdsWorkbookImpl.class);
	private final com.github.miachm.sods.SpreadSheet delegate;

	public OdsWorkbookImpl(InputStream in) throws IOException {
		logger.debug("OdsWorkbookImpl.<init>");
		this.delegate = new com.github.miachm.sods.SpreadSheet(in);
	}

	@Override
	public Sheet getSheetAt(int index) {
		logger.debug("OdsWorkbookImpl.getSheetAt({})", index);
		final com.github.miachm.sods.Sheet sodsSheet = delegate.getSheet(index);
		return new Sheet() {

			@Override
			public String getSheetName() {
				logger.debug("OdsWorkbookImpl.getSheetAt.Sheet.getSheetName");
				
				String sheetName = sodsSheet.getName();
				logger.debug("OdsWorkbookImpl.getSheetAt.Sheet.getSheetName returning: {}", sheetName);
				return sheetName;
			}

			@Override
			public Row getRow(int rowIndex) {
				logger.debug("OdsWorkbookImpl.getSheetAt.Sheet.getRow({})", rowIndex);
				if (rowIndex >= sodsSheet.getMaxRows())
					return null;

				boolean zeileIstLeer = true;
				for (int c = 0; c < sodsSheet.getMaxColumns(); c++) {
					Object val = sodsSheet.getRange(rowIndex, c).getValue();
					if (val != null && !val.toString().trim().isEmpty()) {
						zeileIstLeer = false;
						break;
					}
				}
				if (zeileIstLeer)
					return null;
				return new OdsRowImpl(sodsSheet, rowIndex);
			}

			@Override
			public int getLastRowNum() {
				logger.debug("OdsWorkbookImpl.getSheetAt.Sheet.getLastRowNum");
				return sodsSheet.getMaxRows() - 1;
			}

			@Override
			public Iterator<Row> iterator() {
				logger.debug("OdsWorkbookImpl.getSheetAt.Sheet.iterator");
				List<Row> rows = new ArrayList<>();
				for (int i = 0; i < sodsSheet.getMaxRows(); i++) {
					Row r = getRow(i);
					if (r != null)
						rows.add(r);
				}
				return rows.iterator();
			}

			@Override
			public int getPhysicalNumberOfRows() {
				logger.debug("OdsWorkbookImpl.getSheetAt.Sheet.getPhysicalNumberOfRows");
				return sodsSheet.getMaxRows();
			}
		};
	}

	@Override
	public int getNumberOfSheets() {
		logger.debug("OdsWorkbookImpl.getNumberOfSheets");
		return delegate.getNumSheets();
	}

	@Override
	public String getSheetName(int index) {
		logger.debug("OdsWorkbookImpl.getSheetName({})", index);
		return delegate.getSheet(index).getName();
	}

	@Override
	public void close() {
		logger.debug("OdsWorkbookImpl.close");
	}

	@Override
	public Iterator<Sheet> iterator() {
		logger.debug("OdsWorkbookImpl.iterator");
		List<Sheet> sheets = new ArrayList<>();
		for (int i = 0; i < getNumberOfSheets(); i++) {
			sheets.add(getSheetAt(i));
		}
		return sheets.iterator();
	}
}

class OdsRowImpl implements Row {

	private static final Logger logger = LoggerFactory.getLogger(OdsRowImpl.class);
	private final com.github.miachm.sods.Sheet sheet;
	private final int rowIndex;

	public OdsRowImpl(com.github.miachm.sods.Sheet sheet, int rowIndex) {
		logger.debug("OdsRowImpl.<init>(rowIndex={})", rowIndex);
		this.sheet = sheet;
		this.rowIndex = rowIndex;
	}

	@Override
	public Cell getCell(int cellNum) {
		logger.debug("OdsRowImpl.getCell({})", cellNum);
		if (cellNum >= sheet.getMaxColumns() || cellNum < 0)
			return null;
		com.github.miachm.sods.Range range = sheet.getRange(rowIndex, cellNum);
		Object val = range.getValue();
		if (val == null || val.toString().trim().isEmpty())
			return null;
		return new OdsCellImpl(range, rowIndex, cellNum);
	}

	@Override
	public int getRowNum() {
		logger.debug("OdsRowImpl.getRowNum");
		return rowIndex;
	}

	@Override
	public short getLastCellNum() {
		logger.debug("OdsRowImpl.getLastCellNum");
		return (short) sheet.getMaxColumns();
	}

	@Override
	public Iterator<Cell> iterator() {
		logger.debug("OdsRowImpl.iterator");
		List<Cell> cells = new ArrayList<>();
		for (int i = 0; i < sheet.getMaxColumns(); i++) {
			Cell c = getCell(i);
			if (c != null)
				cells.add(c);
		}
		return cells.iterator();
	}
}

class OdsCellImpl implements Cell {

	private static final Logger logger = LoggerFactory.getLogger(OdsCellImpl.class);
	private final com.github.miachm.sods.Range range;
	private final int r;
	private final int c;

	public OdsCellImpl(com.github.miachm.sods.Range range, int r, int c) {
		logger.debug("OdsCellImpl.<init>(r={}, c={})", r, c);
		this.range = range;
		this.r = r;
		this.c = c;
	}

	@Override
	public CellType getCellType() {
		logger.debug("OdsCellImpl.getCellType");
		Object val = range.getValue();
		if (val == null)
			return CellType.BLANK;
		if (val instanceof Number)
			return CellType.NUMERIC;
		if (val instanceof Boolean)
			return CellType.BOOLEAN;
		return CellType.STRING;
	}

	@Override
	public String getStringCellValue() {
	    Object val = range.getValue();
	    String result = val != null ? val.toString() : "";
	    logger.debug("OdsCellImpl.getStringCellValue returning {}", result);
	    return result;
	}

	@Override
	public double getNumericCellValue() {
		logger.debug("OdsCellImpl.getNumericCellValue");
		Object val = range.getValue();
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		try {
			return Double.parseDouble(getStringCellValue());
		} catch (Exception e) {
			return 0.0;
		}
	}

	@Override
	public boolean getBooleanCellValue() {
		logger.debug("OdsCellImpl.getBooleanCellValue");
		Object val = range.getValue();
		if (val instanceof Boolean)
			return (Boolean) val;
		return Boolean.parseBoolean(getStringCellValue());
	}

	@Override
	public int getRowIndex() {
		logger.debug("OdsCellImpl.getRowIndex returning r = {}", r);
		return r;
	}

	@Override
	public int getColumnIndex() {
		logger.debug("OdsCellImpl.getColumnIndex returning c = {}, c");
		return c;
	}

	@Override
	public String getCellFormula() {
		logger.debug("OdsCellImpl.getCellFormula");
		Object val = range.getValue();
		return val != null ? val.toString() : "";
	}

	@Override
	public java.util.Date getDateCellValue() {
		logger.debug("OdsCellImpl.getDateCellValue");
		Object val = range.getValue();
		if (val == null) {
			return null;
		}
		if (val instanceof java.util.Calendar) {
			return ((java.util.Calendar) val).getTime();
		}
		if (val instanceof java.util.Date) {
			return (java.util.Date) val;
		}
		try {
			if (val instanceof Number) {
				double odsTimestamp = ((Number) val).doubleValue();
				long ms = (long) ((odsTimestamp - 25569.0) * 86400000L);
				return new java.util.Date(ms);
			}
			String str = val.toString().trim();
			if (str.length() >= 10) {
				java.text.SimpleDateFormat odfParser = new java.text.SimpleDateFormat("yyyy-MM-dd");
				return odfParser.parse(str.substring(0, 10));
			}
		} catch (Exception e) {
			// Wenn es fehlschlägt, geben wir null zurück
		}
		return null;
	}
}