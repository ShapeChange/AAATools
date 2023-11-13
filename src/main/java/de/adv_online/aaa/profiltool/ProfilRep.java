/**
 * AAA-Profiltool
 *
 * The class in this file implements the ShapeChange Target interface to 
 * generate and load the 3AP files.
 *
 * (c) 2009-2012 Arbeitsgemeinschaft der Vermessungsverwaltungen der 
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

package de.adv_online.aaa.profiltool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import de.interactive_instruments.ShapeChange.Options;
import de.interactive_instruments.ShapeChange.ShapeChangeResult;
import de.interactive_instruments.ShapeChange.Model.ClassInfo;
import de.interactive_instruments.ShapeChange.Model.Info;
import de.interactive_instruments.ShapeChange.Model.Model;
import de.interactive_instruments.ShapeChange.Model.PackageInfo;
import de.interactive_instruments.ShapeChange.Model.PropertyInfo;
import de.interactive_instruments.ShapeChange.Model.EA.ClassInfoEA;
import de.interactive_instruments.ShapeChange.Model.EA.PackageInfoEA;
import de.interactive_instruments.ShapeChange.Model.EA.PropertyInfoEA;

public class ProfilRep {

    private PackageInfo pi = null;
    private Model model = null;
    private Options options = null;
    private ShapeChangeResult result = null;
    private String modellart = "";
    private String profil = "";
    private boolean gdb = false;
    private String aaaZielVersion = "6.0.1";
    private boolean set = false;

    private HashSet<ClassInfo> processed = new HashSet<ClassInfo>();
    private HashSet<ClassInfo> written = new HashSet<ClassInfo>();
    private HashSet<Info> Members = new HashSet<Info>();

    public ProfilRep(PackageInfo p, Model m, Options o, ShapeChangeResult r, String ma, String n, boolean g, String v) {
	pi = p;
	model = m;
	options = o;
	result = r;
	modellart = ma;
	aaaZielVersion = v;
	profil = n;
	gdb = g;

	if (!setModellartOnly())
	    for (ClassInfo ci : model.classes(pi)) {
		SetClass(ci, false);
	    }

	set = true;
    }

    public ProfilRep(PackageInfo p, Model m, Options o, ShapeChangeResult r, String ma, String n, String v) {
	pi = p;
	model = m;
	options = o;
	result = r;
	modellart = ma;
	aaaZielVersion = v;
	profil = n;
	gdb = false;

	for (ClassInfo ci : model.classes(pi)) {
	    LoadClass(ci);
	}

	set = true;
    }

    private boolean setModellartOnly() {
	return profil.isEmpty();
    }

    public boolean isSet() {
	return set;
    }

    private void LoadClass(ClassInfo ci) {
	int cat = ci.category();
	switch (cat) {
	case Options.UNKNOWN:
	case Options.FEATURE:
	case Options.UNION:
	case Options.DATATYPE:
	case Options.MIXIN:
	case Options.OBJECT:
	    if (!processed.contains(ci)) {
		processed.add(ci);
		if (inModel(ci)) {
		    Members.add(ci);
		    for (String st : ci.supertypes()) {
			ClassInfo cis = model.classById(st);
			if (cis != null && cis.inSchema(pi) && !processed.contains(cis)) {
			    LoadClass(cis);
			}
		    }
		    for (PropertyInfo propi : ci.properties().values()) {
			if (inModel(propi)) {
			    Members.add(propi);
			    // Bei einer Relation auch die inverse Rolle
			    PropertyInfo rpropi = propi.reverseProperty();
			    if (rpropi != null) {
				Members.add(rpropi);
			    }
			}
		    }
		}
	    }
	    break;
	case Options.ENUMERATION:
	case Options.CODELIST:
	    if (!processed.contains(ci)) {
		processed.add(ci);
		if (inModel(ci)) {
		    Members.add(ci);
		    for (PropertyInfo propi : ci.properties().values()) {
			if (inModel(propi)) {
			    Members.add(propi);
			}
		    }
		}
	    }
	    break;
	default:
	    result.addWarning("Klasse mit unbekannter Art wurde nicht verarbeitet: " + ci.name());
	    break;
	}
    }

    private boolean inModel(Info i) {
	String s1 = i.taggedValue(tag());
	if (s1 == null || s1.trim().length() == 0)
	    return setModellartOnly();
	String[] sa = s1.split(",");
	for (String s2 : sa) {
	    if (s2.trim().equals(name()))
		return true;
	}
	return false;
    }

    private String tag() {
	return (setModellartOnly() ? "AAA:Modellart" : "AAA:Profile");
    }

    private void addToModel(Info i) {
	String s1 = i.taggedValue(tag());
	if (s1 == null || s1.trim().length() == 0) {
	    // only add to AAA:Profile, for AAA:Modellart a blank value means all
	    // Modellarten are included
	    if (!setModellartOnly()) {
		if (i.getClass().getName().equals("de.interactive_instruments.ShapeChange.Model.EA.PackageInfoEA")) {
		    PackageInfoEA iea = (PackageInfoEA) i;
		    iea.taggedValue(tag(), name());
		} else if (i.getClass().getName()
			.equals("de.interactive_instruments.ShapeChange.Model.EA.ClassInfoEA")) {
		    ClassInfoEA iea = (ClassInfoEA) i;
		    iea.taggedValue(tag(), name());
		} else if (i.getClass().getName()
			.equals("de.interactive_instruments.ShapeChange.Model.EA.PropertyInfoEA")) {
		    PropertyInfoEA iea = (PropertyInfoEA) i;
		    iea.taggedValue(tag(), name());
		} else {
		    result.addInfo("Unsupported Java class: " + i.getClass().getName());
		}
	    }
	    return;
	}
	String[] sa = s1.split(",");
	for (String s2 : sa) {
	    if (s2.trim().equals(name()))
		return;
	}
	if (i.getClass().getName().equals("de.interactive_instruments.ShapeChange.Model.EA.PackageInfoEA")) {
	    PackageInfoEA iea = (PackageInfoEA) i;
	    iea.taggedValue(tag(), s1 + "," + name());
	} else if (i.getClass().getName().equals("de.interactive_instruments.ShapeChange.Model.EA.ClassInfoEA")) {
	    ClassInfoEA iea = (ClassInfoEA) i;
	    iea.taggedValue(tag(), s1 + "," + name());
	} else if (i.getClass().getName().equals("de.interactive_instruments.ShapeChange.Model.EA.PropertyInfoEA")) {
	    PropertyInfoEA iea = (PropertyInfoEA) i;
	    iea.taggedValue(tag(), s1 + "," + name());
	} else {
	    result.addInfo("Unsupported Java class: " + i.getClass().getName());
	}
    }

    private void removeFromModel(Info i) {
	String s1 = i.taggedValue(tag());
	if (s1 == null || s1.trim().length() == 0)
	    return;
	String[] sa = s1.split(",");
	s1 = "";
	boolean found = false;
	for (String s2 : sa) {
	    if (!s2.trim().equals(name())) {
		if (s1.length() == 0)
		    s1 = s2;
		else
		    s1 += "," + s2;
	    } else
		found = true;
	}
	if (!found)
	    return; // nothing to do
	if (i.getClass().getName().equals("de.interactive_instruments.ShapeChange.Model.EA.PackageInfoEA")) {
	    PackageInfoEA iea = (PackageInfoEA) i;
	    iea.taggedValue(tag(), s1);
	} else if (i.getClass().getName().equals("de.interactive_instruments.ShapeChange.Model.EA.ClassInfoEA")) {
	    ClassInfoEA iea = (ClassInfoEA) i;
	    iea.taggedValue(tag(), s1);
	} else if (i.getClass().getName().equals("de.interactive_instruments.ShapeChange.Model.EA.PropertyInfoEA")) {
	    PropertyInfoEA iea = (PropertyInfoEA) i;
	    iea.taggedValue(tag(), s1);
	} else {
	    result.addInfo("Unsupported Java class: " + i.getClass().getName());
	}
    }

    public boolean contains(Info i) {
	if (Members.contains(i))
	    return true;
	return false;
    }

    private boolean MatchingMA(Info i, boolean g) {
	String malist;
	if (g) {
	    malist = i.taggedValue("AAA:Grunddatenbestand");
	    if (malist == null)
		return false;
	    malist = malist.trim();
	    if (malist.length() == 0)
		return false;
	} else {
	    malist = i.taggedValue("AAA:Modellart");
	    if (malist == null)
		return true;
	    malist = malist.trim();
	    if (malist.length() == 0)
		return true;
	}

	for (String ma : malist.split(",")) {
	    if (ma.trim().equals(modellart)) {
		return true;
	    }
	}

	return false;
    }

    private boolean ExportItem(Info i) {
	if (i.name().length() == 0)
	    return false;

	if (!MatchingMA(i, false))
	    return false;

	if (gdb && !MatchingMA(i, true))
	    return false;

	return true;
    }

    private void SetClass(ClassInfo ci, boolean force) {
	boolean export = force || ExportItem(ci);
	int cat = ci.category();
	switch (cat) {
	case Options.UNKNOWN:
	case Options.FEATURE:
	case Options.UNION:
	case Options.DATATYPE:
	case Options.MIXIN:
	case Options.OBJECT:
	    if (export && !processed.contains(ci)) {
		processed.add(ci);
		Members.add(ci);
		for (String st : ci.supertypes()) {
		    ClassInfo cis = model.classById(st);
		    if (cis != null && cis.inSchema(pi) && !processed.contains(cis)) {
			SetClass(cis, true);
		    }
		}
		for (PropertyInfo propi : ci.properties().values()) {
		    SetProperty(propi, false, false);
		}
	    }
	    break;
	case Options.ENUMERATION:
	case Options.CODELIST:
	    if (export && !processed.contains(ci)) {
		processed.add(ci);
		Members.add(ci);
		for (PropertyInfo propi : ci.properties().values()) {
		    SetProperty(propi, false, true);
		}
	    }
	    break;
	default:
	    result.addWarning("Klasse mit unbekannter Art wurde nicht verarbeitet: " + ci.name());
	    break;
	}
    }

    private void ClassRequired(String classId) {
	ClassInfo ci = model.classById(classId);
	if (ci != null) {
	    int cat = ci.category();
	    switch (cat) {
	    case Options.UNKNOWN:
	    case Options.UNION:
	    case Options.DATATYPE:
	    case Options.MIXIN:
	    case Options.FEATURE:
	    case Options.OBJECT:
	    case Options.ENUMERATION:
	    case Options.CODELIST:
		SetClass(ci, true);
		break;
	    default:
		result.addWarning("Klasse mit unbekannter Art wurde nicht verarbeitet: " + ci.name());
		break;
	    }
	}
    }

    private void SetProperty(PropertyInfo propi, boolean force, boolean codedValue) {
	boolean export = (force || (ExportItem(propi) && !setModellartOnly()));
	if (!codedValue)
	    export = export || propi.cardinality().minOccurs > 0;
	export = export && propi.name().length() > 0;
	if (export) {
	    Members.add(propi);
	    if (!codedValue) {
		ClassRequired(propi.typeInfo().id);
		if (!propi.isAttribute() && propi.isNavigable()) {
		    // Bei einer Relation sind auch alle inversen Rollen automatisch Teil des
		    // Profils
		    PropertyInfo rpropi = propi.reverseProperty();
		    if (rpropi != null) {
			Members.add(rpropi);
		    }
		}
	    } else if (!Members.contains(propi.inClass()))
		Members.add(propi.inClass());
	}
    }

    public void writeToFile(String filename) {
	if (!set) {
	    result.addError("Export der Profil- oder Modellartdatei nicht möglich, da keine Definition vorliegt.");
	    return;
	}

	try {
	    PrintWriter out = null;
	    written.clear();
	    try {
		out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8));
		if (setModellartOnly())
		    out.println("AFIS-ALKIS-ATKIS-Modellartdefinition");
		else
		    out.println("AFIS-ALKIS-ATKIS-Profildefinition");
		out.println("Version: " + aaaZielVersion);
		if (setModellartOnly())
		    out.println("Name: " + modellart);
		else
		    out.println("Name: " + modellart + "_" + profil);
		out.println("#");

		Set<ClassInfo> classes = model.classes(pi);
		ClassInfo[] classesArr = new ClassInfo[classes.size()];
		classes.toArray(classesArr);
		Arrays.sort(classesArr, new Comparator<ClassInfo>() {
		    public int compare(ClassInfo ci1, ClassInfo ci2) {
			return ci1.name().compareTo(ci2.name());
		    }
		});
		for (int cidx = 0; cidx < classesArr.length; cidx++) {
		    ClassInfo k = classesArr[cidx];
		    DumpClass(out, k);
		}
	    } finally {
		if (out != null)
		    out.close();
		if (setModellartOnly())
		    result.addInfo("Schreiben der Modellartdatei abgeschlossen.");
		else
		    result.addInfo("Schreiben der Profildatei abgeschlossen.");
	    }
	} catch (IOException e) {
	    if (setModellartOnly())
		result.addError("Problem beim Schreiben von Modellartdatei: " + filename);
	    else
		result.addError("Problem beim Schreiben von Profildatei: " + filename);
	    e.printStackTrace();
	}
    }

    private void AddEntry(PrintWriter out, ClassInfo i, boolean included) {
	String s = i.name() + " --- Objektart/Datentyp";
	if (!included)
	    s = "# " + s;
	out.println(s);
	result.addDebug(s);
    }

    private void AddEntry(PrintWriter out, PropertyInfo i, boolean included, boolean codedValue) {
	String s = i.inClass().name() + "/";
	if (codedValue) {
	    String iv = i.initialValue();
	    if (iv != null && iv.trim().length() > 0)
		s += iv.trim();
	    else
		s += i.name();
	} else {
	    s += i.name();
	}
	int cat = i.inClass().category();
	if (cat == Options.ENUMERATION || cat == Options.CODELIST)
	    s += " --- Werteart";
	else if (i.isAttribute())
	    s += " --- Attributart";
	else
	    s += " --- Relationsart";
	if (!included)
	    s = "# " + s;
	out.println(s);
	result.addDebug(s);
    }

    private void DumpProperty(PrintWriter out, PropertyInfo propi, boolean codedValue) {
	if (propi.name().length() == 0)
	    return;
	if (Members.contains(propi)) {
	    AddEntry(out, propi, true, codedValue);
	} else if (MatchingMA(propi, false) || setModellartOnly()) {
	    AddEntry(out, propi, false, codedValue);
	}
    }

    private void DumpClass(PrintWriter out, ClassInfo ci) {
	int cat = ci.category();
	switch (cat) {
	case Options.MIXIN:
	case Options.UNKNOWN:
	case Options.FEATURE:
	case Options.UNION:
	case Options.DATATYPE:
	case Options.OBJECT:
	    if (!written.contains(ci)) {
		if (Members.contains(ci)) {
		    AddEntry(out, ci, true);
		} else if (MatchingMA(ci, false) || setModellartOnly()) {
		    AddEntry(out, ci, false);
		}
		written.add(ci);
		for (PropertyInfo propi : ci.properties().values()) {
		    DumpProperty(out, propi, false);
		}
	    }
	    break;
	case Options.ENUMERATION:
	case Options.CODELIST:
	    if (!written.contains(ci)) {
		written.add(ci);
		for (PropertyInfo propi : ci.properties().values()) {
		    DumpProperty(out, propi, true);
		}
		break;
	    }
	default:
	    result.addWarning("Klasse mit unbekannter Art wurde nicht verarbeitet: " + ci.name());
	    break;
	}
    }

    public void writeToModel() {
	if (!set) {
	    result.addError(
		    "Übernahme des Profils oder der Modellart in das Modell nicht möglich, da keine Definition vorliegt.");
	    return;
	}

	written.clear();
	Set<ClassInfo> classes = model.classes(pi);
	for (ClassInfo k : classes) {
	    WriteClassToModel(k);
	}
    }

    private void WritePropertyToModel(PropertyInfo propi) {
	if (propi.name().length() == 0)
	    return;
	if (Members.contains(propi)) {
	    addToModel(propi);
	} else if (MatchingMA(propi, false) || setModellartOnly()) {
	    removeFromModel(propi);
	}
	// Bei einer Relation sind auch alle inversen Rollen automatisch Teil des
	// Profils
	PropertyInfo rpropi = propi.reverseProperty();
	if (rpropi != null) {
	    if (Members.contains(rpropi)) {
		addToModel(rpropi);
	    } else if (MatchingMA(rpropi, false) || setModellartOnly()) {
		removeFromModel(rpropi);
	    }
	}
    }

    private void WriteClassToModel(ClassInfo ci) {
	int cat = ci.category();
	switch (cat) {
	case Options.MIXIN:
	case Options.UNKNOWN:
	case Options.FEATURE:
	case Options.UNION:
	case Options.DATATYPE:
	case Options.OBJECT:
	    if (!written.contains(ci)) {
		if (Members.contains(ci)) {
		    addToModel(ci);
		} else if (MatchingMA(ci, false) || setModellartOnly()) {
		    removeFromModel(ci);
		}
		written.add(ci);
		for (PropertyInfo propi : ci.properties().values()) {
		    WritePropertyToModel(propi);
		}
	    }
	    break;
	case Options.ENUMERATION:
	case Options.CODELIST:
	    if (!written.contains(ci)) {
		if (Members.contains(ci)) {
		    addToModel(ci);
		} else if (MatchingMA(ci, false) || setModellartOnly()) {
		    removeFromModel(ci);
		}
		written.add(ci);
		for (PropertyInfo propi : ci.properties().values()) {
		    WritePropertyToModel(propi);
		}
		break;
	    }
	default:
	    result.addWarning("Klasse mit unbekannter Art wurde nicht verarbeitet: " + ci.name());
	    break;
	}
    }

    public void clearInModel() {
	Set<ClassInfo> classes = model.classes(pi);
	for (ClassInfo ci : classes) {
	    removeFromModel(ci);
	    for (PropertyInfo propi : ci.properties().values()) {
		removeFromModel(propi);
		// Bei einer Relation auch die inverse Rolle
		PropertyInfo rpropi = propi.reverseProperty();
		if (rpropi != null) {
		    removeFromModel(rpropi);
		}
	    }
	}
    }

    public String name() {
	if (setModellartOnly())
	    return modellart;
	else
	    return modellart + "_" + profil;
    }

    public ProfilRep(PackageInfo p, Model m, Options o, ShapeChangeResult r, String v, String filename) {
	pi = p;
	model = m;
	options = o;
	result = r;
	aaaZielVersion = v;

	try {
	    boolean error = false;
	    BufferedReader input = new BufferedReader(new FileReader(filename, StandardCharsets.UTF_8));
	    try {
		String line = null;

		line = input.readLine();
		if (line == null || !(line.contains("AFIS-ALKIS-ATKIS-Profildefinition")
			|| line.contains("AFIS-ALKIS-ATKIS-Modellartdefinition"))) {
		    result.addError(
			    "Die Profildatei beginnt nicht mit dem erwateten Wert, 'AFIS-ALKIS-ATKIS-Profildefinition' oder 'AFIS-ALKIS-ATKIS-Modellartdefinition'. Der Ladevorgang wurde abgebrochen.");
		    error = true;
		    return;
		}
		boolean martOnly = line.contains("AFIS-ALKIS-ATKIS-Modellartdefinition");

		line = input.readLine();
		if (line == null || !line.trim().startsWith("Version: ")) {
		    result.addError("Es wurde keine Version angegeben. Der Ladevorgang wurde abgebrochen.");
		    error = true;
		    return;
		} else {
		    String s = line.trim().substring(9);
		    if (!s.equals(aaaZielVersion)) {
			result.addError("Die Version " + s + " des Profils passt nicht zur erwarteten Version "
				+ aaaZielVersion + ". Der Ladevorgang wurde abgebrochen.");
			error = true;
			return;
		    }
		}

		line = input.readLine();
		if (line == null || !line.trim().startsWith("Name: ")) {
		    result.addError("Es wurde kein Name angegeben. Der Ladevorgang wurde abgebrochen.");
		    error = true;
		    return;
		} else {
		    String s = line.trim().substring(6);
		    if (s.contains("_")) {

			/*
			 * Prüfe, dass der Dateiname mit dem in der Datei angegebenen Namen
			 * übereinstimmt.
			 */
			File profileFile = new File(filename);
			String profileFileBaseName = FilenameUtils.getBaseName(profileFile.getName());

			if (!profileFileBaseName.equals(s)) {
			    result.addError("Der Name der Profildatei ('" + profileFileBaseName
				    + "') entspricht nicht dem in der Datei angegebenen Namen ('" + s
				    + "'). Der Ladevorgang wurde abgebrochen.");
			    error = true;
			    return;
			} else {
			    String[] ss = s.split("_", 2);
			    modellart = ss[0].trim();
			    profil = ss[1].trim();
			}

		    } else if (martOnly) {
			modellart = s.trim();
			profil = "";
		    } else {
			result.addError("Der Name '" + s + "' ist ungültig. Der Ladevorgang wurde abgebrochen.");
			error = true;
			return;
		    }
		}

		if (!setModellartOnly()) {
		    gdb = true;
		    for (ClassInfo ci : model.classes(pi)) {
			SetClass(ci, false);
		    }
		}

		while ((line = input.readLine()) != null) {
		    line = line.trim();
		    if (!line.startsWith("#")) {
			String[] s = line.split(" --- ");
			if (s.length != 2) {
			    result.addError("Zeile mit unerlaubter Syntax: '" + line + "'");
			} else {
			    s[0] = s[0].trim();
			    s[1] = s[1].trim();
			    if (s[1].equals("Objektart/Datentyp")) {

				Optional<ClassInfo> cixOpt = lookupClass(s[0], pi);

				if (cixOpt.isPresent()) {
				    ClassInfo cix = cixOpt.get();
				    if (MatchingMA(cix, false) || setModellartOnly()) {
					SetClass(cix, true);
				    } else {
					result.addError("Objektart/Datentyp " + s[0]
						+ " kann dem Profil nicht zugeordnet werden, da es kein Bestandteil der betreffenden Modellart ist.");
				    }
				} else {
				    result.addError("Objektart/Datentyp " + s[0]
					    + " kann dem Profil oder der Modellart nicht zugeordnet werden, die Klasse wurde nicht im Anwendungsschema gefunden.");
				}

			    } else if (s[1].equals("Attributart") || s[1].equals("Relationsart")
				    || s[1].equals("Werteart")) {
				String[] s2 = s[0].split("/", 2);
				if (s2.length != 2) {
				    result.addError("Zeile mit unerlaubter Syntax: '" + line + "'");
				} else {
				    s2[0] = s2[0].trim();
				    s2[1] = s2[1].trim();

				    Optional<ClassInfo> cixOpt = lookupClass(s2[0], pi);

				    if (cixOpt.isPresent()) {
					ClassInfo cix = cixOpt.get();
					if (MatchingMA(cix, false) || setModellartOnly()) {
					    SetClass(cix, true);
					    boolean found = false;
					    for (PropertyInfo propi : cix.properties().values()) {
						String sx1 = propi.name();
						String sx2 = propi.initialValue();
						if (sx1 != null && sx1.toLowerCase().equals(s2[1].toLowerCase())) {
						    SetProperty(propi, true, false);
						    found = true;
						    break;
						} else if (s[1].equals("Werteart") && sx2 != null
							&& sx2.toLowerCase().equals(s2[1].toLowerCase())) {
						    SetProperty(propi, true, true);
						    found = true;
						    break;
						}
					    }
					    if (!found) {
						result.addError(s[1] + " " + s2[1] + " zu Objektart/Datentyp " + s2[0]
							+ " kann dem Profil nicht zugeordnet werden, die Eigenschaft wurde nicht im Anwendungsschema gefunden.");
					    }
					} else {
					    result.addError(s[1] + " " + s2[1] + " zu Objektart/Datentyp " + s2[0]
						    + " kann dem Profil nicht zugeordnet werden, da sie kein Bestandteil der betreffenden Modellart ist.");
					}

				    } else {
					result.addError("Objektart/Datentyp " + s2[0]
						+ " kann dem Profil nicht zugeordnet werden, die Klasse wurde nicht im Anwendungsschema gefunden.");
				    }
				}
			    }
			}
		    }
		}
	    } finally {
		input.close();
		if (!error) {
		    result.addInfo("Laden der Datei abgeschlossen.");
		    set = true;
		}
	    }
	} catch (IOException e) {
	    result.addError("Problem beim Lesen von Datei: " + filename);
	    e.printStackTrace();
	}
    }

    /**
     * This method looks up a class in the model by name, scoped to a particular
     * schema. The method can handle multiple classes with same name in the model!
     * 
     * @param className name of the class to look up (a model may contain multiple
     *                  classes with same name)
     * @param pi        package which defines the schema to which the class must
     *                  belong, too
     * @return class with given name and in same schema, or empty
     */
    private Optional<ClassInfo> lookupClass(String className, PackageInfo pi) {
	for (ClassInfo cix : model.classes()) {
	    if (cix.name().equals(className) && cix.inSchema(pi)) {
		return Optional.of(cix);
	    }
	}
	return Optional.empty();
    }
}
