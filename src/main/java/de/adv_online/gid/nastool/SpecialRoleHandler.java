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

import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Johannes Echterhoff (echterhoff at interactive-instruments dot de)
 *
 */
public class SpecialRoleHandler {

    private SortedMap<String, SpecialRole> roleByNameMap = new TreeMap<>();

    public SpecialRoleHandler() {

	// AP_GPO, inverse Rolle von dientZurDarstellungVon (AA_Objekt)
	SpecialRole sr = new SpecialRole("inversZu_dientZurDarstellungVon_AP_Darstellung", "16.1");
	roleByNameMap.put(sr.getRoleName(), sr);

	sr = new SpecialRole("inversZu_dientZurDarstellungVon_AX_Gestaltung3D", "16.2");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("inversZu_dientZurDarstellungVon_AP_FPO", "16.3");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("inversZu_dientZurDarstellungVon_AP_KPO_3D", "16.4");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("inversZu_dientZurDarstellungVon_AP_LPO", "16.5");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("inversZu_dientZurDarstellungVon_AP_PTO", "16.6");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("inversZu_dientZurDarstellungVon_AP_LTO", "16.7");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("inversZu_dientZurDarstellungVon_AP_PPO", "16.8");
	roleByNameMap.put(sr.getRoleName(), sr);

	// AX_Bauwerk3D.beziehtSichAuf (AX_BauwerkeEinrichtungenUndSonstigeAngaben)
	sr = new SpecialRole("beziehtSichAuf_AX_Leitung", "4510.1");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Netzknoten", "4510.2");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_BauwerkOderAnlageFuerSportFreizeitUndErholung", "4510.3");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Ast", "4510.4");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Sickerstrecke", "4510.5");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_EinrichtungInOeffentlichenBereichen", "4510.6");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Bahnverkehrsanlage", "4510.7");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Ortslage", "4510.8");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_UntergeordnetesGewaesser", "4510.9");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Flugverkehrsanlage", "4510.10");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_BauwerkOderAnlageFuerIndustrieUndGewerbe", "4510.11");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Wasserspiegelhoehe", "4510.12");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_BauwerkImVerkehrsbereich", "4510.13");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_SchifffahrtslinieFaehrverkehr", "4510.14");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Schleuse", "4510.15");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Transportanlage", "4510.16");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Grenzuebergang", "4510.17");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Nullpunkt", "4510.18");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_WegPfadSteig", "4510.19");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_SonstigesBauwerkOderSonstigeEinrichtung", "4510.20");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Turm", "4510.21");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Hafen", "4510.22");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Gewaessermerkmal", "4510.23");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_BauwerkImGewaesserbereich", "4510.24");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Strassenverkehrsanlage", "4510.25");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_VorratsbehaelterSpeicherbauwerk", "4510.26");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_HistorischesBauwerkOderHistorischeEinrichtung", "4510.27");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Gleis", "4510.28");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_HeilquelleGasquelle", "4510.29");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Abschnitt", "4510.30");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Vegetationsmerkmal", "4510.31");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Polder", "4510.32");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_EinrichtungenFuerDenSchiffsverkehr", "4510.33");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Testgelaende", "4510.34");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_Gewaesserstationierungsachse", "4510.35");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("beziehtSichAuf_AX_SeilbahnSchwebebahn", "4510.36");
	roleByNameMap.put(sr.getRoleName(), sr);

	// AX_SonstigesBauwerkOderSonstigeEinrichtung.gehoertZuBauwerk
	// (AX_BauwerkeEinrichtungenUndSonstigeAngaben)
	sr = new SpecialRole("inversZu_gehoertZuBauwerk", "10000");
	roleByNameMap.put(sr.getRoleName(), sr);
	/*
	 * Im Folgenden eigentlich immer 709.xyz, aber das wurde zuvor schon nicht
	 * richtig beachtet, deshalb 15000.
	 */
	sr = new SpecialRole("gehoertZuBauwerk_AX_Leitung", "15000.1");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Netzknoten", "15000.2");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_BauwerkOderAnlageFuerSportFreizeitUndErholung", "15000.3");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Ast", "15000.4");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Sickerstrecke", "15000.5");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_EinrichtungInOeffentlichenBereichen", "15000.6");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Bahnverkehrsanlage", "15000.7");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Ortslage", "15000.8");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_UntergeordnetesGewaesser", "15000.9");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Flugverkehrsanlage", "15000.10");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_BauwerkOderAnlageFuerIndustrieUndGewerbe", "15000.11");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Wasserspiegelhoehe", "15000.12");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_BauwerkImVerkehrsbereich", "15000.13");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_SchifffahrtslinieFaehrverkehr", "15000.14");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Schleuse", "15000.15");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Transportanlage", "15000.16");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Grenzuebergang", "15000.17");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Nullpunkt", "15000.18");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_WegPfadSteig", "15000.19");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_SonstigesBauwerkOderSonstigeEinrichtung", "15000.20");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Turm", "15000.21");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Hafen", "15000.22");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Gewaessermerkmal", "15000.23");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_BauwerkImGewaesserbereich", "15000.24");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Strassenverkehrsanlage", "15000.25");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_VorratsbehaelterSpeicherbauwerk", "15000.26");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_HistorischesBauwerkOderHistorischeEinrichtung", "15000.27");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Gleis", "15000.28");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_HeilquelleGasquelle", "15000.29");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Abschnitt", "15000.30");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Vegetationsmerkmal", "15000.31");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Polder", "15000.32");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_EinrichtungenFuerDenSchiffsverkehr", "15000.33");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Testgelaende", "15000.34");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_Gewaesserstationierungsachse", "15000.35");
	roleByNameMap.put(sr.getRoleName(), sr);
	sr = new SpecialRole("gehoertZuBauwerk_AX_SeilbahnSchwebebahn", "15000.36");
	roleByNameMap.put(sr.getRoleName(), sr);
    }

    public Optional<String> determineSpecialSequenceNumber(String roleName) {
	if (this.roleByNameMap.containsKey(roleName)) {
	    return Optional.of(this.roleByNameMap.get(roleName).getSequenceNumber());
	} else {
	    return Optional.empty();
	}
    }

    public boolean forceSequenceNumberTagUpdate(String roleName) {
	return this.roleByNameMap.containsKey(roleName);
    }

    private class SpecialRole {
	private final String roleName;
	private final String sequenceNumber;

	SpecialRole(String roleName, String sequenceNumber) {
	    this.roleName = roleName;
	    this.sequenceNumber = sequenceNumber;
	}

	public String getRoleName() {
	    return roleName;
	}

	public String getSequenceNumber() {
	    return sequenceNumber;
	}
    }

}
