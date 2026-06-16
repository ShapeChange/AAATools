/**
 * NAS-Tool (schema transformer)
 *
 * The class in this file implements the ShapeChange Target interface to 
 * generate and load the 3AP files.
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

package de.adv_online.gid.nastool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.sparx.Attribute;
import org.sparx.AttributeTag;
import org.sparx.Collection;
import org.sparx.Connector;
import org.sparx.ConnectorEnd;
import org.sparx.Element;
import org.sparx.Package;
import org.sparx.Repository;
import org.sparx.RoleTag;
import org.sparx.TaggedValue;

import de.interactive_instruments.shapechange.core.Options;
import de.interactive_instruments.shapechange.core.ShapeChangeAbortException;
import de.interactive_instruments.shapechange.core.ShapeChangeResult;
import de.interactive_instruments.shapechange.ea.util.EAAttributeUtil;
import de.interactive_instruments.shapechange.ea.util.EAConnectorEndUtil;
import de.interactive_instruments.shapechange.ea.util.EAElementUtil;
import de.interactive_instruments.shapechange.ea.util.EAException;
import de.interactive_instruments.shapechange.ea.util.EAPackageUtil;
import de.interactive_instruments.shapechange.ea.util.EARepositoryUtil;
import de.interactive_instruments.shapechange.ea.util.EATaggedValue;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EAElement;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EAPackage;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EARepository;

public class ImplementationSchemaTransformerHelper_GID {

    Repository rep = null;
    EARepository eaRepo = null;
    private ShapeChangeResult result = null;

    protected HashMap<String, EAPackage> allPackages = new HashMap<>();
    protected HashMap<String, EAPackage> gidPackages = new HashMap<>();
    HashMap<String, EAElement> allClasses = new HashMap<>();
    HashMap<String, EAElement> gidClasses = new HashMap<>();
    private int SeqNo = 32000;
    protected SortedSet<String> aaaVersionTagValues = new TreeSet<>();

    protected SpecialRoleHandler specialRoleHandler = new SpecialRoleHandler();

    /**
     * @return Die GID:AAAVersion, die für diesen Durchlauf relevant ist.
     *         <code>null</code>, falls kein eindeutiger Wert ermittelt werden
     *         konnte.
     */
    public String relevantAaaVersion() {
	if (this.aaaVersionTagValues.size() != 1) {
	    return null;
	} else {
	    return this.aaaVersionTagValues.getFirst();
	}
    }

    public void initialise(Options o, ShapeChangeResult r, String repositoryFileName) throws ShapeChangeAbortException {
	result = r;

	/** Make sure repository file exists */
	java.io.File repfile = new java.io.File(repositoryFileName);
	boolean ex = true;
	if (!repfile.exists()) {
	    ex = false;
	    if (!repositoryFileName.toLowerCase().endsWith(".qea")) {
		repositoryFileName += ".qea";
		repfile = new java.io.File(repositoryFileName);
		ex = repfile.exists();
	    }
	}
	if (!ex) {
	    r.addFatalError(null, 31, repositoryFileName);
	    throw new ShapeChangeAbortException();
	}

	/** Connect to EA Repository */
	String absname = repfile.getAbsolutePath();
	rep = new Repository();
	if (!rep.OpenFile(absname)) {
	    String errormsg = rep.GetLastError();
	    r.addFatalError(null, 30, errormsg, repositoryFileName);
	    throw new ShapeChangeAbortException();
	}
    }

    public void shutdown() {
	rep.CloseFile();
	rep.Exit();
	rep = null;
    }

    public Optional<Element> getElement(String schemaPkgFullName, String nameOfContainedElement) {

	List<EAElement> elmts = eaRepo.lookupElementByName(nameOfContainedElement);

	for (EAElement elmt : elmts) {
	    if (elmt.getFullName().startsWith(schemaPkgFullName)) {
		Element e = rep.GetElementByID(elmt.getElementId());
		return Optional.of(e);
	    }
	}

	return Optional.empty();
    }

    /**
     * Multiple Vererbung: Weder ISO 19136 / GML 3.3 noch ISO/TS 19139 unterstützen
     * in den Abbildungsregeln multiple Vererbung, das AAA-Anwendungsschema
     * verwendet diese jedoch in Mixin-Klassen (z.B. AP_GPO, AX_Katalogeintrag). Die
     * Mixin-Klassen werden aufgelöst:
     * <ul>
     * <li>Alle Attribute werden in die nächsten in der NAS codierten Subtypen
     * kopiert.</li>
     * <li>Alle Relationen zu den Mixin-Klassen werden ebenfalls jeweils auf die
     * nächsten in der NAS codierten Subtypen kopiert. Dabei wird der Rollenname
     * durch Anhängen des Klassennamens geändert, um die Eindeutigkeit der
     * Eigenschaftsnamen zu gewährleisten.</li>
     * <li>Die Mixin-Klassen werden gelöscht.</li>
     * </ul>
     * 
     * @param importedMixins tbd
     */
    public void resolveMixins(boolean importedMixins) {

	List<String> tobedeleted = new ArrayList<String>();
	for (EAElement eaElmt : gidClasses.values()) {
	    Element e = rep.GetElementByID(eaElmt.getElementId());
	    String st = e.GetStereotype().toLowerCase();
	    if (isTypeStereotype(st)) {
		copyDown(e);
		tobedeleted.add(e.GetName());
	    }
	}

	for (String s : tobedeleted) {
	    deleteClass(s);
	}

	if (importedMixins) {
	    for (EAElement eaElmt : gidClasses.values()) {
		Element e = rep.GetElementByID(eaElmt.getElementId());
		result.addDebug("Now processing imported mixins for GID class " + e.GetName());
		boolean cont = true;
		while (cont) {
		    cont = false;
		    Collection<Connector> c = e.GetConnectors();
		    for (short i = 0; i < c.GetCount(); i++) {
			Connector ci = c.GetAt(i);
			String s = ci.GetType();
			if (!s.equals("Generalization"))
			    continue;
			if (ci.GetClientID() != e.GetElementID())
			    continue;
			Element e1 = rep.GetElementByID(ci.GetSupplierID());
			if (e1 != null && isTypeStereotype(e1.GetStereotype().toLowerCase())) {
			    copyDown(e1);
			    c.Delete(i);
			    if (!e.Update()) {
				result.addError("Fehler beim Löschen von Generaliserung zu Mixin '" + e1.GetName()
					+ "' in '" + e.GetName() + "': " + e.GetLastError());
			    } else {
				result.addDebug("Generaliserung zu Mixin '" + e1.GetName() + "' in '" + e.GetName()
					+ "' gelöscht.");
			    }
			    c.Refresh();
			    cont = true;
			    break;
			}
		    }
		}
	    }
	}
    }

    /**
     * Bei allen Klassen wird das UML Tagged Value „xsdEncodingRule“ gesetzt:
     * 
     * <ul>
     * <li>"NAS" außer bei Typen, die mit einer der Zeichenketten "AX_DQ", "AX_LI",
     * "AX_Datenerhebung" beginnen;</li>
     * <li>bei diesen wird „iso19139_2007“ verwendet.</li>
     * </ul>
     * 
     * Bei Klassen werden die folgenden UML Tagged Values gesetzt:
     * <ul>
     * <li>noPropertyType: <code>true</code> bei Feature Type; <code>false</code>
     * bei Data Type</li>
     * <li>byValuePropertyType: <code>false</code> bei Feature Type und Data
     * Type</li>
     * <li>isCollection: <code>false</code> bei Feature Type und DataType</li>
     * <li>asDictionary: <code>true</code>, nur bei CodeList</li>
     * </ul>
     */
    public void setTaggedValues() {

	for (EAPackage eaPkg : gidPackages.values()) {
	    result.addDebug("Now looking up package " + eaPkg.getFullName() + " (PkgId: " + eaPkg.getPkgId()
		    + ", Pkg ElementId: " + eaPkg.getPkgElementId() + ")");
	    Package pkg = rep.GetPackageByID(eaPkg.getPkgId());
	    try {
		EAPackageUtil.updateTaggedValue(pkg, "xsdEncodingRule", "NAS", false);
	    } catch (EAException ex) {
		result.addError(ex.getMessage());
	    }
	}

	for (EAElement eaElmt : gidClasses.values()) {
	    result.addDebug(
		    "Now setting tagged values on gid class " + eaElmt.getName() + " (" + eaElmt.getFullName() + ")");
	    Element e = rep.GetElementByID(eaElmt.getElementId());
	    String n = eaElmt.getName();
	    String st = e.GetStereotype().toLowerCase();
	    String type = e.GetType().toLowerCase();

	    try {
		if ((isEnumerationStereotype(st) || type.equals("enumeration")) && (n.equals("AX_BezugspunktDach")
			|| n.equals("AX_Datenerhebung") || n.equals("AX_Datenerhebung3D")
			|| n.equals("AX_Datenerhebung_Punktort") || n.startsWith("AX_LI") || n.startsWith("AX_DQ"))) {
		    EAElementUtil.updateTaggedValue(e, "xsdEncodingRule", "iso19139_2007", false);
		} else {
		    EAElementUtil.updateTaggedValue(e, "xsdEncodingRule", "NAS", false);
		}
		if (isFeatureTypeStereotype(st)) {
		    EAElementUtil.updateTaggedValue(e, "noPropertyType", "true", false);
		    EAElementUtil.updateTaggedValue(e, "byValuePropertyType", "false", false);
		    EAElementUtil.updateTaggedValue(e, "isCollection", "false", false);
		} else if (isDataTypeStereotype(st)) {
		    EAElementUtil.updateTaggedValue(e, "noPropertyType", "false", false);
		    EAElementUtil.updateTaggedValue(e, "byValuePropertyType", "false", false);
		    EAElementUtil.updateTaggedValue(e, "isCollection", "false", false);
		} else if (isCodelistStereotype(st)) {
		    EAElementUtil.updateTaggedValue(e, "asDictionary", "true", false);
		}
	    } catch (EAException ex) {
		result.addError(ex.getMessage());
	    }

	    deleteMethods(e);

	    /*
	     * Nicht navigierbare Assoziationsrollen werden: 1. navigierbar gesetzt, 2.
	     * sofern nicht vorhanden mit dem Namen „inversZu_“ und den Namen der inversen
	     * Rolle versehen, 3. mit einer minimalen Kardinalität von "0" versehen, 4. der
	     * UML Tagged Value "reverseRoleNAS" wird auf „true“ gesetzt
	     */
	    boolean cont = true;
	    ConnectorEnd ei1, ei2;
	    Element e2;
	    while (cont) {
		cont = false;
		Collection<Connector> c = e.GetConnectors();
		SortedMap<String, Connector> map = new TreeMap<String, Connector>();
		for (Connector r2 : c) {
		    String rt = r2.GetType();
		    if (rt.equals("Association") || rt.equals("Aggregation")) {
			String key = (r2.GetClientID() == e.GetElementID()
				? rep.GetElementByID(r2.GetSupplierID()).GetName() + "." + r2.GetSupplierEnd().GetRole()
					+ "." + r2.GetClientEnd().GetRole()
				: rep.GetElementByID(r2.GetClientID()).GetName() + "." + r2.GetClientEnd().GetRole()
					+ "." + r2.GetSupplierEnd().GetRole());
			if (map.containsKey(key))
			    result.addError(
				    "Fehler beim Sortieren der Relationen, Schlüssel '" + key + "' existiert bereits.");
			map.put(key, r2);
		    }
		}
		for (Connector ei : map.values()) {
		    if (ei.GetClientID() == 0 || ei.GetSupplierID() == 0) {
			for (short i = 0; i < c.GetCount(); i++) {
			    if (c.GetAt(i).GetConnectorID() == ei.GetConnectorID()) {
				c.Delete(i);
				if (!e.Update()) {
				    result.addError("Fehler beim Löschen von hängender Relation in '" + e.GetName()
					    + "': " + e.GetLastError());
				} else {
				    result.addDebug("Hängende Relation in '" + e.GetName() + "' gelöscht.");
				}
				c.Refresh();
				cont = true;
				break;
			    }
			}
			if (cont)
			    break;
			else
			    result.addError("Fehler beim Löschen von hängender Relation in '" + e.GetName()
				    + "': Connector not found");
		    }
		    if (ei.GetClientID() == e.GetElementID()) {
			if (ei.GetSupplierID() == e.GetElementID()) {
			    e2 = e;
			    if (ei.GetSupplierEnd().GetNavigable().equals("Navigable")) {
				ei1 = ei.GetSupplierEnd();
				ei2 = ei.GetClientEnd();
			    } else {
				ei1 = ei.GetClientEnd();
				ei2 = ei.GetSupplierEnd();
			    }
			} else {
			    e2 = rep.GetElementByID(ei.GetSupplierID());
			    ei1 = ei.GetClientEnd();
			    ei2 = ei.GetSupplierEnd();
			}
		    } else {
			e2 = rep.GetElementByID(ei.GetClientID());
			ei1 = ei.GetSupplierEnd();
			ei2 = ei.GetClientEnd();
		    }
		    String s = e2.GetStereotype().toLowerCase();
		    if (isFeatureTypeStereotype(s) || s.equals("") || isTypeStereotype(s)) {

			updateTaggedValueRole(ei2, "inlineOrByReference", "byReference", true);

			if (!ei2.GetNavigable().equals("Navigable")) {

			    updateTaggedValueRole(ei2, "reverseRoleNAS", "true", true);

			    String mul = ei2.GetCardinality();
			    if (mul.contains("1..")) {
				mul = mul.replace("1..", "0..");
			    } else if (mul.contains("2..")) {
				mul = mul.replace("2..", "0..");
			    } else if (!mul.contains("..")) {
				mul = "0.." + mul;
			    } else if (!mul.contains("0..")) {
				result.addError("Fehler beim Aktualisieren von inverser Rolle in '" + e.GetName()
					+ "' zu '" + e2.GetName() + "': Multiplizität '" + mul + "' nicht erkannt.");
			    }
			    ei2.SetCardinality(mul);
			    ei2.SetNavigable("Navigable");
			    if (ei2.GetRole().equals("")) {
				ei2.SetRole("inversZu_" + ei1.GetRole());

				updateTaggedValueRole(ei2, "sequenceNumber", Integer.valueOf(SeqNo++).toString(),
					false);
			    }
			    if (!ei2.Update()) {
				result.addError("Fehler beim Aktualisieren von inverser Rolle in '" + e.GetName()
					+ "' zu '" + e2.GetName() + "': " + ei2.GetLastError());
			    } else {
				result.addDebug("Inverse Rolle in '" + e.GetName() + "' zu '" + e2.GetName()
					+ "' aktualisiert.");
			    }
			    e.GetConnectors().Refresh();
			}
		    }
		}
	    }
	}
    }

    private boolean isEnumerationStereotype(String st_lowercase) {
	return Strings.CS.equalsAny(st_lowercase, "enumeration", "gid_enumeration");
    }

    private boolean isFeatureTypeStereotype(String st_lowercase) {
	return Strings.CS.equalsAny(st_lowercase, "featuretype", "gid_featuretype");
    }

    private boolean isTypeStereotype(String st_lowercase) {
	return Strings.CS.equalsAny(st_lowercase, "type", "gid_mixin");
    }

    private boolean isDataTypeStereotype(String st_lowercase) {
	return Strings.CS.equalsAny(st_lowercase, "datatype", "gid_datatype");
    }

    private boolean isCodelistStereotype(String st_lowercase) {
	return Strings.CS.equalsAny(st_lowercase, "codelist", "gid_codeset");
    }

    public void addGeneralization(Element e1, Element e2) {
	if (e1 != null && e2 != null) {

	    try {
		EARepositoryUtil.createEAGeneralization(rep, e1, e2);
		result.addDebug("Generalisierung '" + e1.GetName() + "'-'" + e2.GetName() + "' erzeugt");

	    } catch (EAException ex) {
		result.addError("Fehler beim Erzeugen der Generalisierung '" + e1.GetName() + "'-'" + e2.GetName()
			+ "': " + ex.getMessage());
	    }
	}
    }

    public void removeGeneralization(String sub, String sup) {

	// TODO move removal of generalization relationship to the EA util classes

	EAElement eaElmt1 = gidClasses.get(sub);

	EAElement eaElmt2 = gidClasses.containsKey(sup) ? gidClasses.get(sup) : allClasses.get(sup);

	Element e1 = rep.GetElementByID(eaElmt1.getElementId());
	Element e2 = rep.GetElementByID(eaElmt2.getElementId());
	if (e1 == null) {
	    result.addError("Klasse '" + sub + "' nicht gefunden.");
	} else if (e2 == null) {
	    result.addError("Klasse '" + sup + "' nicht gefunden.");
	} else {
	    int i1 = e1.GetElementID();
	    int i2 = e2.GetElementID();
	    e1.GetConnectors().Refresh();
	    Collection<Connector> c = e1.GetConnectors();
	    for (short i = 0; i < c.GetCount(); i++) {
		Connector ei = c.GetAt(i);
		int ic = ei.GetClientID();
		int is = ei.GetSupplierID();
		if (is == i2) {
		    c.Delete(i);
		    if (!e1.Update()) {
			result.addError("Fehler beim Löschen von Generalisierung zwischen '" + sub + "' und '" + sup
				+ "': " + e1.GetLastError());
		    } else {
			result.addDebug("Generalisierung zwischen '" + sub + "' und '" + sup + "' gelöscht.");
		    }
		    c.Refresh();
		    break;
		}
	    }
	}
    }

    private void copyDown(Element e) {
	copyDown(e, e);
    }

    protected void copyDown(Element classToCopyDown, Element contextForCopyingDown) {

	int classToCopyDownElementId = classToCopyDown.GetElementID();

	if (classToCopyDown.GetLocked()) {
	    result.addWarning("Element '" + classToCopyDown.GetName() + "' ist gesperrt und wird ignoriert.");
	} else if (!gidClasses.values().stream().anyMatch(elmt -> elmt.getElementId() == classToCopyDownElementId)) {
	    result.addInfo("Element '" + classToCopyDown.GetName()
		    + "' ist nicht Teil des Anwendungsschemas und wird ignoriert.");
	} else {
	    for (Connector conn : contextForCopyingDown.GetConnectors()) {
		// note: clientId = element at source end, supplierId = element at target end

		/*
		 * FIXME Wenn man die Reihenfolge der Verarbeitung der Unterklassen einheitlich
		 * gestalten möchte dann müsste man hier zunächst alle Vererbungsbeziehungen
		 * einsammeln und diese dann passend sortieren (z.B. alphabetisch aufsteigend
		 * nach Name der Unterklasse).
		 */
		if (conn.GetType().equals("Generalization")
			&& conn.GetSupplierID() == contextForCopyingDown.GetElementID()) {
		    /*
		     * contextForCopyingDown is supertype; determine the subtype identified in this
		     * generalization connector
		     */
		    int clientId = conn.GetClientID();
		    Element subtype = rep.GetElementByID(clientId);
		    if (subtype == null) {
			// Nichts zu tun
		    } else if (!gidClasses.values().stream().anyMatch(elmt -> elmt.getElementId() == clientId)) {
			result.addInfo("Element '" + subtype.GetName()
				+ "' ist nicht Teil des Anwendungsschemas und wird ignoriert.");
		    } else {
			String st = subtype.GetStereotype().toLowerCase();
			if (isTypeStereotype(st)) {
			    // subtype is mixin, so copy down to its subclasses
			    copyDown(classToCopyDown, subtype);
			}
			// 2026-02-24 JE: überflüssig da bereits zuvor gecheckt?
//			else if (!gidClasses.containsKey(e2.GetName())) {
//			    result.addInfo("Element '" + e2.GetName()
//				    + "' ist nicht Teil des Anwendungsschemas und wird ignoriert.");
//			} 
			else {
			    for (Attribute a : classToCopyDown.GetAttributes()) {
				try {
				    cloneAttribute(a, subtype);
				} catch (Exception ex) {
				    result.addError("Fehler beim Clonen von Attribut '" + a.GetName()
					    + "' (Zielklasse '" + subtype.GetName() + "'): " + subtype.GetLastError());
				}
			    }
			    SortedMap<String, Connector> map = new TreeMap<String, Connector>();
			    for (Connector r2 : classToCopyDown.GetConnectors()) {
				String rt = r2.GetType();
				if (rt.equals("Association") || rt.equals("Aggregation")) {
				    String key = (r2.GetClientID() == classToCopyDown.GetElementID()
					    ? rep.GetElementByID(r2.GetSupplierID()).GetName() + "."
						    + r2.GetSupplierEnd().GetRole() + "." + r2.GetClientEnd().GetRole()
					    : rep.GetElementByID(r2.GetClientID()).GetName() + "."
						    + r2.GetClientEnd().GetRole() + "."
						    + r2.GetSupplierEnd().GetRole());
				    if (map.containsKey(key))
					result.addError("Fehler beim Sortieren der Relationen, Schlüssel '" + key
						+ "' existiert bereits.");
				    map.put(key, r2);
				}
			    }
			    for (Connector r2 : map.values()) {
				try {
				    cloneAssociation(r2, classToCopyDown, subtype);
				} catch (Exception ex) {
				    result.addError("Fehler beim Clonen von Relation '" + classToCopyDown.GetName()
					    + "'/'" + subtype.GetName() + "'");
				}
			    }
			}
		    }
		}
	    }
	}
    }

    public void cloneAttribute(Attribute a, Element e) {
	String s = a.GetName();
	String s2 = e.GetName();
	Attribute a2 = e.GetAttributes().AddNew(a.GetName(), a.GetType());
	if (!e.Update()) {
	    result.addError("Fehler beim Clonen von Attribut '" + a.GetName() + "' (Zielklasse '" + s2 + "') mit Typ '"
		    + a.GetType() + "': " + e.GetLastError());
	} else {
	    result.addDebug("Attribut '" + s + "' geclont (Zielklasse '" + s2 + "').");
	}
	e.GetAttributes().Refresh();

	a2.SetLowerBound(a.GetLowerBound());
	a2.SetUpperBound(a.GetUpperBound());
	a2.SetNotes(a.GetNotes());
	a2.SetStereotypeEx(a.GetStereotypeEx());
	if (!a2.Update()) {
	    result.addError("Fehler beim Clonen von Attribut '" + s + "' (Zielklasse '" + s2
		    + "') beim Kopieren der Multiplizität, Stereotype und Dokumentation: " + a2.GetLastError());
	}
	for (AttributeTag tv : a.GetTaggedValues()) {
	    try {
		EAAttributeUtil.updateTaggedValue(a2, tv.GetName(), tv.GetValue(), false);
	    } catch (EAException ex) {
		result.addError("Fehler beim Clonen von Attribut '" + s + "' (Zielklasse '" + s2
			+ "') beim Kopieren von Tagged Value ': " + ex.getMessage());

	    }
	}
    }

    private void cloneAssociation(Connector associationWithSupertype, Element supertype, Element subtype) {
	Connector r2;
	Element e3;
	if (associationWithSupertype.GetClientID() == supertype.GetElementID()) {
	    // supertype is association source
	    e3 = rep.GetElementByID(associationWithSupertype.GetSupplierID());
	    // add new association in subtype, using the target from the original
	    // association as target
	    subtype.GetConnectors().Refresh();
	    r2 = subtype.GetConnectors().AddNew("", associationWithSupertype.GetType());
	    if (r2 == null) {
		result.addError("Fehler beim Clonen von Relation '" + supertype.GetName() + "'/'" + subtype.GetName()
			+ "'-'" + e3.GetName() + "'");
		return;
	    }
	    r2.SetSupplierID(e3.GetElementID());
	} else {
	    // supertype is association target
	    e3 = rep.GetElementByID(associationWithSupertype.GetClientID());
	    // add new association in association source element, using the subtype as
	    // target
	    e3.GetConnectors().Refresh();
	    r2 = e3.GetConnectors().AddNew("", associationWithSupertype.GetType());
	    if (r2 == null) {
		result.addError("Fehler beim Clonen von Relation '" + supertype.GetName() + "'/'" + subtype.GetName()
			+ "'-'" + e3.GetName() + "'");
		return;
	    }
	    r2.SetSupplierID(subtype.GetElementID());
	}

	r2.SetDirection("Bi-Directional");

	result.addDebug(
		"Relation '" + supertype.GetName() + "'/'" + subtype.GetName() + "'-'" + e3.GetName() + "' geclont");
	if (!r2.Update()) {
	    result.addError("Fehler beim Clonen von Relation '" + supertype.GetName() + "'/'" + subtype.GetName()
		    + "'-'" + e3.GetName() + "': " + r2.GetLastError());
	}

	ConnectorEnd r1c, r1s, r2c, r2s;
	r1c = associationWithSupertype.GetClientEnd();
	r1s = associationWithSupertype.GetSupplierEnd();
	r2c = r2.GetClientEnd();
	r2s = r2.GetSupplierEnd();

	r2c.SetIsNavigable(r1c.GetIsNavigable());
	String s = r1c.GetCardinality();
	if (s.equals("1"))
	    s = "0..1";
	else if (s.startsWith("1"))
	    s = "0" + s.substring(1);
	r2c.SetCardinality(s);

	if (associationWithSupertype.GetClientID() == supertype.GetElementID()) {
	    r2c.SetRole(r1c.GetRole() + "_" + subtype.GetName());
	} else {
	    r2c.SetRole(r1c.GetRole());
	}
	r2c.SetRoleNote(r1c.GetRoleNote());

	r2c.Update();

	// FIXME Setzen von StereotypeEx funktioniert nicht richtig.

	r2c.SetStereotype(r1c.GetStereotype());
//	try {
//	    EAConnectorEndUtil.setEAStereotypeEx(r2c, "GID::GID_Property");
//	} catch (EAException ex) {
//	    result.addError(ex.getMessage());
//	}

	r2s.SetIsNavigable(r1s.GetIsNavigable());
	s = r1s.GetCardinality();
	if (s.equals("1"))
	    s = "0..1";
	else if (s.startsWith("1"))
	    s = "0" + s.substring(1);
	r2s.SetCardinality(s);

	if (associationWithSupertype.GetClientID() == supertype.GetElementID()) {
	    r2s.SetRole(r1s.GetRole());
	} else {
	    r2s.SetRole(r1s.GetRole() + "_" + subtype.GetName());
	}
	r2s.SetRoleNote(r1s.GetRoleNote());

	r2c.Update();

	r2s.SetStereotype(r1s.GetStereotype());
//	try {
//	    EAConnectorEndUtil.setEAStereotypeEx(r2s, "GID::GID_Property");
//	} catch (EAException ex) {
//	    result.addError(ex.getMessage());
//	}

	for (RoleTag tv : r1c.GetTaggedValues()) {
	    RoleTag tv2 = r2c.GetTaggedValues().AddNew(tv.GetTag(), tv.GetValue());
	    if (!tv2.Update()) {
		result.addError("Fehler beim Clonen von Relation '" + supertype.GetName() + "'/'" + subtype.GetName()
			+ "'-'" + e3.GetName() + "': " + tv.GetLastError());
	    }
	    r2c.GetTaggedValues().Refresh();

	}

	for (RoleTag tv : r1s.GetTaggedValues()) {
	    RoleTag tv2 = r2s.GetTaggedValues().AddNew(tv.GetTag(), tv.GetValue());
	    if (!tv2.Update()) {
		result.addError("Fehler beim Clonen von Relation '" + supertype.GetName() + "'/'" + subtype.GetName()
			+ "'-'" + e3.GetName() + "': " + tv.GetLastError());
	    }
	    r2s.GetTaggedValues().Refresh();

	}

	String r2cRoleName = r2c.GetRole();
	String r2sRoleName = r2s.GetRole();
	if (associationWithSupertype.GetClientID() == supertype.GetElementID()) {
	    updateTaggedValueRole(r2c, "sequenceNumber", determineSequenceNumber(r2cRoleName),
		    determineForceSequenceNumberTagUpdate(r2cRoleName, true));
	    updateTaggedValueRole(r2s, "sequenceNumber", determineSequenceNumber(r2sRoleName),
		    determineForceSequenceNumberTagUpdate(r2sRoleName, false));
	} else {
	    updateTaggedValueRole(r2c, "sequenceNumber", determineSequenceNumber(r2cRoleName),
		    determineForceSequenceNumberTagUpdate(r2cRoleName, false));
	    updateTaggedValueRole(r2s, "sequenceNumber", determineSequenceNumber(r2sRoleName),
		    determineForceSequenceNumberTagUpdate(r2sRoleName, true));
	}

	r2c.Update();
	r2s.Update();
    }

    private boolean determineForceSequenceNumberTagUpdate(String roleName, boolean defaultForForce) {
	return defaultForForce || specialRoleHandler.forceSequenceNumberTagUpdate(roleName);
    }

    private String determineSequenceNumber(String roleName) {

	Optional<String> specialSequenceNumberOpt = specialRoleHandler.determineSpecialSequenceNumber(roleName);

	if (specialSequenceNumberOpt.isPresent()) {
	    return specialSequenceNumberOpt.get();
	} else {
	    return Integer.valueOf(SeqNo++).toString();
	}
    }

//    protected void schemaLocationOfPackage(String name, String locprefix) {
//	EAPackage eaPkg = allPackages.get(name);
//	Package p = rep.GetPackageByID(eaPkg.getPkgId());
//	if (p != null) {
//	    Element e = p.GetElement();
//	    Collection<org.sparx.TaggedValue> cTV = e.GetTaggedValues();
//	    org.sparx.TaggedValue tv = cTV.GetByName("xsdDocument");
//	    if (tv == null) {
//		result.addError("TaggedValue 'xsdDocument' nicht vorhanden bei Paket '" + e.GetName() + "'");
//	    } else {
//		String v2 = tv.GetValue();
//		tv.SetValue(locprefix + v2);
//		if (!tv.Update()) {
//		    result.addError("Fehler beim Setzen von TaggedValue 'xsdDocument'-'" + locprefix + v2 + "': "
//			    + tv.GetLastError());
//		} else {
//		    result.addDebug(
//			    "Setzen von TaggedValue 'xsdDocument'-'" + locprefix + v2 + "' (alter Wert: '" + v2 + "')");
//		}
//	    }
//	} else {
//	    result.addError("Package '" + name + "' nicht gefunden.");
//	}
//    }

    public void deletePackage(String name) {

	if (allPackages.containsKey(name)) {
	    EAPackage eaPkg = allPackages.get(name);
	    EARepositoryUtil.deletePackage(rep, eaPkg.getPkgId());

	    this.allPackages.remove(name);
	    this.gidPackages.remove(name);

	    // also remove classes and subpackages
	    this.removeClasses(this.gidClasses, eaPkg.getFullName());
	    this.removeClasses(this.allClasses, eaPkg.getFullName());

	    this.removePackages(this.gidPackages, eaPkg.getFullName());
	    this.removePackages(this.allPackages, eaPkg.getFullName());
	} else {
	    result.addError("Package '" + name + "' nicht gefunden.");
	}
    }

    private void removeClasses(HashMap<String, EAElement> classesByNameMap, String fullNamePrefix) {
	List<EAElement> classesToRemove = classesByNameMap.values().stream()
		.filter(elmt -> elmt.getFullName().startsWith(fullNamePrefix)).toList();
	for (EAElement e : classesToRemove) {
	    classesByNameMap.remove(e.getName());
	}
    }

    private void removePackages(HashMap<String, EAPackage> packagesByNameMap, String fullNamePrefix) {
	List<EAPackage> packagesToRemove = packagesByNameMap.values().stream()
		.filter(pkg -> pkg.getFullName().startsWith(fullNamePrefix)).toList();
	for (EAPackage p : packagesToRemove) {
	    packagesByNameMap.remove(p.getName());
	}
    }

    /*
     * unused private void movePackage(String name, String pname) {
     * org.sparx.Package p1 = allPackages.get(name); if (p1!=null) {
     * org.sparx.Package p2 = allPackages.get(pname); if (p2!=null) {
     * p1.SetParentID(p2.GetPackageID()); if (!p2.Update()) {
     * result.addError("Fehler beim Verschieben von Paket '"+name+"': "+p2.
     * GetLastError()); } else { result.addDebug("Paket '"+name+"' verschoben."); }
     * } else { result.addError("Paket '"+pname+"' nicht gefunden."); } } else {
     * result.addError("Paket '"+name+"' nicht gefunden."); } }
     */

    private void updateTaggedValueRole(ConnectorEnd ce, String name, String value, boolean force) {

	String existingTVValue = EAConnectorEndUtil.taggedValue(ce, name);

	if (existingTVValue == null) {

	    try {
		EAConnectorEndUtil.addTaggedValue(ce, new EATaggedValue(name, value));
		result.addDebug("Setzen von TaggedValue '" + name + "'-'" + value + "'");
	    } catch (EAException ex) {
		result.addError(
			"Fehler beim Setzen von TaggedValue '" + name + "'-'" + value + "': " + ex.getMessage());
	    }

	} else {
	    if (existingTVValue.equals(value)) {
		result.addDebug("Setzen von TaggedValue '" + name + "'-'" + value + "' (bestehender Wert)");
	    } else if (!force && StringUtils.isNotBlank(existingTVValue)) {
		result.addDebug("Setzen von TaggedValue '" + name + "'-'" + existingTVValue
			+ "' (alter Wert wird nicht geändert durch '" + value + "')");
	    } else {

		try {
		    EAConnectorEndUtil.updateTaggedValue(ce, name, value, false);
		    result.addDebug("Setzen von TaggedValue '" + name + "'-'" + value + "' (alter Wert: '"
			    + existingTVValue + "')");
		} catch (EAException ex) {
		    result.addError("Fehler beim Setzen von TaggedValue '" + name + "'-'" + value + "'/'"
			    + existingTVValue + "': " + ex.getMessage());
		}
	    }
	}
    }

    public void deleteClass(String name) {

	if (gidClasses.containsKey(name)) {
	    EAElement eaElmt = gidClasses.get(name);
	    Package parent = rep.GetPackageByID(eaElmt.getPackageId());
	    EAPackageUtil.deleteElement(parent, eaElmt.getElementId());

	    this.allClasses.remove(eaElmt.getName());
	    this.gidClasses.remove(eaElmt.getName());
	} else {
	    result.addError("Klasse '" + name + "' nicht gefunden.");
	}
    }

    private void deleteMethods(Element e) {

	// TODO nach EA utils verschieben

	while (e.GetMethods().GetCount() > 0) {
	    e.GetMethods().Delete((short) 0);
	    if (!e.Update()) {
		result.addError(
			"Fehler beim Löschen der Methoden von Klasse '" + e.GetName() + "': " + e.GetLastError());
	    } else {
		result.addDebug("Methode von Klasse '" + e.GetName() + "' gelöscht.");
	    }
	    e.GetMethods().Refresh();
	}
    }

    public void addAttribute(String cname, String name, String type, String sequenceNumber) {

	if (gidClasses.containsKey(cname)) {

	    EAElement eaElmt = gidClasses.get(cname);
	    Element e = rep.GetElementByID(eaElmt.getElementId());

	    EAElement typeElmt = gidClasses.containsKey(type) ? gidClasses.get(type) : allClasses.get(type);

	    try {
		Attribute att = EAElementUtil.createEAAttribute(e, name, null, null, null, null, false, false, false,
			null, false, null, type, typeElmt != null ? typeElmt.getElementId() : null);

		EAAttributeUtil.setTaggedValue(att, "sequenceNumber", sequenceNumber);

	    } catch (EAException ex) {
		result.addError("Fehler beim Ergänzen von Attribut '" + cname + "." + name + "': " + ex.getMessage());
	    }
	} else {
	    result.addError("Klasse '" + cname + "' nicht gefunden.");
	}
    }

    public void deleteAttribute(String cname, String attName) {

	if (gidClasses.containsKey(cname)) {

	    EAElement eaElmt = gidClasses.get(cname);
	    Element e = rep.GetElementByID(eaElmt.getElementId());
	    EAElementUtil.deleteAttribute(e, attName);
	} else {
	    result.addError("Klasse '" + cname + "' nicht gefunden.");
	}
    }

    public void changeTypeAndMultiplicity(String cname, String name, String tname, String lower, String upper) {

	if (gidClasses.containsKey(cname)) {

	    EAElement eaElmt = gidClasses.get(cname);
	    Element e = rep.GetElementByID(eaElmt.getElementId());

	    Attribute att = EAElementUtil.getAttributeByName(e, name);

	    if (att != null) {
		try {
		    EAAttributeUtil.setEAType(att, tname);
		    EAElement typeElmt = gidClasses.containsKey(tname) ? gidClasses.get(tname) : allClasses.get(tname);
		    EAAttributeUtil.setEAClassifierID(att, typeElmt == null ? 0 : typeElmt.getElementId());
		    result.addDebug("Typ von Attribut '" + cname + "." + name + "' geändert.");
		} catch (EAException ex) {
		    result.addError("Fehler beim Setzen des Typs von Attribut '" + cname + "." + name + "': "
			    + ex.getMessage());
		}

		try {
		    EAAttributeUtil.setEALowerBound(att, lower);
		    EAAttributeUtil.setEAUpperBound(att, upper);
		    result.addDebug("Multiplizität von Attribut '" + cname + "." + name + "' geändert.");
		} catch (EAException ex) {
		    result.addError("Fehler beim Setzen der Multiplizität von Attribut '" + cname + "." + name + "': "
			    + ex.getMessage());
		}
	    }
	} else {
	    result.addError("Klasse '" + cname + "' nicht gefunden.");
	}
    }

    public void changeType(String cname, String name, String tname) {

	if (gidClasses.containsKey(cname)) {

	    EAElement eaElmt = gidClasses.get(cname);
	    Element e = rep.GetElementByID(eaElmt.getElementId());

	    Attribute att = EAElementUtil.getAttributeByName(e, name);

	    if (att != null) {
		try {
		    EAAttributeUtil.setEAType(att, tname);
		    EAElement typeElmt = gidClasses.containsKey(tname) ? gidClasses.get(tname) : allClasses.get(tname);
		    EAAttributeUtil.setEAClassifierID(att, typeElmt == null ? 0 : typeElmt.getElementId());
		    result.addDebug("Typ von Attribut '" + cname + "." + name + "' geändert.");
		} catch (EAException ex) {
		    result.addError("Fehler beim Setzen des Typs von Attribut '" + cname + "." + name + "': "
			    + ex.getMessage());
		}
	    }
	} else {
	    result.addError("Klasse '" + cname + "' nicht gefunden.");
	}
    }

    public void deleteRole(String cname, String name) {

	// TODO move deletion of role to EA utils classes

	EAElement eaElmt = gidClasses.get(cname);
	Element e = rep.GetElementByID(eaElmt.getElementId());
	if (e != null) {
	    Connector ei;
	    Collection<Connector> c = e.GetConnectors();
	    for (short i = 0; i < c.GetCount(); i++) {
		ei = c.GetAt(i);
		if (ei.GetClientEnd().GetRole().equals(name) || ei.GetSupplierEnd().GetRole().equals(name)) {
		    c.Delete(i);
		    if (!e.Update()) {
			result.addError(
				"Fehler beim Löschen von Rolle '" + cname + "." + name + "': " + e.GetLastError());
		    } else {
			result.addDebug("Rolle '" + cname + "." + name + "' gelöscht.");
		    }
		    c.Refresh();
		    break;
		}
	    }
	} else {
	    result.addError("Klasse '" + cname + "' nicht gefunden.");
	}
    }

    /**
     * @param implSchemaNameByAppSchemaFullName Map of the application schemas to
     *                                          process (key: fully qualified
     *                                          application schema name; value: the
     *                                          name for the implementation schema
     *                                          to generate)
     * @param relevantDependenciesToLoad        Fully qualified name (in the model)
     *                                          of relevant schema dependency
     *                                          packages to load
     * @throws ShapeChangeAbortException tbd
     */
    public void prepareModel(SortedMap<String, String> implSchemaNameByAppSchemaFullName,
	    SortedSet<String> relevantDependenciesToLoad) throws ShapeChangeAbortException {

	// 0. Scan the EA repository

	this.eaRepo = new EARepository(rep, true);

	// 1. create clones of relevant application schemas

	SortedSet<String> implSchemasFullName = new TreeSet<>();

	for (Entry<String, String> e : implSchemaNameByAppSchemaFullName.entrySet()) {

	    String appSchemaFullName = e.getKey();
	    String implSchemaName = e.getValue();

	    Optional<EAPackage> appSchemaPkgOpt = eaRepo.lookupPackage(appSchemaFullName);

	    if (appSchemaPkgOpt.isPresent()) {

		EAPackage appSchemaPkg = appSchemaPkgOpt.get();

		Package p = rep.GetPackageByID(appSchemaPkg.getPkgId());

		// Changed from parsing tag 'version' to parsing tag 'GID:AAAVersion'
		TaggedValue tv = p.GetElement().GetTaggedValues().GetByName("GID:AAAVersion");
		if (tv != null) {
		    aaaVersionTagValues.add(tv.GetValue());
		}

		Package appSchemaClone = p.Clone();

		if (appSchemaClone == null) {

		    result.addFatalError("Fehler beim Klonen des Anwendungsschemas " + appSchemaFullName
			    + ". Ggf. ist das AdV-Paket noch mit dem SVN verbunden.");
		    throw new ShapeChangeAbortException();

		} else {

		    appSchemaClone.SetName(implSchemaName);

		    if (!appSchemaClone.Update()) {
			result.addError("Fehler beim Klonen des Anwendungsschemas " + appSchemaFullName + ": "
				+ p.GetLastError());
			throw new ShapeChangeAbortException();
		    }

		    String implSchemaFullName = StringUtils.substringBeforeLast(appSchemaFullName,
			    appSchemaPkg.getName()) + implSchemaName;
		    implSchemasFullName.add(implSchemaFullName);
		}

	    } else {
		result.addError("Anwendungsschema wurde nicht gefunden: " + appSchemaFullName);
	    }
	}

	/*
	 * 2. Read EARepository again, to take into account the cloned packages, and
	 * this time also elements contained in packages.
	 */

	this.eaRepo = new EARepository(rep, false);

	/*
	 * 3. Prepare packages for relevant application schemas, i.e., the cloned
	 * packages that are going to be transformed to implementation schemas.
	 */

	for (String implSchemaFullName : implSchemasFullName) {

	    Optional<EAPackage> implSchemaPkgOpt = eaRepo.lookupPackage(implSchemaFullName);

	    if (implSchemaPkgOpt.isPresent()) {

		EAPackage implSchemaPkg = implSchemaPkgOpt.get();

		Package p = rep.GetPackageByID(implSchemaPkg.getPkgId());

		preparePackage(p, true);
	    }
	}

	// 4. Prepare packages for relevant dependencies.

	for (String depFullName : relevantDependenciesToLoad) {

	    Optional<EAPackage> depSchemaPkgOpt = eaRepo.lookupPackage(depFullName);

	    if (depSchemaPkgOpt.isPresent()) {

		EAPackage depSchemaPkg = depSchemaPkgOpt.get();

		Package p = rep.GetPackageByID(depSchemaPkg.getPkgId());

		preparePackage(p, false);
	    }
	}
    }

    private void preparePackage(Package p, boolean gid) throws ShapeChangeAbortException {

	for (org.sparx.Package p2 : p.GetPackages()) {
	    preparePackage(p2, gid);
	}

	commonPackagePreparations(p, gid);
    }

    private void commonPackagePreparations(Package p, boolean gid) {

	EAPackage eaPkg = eaRepo.lookupPackageByElementId(p.GetElement().GetElementID()).get();
	// ... remember package by name
	String s = p.GetName();
	if (allPackages.containsKey(s)) {
	    result.addInfo("Information: Paket '" + s + "' mehrfach vorhanden.");
	} else {
	    allPackages.put(s, eaPkg);
	}
	if (gid) {
	    gidPackages.put(s, eaPkg);
	}

	Collection<Element> c = p.GetElements();
	c.Refresh();
	for (Element e : c) {
	    String type = e.GetType();
	    EAElement eaElmt = eaRepo.lookupElement(e.GetElementID()).get();
	    if (type.equalsIgnoreCase("class") || type.equalsIgnoreCase("enumeration")
		    || type.equalsIgnoreCase("datatype")) {
		s = e.GetName();
		if (allClasses.containsKey(s)) {
		    result.addInfo("Information: Classifier '" + s + "' mehrfach vorhanden.");
		} else {
		    allClasses.put(s, eaElmt);
		}
		if (gid) {
		    if (gidClasses.containsKey(s)) {
			result.addError(
				"Information: Classifier '" + s + "' in Implementierungsschemas mehrfach vorhanden.");
		    } else {
			gidClasses.put(s, eaElmt);
		    }
		}
	    }
	}
    }

    public void updateTaggedValue(String className, String attributeName, String tagName, String tagValue) {

	if (gidClasses.containsKey(className)) {
	    EAElement eaElmt = gidClasses.get(className);
	    Element e = rep.GetElementByID(eaElmt.getElementId());

	    Attribute att = EAElementUtil.getAttributeByName(e, attributeName);

	    if (att != null) {
		try {
		    EAAttributeUtil.updateTaggedValue(att, tagName, tagValue, false);
		    result.addDebug("Tag '" + tagName + "' für Attribut '" + className + "." + attributeName
			    + "' auf Wert '" + tagValue + "' gesetzt.");
		} catch (EAException ex) {
		    result.addError("Fehler beim Setzen des Tag '" + tagName + "' auf Attribut '" + className + "."
			    + attributeName + "': " + ex.getMessage());
		}
	    }
	} else {
	    result.addError("Klasse '" + className + "' nicht gefunden.");
	}
    }

}
