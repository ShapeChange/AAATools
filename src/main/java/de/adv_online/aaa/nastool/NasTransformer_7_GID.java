/**
 * NAS-Tool (schema transformer)
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

package de.adv_online.aaa.nastool;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.sparx.Attribute;
import org.sparx.Element;

import de.interactive_instruments.shapechange.core.Options;
import de.interactive_instruments.shapechange.core.ShapeChangeAbortException;
import de.interactive_instruments.shapechange.core.ShapeChangeResult;
import de.interactive_instruments.shapechange.core.model.Transformer;
import org.apache.commons.lang3.StringUtils;

public class NasTransformer_7_GID implements Transformer {

    private ShapeChangeResult result = null;
    private ImplementationSchemaTransformerHelper_GID helper = null;

    public void initialise(Options o, ShapeChangeResult r, String repositoryFileName) throws ShapeChangeAbortException {
	result = r;
	helper = new ImplementationSchemaTransformerHelper_GID();
	helper.initialise(o, r, repositoryFileName);
    }

    public void shutdown() {
	if (helper != null) {
	    helper.shutdown();
	}
    }

    /**
     * Das AAA-Anwendungsschema verwendet einige Konstruktionen in UML, die in den
     * Abbildungsregeln von ISO 19136 Annex E und ISO/TS 19139 nicht unterstützt
     * werden. Daher erfolgt eine skriptgestützte Umsetzung des konzeptuellen
     * AAA-Anwendungsschemas in UML in ein Implementierungsschema.
     */
    public void transform() throws ShapeChangeAbortException {

	// TODO Name des zu transformierenden Schemas sollte konfigurierbar sein
	SortedMap<String, String> implSchemaNameByAppSchemaFullName = new TreeMap<>();
	implSchemaNameByAppSchemaFullName.put(
		"Model::GeoInfoDok::AFIS-ALKIS-ATKIS Anwendungsschema::AFIS-ALKIS-ATKIS Anwendungsschema 7.1", "NAS");
//	schemaMappings.put("AAA_Ausgabekatalog","NAS-AK");
//	schemaMappings.put("BR_Bodenrichtwerte", "NAS-BR");
//	schemaMappings.put("GN_Geographische Informationen","NAS-GN");
//	schemaMappings.put("GV_Geometrische Verbesserungen","NAS-GV");
//	schemaMappings.put("LB_Landbedeckung","NAS-LB");
//	schemaMappings.put("LN_Landnutzung","NAS-LN");

	SortedMap<String, List<String>> dependenciesBySchema = new TreeMap<>();

	dependenciesBySchema.put(
		"Model::GeoInfoDok::AFIS-ALKIS-ATKIS Anwendungsschema::AFIS-ALKIS-ATKIS Anwendungsschema 7.1",
		Arrays.asList(/*
			       * "Model::GeoInfoDok::AAA_Ausgabekatalog",
			       * "Model::GeoInfoDok::AAA_Objektartenkatalog".
			       */
			"Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 2::Core Data Types",
			"Model::ISO/TC 211::ISO 19107 Spatial schema::ISO 19107 Edition 1",
			"Model::ISO/TC 211::Informative::Spatial Examples from ISO 19107::Application Schema",
			"Model::ISO/TC 211::ISO 19108 Temporal schema::ISO 19108 Edition 1",
			"Model::ISO/TC 211::ISO 19109 Rules for application schema::ISO 19109 Edition 2",
			"Model::ISO/TC 211::ISO 19110 Methodology for feature cataloguing::ISO 19110 Edition 2",
			"Model::ISO/TC 211::ISO 19111 Referencing by coordinates::ISO 19111 Edition 3",
			"Model::ISO/TC 211::ISO 19115 Metadata::ISO 19115-1 Edition 1",
			"Model::ISO/TC 211::ISO 19123 Schema for coverage geometry and functions::ISO 19123-1 Edition 1",
			// für die Erzeugung des NAS-Implementierungsschemas wird auch 19123-2 benötigt
			"Model::ISO/TC 211::ISO 19123 Schema for coverage geometry and functions::ISO 19123-2 Edition 1",
			"Model::ISO/TC 211::ISO 19157 Data quality::ISO 19157-1 Edition 1",
			"Model::OGC::Filter Encoding::Filter Encoding 2.0",
			"Model::GeoInfoDok::Web Feature Service Erweiterungen::Web Feature Service Erweiterungen 2.0",
			"Model::OGC::Web Feature Service::Web Feature Service 2.0",
			"Model::OGC::OWS Common::OWS Common 1.1"));

	SortedSet<String> relevantSchemas = new TreeSet<>();

	for (Entry<String, List<String>> e : dependenciesBySchema.entrySet()) {
	    relevantSchemas.addAll(e.getValue());
	}

	helper.prepareModel(implSchemaNameByAppSchemaFullName, relevantSchemas);

	List<String> invalidAaaVersionValues = helper.aaaVersionTagValues.stream()
		.filter(v -> v != null && (v.isEmpty() || !v.startsWith("7"))).collect(Collectors.toList());
	if (!invalidAaaVersionValues.isEmpty()) {
	    result.addFatalError(
		    "Invalider Wert für AAA:Version gefunden. Erwartet wurde durchgängig die Version 7, gefunden wurden die Versionen "
			    + StringUtils.join(helper.aaaVersionTagValues, ", ") + ". Invalide Werte: "
			    + StringUtils.join(invalidAaaVersionValues, ", "));
	    throw new ShapeChangeAbortException();
	}

	/*
	 * Die Modellelemente, die Inhalte besitzen, die nicht in die NAS umgesetzt
	 * werden, werden bei der Ableitung des Implementierungsmodells für den
	 * Datenaustausch entfernt:
	 */
	helper.deletePackage("AAA Versionierungsschema");
	helper.deleteAttribute("AA_Objekt", "identifikator");
	helper.deleteClass("AA_ObjektOhneRaumbezug");
	helper.deleteClass("AX_Fortfuehrung");
	helper.deleteClass("AX_Datenbank");
	helper.deleteClass("AX_Operation_Datenbank");
	helper.deleteClass("AX_TemporaererBereich");
	helper.deleteClass("AX_NeuesObjekt");
	helper.deleteClass("AX_GeloeschtesObjekt");
	helper.deleteClass("AX_AktualisiertesObjekt");
	helper.deleteClass("AX_Fortfuehrungsobjekt");

	/*
	 * Die Modellelemente, die Inhalte besitzen, die auf spezifische Weise in die
	 * NAS umgesetzt werden sollen, werden entsprechend angepasst:
	 */

	/*
	 * Die folgenden Typen erhalten ein neues Attribut und werden von den
	 * konzeptuellen Typen entkoppelt:
	 */
	helper.addAttribute("TA_PointComponent", "position", "GM_Point", "50");
	helper.addAttribute("TA_CurveComponent", "position", "GM_Curve", "50");
	helper.addAttribute("TA_SurfaceComponent", "position", "GM_Surface", "50");
	helper.addAttribute("TA_MultiSurfaceComponent", "position", "GM_Object", "50");

	helper.removeGeneralization("TA_PointComponent", "TS_PointComponent");
	helper.removeGeneralization("TA_CurveComponent", "TS_CurveComponent");
	helper.removeGeneralization("TA_SurfaceComponent", "TS_SurfaceComponent");
	helper.removeGeneralization("TA_MultiSurfaceComponent", "TS_FeatureComponent");

	/*
	 * Die Assoziation mit der Rolle "TA_MultiSurfaceComponent.masche" wird
	 * entsprechend gelöscht.
	 */
	helper.deleteRole("TA_MultiSurfaceComponent", "masche");

	/*
	 * Die folgenden Attribute erhalten einen neuen Typ:
	 */
	// TODO: Wurde laut Transformationsvorschrift für Unions auf GM_Object gesetzt
	helper.changeType("AU_Punkthaufenobjekt", "position", "GM_MultiPoint");
	// TODO: Wurde laut Transformationsvorschrift für Unions auf GM_Object gesetzt
	helper.changeType("AU_KontinuierlichesLinienobjekt", "position", "GM_Curve");
	helper.changeType("AG_Punktobjekt", "position", "GM_Point");
	helper.changeType("AU_UmringObjekt_3D", "position", "GM_MultiCurve");
	// TODO: Wurde laut Transformationsvorschrift für Unions auf GM_Object gesetzt
	helper.changeType("AU_PunkthaufenObjekt_3D", "position", "GM_MultiPoint");

	helper.changeType("AP_TransformationsMatrix_3D", "parameter", "doubleList");
	helper.changeType("AX_DQOhneDatenerhebung", "herkunft", "LI_Lineage");
	helper.changeType("AX_DQMitDatenerhebung", "herkunft", "LI_Lineage");
	helper.changeTypeAndMultiplicity("AX_DQErhebung3D", "herkunft3D", "LI_Lineage", "0", "1");
	helper.changeTypeAndMultiplicity("AX_DQPunktort", "herkunft", "LI_Lineage", "0", "1");
	helper.changeType("AX_DQDachhoehe", "herkunft", "LI_Lineage");
	helper.changeType("AX_DQBodenhoehe", "herkunft", "LI_Lineage");
	helper.changeType("AX_Sperrauftrag", "uuidListe", "URI");
	helper.changeType("AX_Entsperrauftrag", "uuidListe", "URI");
	helper.changeType("ExceptionFortfuehrung", "bereitsGesperrteObjekte", "URI");
	helper.changeType("ExceptionFortfuehrung", "nichtMehrAktuelleObjekte", "URI");
	helper.changeType("ExceptionAAAFortfuehrungOderSperrung", "bereitsGesperrteObjekte", "URI");
	helper.changeType("ExceptionAAAFortfuehrungOderSperrung", "nichtMehrAktuelleObjekte", "URI");
	helper.changeType("ExceptionAAAEntsperren", "uuidListe", "URI");
	helper.changeType("DCP", "HTTP", "URI");
	helper.changeType("DCP", "email", "URI");
	helper.changeType("AX_Fortfuehrungsergebnis", "fortfuehrungsnachweis", "Any");

	/*
	 * Verweise in den Projektsteuerungskatalog werden als XLink-href realisiert
	 * (Map-Entry mit gml:Referencetype in ShapeChange-Konfiguration):
	 */
	helper.addAttribute("AA_Antrag", "art", "AA_Antragsart", "242");
	helper.addAttribute("AA_Projektsteuerung", "art", "AA_Projektsteuerungsart", "249");
	helper.addAttribute("AA_Vorgang", "art", "AA_Vorgangsart", "262");
	helper.addAttribute("AA_Aktivitaet", "art", "AA_Aktivitaetsart", "304");

	/*
	 * Die Klassen des Projektsteuerungskatalogs werden gelöscht:
	 */
	helper.deleteClass("AA_Antragsart");
	helper.deleteClass("AA_Projektsteuerungsart");
	helper.deleteClass("AA_Vorgangsart");
	helper.deleteClass("AA_Aktivitaetsart");
	helper.deleteClass("AA_Projektsteuerungskatalog");
	helper.deleteClass("AA_AktivitaetInVorgang");
	helper.deleteClass("AA_VorgangInProzess");
	helper.deleteClass("AA_Dokumentationsbedarf");
	helper.deleteClass("AA_DurchfuehrungAktivitaet");
	helper.deleteClass("AA_ProzesszuordnungAktivitaet");

	/*
	 * Als Folge der obigen Anpassungen können außerdem die folgenden Typen
	 * gelöscht werden:
	 */

	helper.deleteClass("AA_PunktLinienThema");
	helper.deleteClass("AX_LI_ProcessStep_OhneDatenerhebung");
	helper.deleteClass("AX_LI_ProcessStep_MitDatenerhebung");
	helper.deleteClass("AX_LI_ProcessStep_Punktort");
	helper.deleteClass("AX_LI_ProcessStep_Bodenhoehe");
	helper.deleteClass("AX_LI_ProcessStep_Dachhoehe");
	helper.deleteClass("AX_LI_ProcessStep3D");
	helper.deleteClass("AD_ReferenzierbaresGitter");
	helper.deleteClass("AD_Wertematrix");
	// helper.deleteClass( "AX_Listenelement3D" );
	// helper.deleteClass( "AX_NullEnumeration3D" );

	/*
	 * Bei allen Klassen wird das UML Tagged Value „xsdEncodingRule“ gesetzt: -
	 * "NAS" außer bei Typen, die mit einer der Zeichenketten "AX_DQ", "AX_LI",
	 * "AX_Datenerhebung" beginnen; - bei diesen wird „iso19139_2007“ verwendet.
	 * 
	 * Bei Klassen werden die folgenden UML Tagged Values gesetzt: - noPropertyType:
	 * “true” bei Feature Type; “false” bei Data Type und Union -
	 * byValuePropertyType: “false” bei Feature Type, Data Type und Union -
	 * isCollection: "false" bei Feature Type, DataType und Union - asDictionary:
	 * „true“, nur bei CodeList
	 */
	helper.setTaggedValues();

	/*
	 * Multiple Vererbung: Weder ISO 19136 / GML 3.3 noch ISO/TS 19139 unterstützen
	 * in den Abbildungsregeln multiple Vererbung, das AAA-Modell verwendet diese
	 * jedoch in Mixin-Klassen (z.B. AP_GPO, AX_Katalogeintrag). Die Mixin-Klassen
	 * werden aufgelöst: - Alle Attribute werden in die nächsten in der NAS
	 * codierten Subtypen kopiert. - Alle Relationen zu den Mixin-Klassen werden
	 * ebenfalls jeweils auf die nächsten in der NAS codierten Subtypen kopiert.
	 * Dabei wird der Rollenname durch Anhängen des Klassennamens geändert, um die
	 * Eindeutigkeit der Eigenschaftsnamen zu gewährleisten. - Die Mixin-Klassen
	 * werden gelöscht.
	 */
	helper.resolveMixins(false);

	/*
	 * Die Eigenschaften von AA_PMO und AA_Objekt werden wie bei Mixin-Klassen
	 * (siehe oben) auf "AD_PunktCoverage" und "AD_GitterCoverage" übertragen, die
	 * konzeptuellen Attribute AA_PMO.ausdehnung, AD_PunktCoverage.geometrie und
	 * AD_PunktCoverage.werte gelöscht. Zusätzlich werden Vererbungsbeziehungen
	 * auf Implementierungen von "CV_DiscreteGridPointCoverage" bzw.
	 * "CV_DiscretePointCoverage" gesetzt.
	 */

	helper.deleteAttribute("AA_PMO", "ausdehnung");
	helper.deleteAttribute("AD_PunktCoverage", "geometrie");
	helper.deleteAttribute("AD_PunktCoverage", "werte");

	Element e1 = helper.gidClasses.get("AA_Objekt");
	if (e1 == null) {
	    result.addError("Klasse 'AA_Objekt' nicht gefunden");
	} else {
	    Element e2 = helper.gidClasses.get("AA_PMO");
	    if (e2 == null) {
		result.addError("Klasse 'AA_PMO' nicht gefunden");
	    } else {
		Element e3 = helper.gidClasses.get("AD_PunktCoverage");
		if (e3 == null) {
		    result.addError("Klasse 'AD_PunktCoverage' nicht gefunden");
		} else {
		    Element e4 = helper.gidClasses.get("AD_GitterCoverage");
		    if (e4 == null) {
			result.addError("Klasse 'AD_GitterCoverage' nicht gefunden");
		    } else {
			for (Attribute a : e1.GetAttributes()) {
			    helper.cloneAttribute(a, e3);
			    helper.cloneAttribute(a, e4);
			}
			for (Attribute a : e2.GetAttributes()) {
			    helper.cloneAttribute(a, e3);
			    helper.cloneAttribute(a, e4);
			}

			/*
			 * Für die Erzeugung des NAS-Implementierungsschemas werden die Classifier aus
			 * 19123-2 benötigt.
			 */
			String iso19123_2_fullName = "Model::ISO/TC 211::ISO 19123 Schema for coverage geometry and functions::ISO 19123-2 Edition 1";
			Optional<Element> mpcElmtOpt = helper.getElement(iso19123_2_fullName, "MultiPointCoverage");
			Optional<Element> rgcElmtOpt = helper.getElement(iso19123_2_fullName, "RectifiedGridCoverage");

			if (mpcElmtOpt.isEmpty()) {
			    result.addError("Could not find 'MultiPointCoverage' in package '" + iso19123_2_fullName
				    + "'. Ensure that the package exists in the model, and that it contains a classifier with that name.");
			} else if (mpcElmtOpt.isEmpty()) {
			    result.addError("Could not find 'RectifiedGridCoverage' in package '" + iso19123_2_fullName
				    + "'. Ensure that the package exists in the model, and that it contains a classifier with that name.");
			} else {
			    helper.addGeneralization(e3, mpcElmtOpt.get());
			    helper.addGeneralization(e4, rgcElmtOpt.get());
			}
		    }
		}
	    }
	}
	helper.deleteClass("AA_PMO");

    }
}
