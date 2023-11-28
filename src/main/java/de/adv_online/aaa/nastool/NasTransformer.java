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

import java.util.HashMap;
import java.util.Iterator;

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

import de.interactive_instruments.ShapeChange.Options;
import de.interactive_instruments.ShapeChange.ShapeChangeAbortException;
import de.interactive_instruments.ShapeChange.ShapeChangeResult;
import de.interactive_instruments.ShapeChange.Model.Transformer;

public class NasTransformer implements Transformer {

	private Repository rep = null;
	private ShapeChangeResult result = null;
	private HashMap<String,Package> allPackages = new HashMap<String,Package>();
	private HashMap<String,Element> allClasses = new HashMap<String,Element>();
	private HashMap<String,Element> aaaClasses = new HashMap<String,Element>();
	private int SeqNo = 32000;
	private String aaaVersion = "unbekannt";
	private String path = null;

	public void initialise(Options o, ShapeChangeResult r, String repositoryFileName) throws ShapeChangeAbortException {
		result = r;
        
		/** Make sure repository file exists */
		java.io.File repfile = new java.io.File(repositoryFileName);
		boolean ex = true;
		if(!repfile.exists()) {
			ex = false;
			if(!repositoryFileName.toLowerCase().endsWith(".qea")) {
				repositoryFileName += ".qea";
				repfile = new java.io.File(repositoryFileName);
				ex = repfile.exists();
			}
		}
		if(!ex) {
			r.addFatalError(null, 31, repositoryFileName);
			throw new ShapeChangeAbortException();
		}
		
		/** Connect to EA Repository */
		String absname = repfile.getAbsolutePath();
		rep = new Repository();
		if(!rep.OpenFile(absname)) {
			String errormsg = rep.GetLastError();
			r.addFatalError(null, 30, errormsg, repositoryFileName);
			throw new ShapeChangeAbortException();
		}
		
		path = o.parameter("transformerTargetPath");
	}

	public void shutdown() {
        rep.CloseFile();
        rep.Exit();
        rep = null;		
	}
	
	public void transform() throws ShapeChangeAbortException {
		
		prepareModel();
		
		if (!aaaVersion.startsWith("6.0")) {
    		result.addFatalError("Das AAA-Anwendungsschema ist von der falschen Version. Erwartet wurde die Version 6.0, gefunden wurde die Version "+aaaVersion);
			throw new ShapeChangeAbortException();
		}

		moveClass( "CharacterString", "ISO/TC 211" );
		moveClass( "Measure", "ISO/TC 211" );
		moveClass( "CV_DiscretePointCoverage", "ISO/TC 211" ); 
		setStereotypeClass( "CV_DiscretePointCoverage", "featureType" );
		moveClass( "CV_DiscreteGridPointCoverage", "ISO/TC 211" );
		setStereotypeClass( "CV_DiscreteGridPointCoverage", "featureType" );

		deletePackage("AAA_Katalog");
		deletePackage("AAA Versionierungsschema");
		deletePackage("ISO 19103 Conceptual Schema Language");
		deletePackage("ISO 19107 Spatial Schema");
		deletePackage("ISO 19108 Temporal");
		deletePackage("ISO 19109 Rules for Application Schema");
		deletePackage("ISO 19110 Feature Cataloging");
		deletePackage("ISO 19111 Spatial Referencing by Coordinates");
		deletePackage("ISO 19115 Metadata");
		deletePackage("ISO 19123 Coverages");
		deletePackage("ISO 19136 GML");
		deletePackage("Spatial Examples from ISO 19107");

		deleteClass("AA_ObjektOhneRaumbezug");

		deleteClass("AA_Liniengeometrie");
		deleteClass("AA_Flaechengeometrie");
		deleteClass("AU_Geometrie");
		deleteClass("AG_Geometrie");
		deleteClass("AA_Punktgeometrie");
		deleteClass("AU_Geometrie_3D");
		deleteClass("AA_Punktgeometrie_3D");
		deleteClass("AA_MehrfachLinienGeometrie_3D");
		deleteClass("AA_MehrfachFlaechenGeometrie_3D");
		deleteClass("AA_PunktLinienThema");
		deleteClass("TA_TopologieThema_3D");
		addAttribute("TA_PointComponent","position","GM_Point");
		addAttribute("TA_PointComponent_3D","position","GM_Point");
		addAttribute("TA_CurveComponent","position","GM_Curve");
		addAttribute("TA_CurveComponent_3D","position","GM_Curve");
		addAttribute("TA_SurfaceComponent","position","GM_Surface");
		addAttribute("TA_SurfaceComponent_3D","position","GM_Surface");
		addAttribute("TA_MultiSurfaceComponent","position","GM_Object");
		addAttribute("TA_CompositeSolidComponent_3D","position","GM_CompositeSolid");

		deleteAttribute("AA_Objekt","identifikator");

		changeType( "AU_Punkthaufenobjekt", "position", "GM_MultiPoint" );
		changeType( "AU_KontinuierlichesLinienobjekt", "position", "GM_Curve" );
		changeType( "AU_Flaechenobjekt", "position", "GM_Object" );
		changeType( "AG_Flaechenobjekt", "position", "GM_Object" );

		changeType( "AG_Punktobjekt", "position", "GM_Point" );

		changeType( "AU_Objekt", "position", "GM_Object" );
		changeType( "AG_Objekt", "position", "GM_Object" );
		changeType( "AU_GeometrieObjekt_3D", "position", "GM_Object" );
		changeType( "AU_MehrfachLinienObjekt_3D", "position", "GM_Object" );
		changeType( "AU_MehrfachFlaechenObjekt_3D", "position", "GM_Object" );
		changeType( "AU_UmringObjekt_3D", "position", "GM_MultiCurve" ) ;
		changeType( "AU_PunkthaufenObjekt_3D", "position", "GM_MultiPoint" );

		deleteClass( "AX_LI_Lineage_OhneDatenerhebung" );
		deleteClass( "AX_LI_Lineage_MitDatenerhebung" );
		deleteClass( "AX_LI_Lineage_Punktort" );

		deleteClass( "AX_LI_ProcessStep_OhneDatenerhebung" );
		deleteClass( "AX_LI_ProcessStep_MitDatenerhebung" );
		deleteClass( "AX_LI_ProcessStep_Punktort" );

		deleteClass( "AX_LI_Source_MitDatenerhebung" );
		deleteClass( "AX_LI_Source_Punktort" );

		changeType( "AX_DQOhneDatenerhebung", "herkunft", "LI_Lineage" );
		changeType( "AX_DQMitDatenerhebung", "herkunft", "LI_Lineage" );
		changeType( "AX_DQPunktort", "herkunft", "LI_Lineage" );

		deleteClass( "AX_Fortfuehrung" );
		deleteClass( "AX_Datenbank" );
		deleteClass( "AX_Operation_Datenbank" );
		deleteClass( "AX_TemporaererBereich" );
		deleteClass( "AX_NeuesObjekt" );
		deleteClass( "AX_GeloeschtesObjekt" );
		deleteClass( "AX_AktualisiertesObjekt" );
		deleteClass( "AX_Fortfuehrungsobjekt" );

		deleteRole("AA_Objekt","antrag");
		deleteRole("TA_MultiSurfaceComponent","masche");
		deleteRole("TA_CompositeSolidComponent_3D","koerper");

		addAttribute("AA_Antrag","art","GenericName");
		addAttribute("AA_Projektsteuerung","art","GenericName");
		addAttribute("AA_Vorgang","art","GenericName");
		addAttribute("AA_Aktivitaet","art","GenericName");
		deleteClass( "AA_Antragsart" );
		deleteClass( "AA_Projektsteuerungsart" );
		deleteClass( "AA_Vorgangsart" );
		deleteClass( "AA_Aktivitaetsart" );
		deleteClass( "AA_Projektsteuerungskatalog" );
		deleteClass( "AA_AktivitaetInVorgang" );
		deleteClass( "AA_VorgangInProzess" );
		deleteClass( "AA_Dokumentationsbedarf" );
		deleteClass( "AA_DurchfuehrungAktivitaet" );
		deleteClass( "AA_ProzesszuordnungAktivitaet" );
		
		deleteAttribute("AA_PMO","ausdehnung");
		deleteAttribute("AD_PunktCoverage","geometrie");
		deleteAttribute("AD_PunktCoverage","werte");
		deleteClass( "AD_ReferenzierbaresGitter" );
		deleteClass( "AD_Wertematrix" );
		
		changeType( "DCP", "HTTP", "URI" );
		changeType( "DCP", "email", "URI" );
		changeType( "AX_Schwereanomalie_Schwere", "wert", "Measure" );
		deleteClass( "Acceleration" );

		deleteClass( "AA_UUID" );
		changeType( "ExceptionFortfuehrung", "bereitsGesperrteObjekte", "URI" );
		changeType( "ExceptionFortfuehrung", "nichtMehrAktuelleObjekte", "URI" );
		changeType( "AX_Sperrauftrag", "uuidListe", "URI" );
		changeType( "AX_Entsperrauftrag", "uuidListe", "URI" );
		changeType( "ExceptionAAAEntsperren", "uuidListe", "URI" );
		changeType( "ExceptionAAAFortfuehrungOderSperrung", "bereitsGesperrteObjekte", "URI" );
		changeType( "ExceptionAAAFortfuehrungOderSperrung", "nichtMehrAktuelleObjekte", "URI" );

		for (Element e : aaaClasses.values()) {
			String n = e.GetName();
			String st = e.GetStereotype().toLowerCase();
			if (st.equals("enumeration") &&
				(n.startsWith("AX_LI") || n.startsWith("AX_DQ"))) {
				setTaggedValue(e.GetTaggedValues(), "xsdEncodingRule", "iso19139_2007");
			} else if (n.equals("AX_Datenerhebung") || n.equals("AX_Datenerhebung_Punktort")) {
				setTaggedValue(e.GetTaggedValues(), "xsdEncodingRule", "iso19139_2007");
			} else {
				setTaggedValue(e.GetTaggedValues(), "xsdEncodingRule", "iso19136_2007");
			}
			if (st.equals("featuretype")) {
				setTaggedValue(e.GetTaggedValues(), "noPropertyType", "true");
				setTaggedValue(e.GetTaggedValues(), "byValuePropertyType", "false");
				setTaggedValue(e.GetTaggedValues(), "isCollection", "false");
			} else if (st.equals("datatype") || st.equals("union")) {
				setTaggedValue(e.GetTaggedValues(), "noPropertyType", "false");
				setTaggedValue(e.GetTaggedValues(), "byValuePropertyType", "false");
				setTaggedValue(e.GetTaggedValues(), "isCollection", "false");
			} else if (st.equals("codelist")) {
				setTaggedValue(e.GetTaggedValues(), "asDictionary", "true");
			}
			deleteMethods(e);
			
			boolean cont = true;
			Connector ei;
			ConnectorEnd ei1, ei2;
			Element e2;
	        Collection<Connector> c = e.GetConnectors();
			while (cont) {
				cont = false;
		        for (short i = 0; i < c.GetCount(); i++) {
		        	ei = c.GetAt(i);
		        	String s = ei.GetType();
		        	if (!s.equals("Association") && !s.equals("Aggregation"))
		        		continue;
		        	if (ei.GetClientID()==0 || ei.GetSupplierID()==0) {
		        		c.Delete(i);
			        	if (!e.Update()) {
			        		result.addError("Fehler beim Löschen von hängender Relation in '"+e.GetName()+"': "+e.GetLastError());
			    		} else {
			            	result.addDebug("Hängende Relation in '"+e.GetName()+"' gelöscht.");
			    		}
			        	c.Refresh();
			        	cont = true;
			        	break;
		        	}
		        	if (ei.GetClientID()==e.GetElementID()) {
		        		if (ei.GetSupplierID()==e.GetElementID()) {
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
		        	s = e2.GetStereotype().toLowerCase();
		        	if (s.equals("featuretype") || s.equals("") || s.equals("type")) {
		        		setTaggedValueRole(ei2.GetTaggedValues(),"inlineOrByReference","byReference",true);
		        		if (!ei2.GetNavigable().equals("Navigable")) {
			        		setTaggedValueRole(ei2.GetTaggedValues(),"reverseRoleNAS","true",true);
		        			String mul = ei2.GetCardinality();
		        			if (mul.contains("1..")) {
		        				mul = mul.replace("1..","0..");
		        			} else if (mul.contains("2..")) {
			        				mul = mul.replace("2..","0..");
		        			} else if (!mul.contains("..")) {
		        				mul = "0.." + mul;
		        			} else if (!mul.contains("0..")) {
		        	    		result.addError("Fehler beim Aktualisieren von inverser Rolle in '"+e.GetName()+"' zu '"+e2.GetName()+"': Multiplizität '"+mul+"' nicht erkannt.");
		        			}
		        			ei2.SetCardinality(mul);
		        			ei2.SetNavigable("Navigable");
		        			if (ei2.GetRole().equals("")) {
		        				ei2.SetRole("inversZu_"+ei1.GetRole());
		        				setTaggedValueRole(ei2.GetTaggedValues(),"sequenceNumber", new Integer(SeqNo++).toString(), false);
		        		    	ei2.GetTaggedValues().Refresh();
		        			}
		        	    	if (!ei2.Update()) {
		        	    		result.addError("Fehler beim Aktualisieren von inverser Rolle in '"+e.GetName()+"' zu '"+e2.GetName()+"': "+ei2.GetLastError());
		        			} else {
		        	        	result.addDebug("Inverse Rolle in '"+e.GetName()+"' zu '"+e2.GetName()+"' aktualisiert.");
		        			}		        	
		        	    	e.GetConnectors().Refresh();
		        		}
		        	}
		        }
			}
		}

		for (Element e : aaaClasses.values()) {
			String st = e.GetStereotype().toLowerCase();
			if (st.equals("type"))
				copyDown(e);
		}

		deleteClass("AP_GPO");
		deleteClass("AP_TPO");
		deleteClass("AX_Katalogeintrag");
		deleteClass("AX_BauwerkeEinrichtungenUndSonstigeAngaben");
		deleteClass("AX_Punktort");
		deleteClass("AX_Flurstueck_Kerndaten");
		deleteClass("AA_Objektliste");
		deleteClass("AX_Auftrag");
		deleteClass("AX_Ergebnis");
		deleteClass("AX_AuftragEinrichtungOderFortfuerung");

		Element e1 = aaaClasses.get("AA_Objekt");
		if (e1==null) {
    		result.addError("Klasse 'AA_Objekt' nicht gefunden");
		} else {
			Element e2 = aaaClasses.get("AA_PMO");
			if (e2==null) {
	    		result.addError("Klasse 'AA_PMO' nicht gefunden");
			} else {
				Element e3 = aaaClasses.get("AD_PunktCoverage");
				if (e3==null) {
		    		result.addError("Klasse 'AD_PunktCoverage' nicht gefunden");
				} else {
					Element e4 = aaaClasses.get("AD_GitterCoverage");
					if (e4==null) {
			    		result.addError("Klasse 'AD_GitterCoverage' nicht gefunden");
					} else {
						for (Attribute a : e1.GetAttributes()) {
							cloneAttribute(a, e3);
							cloneAttribute(a, e4);
						}
						for (Attribute a : e2.GetAttributes()) {
							cloneAttribute(a, e3);
							cloneAttribute(a, e4);
						}
						addGeneralization(e3,allClasses.get("CV_DiscretePointCoverage"));
						addGeneralization(e4,allClasses.get("CV_DiscreteGridPointCoverage"));						
					}
				}
			}
		}
		
		deleteClass("AA_PMO");
		
		changeTypeAndMultiplicity( "AA_Objekt", "modellart", "AA_Modellart", "1", "*" ); 
		changeTypeAndMultiplicity( "AA_Objekt", "anlass", "AA_Anlassart", "0", "2" ); 
		changeTypeAndMultiplicity( "AA_Objekt", "zeigtAufExternes", "AA_Fachdatenverbindung", "0", "*" ); 
		changeTypeAndMultiplicity( "AD_PunktCoverage", "modellart", "AA_Modellart", "1", "*" ); 
		changeTypeAndMultiplicity( "AD_PunktCoverage", "anlass", "AA_Anlassart", "0", "2" ); 
		changeTypeAndMultiplicity( "AD_PunktCoverage", "zeigtAufExternes", "AA_Fachdatenverbindung", "0", "*" ); 
		changeTypeAndMultiplicity( "AD_GitterCoverage", "modellart", "AA_Modellart", "1", "*" ); 
		changeTypeAndMultiplicity( "AD_GitterCoverage", "anlass", "AA_Anlassart", "0", "2" ); 
		changeTypeAndMultiplicity( "AD_GitterCoverage", "zeigtAufExternes", "AA_Fachdatenverbindung", "0", "*" ); 
		
		changeType( "AX_Phasenzentrumsvariation_Referenzstationspunkt", "zeile", "doubleList" );
		deleteClass("AX_Phasenzentrumsvariation_Referenzstationspunkt_Zeile");
		
		if (path!=null && path.length()>0) {
			schemaLocationOfPackage("Web Feature Service Erweiterungen",path);
			schemaLocationOfPackage("OWS Common","http://schemas.opengis.net/");
			schemaLocationOfPackage("Web Feature Service",path);
			schemaLocationOfPackage("Web Feature Service Capabilities",path);
			schemaLocationOfPackage("Filter Encoding Capabilities",path);
		}
	}
	
	private void addGeneralization(Element e1, Element e2) {
		if (e1!=null && e2!=null) {
			e1.GetConnectors().Refresh();
	        Connector r = e1.GetConnectors().AddNew("", "Generalization");
	        if (r==null)
	    		result.addError("Fehler beim Erzeugen der Generalisierung '"+e1.GetName()+"'-'"+e2.GetName()+"'");
	        else {
	        	r.SetSupplierID(e2.GetElementID());
		        result.addDebug("Generalisierung '"+e1.GetName()+"'-'"+e2.GetName()+"' erzeugt");
		    	if (!r.Update()) {
		    		result.addError("Fehler bei Erzeugung der Generalisierung '"+e1.GetName()+"'-'"+e2.GetName()+"': "+r.GetLastError());
				}  		
	        }
		}
	}
	
	private void copyDown(Element e) {
		copyDown(e, e);
	}
	
	private void copyDown(Element e0, Element e) {
		for (Connector r : e.GetConnectors()) {
			if (r.GetType().equals("Generalization") && 
				r.GetSupplierID()==e.GetElementID()) {
				Element e2 = rep.GetElementByID(r.GetClientID());
				if (e2!=null) {
					String st = e2.GetStereotype().toLowerCase();
					if (st.equals("type")) {
						copyDown(e0, e2);
					} else {
						for (Attribute a : e0.GetAttributes()) {
							cloneAttribute(a, e2);
						}
						for (Connector r2 : e0.GetConnectors()) {
							String rt = r2.GetType();
							if (rt.equals("Association") || rt.equals("Aggregation")) {
								cloneAssociation(r2, e0, e2);
							}
						}
					}
				}
			}
		}
	}
	
	private void cloneAttribute(Attribute a, Element e) {
		String s = a.GetName();
		String s2 = e.GetName();
		Attribute a2 = e.GetAttributes().AddNew(a.GetName(), a.GetType());
    	if (!e.Update()) {
    		result.addError("Fehler beim Clonen von Attribut '"+a.GetName()+"': "+e.GetLastError());
		} else {
        	result.addDebug("Attribut '"+s+"' geclont (Zielklasse '"+s2+"').");
		}
    	e.GetAttributes().Refresh();
    	
		a2.SetLowerBound(a.GetLowerBound());
		a2.SetUpperBound(a.GetUpperBound());
		a2.SetNotes(a.GetNotes());
		a2.SetStereotype(a.GetStereotype());
    	if (!a2.Update()) {
    		result.addError("Fehler beim Clonen von Attribut '"+s+"': "+a2.GetLastError());
		}
		for (AttributeTag tv : a.GetTaggedValues()) {
			AttributeTag tv2 = a2.GetTaggedValues().AddNew(tv.GetName(), tv.GetValue());
	    	if (!tv2.Update()) {
	    		result.addError("Fehler beim Clonen von Attribut '"+s+"': "+tv.GetLastError());
			}
	    	a2.GetTaggedValues().Refresh();
		}
	}
	
	private void cloneAssociation(Connector r1, Element e1, Element e2) {
		Connector r2;
		Element e3;		
    	if (r1.GetClientID()==e1.GetElementID()) {
    		e3 = rep.GetElementByID(r1.GetSupplierID());
    		e2.GetConnectors().Refresh();
            r2 = e2.GetConnectors().AddNew("", r1.GetType());
            if (r2==null) {
        		result.addError("Fehler beim Clonen von Relation '"+e1.GetName()+"'/'"+e2.GetName()+"'-'"+e3.GetName()+"'");
        		return;
            }
    		r2.SetSupplierID(e3.GetElementID());
    	} else {
    		e3 = rep.GetElementByID(r1.GetClientID());
    		e3.GetConnectors().Refresh();
    		r2= e3.GetConnectors().AddNew("", r1.GetType());
            if (r2==null) {
        		result.addError("Fehler beim Clonen von Relation '"+e1.GetName()+"'/'"+e2.GetName()+"'-'"+e3.GetName()+"'");
        		return;
            }
    		r2.SetSupplierID(e2.GetElementID());
    	}

    	r2.SetDirection("Bi-Directional");
    	
		ConnectorEnd r1c, r1s, r2c, r2s;
		r1c = r1.GetClientEnd();
		r1s = r1.GetSupplierEnd();
		r2c = r2.GetClientEnd();
		r2s = r2.GetSupplierEnd();

		r2c.SetIsNavigable(r1c.GetIsNavigable());
		String s = r1c.GetCardinality();
		if (s.startsWith("1"))
			s = "0"+s.substring(1);
		r2c.SetCardinality(s);
		r2c.SetRoleNote(r1c.GetRoleNote());
		r2c.SetStereotype(r1c.GetStereotype());
    	if (r1.GetClientID()==e1.GetElementID()) {
    		r2c.SetRole(r1c.GetRole()+"_"+e2.GetName());
    	} else {
    		r2c.SetRole(r1c.GetRole());
    	}

		r2s.SetIsNavigable(r1s.GetIsNavigable());
		s = r1s.GetCardinality();
		if (s.startsWith("1"))
			s = "0"+s.substring(1);
		r2s.SetCardinality(s);
    	if (r1.GetClientID()==e1.GetElementID()) {
    		r2s.SetRole(r1s.GetRole());
    	} else {
    		r2s.SetRole(r1s.GetRole()+"_"+e2.GetName());    		
    	}
		r2s.SetRoleNote(r1s.GetRoleNote());
		r2s.SetStereotype(r1s.GetStereotype());

		result.addDebug("Relation '"+e1.GetName()+"'/'"+e2.GetName()+"'-'"+e3.GetName()+"' geclont");
    	if (!r2.Update()) {
    		result.addError("Fehler beim Clonen von Relation '"+e1.GetName()+"'/'"+e2.GetName()+"'-'"+e3.GetName()+"': "+r2.GetLastError());
		}  		

    	for (RoleTag tv : r1c.GetTaggedValues()) {
			RoleTag tv2 = r2c.GetTaggedValues().AddNew(tv.GetTag(), tv.GetValue());
	    	if (!tv2.Update()) {
	    		result.addError("Fehler beim Clonen von Relation '"+e1.GetName()+"'/'"+e2.GetName()+"'-'"+e3.GetName()+"': "+tv.GetLastError());
			}
	    	r2c.GetTaggedValues().Refresh();
		}

    	for (RoleTag tv : r1s.GetTaggedValues()) {
			RoleTag tv2 = r2s.GetTaggedValues().AddNew(tv.GetTag(), tv.GetValue());
	    	if (!tv2.Update()) {
	    		result.addError("Fehler beim Clonen von Relation '"+e1.GetName()+"'/'"+e2.GetName()+"'-'"+e3.GetName()+"': "+tv.GetLastError());
			}
	    	r2s.GetTaggedValues().Refresh();
		}
    	if (r1.GetClientID()==e1.GetElementID()) {
	    	setTaggedValueRole(r2c.GetTaggedValues(),"sequenceNumber", new Integer(SeqNo++).toString(),true);
	    	setTaggedValueRole(r2s.GetTaggedValues(),"sequenceNumber", new Integer(SeqNo++).toString(),false);
    	} else {
	    	setTaggedValueRole(r2c.GetTaggedValues(),"sequenceNumber", new Integer(SeqNo++).toString(),false);
	    	setTaggedValueRole(r2s.GetTaggedValues(),"sequenceNumber", new Integer(SeqNo++).toString(),true);    		
    	}
	}
	
	private void schemaLocationOfPackage(String name, String locprefix) {
		org.sparx.Package p = allPackages.get(name);
		if (p!=null) {
			Element e = p.GetElement();
			Collection<org.sparx.TaggedValue> cTV = e.GetTaggedValues();
			org.sparx.TaggedValue tv = cTV.GetByName("xsdDocument");
			if (tv==null) {
        		result.addError("TaggedValue 'xsdDocument' nicht vorhanden bei Paket '"+e.GetName()+"'");
			} 
			else {
				String v2 = tv.GetValue();
				tv.SetValue(locprefix+v2);
		        if (!tv.Update()) {
	        		result.addError("Fehler beim Setzen von TaggedValue 'xsdDocument'-'"+locprefix+v2+"': "+tv.GetLastError());
		        } else {
	        		result.addDebug("Setzen von TaggedValue 'xsdDocument'-'"+locprefix+v2+"' (alter Wert: '"+v2+"')");		        	
		        }
			}		
		} else {
        	result.addError("Package '"+name+"' nicht gefunden.");
		}
	}
	
	private void deletePackage(String name) {
		org.sparx.Package e = allPackages.get(name);
		if (e!=null) {
			Package parent = rep.GetPackageByID(e.GetParentID());
	        Package ei;
	        Collection<Package> c = parent.GetPackages();
	        for (short i = 0; i < c.GetCount(); i++) {
	        	ei = c.GetAt(i);
	        	if (ei.GetPackageID()==e.GetPackageID()) {
	        		c.Delete(i);
		        	if (!parent.Update()) {
		        		result.addError("Fehler beim Löschen von Package '"+name+"': "+parent.GetLastError());
		    		} else {
		            	result.addDebug("Package '"+name+"' gelöscht.");
		    		}
		        	c.Refresh();
	        		break;
	        	}
	        }
		} else {
        	result.addError("Package '"+name+"' nicht gefunden.");
		}
	}

	/* unused
	private void movePackage(String name, String pname) {
		org.sparx.Package p1 = allPackages.get(name);
		if (p1!=null) {
			org.sparx.Package p2 = allPackages.get(pname);
			if (p2!=null) {
				p1.SetParentID(p2.GetPackageID());
	        	if (!p2.Update()) {
	        		result.addError("Fehler beim Verschieben von Paket '"+name+"': "+p2.GetLastError());
	    		} else {
	            	result.addDebug("Paket '"+name+"' verschoben.");
	    		}
			} else {
	        	result.addError("Paket '"+pname+"' nicht gefunden.");
			}
		} else {
        	result.addError("Paket '"+name+"' nicht gefunden.");
		}
	}	
	*/
	
	private void setTaggedValue(Collection<org.sparx.TaggedValue> cTV, String n, String v) {
		org.sparx.TaggedValue tv = cTV.GetByName(n);
		if (tv==null) {
			tv = cTV.AddNew(n, v);
	        if (!tv.Update()) {
        		result.addError("Fehler beim Setzen von TaggedValue '"+n+"'-'"+v+"': "+tv.GetLastError());
	        } else {
        		result.addDebug("Setzen von TaggedValue '"+n+"'-'"+v+"'");
	        }
	        cTV.Refresh();
		} 
		else {
			String v2 = tv.GetValue();
			if (v2.equals(v)) {
        		result.addDebug("Setzen von TaggedValue '"+n+"'-'"+v+"' (bestehender Wert)");
			} else {
				tv.SetValue(v);
		        if (!tv.Update()) {
	        		result.addError("Fehler beim Setzen von TaggedValue '"+n+"'-'"+v+"'/'"+v2+"': "+tv.GetLastError());
		        } else {
	        		result.addDebug("Setzen von TaggedValue '"+n+"'-'"+v+"' (alter Wert: '"+v2+"')");		        	
		        }
			}
		}		
	}

	private void setTaggedValueRole(Collection<org.sparx.RoleTag> cTV, String n, String v, boolean force) {
		org.sparx.RoleTag tv=null; 
		Iterator<RoleTag> itr =	cTV.iterator(); 
        while(itr.hasNext()) {
        	org.sparx.RoleTag tv2 = itr.next();
    		if (tv2.GetTag().equals(n)) {
    			tv = tv2;
    			break;
    		}
        }
		if (tv==null) {
			tv = cTV.AddNew(n, v);
	        if (!tv.Update()) {
        		result.addError("Fehler beim Setzen von TaggedValue '"+n+"'-'"+v+"': "+tv.GetLastError());
	        } else {
        		result.addDebug("Setzen von TaggedValue '"+n+"'-'"+v+"'");
	        }
	        cTV.Refresh();
		} 
		else {
			String v2 = tv.GetValue();
			if (v2.equals(v)) {
        		result.addDebug("Setzen von TaggedValue '"+n+"'-'"+v+"' (bestehender Wert)");
			} else if (!force) {
	        	result.addDebug("Setzen von TaggedValue '"+n+"'-'"+v2+"' (alter Wert wird nicht geändert durch '"+v+"')");		        	
			} else {
				tv.SetValue(v);
		        if (!tv.Update()) {
	        		result.addError("Fehler beim Setzen von TaggedValue '"+n+"'-'"+v+"'/'"+v2+"': "+tv.GetLastError());
		        } else {
	        		result.addDebug("Setzen von TaggedValue '"+n+"'-'"+v+"' (alter Wert: '"+v2+"')");		        	
		        }
			}
		}		
	}
	
	private void deleteClass(String name) {
		org.sparx.Element e = allClasses.get(name);
		if (e!=null) {
			Package parent = rep.GetPackageByID(e.GetPackageID());
	        Element ei;
	        Collection<Element> c = parent.GetElements();
	        for (short i = 0; i < c.GetCount(); i++) {
	        	ei = c.GetAt(i);
	        	if (ei.GetElementID()==e.GetElementID()) {
	        		c.Delete(i);
		        	if (!parent.Update()) {
		        		result.addError("Fehler beim Löschen von Klasse '"+name+"': "+parent.GetLastError());
		    		} else {
		            	result.addDebug("Klasse '"+name+"' gelöscht.");
		    		}
		        	c.Refresh();
	        		break;
	        	}
	        }
		} else {
        	result.addError("Klasse '"+name+"' nicht gefunden.");
		}
	}

	private void deleteMethods(Element e) {
		while (e.GetMethods().GetCount()>0) {
			e.GetMethods().Delete((short)0);
	    	if (!e.Update()) {
	    		result.addError("Fehler beim Löschen der Methoden von Klasse '"+e.GetName()+"': "+e.GetLastError());
			} else {
	        	result.addDebug("Methode von Klasse '"+e.GetName()+"' gelöscht.");
			}
			e.GetMethods().Refresh();
		}
	}	

	private void moveClass(String name, String pname) {
		org.sparx.Element e = allClasses.get(name);
		if (e!=null) {
			org.sparx.Package p = allPackages.get(pname);
			if (p!=null) {
				e.SetPackageID(p.GetPackageID());
	        	if (!e.Update()) {
	        		result.addError("Fehler beim Verschieben von Klasse '"+name+"': "+e.GetLastError());
	    		} else {
	            	result.addDebug("Klasse '"+name+"' verschoben.");
	    		}
			} else {
	        	result.addError("Package '"+pname+"' nicht gefunden.");
			}
		} else {
        	result.addError("Klasse '"+name+"' nicht gefunden.");
		}
	}	

	private void setStereotypeClass(String name, String st) {
		org.sparx.Element e = allClasses.get(name);
		if (e!=null) {
			e.SetStereotype(st);
        	if (!e.Update()) {
        		result.addError("Fehler beim Setzen des Stereotyps von Klasse '"+name+"': "+e.GetLastError());
    		} else {
            	result.addDebug("Stereotyp von Klasse '"+name+"' gesetzt.");
    		}
		} else {
        	result.addError("Klasse '"+name+"' nicht gefunden.");
		}
	}	
	
	private void addAttribute(String cname, String name, String type) {
		org.sparx.Element e = allClasses.get(cname);
		if (e!=null) {
			Attribute a = e.GetAttributes().AddNew(name, type);
        	if (!a.Update()) {
        		result.addError("Fehler beim Ergänzen von Attribut '"+cname+"."+name+"': "+a.GetLastError());
    		} else {
            	result.addDebug("Attribut '"+cname+"."+name+"' ergänzt.");
    		}
			e.GetAttributes().Refresh();
		} else {
        	result.addError("Klasse '"+cname+"' nicht gefunden.");
		}
	}	

	private void deleteAttribute(String cname, String name) {
		org.sparx.Element e = allClasses.get(cname);
		if (e!=null) {
	        Attribute ei;
	        Collection<Attribute> c = e.GetAttributes();
	        for (short i = 0; i < c.GetCount(); i++) {
	        	ei = c.GetAt(i);
	        	if (ei.GetName().equals(name)) {
	        		c.Delete(i);
		        	if (!e.Update()) {
		        		result.addError("Fehler beim Löschen von Attribut '"+cname+"."+name+"': "+e.GetLastError());
		    		} else {
		            	result.addDebug("Attribut '"+cname+"."+name+"' gelöscht.");
		    		}
		        	c.Refresh();
	        		break;
	        	}
	        }
		} else {
        	result.addError("Klasse '"+cname+"' nicht gefunden.");
		}
	}	

		
	private void changeTypeAndMultiplicity(String cname, String name, String tname, String lower, String upper) {
		org.sparx.Element e = allClasses.get(cname);
		if (e!=null) {
	        Attribute ei;
	        Collection<Attribute> c = e.GetAttributes();
	        for (short i = 0; i < c.GetCount(); i++) {
	        	ei = c.GetAt(i);
	        	if (ei.GetName().equals(name)) {
	        		ei.SetType(tname);
	        		ei.SetLowerBound(lower);
	        		ei.SetUpperBound(upper);
		        	if (!ei.Update()) {
		        		result.addError("Fehler beim Setzen der Multiplizität von Attribut '"+cname+"."+name+"': "+ei.GetLastError());
		    		} else {
		            	result.addDebug("Multiplizität von Attribut '"+cname+"."+name+"' geändert.");
		    		}
		        	c.Refresh();
	        		break;
	        	}
	        }
		} else {
        	result.addError("Klasse '"+cname+"' nicht gefunden.");
		}
	}	

	private void changeType(String cname, String name, String tname) {
		org.sparx.Element e = allClasses.get(cname);
		if (e!=null) {
	        Attribute ei;
	        Collection<Attribute> c = e.GetAttributes();
	        for (short i = 0; i < c.GetCount(); i++) {
	        	ei = c.GetAt(i);
	        	if (ei.GetName().equals(name)) {
	        		ei.SetType(tname);
		        	if (!ei.Update()) {
		        		result.addError("Fehler beim Setzen des Typs von Attribut '"+cname+"."+name+"': "+ei.GetLastError());
		    		} else {
		            	result.addDebug("Typ von Attribut '"+cname+"."+name+"' geändert.");
		    		}
		        	c.Refresh();
	        		break;
	        	}
	        }
		} else {
        	result.addError("Klasse '"+cname+"' nicht gefunden.");
		}
	}	

	private void deleteRole(String cname, String name) {
		org.sparx.Element e = allClasses.get(cname);
		if (e!=null) {
			Connector ei;
	        Collection<Connector> c = e.GetConnectors();
	        for (short i = 0; i < c.GetCount(); i++) {
	        	ei = c.GetAt(i);
	        	if (ei.GetClientEnd().GetRole().equals(name) ||
	        		ei.GetSupplierEnd().GetRole().equals(name)) {
	        		c.Delete(i);
		        	if (!e.Update()) {
		        		result.addError("Fehler beim Löschen von Rolle '"+cname+"."+name+"': "+e.GetLastError());
		    		} else {
		            	result.addDebug("Rolle '"+cname+"."+name+"' gelöscht.");
		    		}
		        	c.Refresh();
	        		break;
	        	}
	        }
		} else {
        	result.addError("Klasse '"+cname+"' nicht gefunden.");
		}
	}	
	
	private void prepareModel() throws ShapeChangeAbortException {
        for (Package p : rep.GetModels()) {
        	preparePackage(p,false);
        }
	}

	private void preparePackage(Package p, boolean aaa) throws ShapeChangeAbortException {
		
		// remove version control
		try {
			if (p.VersionControlGetStatus()!=0) {
				p.VersionControlRemove();
	        	if (!p.Update()) {
	        		result.addFatalError("EA-Fehler: "+p.GetLastError());
	    			throw new ShapeChangeAbortException();
	    		}
			}
		}
		catch (Exception e) {
            e.printStackTrace();
            result.addFatalError("EA-Fehler: "+rep.GetLastError());
			throw new ShapeChangeAbortException();
		}
		
		if (p.GetName().equals("AFIS-ALKIS-ATKIS Anwendungsschema")) {
			aaa = true;
			TaggedValue tv = p.GetElement().GetTaggedValues().GetByName("version");
			if (tv!=null)
				aaaVersion = tv.GetValue();
		}
		
        for (org.sparx.Package p2 : p.GetPackages()) {
        	preparePackage(p2,aaa);
        }
        
        // ... and delete all diagrams
        while (p.GetDiagrams().GetCount()>0) {
        	p.GetDiagrams().Delete((short) 0);
        	if (!p.Update()) {
        		result.addFatalError("EA-Fehler: "+p.GetLastError());
    			throw new ShapeChangeAbortException();
    		}
        	p.GetDiagrams().Refresh();
        }
        
        // ... remember package by name
        String s = p.GetName();
        if (allPackages.containsKey(s)) {
        	result.addDebug("Information: Paket '"+s+"' mehrfach vorhanden.");
        } else {
        	allPackages.put(s, p);
        }
        
        Collection<Element> c = p.GetElements();
        c.Refresh();
        for (Element e : c) {
        	if (e.GetType().equals("Class")) {
            	s = e.GetName();
                if (allClasses.containsKey(s)) {
                	result.addDebug("Information: Klasse '"+s+"' mehrfach vorhanden.");
                } else {
                	allClasses.put(s, e);
                }
            	if (aaa) 
            		aaaClasses.put(s, e);
        	}
        }
	}

}
