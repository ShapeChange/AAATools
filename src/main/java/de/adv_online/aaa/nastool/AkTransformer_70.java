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

import org.sparx.Connector;

import de.interactive_instruments.ShapeChange.Options;
import de.interactive_instruments.ShapeChange.ShapeChangeAbortException;
import de.interactive_instruments.ShapeChange.ShapeChangeResult;
import de.interactive_instruments.ShapeChange.Model.Transformer;

public class AkTransformer_70 implements Transformer {

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
	 * Der AAA-Ausgabekatalog verwendet einige Konstruktionen in UML, die in den Abbildungsregeln 
	 * von ISO 19136 Annex E und ISO/TS 19139 nicht unterstützt werden. Daher erfolgt eine 
	 * skriptgestützte Umsetzung des konzeptuellen Anwendungsschemas in UML in ein 
	 * Implementierungsschema.
	 */
	public void transform() throws ShapeChangeAbortException {
		
		helper.prepareModel("AAA_Ausgabekatalog","NAS-AK");
		
		helper.deleteClass("List of Elements in Diagram AX_Benutzungsergebnis");
		
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
		helper.resolveMixins(true);		
	}
}
