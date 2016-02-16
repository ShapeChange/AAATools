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

import de.interactive_instruments.ShapeChange.Options;
import de.interactive_instruments.ShapeChange.ShapeChangeAbortException;
import de.interactive_instruments.ShapeChange.ShapeChangeResult;
import de.interactive_instruments.ShapeChange.Model.ClassInfo;
import de.interactive_instruments.ShapeChange.Model.Model;
import de.interactive_instruments.ShapeChange.Model.PackageInfo;
import de.interactive_instruments.ShapeChange.Target.Target;

/**
 * @author Clemens Portele (portele <at> interactive-instruments <dot> de)
 *
 */
public class Profil implements Target {

	public static final int TARGET_AAA_Profiltool = 402;
	public static final int STATUS_WRITE_3AP = 31;
	public static final int STATUS_WRITE_MODEL = 32;
	public static final int STATUS_CLEAN_MODEL = 33;
	
	private PackageInfo pi = null;
	private Model model = null;
	private Options options = null;
	private ShapeChangeResult result = null;

	private String outputDirectory = null;
	private ProfilRep theProfile = null;
	private String quelle = null;
	private String ziel = null;
	private boolean error = false;
	
	public int getTargetID() {
		return TARGET_AAA_Profiltool;
	}

	// FIXME New diagnostics-only flag is to be considered
	public void initialise(PackageInfo p, Model m, Options o,
			ShapeChangeResult r, boolean diagOnly) throws ShapeChangeAbortException {
		pi = p;
		model = m;
		options = o;
		result = r;
		
		String mart = "FIXME";
		String profil = "";

		if (!options.gmlVersion.equals("3.2")) {
			result.addError(null, 110, pi.name());
			return;
		}

		outputDirectory = options.parameter(this.getClass().getName(),"Verzeichnis");
		if (outputDirectory==null)
			outputDirectory = options.parameter("directory");
		if (outputDirectory==null)
			outputDirectory = options.parameter(".");

		String s = options.parameter(this.getClass().getName(),"Modellart");
		if (s!=null)
			mart = s.trim();
		
		s = options.parameter(this.getClass().getName(),"Profil");
		if (s!=null)
			profil = s.trim();

		s = options.parameter(this.getClass().getName(),"Quelle");
		if (s!=null)
			quelle = s.trim();
		
		s = options.parameter(this.getClass().getName(),"Ziel");
		if (s!=null)
			ziel = s.trim();

		s = p.taggedValue("AAA:AAAVersion");
		String zielversion = "6.0.1";
		if (s!=null && !s.isEmpty())
			zielversion = s;
					
		if (ziel==null) {
			result.addError("Der Parameter 'Ziel' fehlt. Die Ausführung des Profiltools wird abgebrochen");
			error = true;		
		} else if (quelle==null) {
			result.addError("Der Parameter 'Quelle' fehlt. Die Ausführung des Profiltools wird abgebrochen");
			error = true;
		} else if (quelle.equals("Datei")) {
			if (profil.isEmpty())
				theProfile = new ProfilRep(p, m, o, r, zielversion, outputDirectory+"/"+mart+".3ap");
			else
				theProfile = new ProfilRep(p, m, o, r, zielversion, outputDirectory+"/"+mart+"_"+profil+".3ap");
		} else if (quelle.equals("Modell")) {
			theProfile = new ProfilRep(p, m, o, r, mart, profil, zielversion);
		} else if (quelle.equals("Neu_Minimal") || quelle.equals("Neu_Maximal")) {
			if (mart.equals("FIXME") || profil.equals("FIXME") || mart.equals("") || profil.equals("")) {
				result.addError("Der Parameter 'Modellart' oder 'Profil' fehlt, das Erzeugen eines Profils ist nicht möglich. Die Ausführung des Profiltools wird abgebrochen");
				error = true;
			} else {
				boolean gdb = false;
				if (quelle.equals("Neu_Minimal"))
					gdb = true;
				theProfile = new ProfilRep(p, m, o, r, mart, profil, gdb, zielversion);
			}
		} else {
			result.addError("Der Parameter 'Quelle' hat den unbekannten Wert '"+quelle+"'. Die Ausführung des Profiltools wird abgebrochen.");
			error = true;
		}
		
		if (theProfile!=null && !theProfile.isSet()) {
			result.addError("Das Profil konnte nicht vollständig erstellt/geladen werden. Die Ausführung des Profiltools wird abgebrochen.");
			error = true;
		}
	}
	
	public void process(ClassInfo ci) {}

	public void write() {
		if (error)
			return;

		if (ziel.equals("DateiModell")) {
			theProfile.writeToFile(outputDirectory+"/"+theProfile.name()+"__export.3ap");
			theProfile.writeToModel();
			result.addResult(getTargetID(), outputDirectory, theProfile.name()+"__export.3ap", options.parameter(this.getClass().getName(),"modellarten"));
		} else if (ziel.equals("Modell")) {
			theProfile.writeToModel();
		} else if (ziel.equals("Datei")) {
			theProfile.writeToFile(outputDirectory+"/"+theProfile.name()+"__export.3ap");
			result.addResult(getTargetID(), outputDirectory, theProfile.name()+"__export.3ap", options.parameter(this.getClass().getName(),"modellarten"));
		} else if (ziel.equals("Ohne")) {
			theProfile.clearInModel();
		} else {
			result.addError("Der Parameter 'Ziel' hat den unbekannten Wert '"+ziel+"'. Die Ausführung des Profiltools wird abgebrochen");
			error = true;
		}
	}
}