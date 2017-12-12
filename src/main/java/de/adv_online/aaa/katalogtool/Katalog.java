/**
 * AAA-Katalogtool
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

package de.adv_online.aaa.katalogtool;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.adv_online.aaa.profiltool.ProfilRep;
import de.interactive_instruments.ShapeChange.Converter;
import de.interactive_instruments.ShapeChange.Options;
import de.interactive_instruments.ShapeChange.ShapeChangeAbortException;
import de.interactive_instruments.ShapeChange.ShapeChangeResult;
import de.interactive_instruments.ShapeChange.Type;
import de.interactive_instruments.ShapeChange.Model.ClassInfo;
import de.interactive_instruments.ShapeChange.Model.Constraint;
import de.interactive_instruments.ShapeChange.Model.Info;
import de.interactive_instruments.ShapeChange.Model.Model;
import de.interactive_instruments.ShapeChange.Model.PackageInfo;
import de.interactive_instruments.ShapeChange.Model.PropertyInfo;
import de.interactive_instruments.ShapeChange.Model.TaggedValues;
import de.interactive_instruments.ShapeChange.ModelDiff.DiffElement;
import de.interactive_instruments.ShapeChange.ModelDiff.DiffElement.Operation;
import de.interactive_instruments.ShapeChange.ModelDiff.Differ;
import de.interactive_instruments.ShapeChange.ModelDiff.DiffElement.ElementType;
import de.interactive_instruments.ShapeChange.ShapeChangeResult.MessageContext;
import de.interactive_instruments.ShapeChange.Target.Target;
import de.interactive_instruments.ShapeChange.UI.StatusBoard;

/**
 * @author Clemens Portele (portele <at> interactive-instruments <dot> de)
 *
 */
public class Katalog implements Target {

	public static final int STATUS_WRITE_RTF = 21;
	public static final int STATUS_WRITE_NART = 22;
	public static final int STATUS_WRITE_HTML = 23;
	public static final int STATUS_WRITE_XML = 24;
	public static final int STATUS_WRITE_GFC = 25;
	public static final int STATUS_WRITE_CSV = 26;
	
	private PackageInfo pi = null;
	private Model model = null;
	private Options options = null;
	private ShapeChangeResult result = null;
	private boolean printed = false;
	private Document document = null;
	private String outputDirectory = null;
	private Boolean error = false; 
	private Element root = null;

	private String Prefixes = "";
	private String Package = "";
	private HashSet<ClassInfo> additionalClasses = new HashSet<ClassInfo>();
	private HashSet<ClassInfo> enumerations = new HashSet<ClassInfo>();
	private HashSet<ProfilRep> profile = new HashSet<ProfilRep>();
	private Boolean OnlyGDB = false;
	private Boolean OnlyProfile = false;
	private Boolean Retired = false;
	private String[] MAList;
	private String[] PList;
	private String PQuelle = "Modell";
	private Model refModel = null;
	private PackageInfo refPackage = null;
	private SortedMap<Info, SortedSet<DiffElement>> diffs = null;
	private Differ differ = null;
	private Boolean Inherit = false;
	private HashSet<PropertyInfo> exportedAssociation = new HashSet<PropertyInfo>();
	private HashSet<PropertyInfo> processedProperty = new HashSet<PropertyInfo>();
	
	private Map<String, String> regeln = null; 
	private String OutputFormat  = "";

	// FIXME New diagnostics-only flag is to be considered
	public void initialise(PackageInfo p, Model m, Options o,
			ShapeChangeResult r, boolean diagOnly) throws ShapeChangeAbortException {
		pi = p;
		model = m;
		options = o;
		result = r;
		
		if (!options.gmlVersion.equals("3.2")) {
			result.addError(null, 110, pi.name());
			error = true;
			return;
		}
		
		outputDirectory = options.parameter(this.getClass().getName(),"Verzeichnis");
		if (outputDirectory==null)
			outputDirectory = options.parameter("outputDirectory");
		if (outputDirectory==null)
			outputDirectory = options.parameter(".");

		String s = options.parameter(this.getClass().getName(),"nurGrunddatenbestand");
		if (s!=null && s.equals("true"))
			OnlyGDB = true;

		s = options.parameter(this.getClass().getName(),"geerbteEigenschaften");
		if (s!=null && s.equals("true"))
			Inherit = true;

		s = options.parameter(this.getClass().getName(),"stillgelegteElemente");
		if (s!=null && s.equals("true"))
			Retired = true;

		s = options.parameter(this.getClass().getName(),"profile");
		if (s==null || s.trim().length()==0)
			PList = new String[0];
		else
			PList = s.trim().split(",");
		if (PList.length>0)
			OnlyProfile = true;
		
		s = options.parameter(this.getClass().getName(),"profilquelle");
		if (s!=null)
			PQuelle = s.trim();
		if (!PQuelle.equals("Datei") && !PQuelle.equals("Modell")) {
			result.addError("Die Profilquelle '"+PQuelle+"' ist unbekannt, es wird 'Modell' verwendet.");
			PQuelle = "Modell";
		}
					
		s = options.parameter(this.getClass().getName(),"modellarten");
		if (s==null || s.trim().length()==0)
			MAList = new String[0];
		else
			MAList = s.trim().split(",");
		
		s = options.parameter(this.getClass().getName(),"schemakennungen");
		if (s!=null && s.length()>0)
			Prefixes = s;
		else
			Prefixes = "*";

		s = options.parameter(this.getClass().getName(),"paket");
		if (s!=null && s.length()>0)
			Package = s;
		else
			Package = "";

		s = options.parameter(this.getClass().getName(),"ausgabeformat");
		if (s!=null && s.length()>0)
			OutputFormat = s;
		else
			OutputFormat = "";

		refModel = getReferenceModel();
		if (refModel!=null) {
			SortedSet<PackageInfo> set = refModel.schemas(p.name());
			if (set.size()==1) {
				differ = new Differ(true, MAList);
				refPackage = set.iterator().next();
				diffs = differ.diff(p, refPackage);
				for (Entry<Info,SortedSet<DiffElement>> me : diffs.entrySet()) {
					MessageContext mc = result.addInfo("Model difference - "+me.getKey().fullName().replace(p.fullName(),p.name()));
					if (mc!=null) {
						for (DiffElement diff : me.getValue()) {
							s = diff.change+" "+diff.subElementType;
							if (diff.subElementType==ElementType.TAG)
								s += "("+diff.tag+")";
							if (diff.subElement!=null) {
								s += " "+diff.subElement.name(); 
								if (diff.subElementType==ElementType.CLASS || diff.subElementType==ElementType.SUBPACKAGE || diff.subElementType==ElementType.PROPERTY) {
									String s2 = diff.subElement.taggedValue("AAA:Kennung"); 
									if (s2!=null && !s2.isEmpty()) 
										s += " ("+s2+")";
								} else if (diff.subElementType==ElementType.ENUM) {
									String s2 = ((PropertyInfo)diff.subElement).initialValue(); 
									if (s2!=null && !s2.isEmpty()) 
										s += " ("+s2+")";
								}
							} else if (diff.diff!=null)
								s += " "+ differ.diff_toString(diff.diff).replace("[[/ins]][[ins]]", "").replace("[[/del]][[del]]", "").replace("[[ins]][[/ins]]", "").replace("[[del]][[/del]]", "");
							else
								s += " ???";
							mc.addDetail(s);
						}
					}
				}
			}
		}
					
		document = createDocument();

		ProcessingInstruction proci;
		proci = document.createProcessingInstruction("xml-stylesheet",
				"type='text/xsl' href='./aaa-html.xsl'");
		document.appendChild(proci);
		
		document.appendChild(document.createComment("(c) Arbeitsgemeinschaft der Vermessungsverwaltungen der Länder der Bundesrepublik Deutschland [http://www.adv-online.de/]"));

		root = document.createElement("FC_FeatureCatalogue");
		document.appendChild(root);
		addAttribute(document, root, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		addAttribute(document, root, "xsi:noNamespaceSchemaLocation", "Katalogtool.xsd");

		Element e1 = document.createElement("name");
		e1.setTextContent(pi.name());
		root.appendChild(e1);

		e1 = document.createElement("scope");
		s = pi.documentation() + "\n"+"Berücksichtigte Modellarten:";
		ClassInfo cma = model.classByName("AA_AdVStandardModell");
		Collection<PropertyInfo> cpropi = null;
		if (cma!=null)
			cpropi = cma.properties().values();

		for (String ma : MAList) {
			s += "\n"+ma+": ";
			String s1 = "(unbekannt)";
			if (cpropi!=null) {
				for (PropertyInfo propi : cpropi) {
					String siv = propi.initialValue();
					if (siv!=null && siv.equals(ma)) {
						s1 = propi.name();
						break;
					}	
				}
			}
			s += s1;
		}
		e1.setTextContent(s);
		root.appendChild(e1);
		
		for (String ma : MAList) {
			e1 = document.createElement("modellart");
			e1.setTextContent(ma);
			root.appendChild(e1);			
		}

		for (String pf : PList) {
			e1 = document.createElement("profil");
			e1.setTextContent(pf);
			root.appendChild(e1);			
		}

		e1 = document.createElement("versionNumber");
		e1.setTextContent(pi.version());
		root.appendChild(e1);
		
		s = pi.taggedValue("AAA:Datum");
		e1 = document.createElement("versionDate");
		if (s!=null)
			e1.setTextContent(pi.taggedValue("AAA:Datum"));
		else
			e1.setTextContent("(unbekannt)");
		root.appendChild(e1);			

		Element e2, e3, e4;
		e1 = document.createElement("producer");
		e2 = document.createElement("CI_ResponsibleParty");
		e3 = document.createElement("CI_MandatoryParty");
		e4 = document.createElement("organisationName");
		s = pi.taggedValue("AAA:Organisation");
		if (s!=null)
			e4.setTextContent(s);
		else
			e4.setTextContent("(unbekannt)");
		root.appendChild(e1);
		e1.appendChild(e2);
		e2.appendChild(e3);
		e3.appendChild(e4);
		
		if (refPackage!=null){
			e1 = document.createElement("referenceModelVersionNumber");
			root.appendChild(e1);
			e1.setTextContent(refPackage.version());
		}
		
		s = p.taggedValue("AAA:AAAVersion");
		String zielversion = "6.0.1";
		if (s!=null && !s.isEmpty())
			zielversion = s;

		for (String pf : PList) {
			if (PQuelle.equals("Datei"))
				profile.add(new ProfilRep(pi, model, options, result, zielversion, outputDirectory+"/"+pf+".3ap"));
			else {
				String[] sa = pf.split("_",2);
				profile.add(new ProfilRep(pi, model, options, result, sa[0], sa[1], zielversion));
			}
		}
		
		try {
			PrintPackage(pi,null);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	private Model getReferenceModel() {
		String imt = options.parameter(this.getClass().getName(),"referenceModelType");
		String mdl = options.parameter(this.getClass().getName(),"referenceModelFile");
		
		if (imt==null || mdl==null || imt.isEmpty() || mdl.isEmpty())
			return null;
		
		// Support original model type codes
		if (imt.equalsIgnoreCase("ea7"))
			imt = "de.interactive_instruments.ShapeChange.Model.EA.EADocument";
		else if (imt.equalsIgnoreCase("xmi10"))
			imt = "de.interactive_instruments.ShapeChange.Model.Xmi10.Xmi10Document";
		else if (imt.equalsIgnoreCase("gsip"))
			imt = "us.mitre.ShapeChange.Model.GSIP.GSIPDocument";
		
		Model m = null;
		
		// Get model object from reflection API
		@SuppressWarnings("rawtypes")
		Class theClass;
		try {
			theClass = Class.forName(imt);
			if (theClass==null) {
				result.addError(null, 17, imt); 
				result.addError(null, 22, mdl); 
				return null;
			}
			m = (Model)theClass.newInstance();
			if (m != null) {
				m.initialise(result, options, mdl);
			} else {
				result.addError(null, 17, imt); 
				result.addError(null, 22, mdl); 
				return null;
			}
		} catch (ClassNotFoundException e) {
			result.addError(null, 17, imt); 
			result.addError(null, 22, mdl); 
		} catch (InstantiationException e) {
			result.addError(null, 19, imt); 
			result.addError(null, 22, mdl); 
		} catch (IllegalAccessException e) {
			result.addError(null, 20, imt); 
			result.addError(null, 22, mdl); 
		} catch (ShapeChangeAbortException e) {
			result.addError(null, 22, mdl); 
			m = null;
		}
		return m;		
	}

	private void PrintPackage(PackageInfo pix, Operation op) throws Exception {
		if (ExportPackage(pix,op)) {
			Element e1, e2, e3;
			if (pix.containedPackages().size()==0) {
				e1 = document.createElement("AC_Objektartengruppe");
				if (op!=null)
					addAttribute(document,e1,"mode",op.toString());
				e3 = document.createElement("Objektbereichzugehoerigkeit");
			} else {
				e1 = document.createElement("AC_Objektbereich");
				e3 = null;
			}
			addAttribute(document,e1,"id","_P"+pix.id());
			if (op!=null)
				addAttribute(document,e1,"mode",op.toString());
			root.appendChild(e1);
			e2 = document.createElement("name");
			String s = pix.name();
			if (diffs!=null && diffs.get(pix)!=null)
				for (DiffElement diff : diffs.get(pix)) {
					if (diff.subElementType==ElementType.NAME) {
						s = differ.diff_toString(diff.diff);
						break;
					}
				}
			e2.setTextContent(PrepareToPrint(s));
			if (op!=null)
				addAttribute(document,e2,"mode",op.toString());
			e1.appendChild(e2);
			s = pix.documentation();
			if (diffs!=null && diffs.get(pix)!=null)
				for (DiffElement diff : diffs.get(pix)) {
					if (diff.subElementType==ElementType.DOCUMENTATION) {
						s = differ.diff_toString(diff.diff).replace("[[/ins]][[ins]]", "").replace("[[/del]][[del]]", "").replace("[[ins]][[/ins]]", "").replace("[[del]][[/del]]", "");
						break;
					}
				}
			if (s!=null && s.length()>0) {
				PrintLineByLine(s,"definition",e1,op);
			}
			s = pix.taggedValue("AAA:Kennung");
			if (diffs!=null && diffs.get(pix)!=null)
				for (DiffElement diff : diffs.get(pix)) {
					if (diff.subElementType==ElementType.TAG && diff.tag.equalsIgnoreCase("AAA:Kennung")) {
						s = differ.diff_toString(diff.diff);
						break;
					}
				}
			if (s!=null && s.length()>0) {
				e2 = document.createElement("code");
				e2.setTextContent(PrepareToPrint(s));
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);
			}
			if (e3!=null)
				e1.appendChild(e3);
			
			if (pix.stereotype("retired")) {
				e2 = document.createElement("retired");
				e2.setTextContent("true");
				e1.appendChild(e2);
			}
		}
		
		try {
			for (PackageInfo pix2 : pix.containedPackages()) {
				boolean found = false;
				if (diffs!=null && diffs.get(pix)!=null)
					for (DiffElement diff : diffs.get(pix)) {
						if (diff.subElementType==ElementType.SUBPACKAGE && diff.subElement==pix2 && diff.change==Operation.INSERT) {
							PrintPackage(pix2,Operation.INSERT);
							found = true;
							break;
						}
					}			
				if (!found)
					PrintPackage(pix2,op);
			}
			if (diffs!=null && diffs.get(pix)!=null)
				for (DiffElement diff : diffs.get(pix)) {
					if (diff.subElementType==ElementType.SUBPACKAGE && diff.change==Operation.DELETE) {
						PrintPackage((PackageInfo)diff.subElement,Operation.DELETE);
					}
					if (diff.subElementType==ElementType.CLASS && diff.change==Operation.DELETE) {
						int cat = ((ClassInfo)diff.subElement).category();
						if (cat!=Options.CODELIST && cat!=Options.ENUMERATION)
							PrintClass((ClassInfo)diff.subElement,true,Operation.DELETE,pix);
					}
				}			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private int count(String str, String substr) {
		int count = 0;
		int idx = 0;
	    while ((idx = str.indexOf(substr, idx)) != -1) {
	        idx++;
	        count++;
	    }

	    return count;
	}
	
	private void PrintLineByLine(String s, String ename, Element e1, Operation op) {
		boolean ins = false;
		boolean del = false;
		String[] lines = s.replace("\r\n", "\n").replace("\r", "\n").split("\n");
		for(String line : lines) {
			Element e2 = document.createElement(ename);
			
			line = PrepareToPrint(line);
			
			if (ins) {
				line = "[[ins]]"+line;
				ins = false;
			} else if (del) {
				line = "[[del]]"+line;
				del = false;
			}
			
			if (count(line,"[[ins]]")>count(line,"[[/ins]]")) {
				ins = true;
				line += "[[/ins]]";
			} else if (count(line,"[[del]]")>count(line,"[[/del]]")) {
				del = true;
				line += "[[/del]]";
			}			

			e2.setTextContent(line);
			if (op!=null)
				addAttribute(document,e2,"mode",op.toString());
			e1.appendChild(e2);
		}		
	}

	private String PrepareToPrint(String s) {
		s = s.trim();
		return s;
	}

	/** Add attribute to an element */
	protected void addAttribute(Document document, Element e, String name, String value) {
		Attr att = document.createAttribute(name);
		att.setValue(value);
		e.setAttributeNode(att);
	}

	protected Document createDocument() {
		Document document = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.newDocument();
		} catch (ParserConfigurationException e) {
			result.addFatalError(null, 2);
			String m = e.getMessage();
			if (m != null) {
				result.addFatalError(m);
			}
			e.printStackTrace(System.err);
			System.exit(1);
		} catch (Exception e) {
			result.addFatalError(e.getMessage());
			e.printStackTrace(System.err);
			System.exit(1);
		}

		return document;
	}
	
	private boolean packageInPackage(PackageInfo pi) {
		if (Package.length()==0)
			return true;
		if (pi.name().equals(Package))
			return true;
		if (pi.isSchema())
			return false;
		return packageInPackage(pi.owner());
	}

	/* (non-Javadoc)
	 * @see de.interactive_instruments.ShapeChange.Target.Target#process(de.interactive_instruments.ShapeChange.Model.ClassInfo)
	 */
	public void process(ClassInfo ci) {
		if (error)
			return;

		if (!Prefixes.contains("*") && !Prefixes.contains(ci.name().substring(0, 3))) {
			return;
		}
		
		if (!packageInPackage(ci.pkg()))
			return;

		Operation op = null;
		if (diffs!=null && diffs.get(ci.pkg())!=null)
			for (DiffElement diff : diffs.get(ci.pkg())) {
				if (diff.subElementType==ElementType.CLASS && ((ClassInfo)diff.subElement)==ci && diff.change==Operation.INSERT) {
					op=Operation.INSERT;
					break;
				}
			}
		if (op==null) {
			PackageInfo pix = ci.pkg();
			while (pix!=null) {
				if (diffs!=null && diffs.get(pix.owner())!=null)
					for (DiffElement diff : diffs.get(pix.owner())) {
						if (diff.subElementType==ElementType.SUBPACKAGE && ((PackageInfo)diff.subElement)==pix && diff.change==Operation.INSERT) {
							op=Operation.INSERT;
							pix=null;
							break;
						}
					}
				if (pix!=null)
					pix = pix.owner();
			}
		}
		
		int cat = ci.category();
		switch (cat) {
		case Options.FEATURE:
		case Options.OBJECT:
			PrintClass(ci,true,op,ci.pkg());
			break;
		case Options.MIXIN:
			PrintClass(ci,true,op,ci.pkg());
			break;
		case Options.DATATYPE:
		case Options.UNION:
		case Options.BASICTYPE:
			PrintClass(ci,true,op,ci.pkg());
			for (String t :  ci.supertypes()) {
				ClassInfo cix = model.classById(t);
				if (cix!=null && (Prefixes.contains("*") || Prefixes.contains(cix.name().substring(0, 3)))) {
					additionalClasses.add(cix);
				} 
			}
			break;
		case Options.CODELIST:
		case Options.ENUMERATION:
			// PrintValues(ci);
			break;
		}
	}

	private void PrintValues(ClassInfo ci, Operation op) {
				
		for (PropertyInfo propi : ci.properties().values()) {
			if (propi==null) continue;
			if (!ExportValue(propi)) continue;

			Operation top = op;
			if (diffs!=null && diffs.get(ci)!=null)
				for (DiffElement diff : diffs.get(ci)) {
					if (diff.subElementType==ElementType.ENUM && ((PropertyInfo)diff.subElement)==propi && diff.change==Operation.INSERT) {
						top = Operation.INSERT;
						break;
					}
				}			
			
			PrintValue(propi, top);
		}

		if (diffs!=null && diffs.get(ci)!=null)
			for (DiffElement diff : diffs.get(ci)) {
				if (diff.subElementType==ElementType.ENUM && diff.change==Operation.DELETE) {
					PrintValue((PropertyInfo)diff.subElement, Operation.DELETE);
				}
			}			
	}
	
	private void PrintValue(PropertyInfo propi, Operation op) {
		
		Element e1 = document.createElement("FC_Value");
		addAttribute(document,e1,"id","_A"+propi.id());
		if (op!=null)
			addAttribute(document,e1,"mode",op.toString());
		root.appendChild(e1);

		Element e2 = document.createElement("label");
		String s = propi.name();
		if (diffs!=null && diffs.get(propi)!=null)
			for (DiffElement diff : diffs.get(propi)) {
				if (diff.subElementType==ElementType.NAME) {
					s = differ.diff_toString(diff.diff);
					break;
				}
			}
		e2.setTextContent(PrepareToPrint(s));
		if (op!=null)
			addAttribute(document,e2,"mode",op.toString());
		e1.appendChild(e2);

		e2 = document.createElement("code");
		s = propi.initialValue();
		if (s==null || s.length()==0)
			s = "(wie Bezeichner)";
		e2.setTextContent(PrepareToPrint(s));
		if (op!=null)
			addAttribute(document,e2,"mode",op.toString());
		e1.appendChild(e2);

		if (propi.stereotype("retired")) {
			e2 = document.createElement("retired");
			e2.setTextContent("true");
			e1.appendChild(e2);
		}
		
		e2 = document.createElement("definition");
		s = propi.documentation();
		if (diffs!=null && diffs.get(propi)!=null)
			for (DiffElement diff : diffs.get(propi)) {
				if (diff.subElementType==ElementType.DOCUMENTATION) {
					s = differ.diff_toString(diff.diff);
					break;
				}
			}
		e2.setTextContent(PrepareToPrint(s));
		if (op!=null)
			addAttribute(document,e2,"mode",op.toString());
		e1.appendChild(e2);
		
		PrintStandardElements(propi,e1,op);
		
		String nart = propi.taggedValue("AAA:Nutzungsartkennung");
		if (nart!=null && nart.length()>0) {
			e2 = document.createElement("nutzungsartkennung");
			e2.setTextContent(nart);
			if (op!=null)
				addAttribute(document,e2,"mode",op.toString());
			e1.appendChild(e2);
		}
	}

	private boolean MatchingMA(String malist) {
		if (malist==null)
			return true;

		malist = malist.trim();
		
		if (malist.length()==0)
			return true;

		for (String ma : malist.split(",")) {
			ma = ma.trim();
			for (String max : MAList) {
				if (ma.equals(max)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean MatchingGDB(String malist) {
		if (!OnlyGDB)
			return true;
		
		if (malist==null)
			return false;

		malist = malist.trim();
		
		if (malist.length()==0)
			return false;

		for (String ma : malist.split(",")) {
			ma = ma.trim();
			for (String max : MAList) {
				if (ma.equals(max)) {
					return true;
				}
			}
		}		

		return false;
	}
	
	private boolean MatchingProfile(String pflist) {
		if (!OnlyProfile)
			return true;

		if (pflist==null)
			return false;
	
		pflist = pflist.trim();
		
		if (pflist.length()==0)
			return false;

		for (String pf : pflist.split(",")) {
			pf = pf.trim();
			for (String pfx : PList) {
				if (pf.equals(pfx)) {
					return true;
				}
			}
		}		

		return false;
	}

	private boolean ExportItem(Info i, boolean considerProfile) {
		if (!MatchingMA(i.taggedValue("AAA:Modellart")))
			return false;
		
		if (considerProfile && OnlyGDB && !MatchingGDB(i.taggedValue("AAA:Grunddatenbestand")))
			return false;
		
		if (considerProfile && OnlyProfile) {
			String s = "";
			for (ProfilRep pf : profile) {
				if (pf.contains(i)) {
					if (s.length()>0)
						s += ",";
					s += pf.name();
				}
			}
			if (!MatchingProfile(s))
				return false;
		}
		
		String s = i.name();
		if (!Retired && i.stereotype("retired"))
			return false;
		
		return true;
	}
	
	private boolean ExportValue(PropertyInfo propi) {
		return ExportItem(propi, true);
	}

	private boolean ExportProperty(PropertyInfo propi) {
		if (propi.name().length()==0)
			return false;
		
		if (propi.name().startsWith("role_"))
			return false;
		
		return ExportItem(propi, true);
	}

	private boolean ExportClass(ClassInfo ci, Boolean onlyProperties, Operation op) {
		if (!ci.inSchema(pi) && op!=Operation.DELETE && !onlyProperties)
			return false;

		if (!Prefixes.contains("*") && !Prefixes.contains(ci.name().substring(0, 3)) && !onlyProperties)
			return false;

		if (!packageInPackage(ci.pkg()) && op!=Operation.DELETE && !onlyProperties)
			return false;
		
		return ExportItem(ci, true);
	}

	private boolean ExportPackage(PackageInfo pi, Operation op) {
		if (!packageInPackage(pi) && op!=Operation.DELETE)
			return false;
		
		return ExportItem(pi, false);
	}

	private String getDocBrEkKbd(ClassInfo ci, String cat, String filter){
		String ret = null;
		String doc = ci.documentation();
		if (diffs!=null && diffs.get(ci)!=null)
			for (DiffElement diff : diffs.get(ci)) {
				if (diff.subElementType==ElementType.DOCUMENTATION) {
					doc = differ.diff_toString(diff.diff);
					break;
				}
			}

		String marker = options.parameter( this.getClass().getName(), "notesRuleMarker" );
		if(marker==null || marker.length()==0)
			marker = "-==-";
		
		if(regeln==null){
			regeln = new TreeMap<String, String>();
			regeln.put("BR", "Bildungsregel");
			regeln.put("EK", "Erfassungskriterium");
			regeln.put("KBD", "Konsistenzbedingung");
		}
		
		String reg = regeln.get(cat);
		if(reg==null)
			return null;
		
		if(doc!=null){
			filter = marker + " " + (reg + " " + filter).trim() + " " + marker;
			if(doc.contains(filter)){
				int start = doc.indexOf(filter)+filter.length();
				int end = doc.indexOf(marker, start);
				if(end==-1)
					end = doc.length();
				ret = doc.substring(start, end).trim();
			}
		}
		return ret;
	}
	
	private String getDoc(ClassInfo ci){

		String doc = ci.documentation();
		if (diffs!=null && diffs.get(ci)!=null)
			for (DiffElement diff : diffs.get(ci)) {
				if (diff.subElementType==ElementType.DOCUMENTATION) {
					doc = differ.diff_toString(diff.diff);
					break;
				}
			}
		
		String marker = options.parameter( this.getClass().getName(), "notesRuleMarker" );
		if(marker==null || marker.length()==0)
			marker = "-==-";
		
		if(doc!=null && doc.contains(marker)){
			int end = doc.indexOf(marker);
			if(end==-1)
				end = doc.length();
			doc = doc.substring(0, end).trim();
		}
		return doc;
	}
	
	private void PrintClass(ClassInfo ci, boolean onlyProperties, Operation op, PackageInfo pix) {
		if (!ExportClass(ci, onlyProperties, op))
			return;
		
		if (onlyProperties) {
			Element e1 = document.createElement("AC_FeatureType");
			addAttribute(document,e1,"id","_C"+ci.id());
			if (op!=null)
				addAttribute(document,e1,"mode",op.toString());
			root.appendChild(e1);
			
			Element e2 = document.createElement("name");
			String s = ci.name();
			if (diffs!=null && diffs.get(ci)!=null)
				for (DiffElement diff : diffs.get(ci)) {
					if (diff.subElementType==ElementType.NAME) {
						s = differ.diff_toString(diff.diff);
						break;
					}
				}
			e2.setTextContent(PrepareToPrint(s));
			if (op!=null)
				addAttribute(document,e2,"mode",op.toString());
			e1.appendChild(e2);
			
			s = getDoc(ci);
			String s1 = "";
			String s2 = "";
			String s3 = "";
			String s4 = "";
			
			s = s.replace("Lebenszeitinterval:", "Lebenszeitintervall:");
			s = s.replace("Lebenszeitintervallbescheibung:", "Lebenszeitintervall:");
			if (s.contains("Lebenszeitintervall:")) {
				String[] sarr = s.split("Lebenszeitintervall:");
				s = sarr[0];
				s1 = sarr[1];
			}
	
			if (s!=null && s.length()>0) {
				PrintLineByLine(s,"definition",e1,op);
			}
			
			if (ci.isAbstract()) {
				PrintLineByLine("Es handelt sich um eine abstrakte Objektart.","definition",e1,op);
			}
			
			s = ci.taggedValue("AAA:Kennung");
			if (diffs!=null && diffs.get(ci)!=null)
				for (DiffElement diff : diffs.get(ci)) {
					if (diff.subElementType==ElementType.TAG && diff.tag.equalsIgnoreCase("AAA:Kennung")) {
						s = differ.diff_toString(diff.diff);
						break;
					}
				}
			if (s!=null && s.length()>0) {
				e2 = document.createElement("code");
				e2.setTextContent(PrepareToPrint(s));
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);
			}

			if (ci.stereotype("retired")) {
				e2 = document.createElement("retired");
				e2.setTextContent("true");
				e1.appendChild(e2);
			}
			
			for (String t :  ci.supertypes()) {
				ClassInfo cix = model.classById(t);
				if (cix!=null) {
					e2 = document.createElement("subtypeOf");
					s = cix.name();
					if (diffs!=null && diffs.get(ci)!=null)
						for (DiffElement diff : diffs.get(ci)) {
							if (diff.subElementType==ElementType.SUPERTYPE && diff.change==Operation.INSERT && (ClassInfo)diff.subElement==cix) {
								s = "[[ins]]"+s+"[[/ins]]";
								break;
							}
						}		
					e2.setTextContent(s);
					if (op!=null)
						addAttribute(document,e2,"mode",op.toString());
					e1.appendChild(e2);					
				}
			}
			if (diffs!=null && diffs.get(ci)!=null)
				for (DiffElement diff : diffs.get(ci)) {
					if (diff.subElementType==ElementType.SUPERTYPE && diff.change==Operation.DELETE) {
						e2 = document.createElement("subtypeOf");
						s = "[[del]]"+diff.subElement.name()+"[[/del]]";
						e2.setTextContent(s);
						if (op!=null)
							addAttribute(document,e2,"mode",op.toString());
						e1.appendChild(e2);					
					}
				}		
			
			PrintProperties(ci, true, e1, op);
			
			e2 = document.createElement("Objektartengruppenzugehoerigkeit");
			if (op!=Operation.DELETE)
				addAttribute(document,e2,"idref","_P"+ci.pkg().id());
			else
				addAttribute(document,e2,"idref","_P"+pix.id());
			e1.appendChild(e2);					
			
			s = getDocBrEkKbd(ci, "BR", "");
			if (s!=null && s.length()>0) {
				s2 = s;
			}
			for (String ma : MAList) {
				s = getDocBrEkKbd(ci, "BR", ma);
				if (s!=null && s.length()>0) {
					s2 += "\n"+ma+": "+s;
				}
			}		
	
			// Old style for constraints
			s = getDocBrEkKbd(ci, "KBD", "");
			if (s!=null && s.length()>0) {
				s3 = s;
			}
			for (String ma : MAList) {
				s = getDocBrEkKbd(ci, "KBD", ma);
				if (s!=null && s.length()>0) {
					s3 += "\n"+ma+": "+s;
				}
			}
			
			// New style for constraints, only if no reference model, as otherwise this has already been taken into account so that we could diff it
			if (refModel==null) { 
				for (Constraint ocl : ci.constraints()) {
					s = null;
					
					// Ignore constraints on supertypes
					if (!ocl.contextModelElmt().id().equals(ci.id()))
						continue;
					
					if (ocl.name().equalsIgnoreCase("alle")) {
						s = ocl.text();
					} else {
						for (String ma : MAList) {
							if (ocl.name().equalsIgnoreCase(ma)) {
								s = ocl.text();
								break;
							}
						}
					}
					if (s!=null) {
						String[] sa = s.split("/\\*");
						for (String sc: sa) {
							sc = sc.trim();
							if (sc.isEmpty())
								continue;
							if (sc.contains("*/")) {
								sc = sc.replaceAll("\\*/.*","");
								sc = sc.trim();
							}
							if (ocl.name().equalsIgnoreCase("alle")) {
								s3 += "\n"+sc;
							} else {
								s3 += "\n"+ocl.name()+": "+sc;
							}
						}
					}
				}			
			}
			
			s = getDocBrEkKbd(ci, "EK", "");
			if (s!=null && s.length()>0) {
				s4 = s;
			}
			for (String ma : MAList) {
				s = getDocBrEkKbd(ci, "EK", ma);
				if (s!=null && s.length()>0) {
					s4 += "\n"+ma+": "+s;
				}
			}		
	
			if (s4.length()>0) {
				PrintLineByLine(s4,"Erfassungskriterium",e1,op);
			}
			
			if (s3.length()>0) {
				PrintLineByLine(s3,"Konsistenzbedingung",e1,op);
			}
	
			if (s2.length()>0) {
				PrintLineByLine(s2,"Bildungsregel",e1,op);
			}
	
			if (s1.length()>0) {
				PrintLineByLine(s1,"Lebenszeitintervall",e1,op);
			}
			
			if (ci.isKindOf("AA_REO")) {
				e2 = document.createElement("wirdTypisiertDurch");
				e2.setTextContent("REO");
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);					
			}
			if (ci.isKindOf("AA_NREO")) {
				e2 = document.createElement("wirdTypisiertDurch");
				e2.setTextContent("NREO");
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);					
			}
			if (ci.isKindOf("AA_ZUSO")) {
				e2 = document.createElement("wirdTypisiertDurch");
				e2.setTextContent("ZUSO");
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);					
			}
			if (ci.isKindOf("AA_PMO")) {
				e2 = document.createElement("wirdTypisiertDurch");
				e2.setTextContent("PMO");
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);					
			}
			
			switch (ci.category()) {
			case Options.FEATURE:
				e2 = document.createElement("bedeutung");
				e2.setTextContent("Objektart");
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);
				break;
			case Options.DATATYPE:
				e2 = document.createElement("bedeutung");
				e2.setTextContent("Datentyp");
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);
				break;
			case Options.UNION:
				e2 = document.createElement("bedeutung");
				e2.setTextContent("Auswahldatentyp");
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);
				break;
			}
			
			PrintStandardElements(ci,e1,op);
			
			s = ci.taggedValue("AAA:Nutzungsartkennung");
			if (s!=null && s.length()>0) {
				e2 = document.createElement("nutzungsartkennung");
				e2.setTextContent(s);
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);
			}
			
			s = ci.taggedValue("AAA:Nutzungsart");
			if (s!=null && s.length()>0) {
				e2 = document.createElement("nutzungsart");
				e2.setTextContent(s);
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);
			}
		}
		
		PrintProperties(ci, false, root, op);
	}

	private void PrintProperties(ClassInfo ci, boolean listOnly, Element e1, Operation op) {
		for (PropertyInfo propi : ci.properties().values()) {
			
			Operation top = op;
			if (diffs!=null && diffs.get(ci)!=null)
				for (DiffElement diff : diffs.get(ci)) {
					if (diff.subElementType==ElementType.PROPERTY && ((PropertyInfo)diff.subElement)==propi && diff.change==Operation.INSERT) {
						top = Operation.INSERT;
						break;
					}
				}			
						
			if (listOnly)
				PrintPropertyRef(propi, e1, top);
			else 
				PrintProperty(propi, top);
		}
		
		if (diffs!=null && diffs.get(ci)!=null)
			for (DiffElement diff : diffs.get(ci)) {
				if (diff.subElementType==ElementType.PROPERTY && diff.change==Operation.DELETE) {
					if (listOnly)
						PrintPropertyRef((PropertyInfo)diff.subElement, e1, Operation.DELETE);
					else 
						PrintProperty((PropertyInfo)diff.subElement, Operation.DELETE);
				}
			}			
		
		if (listOnly && Inherit) {
			for (String cid : ci.supertypes()) {
				ClassInfo cix = model.classById(cid);		
				if (cix!=null) {
					PrintProperties(cix, listOnly, e1, op);
				}
			}
		}
	}
	
	private void PrintPropertyRef(PropertyInfo propi, Element e1, Operation op) {
		if (ExportProperty(propi)) {
			Element e2 = document.createElement("characterizedBy");
			addAttribute(document,e2,"idref","_A"+propi.id());
			if (op!=null)
				addAttribute(document,e2,"mode",op.toString());
			e1.appendChild(e2);
		} 
	}

	private void PrintProperty(PropertyInfo propi, Operation op) {
		if (!ExportProperty(propi)) 
			return;
		
		if (processedProperty.contains(propi))
			return;
		
		Element e1, e2;
		String assocId = "__FIXME";
		if (!propi.isAttribute()) {
			if (!exportedAssociation.contains(propi)) {
				e1 = document.createElement("FC_FeatureRelationship");
				assocId = "__"+propi.id();
				addAttribute(document,e1,"id",assocId);
				root.appendChild(e1);
				e2 = document.createElement("name");
				e2.setTextContent(PrepareToPrint("(unbestimmt)"));
				e1.appendChild(e2);
				e2 = document.createElement("roles");
				addAttribute(document,e2,"idref","_A"+propi.id());
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);
				exportedAssociation.add(propi);
				PropertyInfo propi2 = propi.reverseProperty();
				if (propi2!=null) {
					if (ExportProperty(propi2)) {
						e2 = document.createElement("roles");
						addAttribute(document,e2,"idref","_A"+propi2.id());
						if (op!=null)
							addAttribute(document,e2,"mode",op.toString());
						e1.appendChild(e2);
					}
					exportedAssociation.add(propi2);
				}
			} else {
				PropertyInfo propi2 = propi.reverseProperty();
				if (propi2!=null) {
					assocId = "__"+propi2.id();
				}
			}
		}
		
		PrintPropertyDetail(propi,assocId,op);
		PropertyInfo propi2 = propi.reverseProperty();
		if (propi2!=null && ExportProperty(propi2))
			PrintPropertyDetail(propi2,assocId,op);
		
		processedProperty.add(propi);
	}

	private void PrintPropertyDetail(PropertyInfo propi, String assocId, Operation op) {
		Element e1, e2;
		if (propi.isAttribute())
			e1 = document.createElement("FC_FeatureAttribute");
		else
			e1 = document.createElement("FC_RelationshipRole");
		addAttribute(document,e1,"id","_A"+propi.id());
		if (op!=null)
			addAttribute(document,e1,"mode",op.toString());
		root.appendChild(e1);
			
		e2 = document.createElement("name");
		String s = propi.name();
		if (diffs!=null && diffs.get(propi)!=null)
			for (DiffElement diff : diffs.get(propi)) {
				if (diff.subElementType==ElementType.NAME) {
					s = differ.diff_toString(diff.diff);
					break;
				}
			}
		e2.setTextContent(PrepareToPrint(s));
		if (op!=null)
			addAttribute(document,e2,"mode",op.toString());
		e1.appendChild(e2);
		
		e2 = document.createElement("cardinality");
		s = propi.cardinality().toString();
		if (diffs!=null && diffs.get(propi)!=null)
			for (DiffElement diff : diffs.get(propi)) {
				if (diff.subElementType==ElementType.MULTIPLICITY) {
					s = differ.diff_toString(diff.diff);
					break;
				}
			}
		e2.setTextContent(PrepareToPrint(s));
		if (op!=null)
			addAttribute(document,e2,"mode",op.toString());
		e1.appendChild(e2);
			
		s = propi.documentation();
		if (diffs!=null && diffs.get(propi)!=null)
			for (DiffElement diff : diffs.get(propi)) {
				if (diff.subElementType==ElementType.DOCUMENTATION) {
					s = differ.diff_toString(diff.diff);
					break;
				}
			}
		if (s!=null && s.length()>0) {
			PrintLineByLine(s,"definition",e1,op);
		}
		
		if (!propi.isAttribute() && !propi.isNavigable()) {
			PrintLineByLine("Es handelt sich um die inverse Relationsrichtung.","definition",e1,op);
		}
		
		if (propi.isDerived()) {
			PrintLineByLine("Es handelt sich um eine abgeleitete Eigenschaft.","definition",e1,op);
		}
		
		s = propi.initialValue();
		if (propi.isAttribute() && s!=null && s.length()>0) {
			PrintLineByLine("Das Attribut ist bei Objekterzeugung mit dem Wert "+PrepareToPrint(s)+" vorbelegt.","definition",e1,op);
		}			
			
		s = propi.taggedValue("AAA:Kennung");
		if (diffs!=null && diffs.get(propi)!=null)
			for (DiffElement diff : diffs.get(propi)) {
				if (diff.subElementType==ElementType.TAG && diff.tag.equalsIgnoreCase("AAA:Kennung")) {
					s = differ.diff_toString(diff.diff);
					break;
				}
			}
		if (s!=null && s.length()>0) {
			e2 = document.createElement("code");
			if (propi.isDerived())
				s = "(DER) "+s;
			e2.setTextContent(PrepareToPrint(s));
			if (op!=null)
				addAttribute(document,e2,"mode",op.toString());
			e1.appendChild(e2);
		}

		if (propi.stereotype("retired")) {
			e2 = document.createElement("retired");
			e2.setTextContent("true");
			e1.appendChild(e2);
		}
				
		s = propi.taggedValue("AAA:objektbildend");
		if (s!=null && s.toLowerCase().equals("true")) {
			e2 = document.createElement("objektbildend");
			e2.setTextContent("true");
			if (op!=null)
				addAttribute(document,e2,"mode",op.toString());
			e1.appendChild(e2);
		}
			
		PrintStandardElements(propi,e1,op);

		e2 = document.createElement("inType");
		addAttribute(document,e2,"idref","_C"+propi.inClass().id());
		addAttribute(document,e2,"name",propi.inClass().name());
		if (op!=null)
			addAttribute(document,e2,"mode",op.toString());
		e1.appendChild(e2);					
		
		Type ti = propi.typeInfo();
		if (!propi.isAttribute()) {
			if (ti!=null) {
				e2 = document.createElement("FeatureTypeIncluded");
				ClassInfo cix = model.classById(ti.id);
				if (cix!=null && ExportClass(cix,false, null)) {
					addAttribute(document,e2,"idref","_C"+ti.id);
				}
				s = ti.name;
				if (diffs!=null && diffs.get(propi)!=null)
					for (DiffElement diff : diffs.get(propi)) {
						if (diff.subElementType==ElementType.VALUETYPE) {
							s = differ.diff_toString(diff.diff);
							break;
						}
					}
				addAttribute(document,e2,"name",s);
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);					
			}
			e2 = document.createElement("relation");
			addAttribute(document,e2,"idref",assocId);
			e1.appendChild(e2);
			PropertyInfo propi2 = propi.reverseProperty();
			if (propi2!=null && ExportProperty(propi2)) {
				e2 = document.createElement("InverseRole");
				addAttribute(document,e2,"idref","_A"+propi2.id());
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);
			}
			e2 = document.createElement("orderIndicator");
			if (propi.isOrdered())
				e2.setTextContent("1");
			else
				e2.setTextContent("0");		
			if (op!=null)
				addAttribute(document,e2,"mode",op.toString());
			e1.appendChild(e2);
		} else {
			if (ti!=null) {
				ClassInfo cix;
				if (op!=Operation.DELETE)
					cix = model.classById(ti.id);
				else
					cix = refModel.classById(ti.id);
				if (cix!=null) {
					int cat = cix.category();
					switch (cat) {
					case Options.CODELIST:
					case Options.ENUMERATION:
						e2 = document.createElement("ValueDataType");
						s = cix.name();
						if (diffs!=null && diffs.get(propi)!=null)
							for (DiffElement diff : diffs.get(propi)) {
								if (diff.subElementType==ElementType.VALUETYPE) {
									s = differ.diff_toString(diff.diff);
									break;
								}
							}
						e2.setTextContent(PrepareToPrint(s));
						if (op!=null)
							addAttribute(document,e2,"mode",op.toString());
						e1.appendChild(e2);
						e2 = document.createElement("ValueDomainType");
						if (op!=null)
							addAttribute(document,e2,"mode",op.toString());
						e1.appendChild(e2);
						if (!cix.name().equals("Boolean")) {
							e2.setTextContent("1");
							for (PropertyInfo ei : cix.properties().values()) {
								if (ei!=null && ExportValue(ei)) {
									e2 = document.createElement("enumeratedBy");
									addAttribute(document,e2,"idref","_A"+ei.id());
									e1.appendChild(e2);
								}
							}
							if (diffs!=null && diffs.get(cix)!=null)
								for (DiffElement diff : diffs.get(cix)) {
									if (diff.subElementType==ElementType.ENUM && diff.change==Operation.DELETE) {
										e2 = document.createElement("enumeratedBy");
										addAttribute(document,e2,"idref","_A"+((PropertyInfo)diff.subElement).id());
										e1.appendChild(e2);
									}
								}			
							if (op!=Operation.DELETE) {
								if (cix.inSchema(propi.inClass().pkg()))
									enumerations.add(cix);
							} else {
								if (cix.inSchema(refPackage))
									enumerations.add(cix);
							}
						} else {
							e2.setTextContent("0");
						}
						break;
					default:
						e2 = document.createElement("ValueDataType");
						if (ExportClass(cix,false,null))
							addAttribute(document,e2,"idref","_C"+cix.id());
						s = cix.name();
						if (diffs!=null && diffs.get(propi)!=null)
							for (DiffElement diff : diffs.get(propi)) {
								if (diff.subElementType==ElementType.VALUETYPE) {
									s = differ.diff_toString(diff.diff);
									break;
								}
							}
						e2.setTextContent(PrepareToPrint(s));
						if (op!=null)
							addAttribute(document,e2,"mode",op.toString());
						e1.appendChild(e2);
						e2 = document.createElement("ValueDomainType");
						e2.setTextContent("0");
						if (op!=null)
							addAttribute(document,e2,"mode",op.toString());
						e1.appendChild(e2);
						break;
					}
				} else {
					e2 = document.createElement("ValueDataType");
					s = ti.name;
					if (diffs!=null && diffs.get(propi)!=null)
						for (DiffElement diff : diffs.get(propi)) {
							if (diff.subElementType==ElementType.VALUETYPE) {
								s = differ.diff_toString(diff.diff);
								break;
							}
						}
					e2.setTextContent(PrepareToPrint(s));
					if (op!=null)
						addAttribute(document,e2,"mode",op.toString());
					e1.appendChild(e2);
				}
			} else {
				e2 = document.createElement("ValueDataType");
				e2.setTextContent("(unbestimmt)");
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);
			}
		}		
	}
	
	private void PrintStandardElements(Info i, Element e1, Operation op) {
		Element e2;
		
		String s = i.taggedValue("AAA:Modellart");
		if (s!=null) {
			for (String ma : s.split(",")) {
				ma = ma.trim();
				for (String max : MAList) {
					if (ma.contains(max)) {
						e2 = document.createElement("modellart");
						e2.setTextContent(ma);
						boolean found = false;
						if (diffs!=null && diffs.get(i)!=null)
							for (DiffElement diff : diffs.get(i)) {
								if (diff.subElementType==ElementType.AAAMODELLART && diff.tag.equals(ma) && diff.change==Operation.INSERT) {
									addAttribute(document,e2,"mode",Operation.INSERT.toString());
									found = true;
									break;
								}
							}
						if (!found && op!=null)
							addAttribute(document,e2,"mode",op.toString());
						e1.appendChild(e2);					
						break;
					}
				}
			}
		}
		if (diffs!=null && diffs.get(i)!=null)
			for (DiffElement diff : diffs.get(i)) {
				if (diff.subElementType==ElementType.AAAMODELLART && diff.change==Operation.DELETE) {
					e2 = document.createElement("modellart");
					e2.setTextContent(diff.tag);
					addAttribute(document,e2,"mode",Operation.DELETE.toString());
					e1.appendChild(e2);					
				}
			}
		
		s = i.taggedValue("AAA:Grunddatenbestand");
		if (s!=null) {
			for (String ma : s.split(",")) {
				ma = ma.trim();
				for (String max : MAList) {
					if (ma.contains(max)) {
						e2 = document.createElement("grunddatenbestand");
						e2.setTextContent(ma);
						boolean found = false;
						if (diffs!=null && diffs.get(i)!=null)
							for (DiffElement diff : diffs.get(i)) {
								if (diff.subElementType==ElementType.AAAGRUNDDATENBESTAND && diff.tag.equals(ma) && diff.change==Operation.INSERT) {
									addAttribute(document,e2,"mode",Operation.INSERT.toString());
									found = true;
									break;
								}
							}
						if (!found && op!=null)
							addAttribute(document,e2,"mode",op.toString());
						e1.appendChild(e2);					
						break;
					}
				}
			}
		}
		if (diffs!=null && diffs.get(i)!=null)
			for (DiffElement diff : diffs.get(i)) {
				if (diff.subElementType==ElementType.AAAGRUNDDATENBESTAND && diff.change==Operation.DELETE) {
					e2 = document.createElement("grunddatenbestand");
					e2.setTextContent(diff.tag);
					addAttribute(document,e2,"mode",Operation.DELETE.toString());
					e1.appendChild(e2);					
				}
			}
		
		s = "";
		for (ProfilRep pf : profile) {
			if (pf.contains(i)) {
				if (s.length()>0)
					s += ",";
				s += pf.name();
			}
		}
		if (s!=null) {
			for (String pf : s.split(",")) {
				pf = pf.trim();
				for (String pfx : PList) {
					if (pf.equals(pfx)) {
						e2 = document.createElement("profil");
						e2.setTextContent(pf);
						if (op!=null)
							addAttribute(document,e2,"mode",op.toString());
						e1.appendChild(e2);					
						break;
					}
				}
			}
		}
		
		TaggedValues taggedValues = i.taggedValuesForTagList(options.parameter("representTaggedValues"));
		if(!taggedValues.isEmpty()) {
			// sort results alphabetically by tag name for consistent output
			TreeSet<String> tags = new TreeSet<String>(taggedValues.keySet());
			for(String tag : tags) {
				// sort values
				String[] values = taggedValues.get(tag);
				List<String> valueList = Arrays.asList(values);
				Collections.sort(valueList);
				
				for(String v : values) {
					if (v.trim().length() > 0) {
						e2 = document.createElement("taggedValue");
						e2.setTextContent(v);
						addAttribute(document,e2,"tag",tag);
						e1.appendChild(e2);					
					}
				}
			}
		}

		s = i.taggedValue("AAA:Revisionsnummer");
		if (s!=null && !s.isEmpty()) {
			e2 = document.createElement("letzteAenderungRevisionsnummer");
			e2.setTextContent(PrepareToPrint(s));
			e1.appendChild(e2);
		} 		
	}

	/* (non-Javadoc)
	 * @see de.interactive_instruments.ShapeChange.Target.Target#write()
	 */
	public void write() {
		if (error || printed)
			return;

		for (ClassInfo cix : additionalClasses) {
			if (Prefixes.contains("*") || Prefixes.contains(cix.name().substring(0, 3))) {
				Operation top = null;
				if (diffs!=null && diffs.get(cix.pkg())!=null)
					for (DiffElement diff : diffs.get(cix.pkg())) {
						if (diff.subElementType==ElementType.CLASS && (ClassInfo)diff.subElement==cix) {
							top = diff.change;
							break;
						}
					}
				PrintClass(cix,false,top,cix.pkg());
			}
		}
		for (ClassInfo cix : enumerations) {
			Operation top = null;
			if (diffs!=null && diffs.get(cix.pkg())!=null)
				for (DiffElement diff : diffs.get(cix.pkg())) {
					if (diff.subElementType==ElementType.CLASS && (ClassInfo)diff.subElement==cix) {
						top = diff.change;
						break;
					}
				}
			PrintValues(cix,top);
		}
		
		Properties outputFormat = OutputPropertiesFactory.getDefaultMethodProperties("xml");
		outputFormat.setProperty("indent", "yes");
		outputFormat.setProperty("{http://xml.apache.org/xalan}indent-amount", "2");
		outputFormat.setProperty("encoding", model.characterEncoding());

		try {
			String xmlName = pi.xsdDocument().replace(".xsd", "")+".tmp.xml";
	        OutputStream fout= new FileOutputStream(outputDirectory + "/" + xmlName);
	        OutputStream bout= new BufferedOutputStream(fout);
	        OutputStreamWriter outputXML = new OutputStreamWriter(bout, outputFormat.getProperty("encoding"));

			Serializer serializer = SerializerFactory.getSerializer(outputFormat);
			serializer.setWriter(outputXML);
			serializer.asDOMSerializer().serialize(document);
			outputXML.close();
			
			String outfileBasename = pi.xsdDocument().replace(".xsd", "");

			writeNART(xmlName, outfileBasename);
			writeRTF(xmlName, outfileBasename);
			writeHTML(xmlName, outfileBasename);
			writeXML(xmlName, outfileBasename);
			writeGFC(xmlName, outfileBasename);
			writeCSV(xmlName, outfileBasename);

	        File outDir = new File(outputDirectory);
			File xmlFile = new File(outDir, xmlName);
			
			String s = options.parameter(this.getClass().getName(),"tmpLoeschen");
			if (s!=null && s.equalsIgnoreCase("true"))
				xmlFile.delete();
			
		} catch (Exception e) {
			String m = e.getMessage();
			if (m != null) {
				result.addError(m);
			}
			e.printStackTrace(System.err);
		}
		
		if (refModel!=null)
			refModel.shutdown();
		
		printed = true;
	}

	private void writeRTF(String xmlName, String outfileBasename){
		StatusBoard.getStatusBoard().statusChanged(STATUS_WRITE_RTF);

		if(!OutputFormat.toLowerCase().contains("rtf"))
			return;
		
		String xslfofileName = options.parameter(this.getClass().getName(),"xslrtfFile");
		if (xslfofileName==null)
			xslfofileName = "aaa-rtf.xsl";
		String rtffileName = outfileBasename+".rtf";
		
		if(xmlName!=null && xmlName.length()>0
				&& xslfofileName!=null && xslfofileName.length()>0
				&& rtffileName!=null && rtffileName.length()>0){
			xsltWrite(xmlName, xslfofileName, rtffileName);
		}
	}
	
	private void writeHTML(String xmlName, String outfileBasename){
		StatusBoard.getStatusBoard().statusChanged(STATUS_WRITE_HTML);

		if(!OutputFormat.toLowerCase().contains("html"))
			return;
		
		String xslfofileName = options.parameter(this.getClass().getName(),"xslhtmlFile");
		if (xslfofileName==null)
			xslfofileName = "aaa-html.xsl";
		String htmlfileName = outfileBasename+".html";
		
		if(xmlName!=null && xmlName.length()>0
				&& xslfofileName!=null && xslfofileName.length()>0
				&& htmlfileName!=null && htmlfileName.length()>0){
			xsltWrite(xmlName, xslfofileName, htmlfileName);
		}
	}
	
	private void writeXML(String xmlName, String outfileBasename){
		StatusBoard.getStatusBoard().statusChanged(STATUS_WRITE_XML);

		if(!OutputFormat.toLowerCase().contains("xml"))
			return;
		
		String xslfofileName = options.parameter(this.getClass().getName(),"xslxmlFile");
		if (xslfofileName==null)
			xslfofileName = "aaa-xml.xsl";
		String xmloutFileName = outfileBasename+".xml";
		
		if(xmlName!=null && xmlName.length()>0
				&& xslfofileName!=null && xslfofileName.length()>0
				&& xmloutFileName!=null && xmloutFileName.length()>0){
			xsltWrite(xmlName, xslfofileName, xmloutFileName);
		}
	}
	
	private void writeGFC(String xmlName, String outfileBasename){
		StatusBoard.getStatusBoard().statusChanged(STATUS_WRITE_GFC);

		if(!OutputFormat.toLowerCase().contains("gfc"))
			return;
		
		String xslfofileName = options.parameter(this.getClass().getName(),"xslgfcFile");
		if (xslfofileName==null)
			xslfofileName = "aaa-xml-gfc.xsl";
		String xmloutFileName = outfileBasename+".gfc.xml";
		
		if(xmlName!=null && xmlName.length()>0
				&& xslfofileName!=null && xslfofileName.length()>0
				&& xmloutFileName!=null && xmloutFileName.length()>0){
			xsltWrite(xmlName, xslfofileName, xmloutFileName);
		}
	}
	
	private void writeCSV(String xmlName, String outfileBasename){
		StatusBoard.getStatusBoard().statusChanged(STATUS_WRITE_CSV);

		if(!OutputFormat.toLowerCase().contains("csv"))
			return;
		
		String xslfofileName = options.parameter(this.getClass().getName(),"xslcsvFile");
		if (xslfofileName==null)
			xslfofileName = "aaa-csv.xsl";
		String csvoutFileName = outfileBasename+".csv";
		
		if(xmlName!=null && xmlName.length()>0
				&& xslfofileName!=null && xslfofileName.length()>0
				&& csvoutFileName!=null && csvoutFileName.length()>0){
			xsltWrite(xmlName, xslfofileName, csvoutFileName);
		}
	}
	
	private void writeNART(String xmlName, String outfileBasename){
		StatusBoard.getStatusBoard().statusChanged(STATUS_WRITE_NART);

		if(!OutputFormat.toLowerCase().contains("csv"))
			return;
		
		String xslfofileName = options.parameter(this.getClass().getName(),"xslnartcsvFile");
		if (xslfofileName==null)
			xslfofileName = "aaa-nart-csv.xsl";
		String csvoutFileName = outfileBasename+"-nart.csv";
		
		if(xmlName!=null && xmlName.length()>0
				&& xslfofileName!=null && xslfofileName.length()>0
				&& csvoutFileName!=null && csvoutFileName.length()>0){
			xsltWrite(xmlName, xslfofileName, csvoutFileName);
		}
	}
	
   private void xsltWrite(String xmlName, String xsltfileName, String outfileName){
	   try{
			String xsltPath = options.parameter(this.getClass().getName(),"xsltPfad");
			if (xsltPath==null)
				xsltPath = "src/main/resources/xslt";

			// Setup directories
            File outDir = new File(outputDirectory);

            // Setup input and output files
            File xmlFile = new File(outDir, xmlName);
           	File outFile = new File(outDir, outfileName);
	        
           	if(!xmlFile.canRead()){
            	result.addError(null, 301, xmlFile.getName(), outfileName);
            	return;
            }
		    
		    Source xmlSource = new StreamSource(xmlFile);
		    Result res = new StreamResult(outFile);
		 
			if (xsltfileName!=null && !xsltfileName.isEmpty()) {

				StreamSource xsltSource = null;
	           	if (xsltPath.toLowerCase().startsWith("http")) {
	           		// get xslt via URL
	           		URL url = new URL(xsltPath+"/"+xsltfileName);
	           		URLConnection urlConnection = url.openConnection();
	           		xsltSource = new StreamSource(urlConnection.getInputStream());
	           	} else {
	           		// get it from the file system
	           		try {
						File xsltFile = new File(xsltPath+"/"+xsltfileName);
						if(!xsltFile.canRead()){
							throw new Exception("Cannot read "+xsltPath+"/"+xsltFile.getName());
						}
						xsltSource = new StreamSource(xsltFile);
	           		} catch (Exception e) {
		           		// try to get it from the JAR file
						InputStream stream = getClass().getResourceAsStream("/xslt/"+xsltfileName);
						xsltSource = new StreamSource(stream);
	           		}
	           	}
	           	
				if (xsltSource!=null) {
				    TransformerFactory transFact = TransformerFactory.newInstance( );
				    Transformer trans = transFact.newTransformer(xsltSource);
				    trans.transform(xmlSource, res);
					result.addResult(getTargetName(), outputDirectory, outfileName, options.parameter(this.getClass().getName(),"modellarten"));
				}
			}
		    

	   } catch (Exception e) {
			String m = e.getMessage();
			if (m != null) {
				result.addError(m);
			}
	       e.printStackTrace(System.err);
	   }	
		    
	}

@Override
public String getTargetName() {
	return "AAA-Objektartenkatalog";
}
}
