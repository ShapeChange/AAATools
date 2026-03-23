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
 * Trierer Strasse 70-72
 * 53115 Bonn
 * Germany
 */

package de.adv_online.aaa.uml;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.sparx.Attribute;
import org.sparx.Collection;
import org.sparx.Element;
import org.sparx.Repository;

import de.interactive_instruments.shapechange.core.InputAndLogParameterProvider;
import de.interactive_instruments.shapechange.core.MessageSource;
import de.interactive_instruments.shapechange.core.Options;
import de.interactive_instruments.shapechange.core.ShapeChangeAbortException;
import de.interactive_instruments.shapechange.core.ShapeChangeResult;
import de.interactive_instruments.shapechange.core.model.Transformer;
import de.interactive_instruments.shapechange.ea.util.EAAttributeUtil;
import de.interactive_instruments.shapechange.ea.util.EAElementUtil;
import de.interactive_instruments.shapechange.ea.util.EAException;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EAElement;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EAPackage;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EARepository;

/**
 * @author Johannes Echterhoff (echterhoff at interactive-instruments dot de)
 *
 */
public class LandnutzungsartkennungTagTransformer implements Transformer, MessageSource, InputAndLogParameterProvider {

    public static final String PARAM_LN_SCHEMA_FULL_NAME = "landnutzungSchemaFullName";
    public static final String PARAM_LANDNUTZUNGSARTKENNUNG_XLSX_FILE_PATH = "landnutzungsartkennungXlsxFilePath";

    public static final String TICKET_NUMMER = "7156";

    private static final boolean DO_CHANGE_TVS = true;

    private static final String FQNAME_TV_LANDNUTZUNGSARTKENNUNG = "GID::LN_LandnutzungsartkennungElement::LN:Landnutzungsartkennung";
    private static final String TV_GID_REVISIONSNUMMER = "GID:Revisionsnummer";
    private static final String FQNAME_TV_GID_REVISIONSNUMMER = "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer";

    private String landnutzungSchemaFullName = null;
    private String landnutzungsartkennungXlsxFilePath = null;

    private ShapeChangeResult result = null;
    private Options options = null;

    private Repository rep = null;
    private EARepository eaRepo = null;
    private EAPackage lnSchemaPkg = null;

    private List<LandnutzungsartkennungInfo> lakInfos = null;

    public void initialise(Options o, ShapeChangeResult r, String repositoryFileName) throws ShapeChangeAbortException {

	result = r;
	options = o;

	landnutzungSchemaFullName = o.parameterAsString(null, PARAM_LN_SCHEMA_FULL_NAME, "", false, true);
	landnutzungsartkennungXlsxFilePath = o.parameterAsString(null, PARAM_LANDNUTZUNGSARTKENNUNG_XLSX_FILE_PATH, "",
		false, true);

	if (checkExcelFile() & checkRepository(repositoryFileName)) {
	    // all pre-requisites have been met
	} else {
	    throw new ShapeChangeAbortException();
	}

    }

    private boolean checkExcelFile() {

	boolean checkPassed = true;

	File excelFile = new File(landnutzungsartkennungXlsxFilePath);
	if (!excelFile.exists()) {
	    result.addFatalError(this, 100, landnutzungsartkennungXlsxFilePath);
	    checkPassed = false;
	} else {
	    LandnutzungsartkennungLoader loader = new LandnutzungsartkennungLoader(excelFile, options, result);
	    this.lakInfos = loader.getLandnutzungsartkennungInfos();
	}

	return checkPassed;
    }

    private boolean checkRepository(String repositoryFileName) {

	boolean checkPassed = true;

	/** Make sure repository file exists */
	File repfile = new File(repositoryFileName);
	boolean ex = true;
	if (!repfile.exists()) {
	    ex = false;
	    if (!repositoryFileName.toLowerCase().endsWith(".qea")) {
		repositoryFileName += ".qea";
		repfile = new File(repositoryFileName);
		ex = repfile.exists();
	    }
	}
	if (!ex) {
	    result.addFatalError(null, 31, repositoryFileName);
	    checkPassed = false;
	} else {

	    /** Connect to EA Repository */
	    String absname = repfile.getAbsolutePath();
	    rep = new Repository();
	    if (!rep.OpenFile(absname)) {
		String errormsg = rep.GetLastError();
		result.addFatalError(null, 30, errormsg, repositoryFileName);
		checkPassed = false;
	    }

	    this.eaRepo = new EARepository(rep);

	    Optional<EAPackage> lnSchemaPkgOpt = eaRepo.lookupPackage(landnutzungSchemaFullName);

	    if (lnSchemaPkgOpt.isEmpty()) {
		result.addFatalError(this, 101, landnutzungSchemaFullName);
		checkPassed = false;
	    } else {
		lnSchemaPkg = lnSchemaPkgOpt.get();
	    }
	}

	return checkPassed;
    }

    public void shutdown() {
	rep.CloseFile();
	rep.Exit();
	rep = null;
    }

    @Override
    public void transform() throws ShapeChangeAbortException {

	try {

	    SortedMap<String, EAPackage> lnPkgsByFullName = eaRepo.all(lnSchemaPkg);

	    for (EAPackage eaPkg : lnPkgsByFullName.values()) {
		Element pkgElmt = this.rep.GetElementByID(eaPkg.getPkgElementId());
		String gidKennung = EAElementUtil.taggedValue(pkgElmt, "GID:Kennung");
		List<LandnutzungsartkennungInfo> lnaksForPackage = this.lakInfos.stream()
			.filter(i -> i.isElementOrPackageInfo() && i.getObjektart().equals(gidKennung)).toList();

		if (!lnaksForPackage.isEmpty()) {
		    this.result.addDebug(this, 102, eaPkg.getName(), gidKennung, toString(lnaksForPackage));

		    if (DO_CHANGE_TVS) {
			updateElement(pkgElmt, toString(lnaksForPackage));
		    }
		}
	    }

	    List<EAElement> lnElements = eaRepo.elementsAll(lnSchemaPkg).values().stream().toList();

	    for (EAElement eaElmt : lnElements) {

		Element elmt = this.rep.GetElementByID(eaElmt.getElementId());
		String gidKennung = EAElementUtil.taggedValue(elmt, "GID:Kennung");
		List<LandnutzungsartkennungInfo> lnaks = this.lakInfos.stream()
			.filter(i -> i.getObjektart().equals(gidKennung)).toList();

		List<LandnutzungsartkennungInfo> lnaksForFeatureType = lnaks.stream()
			.filter(i -> i.isElementOrPackageInfo()).toList();

		if (!lnaksForFeatureType.isEmpty()) {

		    this.result.addDebug(this, 103, eaElmt.getName(), gidKennung, toString(lnaksForFeatureType));

		    if (DO_CHANGE_TVS) {
			updateElement(elmt, toString(lnaksForFeatureType));
		    }

		    Collection<Attribute> atts = elmt.GetAttributes();
		    atts.Refresh();

		    for (short i = 0; i < atts.GetCount(); i++) {
			Attribute att = atts.GetAt(i);

			/*
			 * Wir suchen für alle in der Excel-Tabelle definierten vier Fälle nach
			 * relevanten Attributen und Wertearten.
			 */

			processAttribute(eaElmt, att, lnaks, lnElements, 1);
			processAttribute(eaElmt, att, lnaks, lnElements, 2);
			processAttribute(eaElmt, att, lnaks, lnElements, 3);
			processAttribute(eaElmt, att, lnaks, lnElements, 4);
		    }
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}
    }

    private void updateElement(Element elmt, String lnaksString) throws EAException {
	EAElementUtil.updateTaggedValue(elmt, FQNAME_TV_LANDNUTZUNGSARTKENNUNG, lnaksString, false);
	String gidRevNr = EAElementUtil.taggedValue(elmt, TV_GID_REVISIONSNUMMER);
	EAElementUtil.updateTaggedValue(elmt, FQNAME_TV_GID_REVISIONSNUMMER,
		determineGidRevisionsnummer(gidRevNr, TICKET_NUMMER), false);
    }

    private void updateAttribute(Attribute att, String lnaksString) throws EAException {
	EAAttributeUtil.updateTaggedValue(att, FQNAME_TV_LANDNUTZUNGSARTKENNUNG, lnaksString, false);
	String gidRevNr = EAAttributeUtil.taggedValue(att, TV_GID_REVISIONSNUMMER);
	EAAttributeUtil.updateTaggedValue(att, FQNAME_TV_GID_REVISIONSNUMMER,
		determineGidRevisionsnummer(gidRevNr, TICKET_NUMMER), false);
    }

    private String determineGidRevisionsnummer(String gidRevisionsnummerTvAktuell, String ticketNummer) {

	if (StringUtils.isNotBlank(gidRevisionsnummerTvAktuell)) {
	    return gidRevisionsnummerTvAktuell + ", #" + ticketNummer;
	} else {
	    return "#" + ticketNummer;
	}
    }

    private void processAttribute(EAElement eaElmt, Attribute att, List<LandnutzungsartkennungInfo> lnaks,
	    List<EAElement> lnElements, int attCase) throws EAException {

	String attGidKennung = EAAttributeUtil.taggedValue(att, "GID:Kennung");

	List<LandnutzungsartkennungInfo> lnaksForAtt = lnaks.stream()
		.filter(info -> (attCase == 1 && info.getAttributart1().isPresent()
			&& info.getAttributart1().get().equalsIgnoreCase(attGidKennung)
			&& info.getAttributart2().isEmpty() && info.getAttributartPlus1().isEmpty()
			&& info.getAttributartPlus2().isEmpty())
			|| (attCase == 2 && info.getAttributart2().isPresent()
				&& info.getAttributart2().get().equalsIgnoreCase(attGidKennung)
				&& info.getAttributartPlus1().isEmpty() && info.getAttributartPlus2().isEmpty())
			|| (attCase == 3 && info.getAttributartPlus1().isPresent()
				&& info.getAttributartPlus1().get().equalsIgnoreCase(attGidKennung)
				&& info.getAttributartPlus2().isEmpty())
			|| (attCase == 4 && info.getAttributartPlus2().isPresent()
				&& info.getAttributartPlus2().get().equalsIgnoreCase(attGidKennung)))
		.toList();

	// Für das Attribut selbst wird kein Wert gesetzt!

	/*
	 * Jetzt den Typ des Attributs herausfinden und schauen, welche Wertearten
	 * angepasst werden müssen.
	 */
	String type = att.GetType();
	Optional<EAElement> enumerationOpt = lnElements.stream().filter(e -> type.equals(e.getName())).findFirst();
	if (enumerationOpt.isEmpty()) {
	    // Could be CharacterString, etc.
//	    this.result.addDebug(this, 105, eaElmt.getName(), attName, type);
	} else {
	    EAElement enumerationEaElmt = enumerationOpt.get();
	    Element enumerationElmt = this.rep.GetElementByID(enumerationEaElmt.getElementId());

	    // TBD: Auch auf der Enumeration die Revisionsnummer setzen?
	    Collection<Attribute> wats = enumerationElmt.GetAttributes();
	    wats.Refresh();

	    for (short j = 0; j < wats.GetCount(); j++) {
		Attribute wat = wats.GetAt(j);
		String initialValue = EAAttributeUtil.initialValue(wat);
		String watName = wat.GetName();

		/*
		 * Hier müssen wir unterscheiden, ob es in einer Zeile mehrere Werte gibt.
		 */
		List<LandnutzungsartkennungInfo> lnaksForWerteart = lnaksForAtt.stream()
			.filter(lnak -> (attCase == 1 && lnak.getWerteart1().isPresent()

				&& initialValue.equals(lnak.getWerteart1().get()))
				|| (attCase == 2 && lnak.getWerteart2().isPresent()
					&& initialValue.equals(lnak.getWerteart2().get()))
				|| (attCase == 3 && lnak.getWerteartPlus1().isPresent()
					&& initialValue.equals(lnak.getWerteartPlus1().get()))
				|| (attCase == 4 && lnak.getWerteartPlus2().isPresent()
					&& initialValue.equals(lnak.getWerteartPlus2().get())))
			.toList();

		if (!lnaksForWerteart.isEmpty()) {
		    this.result.addDebug(this, 104, type, watName, initialValue, toString(lnaksForWerteart));

		    if (DO_CHANGE_TVS) {
			updateAttribute(wat, toString(lnaksForWerteart));
		    }

		} else {
		    // Für die Werteart ist keine Landnutzungsartkennung in der Tabelle definiert.
		}
	    }
	}
    }

    private String toString(List<LandnutzungsartkennungInfo> lnaks) {
	return StringUtils.join(lnaks.stream().map(i -> i.getLandnutzungsartkennung()).distinct().sorted().toList(),
		", ");
    }

    @Override
    public SortedSet<String> allowedInputParametersWithStaticNames() {
	return new TreeSet<>(Stream.of(PARAM_LANDNUTZUNGSARTKENNUNG_XLSX_FILE_PATH, PARAM_LN_SCHEMA_FULL_NAME)
		.collect(Collectors.toSet()));
    }

    @Override
    public List<Pattern> regexesForAllowedInputParametersWithDynamicNames() {
	return null;
    }

    @Override
    public SortedSet<String> allowedLogParametersWithStaticNames() {
	return null;
    }

    @Override
    public List<Pattern> regexesForAllowedLogParametersWithDynamicNames() {
	return null;
    }

    @Override
    public String message(int mnr) {

	switch (mnr) {

	case 0:
	    return "Context: class '$1$'";
	case 1:
	    return "Context: property '$1$'";

	case 100:
	    return "Excel file with values for LN:Landnutzungsartkennung not found at the path given by parameter '"
		    + PARAM_LANDNUTZUNGSARTKENNUNG_XLSX_FILE_PATH + "': $1$";
	case 101:
	    return "No package found with full name given by parameter '" + PARAM_LN_SCHEMA_FULL_NAME + "': $1$";

	case 102:
	    return "Package '$1$' ($2$), setting LN:Landnutzungsartkennung to $3$";
	case 103:
	    return "Class '$1$' ($2$), setting LN:Landnutzungsartkennung to $3$";
	case 104:
	    return "Werteart '$1$.$2$' ($3$), setting LN:Landnutzungsartkennung to $4$";
	case 105:
	    return "Could not find type '$3$' of attribute '$1$.$2$'";
	case 106:
	    return "Could not find  '$3$' of attribute '$1$.$2$'";
	default:
	    return "(" + LandnutzungsartkennungTagTransformer.class.getName() + ") Unknown message with number: " + mnr;
	}
    }
}
