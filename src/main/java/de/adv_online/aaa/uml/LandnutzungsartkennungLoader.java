/**
 * GeoInfoDok Transformations (Landnutzungsartkennung Tag Transformer)
 *
 * (c) 2009-2026 Arbeitsgemeinschaft der Vermessungsverwaltungen der 
 * Länder der Bundesrepublik Deutschland (AdV)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact:
 * interactive instruments GmbH
 * Bundeskanzlerplatz 2d
 * 53113 Bonn
 * Germany
 */
package de.adv_online.aaa.uml;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import de.interactive_instruments.shapechange.core.MessageSource;
import de.interactive_instruments.shapechange.core.Options;
import de.interactive_instruments.shapechange.core.ShapeChangeResult;

/**
 * Loads Landnutzungsartkennung information from an Excel file.
 * 
 * @author Johannes Echterhoff
 *
 */
public class LandnutzungsartkennungLoader implements MessageSource {

    public static String SHEET_NAME = "LN_Nutzungsarten_2023_11_28_red";

    private Options options;
    private ShapeChangeResult result;

    private List<LandnutzungsartkennungInfo> landnutzungsartkennungInfos = new ArrayList<>();

    private NumberFormat nf;

    public LandnutzungsartkennungLoader(File excelFile, Options options, ShapeChangeResult result) {

	this.nf = DecimalFormat.getInstance();
	this.nf.setMaximumFractionDigits(0);
	this.nf.setGroupingUsed(false);
	
	this.options = options;
	this.result = result;

	if (excelFile != null) {

	    if (!excelFile.exists()) {

		result.addError(this, 36, excelFile.getAbsolutePath());

	    } else {

		try {

		    Workbook xls = WorkbookFactory.create(excelFile);
		    landnutzungsartkennungInfos = parseLandnutzungsartkennungInfos(xls);

		} catch (EncryptedDocumentException e) {

		    result.addError(this, 1, e.getMessage());

		} catch (IOException e) {

		    result.addError(this, 2, e.getMessage());
		}
	    }
	}
    }

    /**
     * @return can be empty but not <code>null</code>
     */
    public List<LandnutzungsartkennungInfo> getLandnutzungsartkennungInfos() {
	return this.landnutzungsartkennungInfos;
    }

    private List<LandnutzungsartkennungInfo> parseLandnutzungsartkennungInfos(Workbook xls) {

	if (xls == null) {
	    return new ArrayList<>();
	}

	Sheet sheet = null;

	for (int i = 0; i < xls.getNumberOfSheets(); i++) {

	    String sheetName = xls.getSheetName(i);

	    if (sheetName.equalsIgnoreCase(SHEET_NAME)) {
		sheet = xls.getSheetAt(i);
		break;
	    }
	}

	if (sheet == null) {
	    result.addError(this, 3);
	    return new ArrayList<>();
	}

	/*
	 * read header row to determine which columns contain relevant information
	 */
	Map<String, Integer> fieldIndexes = new HashMap<String, Integer>();

	Row header = sheet.getRow(sheet.getFirstRowNum());

	if (header == null) {
	    result.addError(this, 4);
	    return new ArrayList<>();
	}

	boolean oaFound = false;
	boolean aa1Found = false;
	boolean wa1Found = false;
	boolean aa2Found = false;
	boolean wa2Found = false;
	boolean aap1Found = false;
	boolean wap1Found = false;
	boolean aap2Found = false;
	boolean wap2Found = false;
	boolean landnutzungsartkennungFound = false;
	boolean textBezFound = false;

	for (short i = header.getFirstCellNum(); i < header.getLastCellNum(); i++) {

	    Cell c = header.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

	    if (c == null) {
		// this is allowed
	    } else {

		String value = c.getStringCellValue();

		if (value.equalsIgnoreCase(LandnutzungsartkennungInfo.OBJEKTART_COLUMN_NAME)) {

		    fieldIndexes.put(LandnutzungsartkennungInfo.OBJEKTART_COLUMN_NAME, (int) i);
		    oaFound = true;

		} else if (value.equalsIgnoreCase(LandnutzungsartkennungInfo.ATTRIBUTART1_COLUMN_NAME)) {

		    fieldIndexes.put(LandnutzungsartkennungInfo.ATTRIBUTART1_COLUMN_NAME, (int) i);
		    aa1Found = true;

		} else if (value.equalsIgnoreCase(LandnutzungsartkennungInfo.WERTEART1_COLUMN_NAME)) {

		    fieldIndexes.put(LandnutzungsartkennungInfo.WERTEART1_COLUMN_NAME, (int) i);
		    wa1Found = true;

		} else if (value.equalsIgnoreCase(LandnutzungsartkennungInfo.ATTRIBUTART2_COLUMN_NAME)) {

		    fieldIndexes.put(LandnutzungsartkennungInfo.ATTRIBUTART2_COLUMN_NAME, (int) i);
		    aa2Found = true;

		} else if (value.equalsIgnoreCase(LandnutzungsartkennungInfo.WERTEART2_COLUMN_NAME)) {

		    fieldIndexes.put(LandnutzungsartkennungInfo.WERTEART2_COLUMN_NAME, (int) i);
		    wa2Found = true;

		} else if (value.equalsIgnoreCase(LandnutzungsartkennungInfo.ATTRIBUTART_PLUS1_COLUMN_NAME)) {

		    fieldIndexes.put(LandnutzungsartkennungInfo.ATTRIBUTART_PLUS1_COLUMN_NAME, (int) i);
		    aap1Found = true;

		} else if (value.equalsIgnoreCase(LandnutzungsartkennungInfo.WERTEART_PLUS1_COLUMN_NAME)) {

		    fieldIndexes.put(LandnutzungsartkennungInfo.WERTEART_PLUS1_COLUMN_NAME, (int) i);
		    wap1Found = true;

		} else if (value.equalsIgnoreCase(LandnutzungsartkennungInfo.ATTRIBUTART_PLUS2_COLUMN_NAME)) {

		    fieldIndexes.put(LandnutzungsartkennungInfo.ATTRIBUTART_PLUS2_COLUMN_NAME, (int) i);
		    aap2Found = true;

		} else if (value.equalsIgnoreCase(LandnutzungsartkennungInfo.WERTEART_PLUS2_COLUMN_NAME)) {

		    fieldIndexes.put(LandnutzungsartkennungInfo.WERTEART_PLUS2_COLUMN_NAME, (int) i);
		    wap2Found = true;

		} else if (value.equalsIgnoreCase(LandnutzungsartkennungInfo.LANDNUTZUNGSARTKENNUNG_COLUMN_NAME)) {

		    fieldIndexes.put(LandnutzungsartkennungInfo.LANDNUTZUNGSARTKENNUNG_COLUMN_NAME, (int) i);
		    landnutzungsartkennungFound = true;

		} else if (value.equalsIgnoreCase(LandnutzungsartkennungInfo.TEXTLICHE_BEZEICHNUNG_COLUMN_NAME)) {

		    fieldIndexes.put(LandnutzungsartkennungInfo.TEXTLICHE_BEZEICHNUNG_COLUMN_NAME, (int) i);
		    textBezFound = true;

		}
	    }
	}

	if (!oaFound || !aa1Found || !wa1Found || !aa2Found || !wa2Found || !aap1Found || !wap1Found || !aap2Found
		|| !wap2Found || !landnutzungsartkennungFound || !textBezFound) {
	    // log message that required fields were not found
	    result.addError(this, 5);
	    return new ArrayList<>();
	}

	/*
	 * Read rule content
	 */
	for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {

	    Row r = sheet.getRow(i);
	    int rowNumber = i + 1;

	    if (r == null) {
		// ignore empty rows
		continue;
	    }

	    LandnutzungsartkennungInfo lni = new LandnutzungsartkennungInfo();

	    // get objektart (required)
	    Cell c = r.getCell(fieldIndexes.get(LandnutzungsartkennungInfo.OBJEKTART_COLUMN_NAME),
		    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	    if (c == null) {
		// log message
		result.addWarning(this, 6, "" + rowNumber);
		continue;
	    } else {
		lni.setObjektart(nf.format(c.getNumericCellValue()));
	    }

	    // get landnutzungsartkennung (required)
	    c = r.getCell(fieldIndexes.get(LandnutzungsartkennungInfo.LANDNUTZUNGSARTKENNUNG_COLUMN_NAME),
		    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	    if (c == null) {
		// log message
		result.addWarning(this, 7, "" + rowNumber);
		continue;
	    } else {
		lni.setLandnutzungsartkennung(nf.format(c.getNumericCellValue()));
	    }

	    // get textliche bezeichnung (required)
	    c = r.getCell(fieldIndexes.get(LandnutzungsartkennungInfo.TEXTLICHE_BEZEICHNUNG_COLUMN_NAME),
		    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	    if (c == null) {
		// log message
		result.addWarning(this, 8, "" + rowNumber);
		continue;
	    } else {
		String text = c.getStringCellValue();
		if (text.contains("nicht weiter untergliedert")) {
		    // ignore
		    continue;
		} else if (text.trim().endsWith("[nicht im Flächensummenschluss]")) {
		    lni.setTextlicheBezeichnung(
			    StringUtils.stripEnd(text.trim(), "[nicht im Flächensummenschluss]").trim());
		} else {
		    lni.setTextlicheBezeichnung(text.trim());
		}
	    }

	    c = r.getCell(fieldIndexes.get(LandnutzungsartkennungInfo.ATTRIBUTART1_COLUMN_NAME),
		    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	    if (c != null) {
		lni.setAttributart1(c.getStringCellValue());
	    }

	    c = r.getCell(fieldIndexes.get(LandnutzungsartkennungInfo.WERTEART1_COLUMN_NAME),
		    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	    if (c != null) {
		lni.setWerteart1(nf.format(c.getNumericCellValue()));
	    }

	    c = r.getCell(fieldIndexes.get(LandnutzungsartkennungInfo.ATTRIBUTART2_COLUMN_NAME),
		    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	    if (c != null) {
		lni.setAttributart2(c.getStringCellValue());
	    }

	    c = r.getCell(fieldIndexes.get(LandnutzungsartkennungInfo.WERTEART2_COLUMN_NAME),
		    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	    if (c != null) {
		lni.setWerteart2(nf.format(c.getNumericCellValue()));
	    }

	    c = r.getCell(fieldIndexes.get(LandnutzungsartkennungInfo.ATTRIBUTART_PLUS1_COLUMN_NAME),
		    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	    if (c != null) {
		lni.setAttributartPlus1(c.getStringCellValue());
	    }

	    c = r.getCell(fieldIndexes.get(LandnutzungsartkennungInfo.WERTEART_PLUS1_COLUMN_NAME),
		    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	    if (c != null) {
		lni.setWerteartPlus1(nf.format(c.getNumericCellValue()));
	    }

	    c = r.getCell(fieldIndexes.get(LandnutzungsartkennungInfo.ATTRIBUTART_PLUS2_COLUMN_NAME),
		    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	    if (c != null) {
		lni.setAttributartPlus2(c.getStringCellValue());
	    }

	    c = r.getCell(fieldIndexes.get(LandnutzungsartkennungInfo.WERTEART_PLUS2_COLUMN_NAME),
		    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	    if (c != null) {
		lni.setWerteartPlus2(nf.format(c.getNumericCellValue()));
	    }

	    System.out.println(lni.toString());

	    this.landnutzungsartkennungInfos.add(lni);
	}

	return this.landnutzungsartkennungInfos;
    }

    @Override
    public String message(int mnr) {

	return switch (mnr) {

	case 1 -> "Invalid format for excel file. Message is: $1$";
	case 2 -> "Could not read excel file. Message is: $1$";

	case 3 -> "Sheet '" + SHEET_NAME + "' not found in excel file.";
	case 4 -> "Header not found in excel sheet.";
	case 5 -> "Did not find required columns in excel sheet.";

	case 6 ->
	    "No objektart found in line $1$ of excel sheet. This kind of message can occur for rows that are seemingly empty in the excel sheet.";
	case 7 -> "No landnutzungsartkennung found in line $1$ of excel sheet.";
	case 8 -> "No textliche Bezeichnung found in line $1$ of excel sheet.";
//	case 8 ->
//	    "Parsing main class name for rule '$1$' was not successful. This rule will not be added to the model.";
//	case 9 -> "";
//	case 10 -> "No main class name provided for rule '$1$'. Parsing the name from the rule text.";
	case 36 -> "??The excel spreadsheet was not found at file location '$1$'.";

	default -> "(" + LandnutzungsartkennungLoader.class.getName() + ") Unknown message with number: " + mnr;
	};
    }
}
