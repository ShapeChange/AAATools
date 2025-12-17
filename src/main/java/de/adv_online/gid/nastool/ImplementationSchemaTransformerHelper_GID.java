/**
 * NAS-Tool (schema transformer)
 *
 * The class in this file implements the ShapeChange Target interface to 
 * generate and load the 3AP files.
 *
 * (c) 2009-2025 Arbeitsgemeinschaft der Vermessungsverwaltungen der 
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
import de.interactive_instruments.shapechange.ea.util.EATaggedValue;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EAElement;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EAPackage;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EARepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

public class ImplementationSchemaTransformerHelper_GID {

    // FIXME Store EAElements and EAPackages instead of Element and Package objects

    private Repository rep = null;
    private EARepository eaRepo = null;
    private ShapeChangeResult result = null;
    protected HashMap<String, Package> allPackages = new HashMap<String, Package>();
    protected List<Package> gidPackages = new ArrayList<>();
    public HashMap<String, Element> allClasses = new HashMap<String, Element>();
    public HashMap<String, Element> gidClasses = new HashMap<String, Element>();
    private int SeqNo = 32000;
    protected List<String> aaaVersionTagValues = new ArrayList<>();
//    private String path = null;
//    protected Package aaaPackage = null;

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

//	path = o.parameter("transformerTargetPath");
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
	for (Element e : gidClasses.values()) {
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
	    for (Element e : gidClasses.values()) {
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

	for (Package pkg : gidPackages) {

	    try {
		EAPackageUtil.updateTaggedValue(pkg, "xsdEncodingRule", "NAS", false);
	    } catch (EAException ex) {
		result.addError(ex.getMessage());
	    }
	}

	for (Element e : gidClasses.values()) {
	    String n = e.GetName();
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
	     * Nicht navigierbare Assoziationsrollen werden - navigierbar gesetzt - sofern
	     * nicht vorhanden mit dem Namen „inversZu_“ und den Namen der inversen Rolle
	     * versehen - mit einer minimalen Kardinalität von "0" versehen - der UML
	     * Tagged Value "reverseRoleNAS" wird auf „true“ gesetzt
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
	    e1.GetConnectors().Refresh();
	    Connector r = e1.GetConnectors().AddNew("", "Generalization");
	    if (r == null)
		result.addError(
			"Fehler beim Erzeugen der Generalisierung '" + e1.GetName() + "'-'" + e2.GetName() + "'");
	    else {
		r.SetSupplierID(e2.GetElementID());
		result.addDebug("Generalisierung '" + e1.GetName() + "'-'" + e2.GetName() + "' erzeugt");
		if (!r.Update()) {
		    result.addError("Fehler bei Erzeugung der Generalisierung '" + e1.GetName() + "'-'" + e2.GetName()
			    + "': " + r.GetLastError());
		}
	    }
	}
    }

    public void removeGeneralization(String sub, String sup) {
	org.sparx.Element e1 = allClasses.get(sub);
	org.sparx.Element e2 = allClasses.get(sup);
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

    protected void copyDown(Element e0, Element e) {
	if (e0.GetLocked()) {
	    result.addWarning("Element '" + e0.GetName() + "' ist gesperrt und wird ignoriert.");
	} /*
	   * FIXME else if (!gidClasses.containsValue(e0)) {
	   * result.addInfo("Element '"+e0.GetName()
	   * +"' ist nicht Teil des Anwendungsschemas und wird ignoriert."); }
	   */ else {
	    for (Connector r : e.GetConnectors()) {
		if (r.GetType().equals("Generalization") && r.GetSupplierID() == e.GetElementID()) {
		    Element e2 = rep.GetElementByID(r.GetClientID());
		    if (e2 == null) {
			// Nichts zu tun
		    } /*
		       * FIXME else if (!gidClasses.containsKey(e2.GetName())) {
		       * result.addInfo("Element '"+e2.GetName()
		       * +"' ist nicht Teil des Anwendungsschemas und wird ignoriert."); }
		       */ else {
			String st = e2.GetStereotype().toLowerCase();
			if (isTypeStereotype(st)) {
			    copyDown(e0, e2);
			} else if (!gidClasses.containsKey(e2.GetName())) {
			    result.addInfo("Element '" + e2.GetName()
				    + "' ist nicht Teil des Anwendungsschemas und wird ignoriert.");
			} else {
			    for (Attribute a : e0.GetAttributes()) {
				try {
				    cloneAttribute(a, e2);
				} catch (Exception ex) {
				    result.addError("Fehler beim Clonen von Attribut '" + a.GetName()
					    + "' (Zielklasse '" + e2.GetName() + "'): " + e2.GetLastError());
				}
			    }
			    SortedMap<String, Connector> map = new TreeMap<String, Connector>();
			    for (Connector r2 : e0.GetConnectors()) {
				String rt = r2.GetType();
				if (rt.equals("Association") || rt.equals("Aggregation")) {
				    String key = (r2.GetClientID() == e0.GetElementID()
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
				    cloneAssociation(r2, e0, e2);
				} catch (Exception ex) {
				    result.addError("Fehler beim Clonen von Relation '" + e0.GetName() + "'/'"
					    + e2.GetName() + "'");
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

    private void cloneAssociation(Connector r1, Element e1, Element e2) {
	Connector r2;
	Element e3;
	if (r1.GetClientID() == e1.GetElementID()) {
	    e3 = rep.GetElementByID(r1.GetSupplierID());
	    e2.GetConnectors().Refresh();
	    r2 = e2.GetConnectors().AddNew("", r1.GetType());
	    if (r2 == null) {
		result.addError("Fehler beim Clonen von Relation '" + e1.GetName() + "'/'" + e2.GetName() + "'-'"
			+ e3.GetName() + "'");
		return;
	    }
	    r2.SetSupplierID(e3.GetElementID());
	} else {
	    e3 = rep.GetElementByID(r1.GetClientID());
	    e3.GetConnectors().Refresh();
	    r2 = e3.GetConnectors().AddNew("", r1.GetType());
	    if (r2 == null) {
		result.addError("Fehler beim Clonen von Relation '" + e1.GetName() + "'/'" + e2.GetName() + "'-'"
			+ e3.GetName() + "'");
		return;
	    }
	    r2.SetSupplierID(e2.GetElementID());
	}

	r2.SetDirection("Bi-Directional");

	result.addDebug("Relation '" + e1.GetName() + "'/'" + e2.GetName() + "'-'" + e3.GetName() + "' geclont");
	if (!r2.Update()) {
	    result.addError("Fehler beim Clonen von Relation '" + e1.GetName() + "'/'" + e2.GetName() + "'-'"
		    + e3.GetName() + "': " + r2.GetLastError());
	}

	ConnectorEnd r1c, r1s, r2c, r2s;
	r1c = r1.GetClientEnd();
	r1s = r1.GetSupplierEnd();
	r2c = r2.GetClientEnd();
	r2s = r2.GetSupplierEnd();

	r2c.SetIsNavigable(r1c.GetIsNavigable());
	String s = r1c.GetCardinality();
	if (s.equals("1"))
	    s = "0..1";
	else if (s.startsWith("1"))
	    s = "0" + s.substring(1);
	r2c.SetCardinality(s);

	if (r1.GetClientID() == e1.GetElementID()) {
	    r2c.SetRole(r1c.GetRole() + "_" + e2.GetName());
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

	if (r1.GetClientID() == e1.GetElementID()) {
	    r2s.SetRole(r1s.GetRole());
	} else {
	    r2s.SetRole(r1s.GetRole() + "_" + e2.GetName());
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
		result.addError("Fehler beim Clonen von Relation '" + e1.GetName() + "'/'" + e2.GetName() + "'-'"
			+ e3.GetName() + "': " + tv.GetLastError());
	    }
	    r2c.GetTaggedValues().Refresh();

	}

	for (RoleTag tv : r1s.GetTaggedValues()) {
	    RoleTag tv2 = r2s.GetTaggedValues().AddNew(tv.GetTag(), tv.GetValue());
	    if (!tv2.Update()) {
		result.addError("Fehler beim Clonen von Relation '" + e1.GetName() + "'/'" + e2.GetName() + "'-'"
			+ e3.GetName() + "': " + tv.GetLastError());
	    }
	    r2s.GetTaggedValues().Refresh();

	}

	if (r1.GetClientID() == e1.GetElementID()) {
	    updateTaggedValueRole(r2c, "sequenceNumber", Integer.valueOf(SeqNo++).toString(), true);
	    updateTaggedValueRole(r2s, "sequenceNumber", Integer.valueOf(SeqNo++).toString(), false);
	} else {
	    updateTaggedValueRole(r2c, "sequenceNumber", Integer.valueOf(SeqNo++).toString(), false);
	    updateTaggedValueRole(r2s, "sequenceNumber", Integer.valueOf(SeqNo++).toString(), true);
	}

	r2c.Update();
	r2s.Update();
    }

    protected void schemaLocationOfPackage(String name, String locprefix) {
	org.sparx.Package p = allPackages.get(name);
	if (p != null) {
	    Element e = p.GetElement();
	    Collection<org.sparx.TaggedValue> cTV = e.GetTaggedValues();
	    org.sparx.TaggedValue tv = cTV.GetByName("xsdDocument");
	    if (tv == null) {
		result.addError("TaggedValue 'xsdDocument' nicht vorhanden bei Paket '" + e.GetName() + "'");
	    } else {
		String v2 = tv.GetValue();
		tv.SetValue(locprefix + v2);
		if (!tv.Update()) {
		    result.addError("Fehler beim Setzen von TaggedValue 'xsdDocument'-'" + locprefix + v2 + "': "
			    + tv.GetLastError());
		} else {
		    result.addDebug(
			    "Setzen von TaggedValue 'xsdDocument'-'" + locprefix + v2 + "' (alter Wert: '" + v2 + "')");
		}
	    }
	} else {
	    result.addError("Package '" + name + "' nicht gefunden.");
	}
    }

    public void deletePackage(String name) {
	org.sparx.Package e = allPackages.get(name);
	if (e != null) {
	    Package parent = rep.GetPackageByID(e.GetParentID());
	    Package ei;
	    Collection<Package> c = parent.GetPackages();
	    for (short i = 0; i < c.GetCount(); i++) {
		ei = c.GetAt(i);
		if (ei.GetPackageID() == e.GetPackageID()) {
		    c.Delete(i);
		    if (!parent.Update()) {
			result.addError("Fehler beim Löschen von Package '" + name + "': " + parent.GetLastError());
		    } else {
			result.addDebug("Package '" + name + "' gelöscht.");
		    }
		    c.Refresh();
		    break;
		}
	    }
	} else {
	    result.addError("Package '" + name + "' nicht gefunden.");
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
	org.sparx.RoleTag tv = null;

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
	org.sparx.Element e = gidClasses.get(name);
	if (e != null) {
	    Package parent = rep.GetPackageByID(e.GetPackageID());
	    Element ei;
	    Collection<Element> c = parent.GetElements();
	    for (short i = 0; i < c.GetCount(); i++) {
		ei = c.GetAt(i);
		if (ei.GetElementID() == e.GetElementID()) {
		    c.Delete(i);
		    if (!parent.Update()) {
			result.addError("Fehler beim Löschen von Klasse '" + name + "': " + parent.GetLastError());
		    } else {
			result.addDebug("Klasse '" + name + "' gelöscht.");
		    }
		    c.Refresh();
		    break;
		}
	    }
	} else {
	    result.addError("Klasse '" + name + "' nicht gefunden.");
	}
    }

    private void deleteMethods(Element e) {
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
	org.sparx.Element e = allClasses.get(cname);
	if (e != null) {
	    Attribute a = e.GetAttributes().AddNew(name, type);
	    if (!a.Update()) {
		result.addError("Fehler beim Ergänzen von Attribut '" + cname + "." + name + "': " + a.GetLastError());
	    } else {
		result.addDebug("Attribut '" + cname + "." + name + "' ergänzt.");
	    }
	    e.GetAttributes().Refresh();
	    try {
		EAAttributeUtil.setTaggedValue(a, "sequenceNumber", sequenceNumber);
	    } catch (EAException ex) {
		result.addError(ex.getMessage());
	    }
	} else {
	    result.addError("Klasse '" + cname + "' nicht gefunden.");
	}
    }

    public void deleteAttribute(String cname, String name) {
	org.sparx.Element e = allClasses.get(cname);
	if (e != null) {

	    // FIXME Extend EAElementUtil to cover attribute deletion?

	    Attribute ei;
	    Collection<Attribute> c = e.GetAttributes();
	    for (short i = 0; i < c.GetCount(); i++) {
		ei = c.GetAt(i);
		if (ei.GetName().equals(name)) {
		    c.Delete(i);
		    if (!e.Update()) {
			result.addError(
				"Fehler beim Löschen von Attribut '" + cname + "." + name + "': " + e.GetLastError());
		    } else {
			result.addDebug("Attribut '" + cname + "." + name + "' gelöscht.");
		    }
		    c.Refresh();
		    break;
		}
	    }
	} else {
	    result.addError("Klasse '" + cname + "' nicht gefunden.");
	}
    }

    public void changeTypeAndMultiplicity(String cname, String name, String tname, String lower, String upper) {
	org.sparx.Element e = allClasses.get(cname);
	if (e != null) {
	    Attribute ei;
	    Collection<Attribute> c = e.GetAttributes();
	    for (short i = 0; i < c.GetCount(); i++) {
		ei = c.GetAt(i);
		if (ei.GetName().equals(name)) {

		    try {
			EAAttributeUtil.setEAType(ei, tname);

			/*
			 * FIXME - eigentlich müsste man die erlaubten Dependencies durchgehen und
			 * korrekt verlinken; für die XSD-Ableitung ist das aber unerheblich
			 */
			EAAttributeUtil.setEAClassifierID(ei, 0);
			result.addDebug("Typ von Attribut '" + cname + "." + name + "' geändert.");
		    } catch (EAException ex) {
			result.addError("Fehler beim Setzen des Typs von Attribut '" + cname + "." + name + "': "
				+ ex.getMessage());
		    }

		    try {
			EAAttributeUtil.setEALowerBound(ei, lower);
			EAAttributeUtil.setEAUpperBound(ei, upper);
			result.addDebug("Multiplizität von Attribut '" + cname + "." + name + "' geändert.");
		    } catch (EAException ex) {
			result.addError("Fehler beim Setzen der Multiplizität von Attribut '" + cname + "." + name
				+ "': " + ex.getMessage());
		    }
		    break;
		}
	    }
	} else {
	    result.addError("Klasse '" + cname + "' nicht gefunden.");
	}
    }

    public void changeType(String cname, String name, String tname) {
	org.sparx.Element e = allClasses.get(cname);
	if (e != null) {
	    Attribute ei;
	    Collection<Attribute> c = e.GetAttributes();
	    for (short i = 0; i < c.GetCount(); i++) {
		ei = c.GetAt(i);
		if (ei.GetName().equals(name)) {
		    try {
			EAAttributeUtil.setEAType(ei, tname);
			EAAttributeUtil.setEAClassifierID(ei, 0);
			result.addDebug("Typ von Attribut '" + cname + "." + name + "' geändert.");
		    } catch (EAException ex) {
			result.addError("Fehler beim Setzen des Typs von Attribut '" + cname + "." + name + "': "
				+ ex.getMessage());
		    }
		    break;
		}
	    }
	} else {
	    result.addError("Klasse '" + cname + "' nicht gefunden.");
	}
    }

    public void deleteRole(String cname, String name) {
	org.sparx.Element e = allClasses.get(cname);
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

		// TBD: Changed from parsing tag 'version' to parsing tag 'AAA:Version'
		TaggedValue tv = p.GetElement().GetTaggedValues().GetByName("AAA:Version");
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

	// ... remember package by name
	String s = p.GetName();
	if (allPackages.containsKey(s)) {
	    result.addDebug("Information: Paket '" + s + "' mehrfach vorhanden.");
	} else {
	    allPackages.put(s, p);
	}
	if (gid) {
	    gidPackages.add(p);
	}

	Collection<Element> c = p.GetElements();
	c.Refresh();
	for (Element e : c) {
	    String type = e.GetType();
	    if (type.equalsIgnoreCase("class") || type.equalsIgnoreCase("enumeration")
		    || type.equalsIgnoreCase("datatype")) {
		s = e.GetName();
		if (allClasses.containsKey(s)) {
		    result.addDebug("Information: Classifier '" + s + "' mehrfach vorhanden.");
		} else {
		    allClasses.put(s, e);
		}
		if (gid) {
		    if (gidClasses.containsKey(s)) {
			result.addError(
				"Information: Classifier '" + s + "' in Implementierungsschemas mehrfach vorhanden.");
		    } else {
			gidClasses.put(s, e);
		    }
		}
	    }
	}
    }

}
