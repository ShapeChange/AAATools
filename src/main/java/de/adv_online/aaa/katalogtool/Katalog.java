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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.adv_online.aaa.profiltool.ProfilRep;
import de.interactive_instruments.ShapeChange.MessageSource;
import de.interactive_instruments.ShapeChange.Options;
import de.interactive_instruments.ShapeChange.RuleRegistry;
import de.interactive_instruments.ShapeChange.ShapeChangeAbortException;
import de.interactive_instruments.ShapeChange.ShapeChangeResult;
import de.interactive_instruments.ShapeChange.Type;
import de.interactive_instruments.ShapeChange.Model.ClassInfo;
import de.interactive_instruments.ShapeChange.Model.Constraint;
import de.interactive_instruments.ShapeChange.Model.ImageMetadata;
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
import de.interactive_instruments.ShapeChange.Util.XsltWriter;
import de.interactive_instruments.ShapeChange.Util.ZipHandler;

/**
 * @author Clemens Portele (portele <at> interactive-instruments <dot> de)
 *
 */
public class Katalog implements Target, MessageSource {

	public static final int STATUS_WRITE_HTML = 23;
	public static final int STATUS_WRITE_XML = 24;
	public static final int STATUS_WRITE_CSV = 26;
	public static final int STATUS_WRITE_DOCX = 27;

	/**
	 * The string used as placeholder in the docx template. The paragraph this
	 * placeholder text belongs to will be replaced with the feature catalogue.
	 */
	public static final String DOCX_PLACEHOLDER = "ShapeChangeFeatureCatalogue";
	public static final String DOCX_TEMPLATE_URL = "resources/templates/aaa-template.docx";
	
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
	
	@Override
	public void registerRulesAndRequirements(RuleRegistry r) {
		// nothing to add
	}

	@Override
	public String getTargetIdentifier() {
		return "aaa-katalogtool";
	}

	@Override
	public String getDefaultEncodingRule() {
		// not relevant for this target
		return null;
	}	

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
				differ = new Differ(true, MAList, model, refModel);
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

		document.appendChild(document.createComment("(c) Arbeitsgemeinschaft der Vermessungsverwaltungen der Länder der Bundesrepublik Deutschland [http://www.adv-online.de/]"));

		root = document.createElement("FC_FeatureCatalogue");
		document.appendChild(root);

		Element e1 = document.createElement("name");
		e1.setTextContent(pi.name());
		root.appendChild(e1);

		ClassInfo cma = model.classByName("AA_AdVStandardModell");
		Collection<PropertyInfo> cpropi = null;
		if (cma!=null)
			cpropi = cma.properties().values();

		for (String ma : MAList) {
			e1 = document.createElement("modellart");
			e1.setTextContent(ma);
			if (cpropi!=null) {
				for (PropertyInfo propi : cpropi) {
					String siv = propi.initialValue();
					if (siv!=null && siv.equals(ma)) {
						e1.setAttribute("name", propi.name());
						break;
					}
				}
			}
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

		if (refPackage!=null){
			e1 = document.createElement("referenceModelVersionNumber");
			root.appendChild(e1);
			e1.setTextContent(refPackage.version());
		}
		
		s = p.taggedValue("AAA:AAAVersion");
		if (s!=null && !s.isEmpty()) {
			e1 = document.createElement("aaaVersionNumber");
			root.appendChild(e1);
			e1.setTextContent(s);
		}

		String zielversion = "6.0.1";
		if (s!=null && !s.isEmpty())
			zielversion = s;		
		
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
		else if (imt.equalsIgnoreCase("scxml"))
			imt = "de.interactive_instruments.ShapeChange.Model.Generic.GenericModel";
		
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
				addAttribute(document,e3,"idref","_"+pix.owner().id());
			} else {
				e1 = document.createElement("AC_Objektbereich");
				e3 = null;
			}
			addAttribute(document,e1,"id","_"+pix.id());
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
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);
			}

			String nart = pix.taggedValue("AAA:Nutzungsartkennung");
			if (nart!=null && nart.length()>0) {
				e2 = document.createElement("nutzungsartkennung");
				e2.setTextContent(nart);
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);
			}
		}
		
		try {
			for (PackageInfo pix2 : pix.containedPackages()) {
				boolean found = false;
				if (diffs!=null && diffs.get(pix)!=null && ExportPackage(pix2,op))
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
			if (diffs!=null && diffs.get(pix)!=null && ExportPackage(pix,op))
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
		PrintLineByLine(s, ename, null, null, e1, op);
	}

	private void PrintLineByLine(String s, String ename, String aname, String aval, Element e1, Operation op) {
		boolean ins = false;
		boolean del = false;
		String[] lines = s.replace("\r\n", "\n").replace("\r", "\n").split("\n");
		for(String line : lines) {
			if (line.isEmpty())
				continue;
			Element e2 = document.createElement(ename);
			if (aname!=null && aval!=null && !aval.isEmpty())
				e2.setAttribute(aname, aval);
			
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

	/** Add attribute to an element 
	 * @param document tbd
	 * @param e tbd
	 * @param name tbd
	 * @param value tbd
	 *  */
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
		
		PackageInfo pix = ci.pkg();
		if (!packageInPackage(pix))
			return;

		Operation op = null;
		if (diffs!=null && pix!=null & diffs.get(pix)!=null)
			for (DiffElement diff : diffs.get(pix)) {
				if (diff.subElementType==ElementType.CLASS && ((ClassInfo)diff.subElement)==ci && diff.change==Operation.INSERT) {
					op=Operation.INSERT;
					break;
				}
			}
		if (op==null) {
			while (pix!=null) {
				if (diffs!=null && pix.owner()!=null && diffs.get(pix.owner())!=null)
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
			Map<String,String> br = new HashMap<>();
			Map<String,String> kbd = new HashMap<>();
			Map<String,String> ek = new HashMap<>();

			if (s!=null && s.length()>0) {
				PrintLineByLine(s,"definition",e1,op);
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
				addAttribute(document,e2,"idref","_"+ci.pkg().id());
			else
				addAttribute(document,e2,"idref","_"+pix.id());
			e1.appendChild(e2);					
			
			s = getDocBrEkKbd(ci, "BR", "");
			if (s!=null && s.length()>0) {
				br.put("*", s);
			}
			for (String ma : MAList) {
				s = getDocBrEkKbd(ci, "BR", ma);
				if (s!=null && s.length()>0) {
					br.put(ma, s);
				}
			}		
	
			// Old style for constraints
			s = getDocBrEkKbd(ci, "KBD", "");
			if (s!=null && s.length()>0) {
				kbd.put("*", s);
			}
			for (String ma : MAList) {
				s = getDocBrEkKbd(ci, "KBD", ma);
				if (s!=null && s.length()>0) {
					kbd.put(ma, s);
				}
			}
			
			// New style for constraints, only if no reference model, as otherwise this has already been taken into account so that we could diff it
			if (refModel==null || op==Operation.INSERT) { 
				for (Constraint ocl : ci.constraints()) {
					s = null;
					
					// Ignore constraints on supertypes
					if (!ocl.contextModelElmt().id().equals(ci.id()))
						continue;

					String makbd = null;
					if (ocl.name().equalsIgnoreCase("alle")) {
						s = ocl.text();
						makbd = "*";
					} else {
						for (String ma : MAList) {
							if (ocl.name().equalsIgnoreCase(ma)) {
								s = ocl.text();
								makbd = ma;
								break;
							}
						}
					}
					if (s!=null && makbd!=null) {
						String[] sa = s.split("/\\*");
						String res = "";
						for (String sc: sa) {
							sc = sc.trim();
							if (sc.isEmpty())
								continue;
							if (sc.contains("*/")) {
								sc = sc.replaceAll("\\*/.*","");
								sc = sc.trim();
							}
							res += sc + System.lineSeparator();
						}
						res = res.trim();
						if (!res.isEmpty())
							kbd.put(makbd, res);
					}
				}			
			}
			
			s = getDocBrEkKbd(ci, "EK", "");
			if (s!=null && s.length()>0) {
				ek.put("*", s);
			}
			for (String ma : MAList) {
				s = getDocBrEkKbd(ci, "EK", ma);
				if (s!=null && s.length()>0) {
					ek.put(ma, s);
				}
			}		

			for (Map.Entry<String, String> entry : ek.entrySet()) {
				PrintLineByLine(entry.getValue(), "erfassungskriterium", "modellart", entry.getKey(), e1, op);
			}

			for (Map.Entry<String, String> entry : kbd.entrySet()) {
				PrintLineByLine(entry.getValue(), "konsistenzbedingung", "modellart", entry.getKey(), e1, op);
			}

			for (Map.Entry<String, String> entry : br.entrySet()) {
				PrintLineByLine(entry.getValue(), "bildungsregel", "modellart", entry.getKey(), e1, op);
			}

			if (ci.isAbstract()) {
				e2 = document.createElement("abstrakt");
				e2.setTextContent("true");
				if (op!=null)
					addAttribute(document,e2,"mode",op.toString());
				e1.appendChild(e2);
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
				PrintProperty(ci, propi, top, op==Operation.DELETE);
		}
		
		if (diffs!=null && diffs.get(ci)!=null)
			for (DiffElement diff : diffs.get(ci)) {
				if (diff.subElementType==ElementType.PROPERTY && diff.change==Operation.DELETE) {
					if (listOnly)
						PrintPropertyRef((PropertyInfo) diff.subElement, e1, Operation.DELETE);
					else 
						PrintProperty(ci, (PropertyInfo)diff.subElement, Operation.DELETE, op==Operation.DELETE);
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

	private void PrintProperty(ClassInfo ci, PropertyInfo propi, Operation op, boolean deletedClass) {
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
		
		PrintPropertyDetail(deletedClass?propi.inClass():ci,propi,assocId,op);
		PropertyInfo propi2 = propi.reverseProperty();
		if (propi2!=null && ExportProperty(propi2)) {
			ClassInfo ci2;
			if (deletedClass || op!=Operation.DELETE)
				ci2 = propi2.inClass();
			else
				ci2 = model.classByName(propi2.inClass().name());
			PrintPropertyDetail(ci2,propi2,assocId,op);
		}
		
		processedProperty.add(propi);
	}

	private void PrintPropertyDetail(ClassInfo ci, PropertyInfo propi, String assocId, Operation op) {
		Element e1, e2;
		if (propi.isAttribute())
			e1 = document.createElement("FC_FeatureAttribute");
		else
			e1 = document.createElement("FC_RelationshipRole");
		addAttribute(document,e1,"id","_A"+propi.id());
		if (op!=null)
			addAttribute(document,e1,"mode",op.toString());
		root.appendChild(e1);

		String s = propi.taggedValue("sequenceNumber");
		if (s!=null) {
			e1.setAttribute("sequenceNumber", PrepareToPrint(s));
		}

		e2 = document.createElement("name");
		s = propi.name();
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
			e2 = document.createElement("inverseRichtung");
			e2.setTextContent("true");
			if (op!=null)
				addAttribute(document,e2,"mode",op.toString());
			e1.appendChild(e2);
		}
		
		if (propi.isDerived()) {
			e2 = document.createElement("derived");
			e2.setTextContent("true");
			if (op!=null)
				addAttribute(document,e2,"mode",op.toString());
			e1.appendChild(e2);
		}
		
		s = propi.initialValue();
		if (propi.isAttribute() && s!=null && s.length()>0) {
			e2 = document.createElement("initialValue");
			e2.setTextContent(PrepareToPrint(s));
			if (op!=null)
				addAttribute(document,e2,"mode",op.toString());
			e1.appendChild(e2);
		}

		if (propi.isAttribute() && propi.isReadOnly()) {
			e2 = document.createElement("readOnly");
			e2.setTextContent("true");
			if (op!=null)
				addAttribute(document,e2,"mode",op.toString());
			e1.appendChild(e2);
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
		addAttribute(document,e2,"idref","_C"+ci.id());
		addAttribute(document,e2,"name",ci.name());
		if (op!=null)
			addAttribute(document,e2,"mode",op.toString());
		e1.appendChild(e2);					
		
		Type ti = propi.typeInfo();
		if (!propi.isAttribute()) {
			if (ti!=null) {
				e2 = document.createElement("FeatureTypeIncluded");
				ClassInfo cix = null;
				if (op!=Operation.DELETE)
					cix = model.classById(ti.id);
				else
					cix = model.classByName(ti.name);
				if (cix!=null && ExportClass(cix,false, null)) {
					addAttribute(document,e2,"idref","_C"+cix.id());
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
		
		s = i.taggedValue("AAA:Landnutzung");
		Operation tagop = op;
		if (diffs!=null && diffs.get(i)!=null) {
			for (DiffElement diff : diffs.get(i)) {
				if (diff.subElementType==ElementType.AAALANDNUTZUNG) {
					if (diff.change==Operation.DELETE) {
						tagop = Operation.DELETE;
						s = "true";
					} else if (diff.change==Operation.INSERT) {
						tagop = Operation.INSERT;
						s = "true";
					}									 
					break;
				}
			}
		}
		if (s!=null && s.length()>0) {
			e2 = document.createElement("taggedValue");
			e2.setTextContent(s);
			addAttribute(document,e2,"tag","AAA:Landnutzung");
			if (tagop!=null)
				addAttribute(document,e2,"mode",tagop.toString());
			e1.appendChild(e2);					
		}	
		
		s = i.stereotypes().contains("retired") ? "true" : null;
		tagop = op;
		if (diffs!=null && diffs.get(i)!=null) {
			for (DiffElement diff : diffs.get(i)) {
				if (diff.subElementType==ElementType.AAARETIRED) {
					if (diff.change==Operation.DELETE) {
						tagop = Operation.DELETE;
						s = "true";
					} else if (diff.change==Operation.INSERT) {
						tagop = Operation.INSERT;
						s = "true";
					}									 
					break;
				}
			}
		}
		if (s!=null && s.length()>0) {
			e2 = document.createElement("retired");
			e2.setTextContent(s);
			if (tagop!=null)
				addAttribute(document,e2,"mode",tagop.toString());
			e1.appendChild(e2);					
		}	
		
		s = i.taggedValue("AAA:GueltigBis");
		if (diffs!=null && diffs.get(i)!=null)
			for (DiffElement diff : diffs.get(i)) {
				if (diff.subElementType==ElementType.AAAGUELTIGBIS) {
					s = differ.diff_toString(diff.diff);
					break;
				}
			}
		if (s!=null && s.length()>0) {
			e2 = document.createElement("taggedValue");
			e2.setTextContent(s);
			addAttribute(document,e2,"tag","AAA:GueltigBis");
			e2.setTextContent(s);
			if (op!=null)
				addAttribute(document,e2,"mode",op.toString());
			e1.appendChild(e2);
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

		String taglist = options.parameter("representTaggedValues");
		if (taglist!=null && taglist.trim().length()>0) {
			// sort results alphabetically by tag name for consistent output
			String[] tagarray = taglist.split(",");
			TreeSet<String> tags = new TreeSet<String>();
			for (String tag : tagarray) {
				if (!tag.matches("AAA:(Modellart|Grunddatenbestand|Kennung|objektbildend|Revisionsnummer|Landnutzung|GueltigBis)"))
					tags.add(tag.trim());
			}
			
			for(String tag : tags) {
				String[] values = i.taggedValuesForTag(tag);
				
				// in AAA there is only one value per tag
				s = values.length>0? values[0].trim() : null;

				// Standard process for text values
				boolean found = false;
				if (diffs!=null && diffs.get(i)!=null) {
					for (DiffElement diff : diffs.get(i)) {
						if (diff.subElementType==ElementType.TAG && diff.tag.equalsIgnoreCase(tag)) {
							s = differ.diff_toString(diff.diff);
							found = true;
							break;
						}
					}
				}
				
				if (s!=null && s.length()>0) {
					e2 = document.createElement("taggedValue");
					e2.setTextContent(s);
					addAttribute(document,e2,"tag",tag);
					if (!found && op!=null)
						addAttribute(document,e2,"mode",op.toString());
					e1.appendChild(e2);					
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

			writeDOCX(xmlName, outfileBasename);
			writeHTML(xmlName, outfileBasename);
			writeXML(xmlName, outfileBasename);
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
	
	/**
	 * Transforms the contents of the temporary feature catalogue xml and
	 * inserts it into a specific place (denoted by a placeholder) of a docx
	 * template file. The result is copied into a new output file. The template
	 * file is not modified.
	 * 
	 * @param xmlName
	 *            Name of the temporary feature catalogue xml file, located in
	 *            the output directory.
	 * @param outfileBasename
	 *            Base name of the output file, without file type ending.
	 */
	private void writeDOCX(String xmlName, String outfileBasename) {

		if (!OutputFormat.toLowerCase().contains("docx"))
			return;

		StatusBoard.getStatusBoard().statusChanged(STATUS_WRITE_DOCX);

		ZipHandler zipHandler = new ZipHandler();

		String xsldocxfileName = options.parameter(this.getClass().getName(),"xsldocxFile");
		if (xsldocxfileName==null)
			xsldocxfileName = "aaa-docx.xsl";
		
		String docxfileName = outfileBasename + ".docx";

		String docxTemplateFilePath = options.parameter(this.getClass().getName(),
				"docxTemplateFilePath");
		if (docxTemplateFilePath == null)
			docxTemplateFilePath = options.parameter("docxTemplateFilePath");
		// if no path is provided, use the directory of the default template
		if (docxTemplateFilePath == null) {
			docxTemplateFilePath = DOCX_TEMPLATE_URL;
			result.addDebug(this, 17, "docxTemplateFilePath",
					DOCX_TEMPLATE_URL);
		}
				
		try {

			// Setup directories
			File outDir = new File(outputDirectory);
			File tmpDir = new File(outDir, "tmpdocx");
			File tmpinputDir = new File(tmpDir, "input");
			File tmpoutputDir = new File(tmpDir, "output");

			// get docx template

			// create temporary file for the docx template copy
			File docxtemplate_copy = new File(tmpDir, "docxtemplatecopy.tmp");

			// populate temporary file either from remote or local URI
			if (docxTemplateFilePath.toLowerCase().startsWith("http")) {
				URL templateUrl = new URL(docxTemplateFilePath);
				FileUtils.copyURLToFile(templateUrl, docxtemplate_copy);
			} else {
				File docxtemplate = new File(docxTemplateFilePath);
				if (docxtemplate.exists()) {
					FileUtils.copyFile(docxtemplate, docxtemplate_copy);
				} else {
					result.addError(this, 19, docxtemplate.getAbsolutePath());
					return;
				}
			}

			/*
			 * Unzip the docx template to tmpinputDir and tmpoutputDir The
			 * contents of the tmpinputdir will be used as input for the
			 * transformation. The transformation result will overwrite the
			 * relevant files in the tmpoutputDir.
			 */
			zipHandler.unzip(docxtemplate_copy, tmpinputDir);
			zipHandler.unzip(docxtemplate_copy, tmpoutputDir);

			/*
			 * Get hold of the styles.xml file from which the transformation
			 * will get relevant information. The path to this file will be used
			 * as a transformation parameter.
			 */
			File styleXmlFile = new File(tmpinputDir, "word/styles.xml");
			if (!styleXmlFile.canRead()) {
				result.addError(null, 301, styleXmlFile.getName(),
						"styles.xml");
				return;
			}

			/*
			 * Get hold of the temporary feature catalog xml file. The path to
			 * this file will be used as a transformation parameter.
			 */
			File xmlFile = new File(outDir, xmlName);
			if (!xmlFile.canRead()) {
				result.addError(null, 301, xmlFile.getName(), xmlName);
				return;
			}

			/*
			 * Get hold of the input document.xml file (internal .xml file from
			 * the docxtemplate). It will be used as the source for the
			 * transformation.
			 */
			File indocumentxmlFile = new File(tmpinputDir, "word/document.xml");
			if (!indocumentxmlFile.canRead()) {
				result.addError(null, 301, indocumentxmlFile.getName(),
						"document.xml");
				return;
			}

			/*
			 * Get hold of the output document.xml file. It will be used as the
			 * transformation target.
			 */
			File outdocumentxmlFile = new File(tmpoutputDir,
					"word/document.xml");
			if (!outdocumentxmlFile.canWrite()) {
				result.addError(null, 307, outdocumentxmlFile.getName(),
						"document.xml");
				return;
			}

			/*
			 * Prepare the transformation.
			 */
			Map<String,String> transformationParameters = new HashMap<String,String>();
			transformationParameters.put("styleXmlPath",
					styleXmlFile.toURI().toString());
			transformationParameters.put("catalogXmlPath",
					xmlFile.toURI().toString());
			transformationParameters.put("DOCX_PLACEHOLDER", DOCX_PLACEHOLDER);

			/*
			 * Execute the transformation.
			 */
			this.xsltWrite(indocumentxmlFile, xsldocxfileName, outdocumentxmlFile, transformationParameters);

			/*
			 * === Create the docx result file ===
			 */

			// Get hold of the output docx file (it will be overwritten or
			// initialized).
			File outFile = new File(outDir, docxfileName);

			/*
			 * Zip the temporary output directory and copy it to the output docx
			 * file.
			 */
			zipHandler.zip(tmpoutputDir, outFile);

			/*
			 * === Delete the temporary directory ===
			 */

			try {
				FileUtils.deleteDirectory(tmpDir);
			} catch (IOException e) {
				result.addWarning(this, 20, e.getMessage());
			}

			result.addResult(getTargetName(), outputDirectory, docxfileName,
					null);

		} catch (Exception e) {
			String m = e.getMessage();
			if (m != null) {
				result.addError(m);
			}
			e.printStackTrace(System.err);
		}
	}
	
	private void writeHTML(String xmlName, String outfileBasename){

		if(!OutputFormat.toLowerCase().contains("html"))
			return;

		StatusBoard.getStatusBoard().statusChanged(STATUS_WRITE_HTML);
		
		String xslfofileName = options.parameter(this.getClass().getName(),"xslhtmlFile");
		if (xslfofileName==null)
			xslfofileName = "aaa-html.xsl";
		String htmlfileName = outfileBasename+".html";
		
		if(xmlName!=null && xmlName.length()>0
				&& xslfofileName!=null && xslfofileName.length()>0
				&& htmlfileName!=null && htmlfileName.length()>0){
            // Setup input and output files
            File outDir = new File(outputDirectory);
            File xmlFile = new File(outDir, xmlName);
           	File outFile = new File(outDir, htmlfileName);
			xsltWrite(xmlFile, xslfofileName, outFile, null);
		}
	}
	
	private void writeXML(String xmlName, String outfileBasename){

		if(!OutputFormat.toLowerCase().contains("xml"))
			return;

		StatusBoard.getStatusBoard().statusChanged(STATUS_WRITE_XML);
		
		String xslfofileName = options.parameter(this.getClass().getName(),"xslxmlFile");
		if (xslfofileName==null)
			xslfofileName = "aaa-xml.xsl";
		String xmloutFileName = outfileBasename+".xml";
		
		if(xmlName!=null && xmlName.length()>0
				&& xslfofileName!=null && xslfofileName.length()>0
				&& xmloutFileName!=null && xmloutFileName.length()>0){
            // Setup input and output files
            File outDir = new File(outputDirectory);
            File xmlFile = new File(outDir, xmlName);
           	File outFile = new File(outDir, xmloutFileName);
			xsltWrite(xmlFile, xslfofileName, outFile, null);
		}
	}
	
	private void writeCSV(String xmlName, String outfileBasename){

		if(!OutputFormat.toLowerCase().contains("csv"))
			return;

		StatusBoard.getStatusBoard().statusChanged(STATUS_WRITE_CSV);

		String xslfofileName = options.parameter(this.getClass().getName(),"xslcsvFile");
		if (xslfofileName==null)
			xslfofileName = "aaa-csv.xsl";
		String csvoutFileName = outfileBasename+".csv";
		
		if(xmlName!=null && xmlName.length()>0
				&& xslfofileName!=null && xslfofileName.length()>0
				&& csvoutFileName!=null && csvoutFileName.length()>0){
            // Setup input and output files
            File outDir = new File(outputDirectory);
            File xmlFile = new File(outDir, xmlName);
           	File outFile = new File(outDir, csvoutFileName);
			xsltWrite(xmlFile, xslfofileName, outFile, null);
		}
	}
	
   private void xsltWrite(File xmlFile, String xsltfileName, File outFile, Map<String,String> parameters){
	   try{
			String xslTransformerFactory = options.parameter(this.getClass().getName(),"xslTransformerFactory");
			if (xslTransformerFactory!=null) {
				try {
					System.setProperty("javax.xml.transform.TransformerFactory",
							xslTransformerFactory);
					@SuppressWarnings("unused")
					TransformerFactory factory = TransformerFactory.newInstance();
					
					if (factory.getClass().getName().equalsIgnoreCase("net.sf.saxon.TransformerFactoryImpl")) {
						// fine - this is an XSLT 2.0 processor
					} else {
						result.addError(this, 102);
					}
				} catch (TransformerFactoryConfigurationError e) {
					result.addError(this, 100, xslTransformerFactory);
				}
			}

			String xsltPath = options.parameter(this.getClass().getName(),"xsltPfad");
			if (xsltPath==null)
				xsltPath = "src/main/resources/xslt";

           	if(!xmlFile.canRead()){
            	result.addError(null, 301, xmlFile.getName(), xmlFile.getCanonicalPath());
            	return;
            }
           	if(!outFile.getParentFile().canWrite()){
            	result.addError(null, 307, outFile.getName(), outFile.getCanonicalPath());
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
				    if (parameters!=null) {
				    	for (Entry<String,String> parameter : parameters.entrySet() ) {
						    trans.setParameter(parameter.getKey().toString(), parameter.getValue());
				    	}
				    }
				    trans.transform(xmlSource, res);
					result.addResult(getTargetName(), outputDirectory, outFile.getCanonicalPath(), options.parameter(this.getClass().getName(),"modellarten"));
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
	
	/**
	 * This is the message text provision proper. It returns a message for a
	 * number.
	 * 
	 * @param mnr
	 *            Message number
	 * @return Message text or null
	 */
	protected String messageText(int mnr) {
		switch (mnr) {
		case 12:
			return "Directory named '$1$' does not exist or is not accessible.";
		case 13:
			return "File '$1$' does not exist or is not accessible.";
		case 17:
			return "No value provided for configuration parameter '$1$', defaulting to: '$2$'.";
		case 18:
			return "XSLT stylesheet $1$ not found.";
		case 19:
			return "DOCX template $1$ not found.";
		case 20:
			return "Could not delete temporary directory created for docx transformation; IOException message is: $1$";
		case 100:
			return "Parameter 'xslTransformerFactory' is set to '$1$'. A transformer with this factory could not be instantiated. Make the implementation of the transformer factory available on the classpath.";
		case 102:
			return "This version of the AAA-Tools requires an XSLT 2.0 processor, which should be set via the configuration parameter 'xslTransformerFactory'. That parameter was not found, and the default TransformerFactory implementation is not 'net.sf.saxon.TransformerFactoryImpl' (which is known to be an XSLT 2.0 processor); ensure that the parameter is configured correctly.";
		}
		return null;
	}

	@Override
	/**
	 * <p>
	 * This method returns messages belonging to the Feature Catalogue target by
	 * their message number. The organization corresponds to the logic in module
	 * ShapeChangeResult. All functions in that class, which require an message
	 * number can be redirected to the function at hand.
	 * </p>
	 * 
	 * @param mnr
	 *            Message number
	 * @return Message text, including $x$ substitution points.
	 */
	public String message(int mnr) {
		// Get the message proper and return it with an identification prefixed
		String mess = messageText(mnr);
		if (mess == null)
			return null;
		String prefix = "";
		if (mess.startsWith("??")) {
			prefix = "??";
			mess = mess.substring(2);
		}
		return prefix + "Katalogtool: " + mess;
	}
}
