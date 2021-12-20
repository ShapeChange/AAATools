/**
 * NAS-Tool (schema transformer)
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

package de.adv_online.aaa.nastool;

import org.sparx.Attribute;
import org.sparx.Element;

import de.interactive_instruments.ShapeChange.Options;
import de.interactive_instruments.ShapeChange.ShapeChangeAbortException;
import de.interactive_instruments.ShapeChange.ShapeChangeResult;
import de.interactive_instruments.ShapeChange.Model.Transformer;

public class NasTransformer_7 implements Transformer {

	private ShapeChangeResult result = null;
	private ImplementationSchemaTransformerHelper helper = null; 

	public void initialise(Options o, ShapeChangeResult r, String repositoryFileName) throws ShapeChangeAbortException {
		result = r;
		helper = new ImplementationSchemaTransformerHelper();
		helper.initialise(o, r, repositoryFileName);
	}

	public void shutdown() {
		if (helper!=null)
			helper.shutdown();
	}

	/**
	 * Das AAA-Anwendungsschema verwendet einige Konstruktionen in UML, die in den Abbildungsregeln 
	 * von ISO 19136 Annex E und ISO/TS 19139 nicht unterstützt werden. Daher erfolgt eine 
	 * skriptgestützte Umsetzung des konzeptuellen AAA-Anwendungsschemas in UML in ein 
	 * Implementierungsschema.
	 */
	public void transform() throws ShapeChangeAbortException {
		
		helper.prepareModel("AFIS-ALKIS-ATKIS Anwendungsschema","NAS");
		
		if (!helper.aaaVersion.startsWith("7")) {
    		result.addFatalError("Das AAA-Anwendungsschema ist von der falschen Version. Erwartet wurde die Version 7, gefunden wurde die Version "+helper.aaaVersion);
			throw new ShapeChangeAbortException();
		}

		/* Die Modellelemente, die Inhalte besitzen, die nicht in die NAS umgesetzt werden, werden bei der 
		 * Ableitung des Implementierungsmodells für den Datenaustausch entfernt:
		 */
		helper.deletePackage("AAA Versionierungsschema");
		helper.deleteAttribute("AA_Objekt","identifikator");
		helper.deleteClass("AA_ObjektOhneRaumbezug");
		helper.deleteClass( "AX_Fortfuehrung" );
		helper.deleteClass( "AX_Datenbank" );
		helper.deleteClass( "AX_Operation_Datenbank" );
		helper.deleteClass( "AX_TemporaererBereich" );
		helper.deleteClass( "AX_NeuesObjekt" );
		helper.deleteClass( "AX_GeloeschtesObjekt" );
		helper.deleteClass( "AX_AktualisiertesObjekt" );
		helper.deleteClass( "AX_Fortfuehrungsobjekt" );

		/* Die Modellelemente, die Inhalte besitzen, die auf spezifische Weise in die NAS umgesetzt werden 
		 * sollen, werden entsprechend angepasst:
		 */

		/* Die folgenden Typen erhalten ein neues Attribut und werden von den konzeptuellen Typen entkoppelt:
		 */
		helper.addAttribute("TA_PointComponent","position","GM_Point", "50");
		helper.addAttribute("TA_CurveComponent","position","GM_Curve", "50");
		helper.addAttribute("TA_SurfaceComponent","position","GM_Surface", "50");
		helper.addAttribute("TA_MultiSurfaceComponent","position","GM_Object", "50");

		helper.removeGeneralization("TA_PointComponent","TS_PointComponent");
		helper.removeGeneralization("TA_CurveComponent","TS_CurveComponent");
		helper.removeGeneralization("TA_SurfaceComponent","TS_SurfaceComponent");
		helper.removeGeneralization("TA_MultiSurfaceComponent","TS_FeatureComponent");
		
		/* Die Assoziation mit der Rolle "TA_MultiSurfaceComponent.masche" wird entsprechend gelöscht.
		 */
		helper.deleteRole("TA_MultiSurfaceComponent","masche");
		
		/* Die folgenden Attribute erhalten einen neuen Typ:
		 */
		helper.changeType( "AU_Punkthaufenobjekt", "position", "GM_MultiPoint" );
		helper.changeType( "AU_KontinuierlichesLinienobjekt", "position", "GM_Curve" );
		helper.changeType( "AU_Flaechenobjekt", "position", "GM_Object" );
		helper.changeType( "AG_Flaechenobjekt", "position", "GM_Object" );
		helper.changeType( "AG_Punktobjekt", "position", "GM_Point" );
		helper.changeType( "AU_Objekt", "position", "GM_Object" );
		helper.changeType( "AG_Objekt", "position", "GM_Object" );
		helper.changeType( "AU_GeometrieObjekt_3D", "position", "GM_Object" );
		helper.changeType( "AU_MehrfachLinienObjekt_3D", "position", "GM_Object" );
		helper.changeType( "AU_MehrfachFlaechenObjekt_3D", "position", "GM_Object" );
		helper.changeType( "AU_UmringObjekt_3D", "position", "GM_MultiCurve" ) ;
		helper.changeType( "AU_PunkthaufenObjekt_3D", "position", "GM_MultiPoint" );
		helper.changeType( "AX_DQOhneDatenerhebung", "herkunft", "LI_Lineage" );
		helper.changeType( "AX_DQMitDatenerhebung", "herkunft", "LI_Lineage" );
		helper.changeTypeAndMultiplicity( "AX_DQPunktort", "herkunft", "LI_Lineage", "0", "1" );
		helper.changeType( "AX_DQDachhoehe", "herkunft", "LI_Lineage" );
		helper.changeType( "AX_DQBodenhoehe", "herkunft", "LI_Lineage" );
		helper.changeTypeAndMultiplicity( "AX_DQErhebung3D", "herkunft3D", "LI_Lineage", "0" , "1" );
		helper.changeType( "AX_DQSchwere", "genauigkeitswert", "Measure" );
		helper.changeType( "AX_Schwereanomalie_Schwere", "wert", "Measure" );
		helper.changeType( "AX_Schwere", "schwerewert", "Measure" );
		helper.changeType( "AX_VertikalerSchweregradient", "genauigkeitVertikalerSchweregradient", "Measure" );
		helper.changeType( "AX_VertikalerSchweregradient", "wertVertikalerSchweregradient", "Measure" );
		helper.changeType( "AX_Leitung", "spannungsebene", "Measure" );
		helper.changeType( "AX_Sperrauftrag", "uuidListe", "URI" );
		helper.changeType( "AX_Entsperrauftrag", "uuidListe", "URI" );
		helper.changeType( "ExceptionFortfuehrung", "bereitsGesperrteObjekte", "URI" );
		helper.changeType( "ExceptionFortfuehrung", "nichtMehrAktuelleObjekte", "URI" );
		helper.changeType( "ExceptionAAAFortfuehrungOderSperrung", "bereitsGesperrteObjekte", "URI" );
		helper.changeType( "ExceptionAAAFortfuehrungOderSperrung", "nichtMehrAktuelleObjekte", "URI" );
		helper.changeType( "ExceptionAAAEntsperren", "uuidListe", "URI" );
		helper.changeType( "DCP", "HTTP", "URI" );
		helper.changeType( "DCP", "email", "URI" );		
		helper.changeType( "AP_TransformationsMatrix_3D", "parameter", "doubleList" );
		helper.changeType( "AX_Fortfuehrungsergebnis", "fortfuehrungsnachweis", "Any" );
		
		/* Verweise in den Projektsteuerungskatalog werden als XLink-href realisiert (Map-Entry mit gml:Referencetype in ShapeChange-Konfiguration):
		 */
		helper.addAttribute("AA_Antrag","art","AA_Antragsart", "242");
		helper.addAttribute("AA_Projektsteuerung","art","AA_Projektsteuerungsart", "249");
		helper.addAttribute("AA_Vorgang","art","AA_Vorgangsart", "262");
		helper.addAttribute("AA_Aktivitaet","art","AA_Aktivitaetsart", "304");

		/* Die Klassen des Projektsteuerungskatalogs werden gelöscht:
		 */
		helper.deleteClass( "AA_Antragsart" );
		helper.deleteClass( "AA_Projektsteuerungsart" );
		helper.deleteClass( "AA_Vorgangsart" );
		helper.deleteClass( "AA_Aktivitaetsart" );
		helper.deleteClass( "AA_Projektsteuerungskatalog" );
		helper.deleteClass( "AA_AktivitaetInVorgang" );
		helper.deleteClass( "AA_VorgangInProzess" );
		helper.deleteClass( "AA_Dokumentationsbedarf" );
		helper.deleteClass( "AA_DurchfuehrungAktivitaet" );
		helper.deleteClass( "AA_ProzesszuordnungAktivitaet" );
		
		/* Als Folge der obigen Anpassungen können außerdem die folgenden Typen gelöscht werden:
		 */
		helper.deleteClass("AA_Liniengeometrie");
		helper.deleteClass("AA_Flaechengeometrie");
		helper.deleteClass("AU_Geometrie");
		helper.deleteClass("AG_Geometrie");
		helper.deleteClass("AU_Geometrie_3D");
		helper.deleteClass("AA_Punktgeometrie");
		helper.deleteClass("AA_Punktgeometrie_3D");
		helper.deleteClass("AA_MehrfachLinienGeometrie_3D");
		helper.deleteClass("AA_MehrfachFlaechenGeometrie_3D");
		helper.deleteClass("AA_PunktLinienThema");
		helper.deleteClass( "AX_LI_ProcessStep_OhneDatenerhebung" );
		helper.deleteClass( "AX_LI_ProcessStep_MitDatenerhebung" );
		helper.deleteClass( "AX_LI_ProcessStep_Punktort" );
		helper.deleteClass( "AX_LI_ProcessStep_Bodenhoehe" );
		helper.deleteClass( "AX_LI_ProcessStep_Dachhoehe" );
		helper.deleteClass( "AX_LI_ProcessStep3D" );
		helper.deleteClass( "Voltage" );
		helper.deleteClass( "Acceleration" );
		helper.deleteClass( "AccelerationGradient" );
		helper.deleteClass( "AD_ReferenzierbaresGitter" );
		helper.deleteClass( "AD_Wertematrix" );
		helper.deleteClass( "AA_UUID" );
		// helper.deleteClass( "AX_Listenelement3D" );
		// helper.deleteClass( "AX_NullEnumeration3D" );		
		
		/* Bei allen Klassen wird das UML Tagged Value „xsdEncodingRule“ gesetzt: 
		 * - "NAS" außer bei Typen, die mit einer der Zeichenketten "AX_DQ", "AX_LI", "AX_Datenerhebung" beginnen;
		 * - bei diesen wird „iso19139_2007“ verwendet.
		 * 
		 * Bei Klassen werden die folgenden UML Tagged Values gesetzt:
		 * - noPropertyType: “true” bei <<FeatureType>>; “false” bei <<DataType>> und <<Union>>
		 * - byValuePropertyType: “false” bei <<FeatureType>>, <<DataType>> und <<Union>
		 * - isCollection: "false" bei <<FeatureType>>, <<DataType>> und <<Union>
		 * - asDictionary: „true“, nur bei <<CodeList>>
		 */
		helper.setTaggedValues();

		/* Multiple Vererbung: Weder ISO 19136 / GML 3.3 noch ISO/TS 19139 unterstützen in den Abbildungsregeln multiple Vererbung, 
		 * das AAA-Modell verwendet diese jedoch in Mixin-Klassen (z.B. AP_GPO, AX_Katalogeintrag). Die Mixin-Klassen 
		 * werden aufgelöst:
		 * - Alle Attribute werden in die nächsten in der NAS codierten Subtypen kopiert.
		 * - Alle Relationen zu den Mixin-Klassen werden ebenfalls jeweils auf die nächsten in der NAS codierten Subtypen kopiert. Dabei wird der Rollenname durch Anhängen des Klassennamens geändert, um die Eindeutigkeit der Ei- genschaftsnamen zu gewährleisten.
		 * - Die <<Type>>-Klassen werden gelöscht.
		 */
		helper.resolveMixins(false);
		
		/* Die Eigenschaften von AA_PMO und AA_Objekt werden wie bei Mixin- Klassen (siehe oben)
		 * auf "AD_PunktCoverage" und "AD_GitterCoverage" übertragen, die konzeptuellen Attribute 
		 * AA_PMO.ausdehnung, AD_PunktCoverage.geometrie und AD_PunktCoverage.werte gelöscht. 
		 * Zusätzlich werden Vererbungsbeziehungen auf Implementierungen von 
		 * "CV_DiscreteGridPointCoverage" bzw. "CV_DiscretePointCoverage" gesetzt. */
		
		helper.deleteAttribute("AA_PMO","ausdehnung");
		helper.deleteAttribute("AD_PunktCoverage","geometrie");
		helper.deleteAttribute("AD_PunktCoverage","werte");
		
		Element e1 = helper.aaaClasses.get("AA_Objekt");
		if (e1==null) {
    		result.addError("Klasse 'AA_Objekt' nicht gefunden");
		} else {
			Element e2 = helper.aaaClasses.get("AA_PMO");
			if (e2==null) {
	    		result.addError("Klasse 'AA_PMO' nicht gefunden");
			} else {
				Element e3 = helper.aaaClasses.get("AD_PunktCoverage");
				if (e3==null) {
		    		result.addError("Klasse 'AD_PunktCoverage' nicht gefunden");
				} else {
					Element e4 = helper.aaaClasses.get("AD_GitterCoverage");
					if (e4==null) {
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
						helper.addGeneralization(e3,helper.allClasses.get("MultiPointCoverage"));
						helper.addGeneralization(e4,helper.allClasses.get("RectifiedGridCoverage"));						
					}
				}
			}
		}		
		helper.deleteClass("AA_PMO");
		
	}
}
