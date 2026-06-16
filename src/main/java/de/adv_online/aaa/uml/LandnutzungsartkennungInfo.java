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

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

/**
 * Helper class to store Landnutzungsartkennung information extracted from an
 * external source.
 * 
 * @author Johannes Echterhoff (echterhoff at interactive-instruments dot de)
 *
 */
public class LandnutzungsartkennungInfo {

    public static final String OBJEKTART_COLUMN_NAME = "objektart";
    public static final String ATTRIBUTART1_COLUMN_NAME = "attributart_1";
    public static final String WERTEART1_COLUMN_NAME = "werteart_1";
    public static final String ATTRIBUTART2_COLUMN_NAME = "attributart_2";
    public static final String WERTEART2_COLUMN_NAME = "werteart_2";
    public static final String ATTRIBUTART_PLUS1_COLUMN_NAME = "attributart_plus_1";
    public static final String WERTEART_PLUS1_COLUMN_NAME = "werteart_plus_1";
    public static final String ATTRIBUTART_PLUS2_COLUMN_NAME = "attributart_plus_2";
    public static final String WERTEART_PLUS2_COLUMN_NAME = "werteart_plus_2";

    public static final String LANDNUTZUNGSARTKENNUNG_COLUMN_NAME = "landnutzungsartkennung";
    public static final String TEXTLICHE_BEZEICHNUNG_COLUMN_NAME = "textliche_bezeichnung";

    private String objektart;
    private String attributart1;
    private String werteart1;
    private String attributart2;
    private String werteart2;
    private String attributartPlus1;
    private String werteartPlus1;
    private String attributartPlus2;
    private String werteartPlus2;

    private String landnutzungsartkennung;
    private String textlicheBezeichnung;

    /**
     * @return the objektart
     */
    public String getObjektart() {
	return objektart;
    }

    /**
     * @param objektart the objektart to set
     */
    public void setObjektart(String objektart) {
	this.objektart = StringUtils.stripToNull(objektart);
    }

    /**
     * @return the attributart1
     */
    public Optional<String> getAttributart1() {
	return Optional.ofNullable(attributart1);
    }

    /**
     * @param attributart1 the attributart1 to set
     */
    public void setAttributart1(String attributart1) {
	this.attributart1 = StringUtils.stripToNull(attributart1);
    }

    /**
     * @return the werteart1
     */
    public Optional<String> getWerteart1() {
	return Optional.ofNullable(werteart1);
    }

    /**
     * @param werteart1 the werteart1 to set
     */
    public void setWerteart1(String werteart1) {
	this.werteart1 = StringUtils.stripToNull(werteart1);
    }

    /**
     * @return the attributart2
     */
    public Optional<String> getAttributart2() {
	return Optional.ofNullable(attributart2);
    }

    /**
     * @param attributart2 the attributart2 to set
     */
    public void setAttributart2(String attributart2) {
	this.attributart2 = StringUtils.stripToNull(attributart2);
    }

    /**
     * @return the werteart2
     */
    public Optional<String> getWerteart2() {
	return Optional.ofNullable(werteart2);
    }

    /**
     * @param werteart2 the werteart2 to set
     */
    public void setWerteart2(String werteart2) {
	this.werteart2 = StringUtils.stripToNull(werteart2);
    }

    /**
     * @return the attributartPlus1
     */
    public Optional<String> getAttributartPlus1() {
	return Optional.ofNullable(attributartPlus1);
    }

    /**
     * @param attributartPlus1 the attributartPlus1 to set
     */
    public void setAttributartPlus1(String attributartPlus1) {
	this.attributartPlus1 = StringUtils.stripToNull(attributartPlus1);
    }

    /**
     * @return the werteartPlus1
     */
    public Optional<String> getWerteartPlus1() {
	return Optional.ofNullable(werteartPlus1);
    }

    /**
     * @param werteartPlus1 the werteartPlus1 to set
     */
    public void setWerteartPlus1(String werteartPlus1) {
	this.werteartPlus1 = StringUtils.stripToNull(werteartPlus1);
    }

    /**
     * @return the attributartPlus2
     */
    public Optional<String> getAttributartPlus2() {
	return Optional.ofNullable(attributartPlus2);
    }

    /**
     * @param attributartPlus2 the attributartPlus2 to set
     */
    public void setAttributartPlus2(String attributartPlus2) {
	this.attributartPlus2 = StringUtils.stripToNull(attributartPlus2);
    }

    /**
     * @return the werteartPlus2
     */
    public Optional<String> getWerteartPlus2() {
	return Optional.ofNullable(werteartPlus2);
    }

    /**
     * @param werteartPlus2 the werteartPlus2 to set
     */
    public void setWerteartPlus2(String werteartPlus2) {
	this.werteartPlus2 = StringUtils.stripToNull(werteartPlus2);
    }

    /**
     * @return the landnutzungsartkennung
     */
    public String getLandnutzungsartkennung() {
	return landnutzungsartkennung;
    }

    /**
     * @param landnutzungsartkennung the landnutzungsartkennung to set
     */
    public void setLandnutzungsartkennung(String landnutzungsartkennung) {
	this.landnutzungsartkennung = StringUtils.stripToNull(landnutzungsartkennung);
    }

    /**
     * @return the textlicheBezeichnung
     */
    public String getTextlicheBezeichnung() {
	return textlicheBezeichnung;
    }

    /**
     * @param textlicheBezeichnung the textlicheBezeichnung to set
     */
    public void setTextlicheBezeichnung(String textlicheBezeichnung) {
	this.textlicheBezeichnung = StringUtils.stripToNull(textlicheBezeichnung);
    }

    public boolean isElementOrPackageInfo() {
	return StringUtils.isBlank(attributart1) && StringUtils.isBlank(attributart2)
		&& StringUtils.isBlank(attributartPlus1) && StringUtils.isBlank(attributartPlus2);
    }

    public boolean hasMatchingAttKennung(String attGidKennung) {
	return attGidKennung.equalsIgnoreCase(attributart1) || attGidKennung.equalsIgnoreCase(attributart2)
		|| attGidKennung.equalsIgnoreCase(attributartPlus1) || attGidKennung.equalsIgnoreCase(attributartPlus2);
    }

    @Override
    public String toString() {
	return "LandnutzungsartkennungInfo [objektart=" + objektart + ", attributart1=" + attributart1 + ", werteart1="
		+ werteart1 + ", attributart2=" + attributart2 + ", werteart2=" + werteart2 + ", attributartPlus1="
		+ attributartPlus1 + ", werteartPlus1=" + werteartPlus1 + ", attributartPlus2=" + attributartPlus2
		+ ", werteartPlus2=" + werteartPlus2 + ", landnutzungsartkennung=" + landnutzungsartkennung
		+ ", textlicheBezeichnung=" + textlicheBezeichnung + "]";
    }
    
    
}
