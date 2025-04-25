/**
 * GeoInfoDok Transformations (Link Transformer)
 *
 * (c) 2009-2024 Arbeitsgemeinschaft der Vermessungsverwaltungen der 
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

package de.adv_online.aaa.uml;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sparx.Attribute;
import org.sparx.Collection;
import org.sparx.Connector;
import org.sparx.Constraint;
import org.sparx.Diagram;
import org.sparx.DiagramObject;
import org.sparx.Element;
import org.sparx.Method;
import org.sparx.Package;
import org.sparx.Parameter;
import org.sparx.Repository;

import de.interactive_instruments.shapechange.core.MessageSource;
import de.interactive_instruments.shapechange.core.Options;
import de.interactive_instruments.shapechange.core.ShapeChangeAbortException;
import de.interactive_instruments.shapechange.core.ShapeChangeResult;
import de.interactive_instruments.shapechange.core.ShapeChangeResult.MessageContext;
import de.interactive_instruments.shapechange.core.model.Transformer;
import de.interactive_instruments.shapechange.ea.util.EAAttributeUtil;
import de.interactive_instruments.shapechange.ea.util.EAConnectorEndUtil;
import de.interactive_instruments.shapechange.ea.util.EAConnectorUtil;
import de.interactive_instruments.shapechange.ea.util.EAConstraintUtil;
import de.interactive_instruments.shapechange.ea.util.EAElementUtil;
import de.interactive_instruments.shapechange.ea.util.EAException;
import de.interactive_instruments.shapechange.ea.util.EAPackageUtil;
import de.interactive_instruments.shapechange.ea.util.modelhelper.AbstractEAModelElement;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EAElement;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EAPackage;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EARepository;

public class LinkTransformer implements Transformer, MessageSource {

    // for dependency checks only
    public static final boolean DUPLICATE_ELEMENTS_CHECK = true;
    public static final boolean IDENTIFY_PACKAGE_DEPENDENCIES = true;
    public static final boolean EXTRA_CHECK_ATTRIBUTE_LINKAGE = false;
    public static final boolean APPLY_TRANSFORMATIONS = false;
    public static final boolean ONLY_LINK_ANALYSIS = true;
    public static final boolean PROCESS_CONNECTORS = true;
    public static final boolean PROCESS_ATTRIBUTES = true;
    public static final boolean COPY_REPOSITORY = false;

    // dependency checks AND model transformations
//    public static final boolean DUPLICATE_ELEMENTS_CHECK = true;
//    public static final boolean IDENTIFY_PACKAGE_DEPENDENCIES = true;
    // public static final boolean EXTRA_CHECK_ATTRIBUTE_LINKAGE = false;
//    public static final boolean APPLY_TRANSFORMATIONS = true;
//    public static final boolean ONLY_LINK_ANALYSIS = false;
//    public static final boolean PROCESS_CONNECTORS = true;
//    public static final boolean PROCESS_ATTRIBUTES = true;
//    public static final boolean COPY_REPOSITORY = true;

    /**
     * If <code>true</code>, the package structure of the GeoInfoDok will be
     * updated. This will be the last processing step. If that model update is not
     * desired, for example to achieve a model with transformed links but original
     * package structure for subsequent dependency and link checking, set the
     * parameter value to <code>false</code>.
     */
    public static final boolean UPDATE_MODEL_STRUCTURE = false;

    public static final String REPO_COPY_NAME_SUFFIX = "_modified";

    public static final String FULL_NAME_FOR_MISSING_ELEMENT = "<element_missing>";

    public static final String AAA_SCHEMA_FULL_NAME = "Model::GeoInfoDok::AFIS-ALKIS-ATKIS Anwendungsschema";
    public static final String GEOINFODOK_PKG_FULL_NAME = "Model::GeoInfoDok";

    private ShapeChangeResult result = null;

    private Repository rep = null;
    private EARepository eaRepo = null;

    /**
     * key: full-name of a (schema) package; value: list of full-names of other
     * (schema) packages that the package depends upon
     */
    protected SortedMap<String, List<String>> dependenciesBySchemaIn = new TreeMap<>();
    protected SortedMap<String, SortedSet<String>> visitedDependenciesBySchemaIn = new TreeMap<>();
    protected String currentSchemaForProcessingSchemaDependencies = null;

    protected SortedMap<String, List<EAPackage>> dependenciesBySchema = new TreeMap<>();

    protected SortedMap<Integer, String> duplicateElementFullNameByElementId = new TreeMap<>();

    /**
     * Defines class mappings:
     * 
     * key: full-name of a class; value: full-name of the target class
     */
    protected SortedMap<String, String> classMappings = new TreeMap<>();

    protected SortedSet<String> aaaMeasureTypesToTransform = new TreeSet<>();

    protected SortedMap<String, List<PackageDependency>> packageDependenciesBySchema = new TreeMap<>();

    protected List<String> metaTypesToIgnoreInDuplicateElementCheck = new ArrayList<>();

    protected boolean inIdentifyPackageDependencies = false;

    public void initialise(Options o, ShapeChangeResult r, String repositoryFileName) throws ShapeChangeAbortException {

	result = r;

	/** Make sure repository file exists */
	File repfile = new File(repositoryFileName);
	boolean ex = true;
	if (!repfile.exists()) {
	    ex = false;
	    if (!repositoryFileName.toLowerCase().endsWith(".qea")) {
		repositoryFileName += ".qea";
		repfile = new File(repositoryFileName);
		ex = repfile.exists();
	    }
	}
	if (!ex) {
	    r.addFatalError(null, 31, repositoryFileName);
	    throw new ShapeChangeAbortException();
	}

	if (COPY_REPOSITORY) {
	    File repfileCopy = new File(repfile.getParentFile(), FileNameUtils.getBaseName(repfile.getName())
		    + REPO_COPY_NAME_SUFFIX + "." + FileNameUtils.getExtension(repfile.getName()));
	    try {
		FileUtils.copyFile(repfile, repfileCopy, StandardCopyOption.REPLACE_EXISTING);
		repfile = repfileCopy;
	    } catch (IOException e) {
		e.printStackTrace();
		throw new ShapeChangeAbortException();
	    }
	}

	/** Connect to EA Repository */
	String absname = repfile.getAbsolutePath();
	rep = new Repository();
	if (!rep.OpenFile(absname)) {
	    String errormsg = rep.GetLastError();
	    r.addFatalError(null, 30, errormsg, repositoryFileName);
	    throw new ShapeChangeAbortException();
	}

	metaTypesToIgnoreInDuplicateElementCheck = Arrays.asList("Boundary", "Note", "ReportSpecification",
		"StandardChart", "Pseudostate", "Text", "InterruptibleActivityRegion", "DecisionNode", "Actor",
		"Activity", "Object", "Metaclass", "Stereotype");
	Collections.sort(metaTypesToIgnoreInDuplicateElementCheck);

	/*
	 * Dependencies for old model structure
	 */

	dependenciesBySchemaIn.put("Model::GeoInfoDok::AAA_Ausgabekatalog", Arrays.asList(AAA_SCHEMA_FULL_NAME,
		"Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1"));

	dependenciesBySchemaIn.put("Model::GeoInfoDok::AAA_Objektartenkatalog",
		Arrays.asList(AAA_SCHEMA_FULL_NAME,
			"Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
			"Model::ISO/TC 211::ISO 19110 Methodology for feature cataloguing::ISO 19110 Edition 2"));

	dependenciesBySchemaIn.put(AAA_SCHEMA_FULL_NAME, Arrays.asList(/*
								        * "Model::GeoInfoDok::AAA_Ausgabekatalog",
								        * "Model::GeoInfoDok::AAA_Objektartenkatalog".
								        */
		"Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
		"Model::ISO/TC 211::ISO 19107 Spatial schema::ISO 19107 Edition 1",
		"Model::ISO/TC 211::ISO 19108 Temporal schema::ISO 19108 Edition 1",
		"Model::ISO/TC 211::ISO 19109 Rules for application schema::ISO 19109 Edition 2",
		"Model::ISO/TC 211::ISO 19110 Methodology for feature cataloguing::ISO 19110 Edition 2",
		"Model::ISO/TC 211::ISO 19111 Referencing by coordinates::ISO 19111 Edition 3",
		"Model::ISO/TC 211::ISO 19115 Metadata::ISO 19115-1 Edition 1",
		"Model::ISO/TC 211::ISO 19123 Schema for coverage geometry and functions::ISO 19123-1 Edition 1",
		"Model::ISO/TC 211::ISO 19157 Data quality::ISO 19157-1 Edition 1", "Model::OGC::Filter Encoding 2.0",
		"Model::GeoInfoDok::Web Feature Service Erweiterungen", "Model::OGC::Web Feature Service 2.0",
		"Model::OGC::OWS Common 1.1"));

	dependenciesBySchemaIn.put("Model::GeoInfoDok::BR_Bodenrichtwerte", Arrays.asList(AAA_SCHEMA_FULL_NAME,
		"Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1"));

	dependenciesBySchemaIn.put("Model::GeoInfoDok::GN_Geographische Informationen", Arrays.asList(
		AAA_SCHEMA_FULL_NAME, "Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1"));

	dependenciesBySchemaIn.put("Model::GeoInfoDok::GV_Geometrische Verbesserungen",
		Arrays.asList(AAA_SCHEMA_FULL_NAME,
			"Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
			"Model::ISO/TC 211::ISO 19107 Spatial schema::ISO 19107 Edition 1"));

	dependenciesBySchemaIn.put("Model::GeoInfoDok::LB_Landbedeckung", Arrays.asList(AAA_SCHEMA_FULL_NAME,
		"Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1"));

	dependenciesBySchemaIn.put("Model::GeoInfoDok::LN_Landnutzung", Arrays.asList(AAA_SCHEMA_FULL_NAME,
		"Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1"));

	dependenciesBySchemaIn.put("Model::GeoInfoDok::Web Feature Service Erweiterungen",
		Arrays.asList("Model::OGC::Web Feature Service 2.0"));

	dependenciesBySchemaIn.put("Model::GeoInfoDok::AAA_Signaturenkatalog", Arrays.asList());

	dependenciesBySchemaIn.put("Model::OGC::Filter Encoding 2.0", Arrays.asList());
	dependenciesBySchemaIn.put("Model::OGC::OWS Common 1.1", Arrays.asList());
	dependenciesBySchemaIn.put("Model::OGC::Web Feature Service 2.0", Arrays.asList());

	/*
	 * 2024-09-26 JE: ISO schemas are not transformed.
	 */

	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
		Arrays.asList());

	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO/TS 19103 Edition 1",
		Arrays.asList());

	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19107 Spatial schema::ISO 19107 Edition 2", Arrays.asList());
	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19107 Spatial schema::ISO 19107 Edition 1", Arrays.asList());

	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19108 Temporal schema::ISO 19108 Edition 1",
		Arrays.asList());

	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19109 Rules for application schema::ISO 19109 Edition 2",
		Arrays.asList());
	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19109 Rules for application schema::ISO 19109 Edition 1",
		Arrays.asList());

	dependenciesBySchemaIn.put(
		"Model::ISO/TC 211::ISO 19110 Methodology for feature cataloguing::ISO 19110 Edition 2",
		Arrays.asList());
	dependenciesBySchemaIn.put(
		"Model::ISO/TC 211::ISO 19110 Methodology for feature cataloguing::ISO 19110 Edition 1",
		Arrays.asList());

	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19111 Referencing by coordinates::ISO 19111 Edition 3",
		Arrays.asList());
	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19111 Referencing by coordinates::ISO 19111 Edition 2",
		Arrays.asList());
	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19111 Referencing by coordinates::ISO 19111-2 Edition 1",
		Arrays.asList());

	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19115 Metadata::ISO DAMD 19115-1 Edition 1 (Amendment 2)",
		Arrays.asList());
	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19115 Metadata::ISO DAMD 19115-1 Edition 1 (Amendment 1)",
		Arrays.asList());
	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19115 Metadata::ISO 19115-1 Edition 1", Arrays.asList());
	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19115 Metadata::ISO 19115-2 Edition 2", Arrays.asList());
	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19115 Metadata::ISO 19115-2 Edition 2 (Amendment 1)",
		Arrays.asList());
	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19115 Metadata::ISO 19115-2 Edition 1", Arrays.asList());
	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19115 Metadata::ISO 19115 Edition 1 (Corrigendum 1)",
		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
			"Model::ISO/TC 211::ISO 19107 Spatial schema::ISO 19107 Edition 1",
			"Model::ISO/TC 211::ISO 19108 Temporal schema::ISO 19108 Edition 1",
			"Model::ISO/TC 211::ISO 19109 Rules for application schema::ISO 19109 Edition 1",
			"Model::ISO/TC 211::ISO 19111 Referencing by coordinates::ISO 19111 Edition 2",
			"Model::ISO/TC 211::Informative::Extended Metadata from 19115"));
	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19115 Metadata::ISO 19115 Edition 1", Arrays.asList());

	dependenciesBySchemaIn.put(
		"Model::ISO/TC 211::ISO 19123 Schema for coverage geometry and functions::ISO 19123-1 Edition 1",
		Arrays.asList());
	dependenciesBySchemaIn.put(
		"Model::ISO/TC 211::ISO 19123 Schema for coverage geometry and functions::ISO 19123-2 Edition 1",
		Arrays.asList());
	dependenciesBySchemaIn.put(
		"Model::ISO/TC 211::ISO 19123 Schema for coverage geometry and functions::ISO 19123 Edition 1",
		Arrays.asList());

	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19136 Geography Markup Language (GML)::ISO 19136 Edition 1",
		Arrays.asList());

	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19157 Data quality::ISO 19157-1 Edition 1", Arrays.asList());
	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19157 Data quality::ISO 19157 Edition 1", Arrays.asList());
	dependenciesBySchemaIn.put("Model::ISO/TC 211::ISO 19157 Data quality::ISO 19157 Edition 1 (Amendment 1)",
		Arrays.asList());

//	/*
//	 * Dependencies for new model structure
//	 * 
//	 * 2024-12-04 JE: Abhängigkeiten innerhalb des AAA-Schemas untersuchen, für
//	 * Refactoring
//	 */
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::3D::AAA 3D 1.0",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::ISO/TC 211::ISO 19108 Temporal schema::ISO 19108 Edition 1",
//			"Model::ISO/TC 211::ISO 19115 Metadata::ISO 19115-1 Edition 1",
//			"Model::GeoInfoDok::AFIS-ALKIS-ATKIS::AFIS-ALKIS-ATKIS 7.1",
//			"Model::GeoInfoDok::Basisschema::Basisschema 1.0",
//			"Model::GeoInfoDok::Praesentationsobjekte::Praesentationsobjekte 1.0"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::AAA Ausgabekatalog::AAA Ausgabekatalog 2.0",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::GeoInfoDok::Codelisten", "Model::GeoInfoDok::AFIS-ALKIS-ATKIS::AFIS-ALKIS-ATKIS 7.1",
//			"Model::GeoInfoDok::Operationen::AAA Operationen 1.0",
//			"Model::GeoInfoDok::Operationen::NAS Operationen 1.0",
//			"Model::GeoInfoDok::Kataloge::Kataloge 1.0"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::AAA Objektartenkatalog::AAA Objektartenkatalog 1.0",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::ISO/TC 211::ISO 19110 Methodology for feature cataloguing::ISO 19110 Edition 2",
//			"Model::GeoInfoDok::Basisschema::Basisschema 1.0"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::AAA Projektsteuerung::AAA Projektsteuerung 1.0",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::GeoInfoDok::Basisschema::Basisschema 1.0", "Model::GeoInfoDok::Codelisten",
//			"Model::GeoInfoDok::Nutzerprofile::AAA Nutzerprofile 1.0"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::AAA Signaturenkatalog::AAA Signaturenkatalog 1.1",
//		Arrays.asList());
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::AFIS-ALKIS-ATKIS::AFIS-ALKIS-ATKIS 7.1",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::ISO/TC 211::ISO 19107 Spatial schema::ISO 19107 Edition 1",
//			"Model::ISO/TC 211::ISO 19108 Temporal schema::ISO 19108 Edition 1",
//			"Model::ISO/TC 211::ISO 19115 Metadata::ISO 19115-1 Edition 1",
//			"Model::ISO/TC 211::ISO 19157 Data quality::ISO 19157-1 Edition 1",
//			"Model::GeoInfoDok::Basisschema::Basisschema 1.0", "Model::GeoInfoDok::Codelisten",
//			"Model::GeoInfoDok::Kataloge::Kataloge 1.0"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::Basisschema::Basisschema 1.0", Arrays.asList(
//		"Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//		"Model::ISO/TC 211::ISO 19107 Spatial schema::ISO 19107 Edition 1",
//		"Model::ISO/TC 211::ISO 19108 Temporal schema::ISO 19108 Edition 1",
//		"Model::ISO/TC 211::Informative::Spatial Examples from ISO 19107::Application Schema::Topology Simple::Simple Topology",
//		"Model::ISO/TC 211::ISO 19109 Rules for application schema::ISO 19109 Edition 1::General Feature Model",
//		"Model::ISO/TC 211::ISO 19115 Metadata::ISO 19115-1 Edition 1",
//		"Model::ISO/TC 211::ISO 19157 Data quality::ISO 19157-1 Edition 1", "Model::GeoInfoDok::Codelisten"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::Bodenrichtwerte::BR_Bodenrichtwerte 3.0",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::GeoInfoDok::Basisschema::Basisschema 1.0", "Model::GeoInfoDok::Codelisten",
//			"Model::GeoInfoDok::Kataloge::Kataloge 1.0"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::Codelisten", Arrays.asList(
////		"Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1"
//	));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::Geographische Informationen::GN_Geographische Informationen 1.0",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::GeoInfoDok::Basisschema::Basisschema 1.0", "Model::GeoInfoDok::Codelisten",
//			"Model::GeoInfoDok::Kataloge::Kataloge 1.0"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::Geometrische Verbesserungen::GV_Geometrische Verbesserungen 1.0",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::ISO/TC 211::ISO 19107 Spatial schema::ISO 19107 Edition 1",
//			"Model::GeoInfoDok::Basisschema::Basisschema 1.0",
//			"Model::GeoInfoDok::Kataloge::Kataloge 1.0"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::Kataloge::Kataloge 1.0",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::GeoInfoDok::Basisschema::Basisschema 1.0"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::Landbedeckung::LB_Landbedeckung 1.0",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::GeoInfoDok::Basisschema::Basisschema 1.0"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::Landnutzung::LN_Landnutzung 1.0",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO/TS 19103 Edition 1",
//			"Model::GeoInfoDok::Basisschema::Basisschema 1.0"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::Nutzerprofile::AAA Nutzerprofile 1.0",
//		Arrays.asList("Model::GeoInfoDok::Basisschema::Basisschema 1.0"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::Nutzerprofile::Nutzerprofile 1.0",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::ISO/TC 211::ISO 19108 Temporal schema::ISO 19108 Edition 1",
//			"Model::ISO/TC 211::ISO 19111 Referencing by coordinates::ISO 19111 Edition 3",
//			"Model::GeoInfoDok::Operationen::AAA Operationen 1.0",
//			"Model::OGC::Web Feature Service::Web Feature Service 2.0",
//			"Model::GeoInfoDok::Kataloge::Kataloge 1.0", "Model::GeoInfoDok::Codelisten",
//			"Model::GeoInfoDok::Nutzerprofile::AAA Nutzerprofile 1.0",
//			"Model::GeoInfoDok::Basisschema::Basisschema 1.0"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::Operationen::AAA Operationen 1.0",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::ISO/TC 211::ISO 19111 Referencing by coordinates::ISO 19111 Edition 3",
//			"Model::OGC::Web Feature Service::Web Feature Service 2.0",
//			"Model::OGC::Filter Encoding::Filter Encoding 2.0", "Model::OGC::OWS Common::OWS Common 1.1",
//			"Model::GeoInfoDok::Basisschema::Basisschema 1.0", "Model::GeoInfoDok::Codelisten"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::Operationen::NAS Operationen 1.0", Arrays.asList(
//		"Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//		"Model::ISO/TC 211::ISO 19107 Spatial schema::ISO 19107 Edition 1",
//		"Model::ISO/TC 211::ISO 19115 Metadata::ISO 19115-1 Edition 1",
//		"Model::GeoInfoDok::AFIS-ALKIS-ATKIS::AFIS-ALKIS-ATKIS 7.1",
//		"Model::GeoInfoDok::Basisschema::Basisschema 1.0", "Model::GeoInfoDok::Codelisten",
//		"Model::GeoInfoDok::Nutzerprofile::Nutzerprofile 1.0",
//		"Model::GeoInfoDok::Operationen::AAA Operationen 1.0", "Model::GeoInfoDok::Kataloge::Kataloge 1.0",
//		"Model::GeoInfoDok::Web Feature Service Erweiterungen::Web Feature Service Erweiterungen 2.0",
//		"Model::OGC::Web Feature Service::Web Feature Service 2.0", "Model::OGC::OWS Common::OWS Common 1.1"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::Praesentationsobjekte::Praesentationsobjekte 1.0",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::GeoInfoDok::Basisschema::Basisschema 1.0"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::Punktmengenobjekte::AAA Punktmengenobjekte 1.0",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::ISO/TC 211::ISO 19107 Spatial schema::ISO 19107 Edition 1",
//			"Model::ISO/TC 211::ISO 19123 Schema for coverage geometry and functions::ISO 19123 Edition 1",
//			"Model::ISO/TC 211::ISO 19157 Data quality::ISO 19157-1 Edition 1",
////			"Model::GeoInfoDok::3D::AAA 3D 1.0", 
//			"Model::GeoInfoDok::Basisschema::Basisschema 1.0"));
//
//	dependenciesBySchemaIn.put("Model::GeoInfoDok::Versionierungsschema::Versionierungsschema 1.0",
//		Arrays.asList("Model::ISO/TC 211::ISO 19103 Conceptual schema language::ISO 19103 Edition 1",
//			"Model::GeoInfoDok::Codelisten", "Model::GeoInfoDok::Basisschema::Basisschema 1.0"));
//
//	dependenciesBySchemaIn.put(
//		"Model::GeoInfoDok::Web Feature Service Erweiterungen::Web Feature Service Erweiterungen 2.0",
//		Arrays.asList());
//
//	dependenciesBySchemaIn.put("Model::OGC::Filter Encoding::Filter Encoding 2.0", Arrays.asList());
//	dependenciesBySchemaIn.put("Model::OGC::OWS Common::OWS Common 1.1", Arrays.asList());
//	dependenciesBySchemaIn.put("Model::OGC::Web Feature Service::Web Feature Service 2.0", Arrays.asList());

	classMappings.put("Length", "Measure");
	classMappings.put("Area", "Measure");
	classMappings.put("SC_CRS", "CRS");
	classMappings.put("AA_UUID", "CharacterString"); // union mapping, for cases where AA_UUID is contained in Set<>
							 // or Sequence<>
	classMappings.put("vector", "Vector"); // correct typo

	aaaMeasureTypesToTransform.add("Acceleration");
	aaaMeasureTypesToTransform.add("AccelerationGradient");
	aaaMeasureTypesToTransform.add("Voltage");
    }

    public void shutdown() {
	rep.CloseFile();
	rep.Exit();
	rep = null;
    }

    @Override
    public void transform() throws ShapeChangeAbortException {

	try {

	    this.eaRepo = new EARepository(rep);

	    boolean allSchemasAndDependenciesFound = identifySchemas();

	    if (!allSchemasAndDependenciesFound) {
		result.addError(this, 104);
	    }

	    if (DUPLICATE_ELEMENTS_CHECK) {
		checkForDuplicateElementsInSchemasAndDependencies();
	    }

	    // 1. analysis of existing package dependencies (to see all the gory details)
	    if (IDENTIFY_PACKAGE_DEPENDENCIES) {
		inIdentifyPackageDependencies = true;
		identifyPackageDependencies();
		printUnvisitedPackageDependencies();
		inIdentifyPackageDependencies = false;
	    }

	    if (EXTRA_CHECK_ATTRIBUTE_LINKAGE) {
		checkAttributeLinkage();
	    }

	    if (APPLY_TRANSFORMATIONS) {
		// 2. explicit model transformations
		// a) measure types in AAA schema
		aaaMeasureTypeTransformation();

		// b) unions
		transformAaaUnions();

		// 3. actual processing of schema dependencies
		processSchemaDependencies();

		/*
		 * LAST (BECAUSE THIS WILL SCREW UP FULL NAMES OF MODEL ELEMENTS): update model
		 * structure
		 */
		if (UPDATE_MODEL_STRUCTURE) {
		    updateModelStructure();
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}
    }

    private void printUnvisitedPackageDependencies() {

	result.addInfo(this, 126);
	for (String schemaFullName : dependenciesBySchema.keySet()) {

	    result.addInfo(this, 127, schemaFullName);

	    List<String> configuredDependencies = dependenciesBySchemaIn.get(schemaFullName);

	    SortedSet<String> visitedDependencies;
	    if (visitedDependenciesBySchemaIn.containsKey(schemaFullName)) {
		visitedDependencies = visitedDependenciesBySchemaIn.get(schemaFullName);
	    } else {
		visitedDependencies = new TreeSet<>();
	    }

	    SortedSet<String> unvisitedDependencies = new TreeSet<>();
	    for (String configuredDependency : configuredDependencies) {
		if (!visitedDependencies.contains(configuredDependency)) {
		    unvisitedDependencies.add(configuredDependency);
		}
	    }

	    for (String unvisitedDependency : unvisitedDependencies) {
		result.addInfo(this, 128, unvisitedDependency);
	    }
	}

    }

    private void checkAttributeLinkage() {

	result.addInfo(this, 124);

	for (String schemaFullName : dependenciesBySchema.keySet()) {

	    result.addInfo(this, 125, schemaFullName);

	    Optional<EAPackage> schemaPkgOpt = eaRepo.lookupPackage(schemaFullName);

	    if (schemaPkgOpt.isPresent()) {

		EAPackage schemaPkg = schemaPkgOpt.get();

		List<EAElement> schemaElements = new ArrayList<>(eaRepo.elementsAll(schemaPkg).values());

		for (EAElement eaElmt : schemaElements) {

		    Element elmt = rep.GetElementByID(eaElmt.getElementId());

		    Collection<Attribute> attributes = elmt.GetAttributes();
		    attributes.Refresh();

		    for (Attribute att : attributes) {

			String attName = att.GetName();
			String attFullName = attFullName(eaElmt, attName);
			int classifierId = att.GetClassifierID();
			String type = att.GetType();

			if (classifierId == 0) {

			    if (type.equals("<undefined>") || StringUtils.isBlank(type)) {
				/*
				 * Should be enum or code. TBD: Should we check more, for example to see if a
				 * type for a normal attribute is missing?
				 * 
				 * Regarding linking, there is nothing we can do if the type is undefined.
				 */

			    } else {

				/*
				 * 2024-12-04 JE: the restriction was only used for testing GID refactoring
				 */
				if (!StringUtils.equalsAnyIgnoreCase(type, "CharacterString", "Boolean", "Real",
					"Integer", "AA_UUID", "Date")) {

				    MessageContext mc = result.addInfo(this, 300, type);
				    if (mc != null) {
					mc.addDetail(this, 3, attFullName);
				    }
				}
			    }
			}
		    }
		}
	    }
	}
    }

    private void updateModelStructure() {

	result.addInfo(this, 1000);

	// NOTE: This will screw up a lot of full names and thus should be done as last
	// transformation

	// GeoInfoDok

	relocatePackage("Model::GeoInfoDok::AAA_Ausgabekatalog", "AAA_Ausgabekatalog 2.0", "Model::GeoInfoDok",
		"AAA Ausgabekatalog");

	relocatePackage("Model::GeoInfoDok::AAA_Objektartenkatalog", "AAA_Objektartenkatalog 1.0", "Model::GeoInfoDok",
		"AAA Objektartenkatalog");

	relocatePackage("Model::GeoInfoDok::AFIS-ALKIS-ATKIS Anwendungsschema", "AFIS-ALKIS-ATKIS Anwendungsschema 7.1",
		"Model::GeoInfoDok", "AFIS-ALKIS-ATKIS Anwendungsschema");

	relocatePackage("Model::GeoInfoDok::BR_Bodenrichtwerte", "BR_Bodenrichtwerte 3.0", "Model::GeoInfoDok",
		"Bodenrichtwerte");

	relocatePackage("Model::GeoInfoDok::GN_Geographische Informationen", "GN_Geographische Informationen 1.0",
		"Model::GeoInfoDok", "Geographische Informationen");

	relocatePackage("Model::GeoInfoDok::GV_Geometrische Verbesserungen", "GV_Geometrische Verbesserungen 1.0",
		"Model::GeoInfoDok", "Geometrische Verbesserungen");

	relocatePackage("Model::GeoInfoDok::LB_Landbedeckung", "LB_Landbedeckung 1.0", "Model::GeoInfoDok",
		"Landbedeckung");

	relocatePackage("Model::GeoInfoDok::LN_Landnutzung", "LN_Landnutzung 1.0", "Model::GeoInfoDok", "Landnutzung");

	relocatePackage("Model::GeoInfoDok::Web Feature Service Erweiterungen", "Web Feature Service Erweiterungen 2.0",
		"Model::GeoInfoDok", "Web Feature Service Erweiterungen");

	relocatePackage("Model::GeoInfoDok::AAA_Signaturenkatalog", "AAA_Signaturenkatalog 1.1", "Model::GeoInfoDok",
		"AAA Signaturenkatalog");

	// OGC

	relocatePackage("Model::OGC::Filter Encoding 2.0", null, "Model::OGC", "Filter Encoding");

	relocatePackage("Model::OGC::OWS Common 1.1", null, "Model::OGC", "OWS Common");

	relocatePackage("Model::OGC::Web Feature Service 2.0", null, "Model::OGC", "Web Feature Service");
    }

    private void relocatePackage(String pkgToRelocateFullName, String newNameForPkgToRelocate,
	    String targetPkgParentFullName, String nameOfNewTargetPkg) {

	// find pkg to relocate and parent of target package (to be created)
	Optional<EAPackage> pkgToRelocateOpt = eaRepo.lookupPackage(pkgToRelocateFullName);
	Optional<EAPackage> targetPkgParentOpt = eaRepo.lookupPackage(targetPkgParentFullName);

	if (pkgToRelocateOpt.isPresent() && targetPkgParentOpt.isPresent()) {

	    Package pkgToRelocate = rep.GetPackageByID(pkgToRelocateOpt.get().getPkgId());
	    Package targetPkgParent = rep.GetPackageByID(targetPkgParentOpt.get().getPkgId());

	    if (StringUtils.isNotBlank(newNameForPkgToRelocate)) {
		try {
		    EAPackageUtil.setEAName(pkgToRelocate, newNameForPkgToRelocate);
		} catch (EAException e) {
		    result.addError(this, 1004, pkgToRelocateFullName, newNameForPkgToRelocate, e.getMessage());
		    return;
		}
	    }

	    try {
		Package newTargetPkg = EAPackageUtil.addPackage(targetPkgParent, nameOfNewTargetPkg, "");

		try {
		    EAPackageUtil.setEAParentID(pkgToRelocate, newTargetPkg.GetPackageID());
		} catch (EAException e) {
		    result.addError(this, 1005, pkgToRelocateFullName, nameOfNewTargetPkg, e.getMessage());
		    return;
		}

	    } catch (EAException e) {
		result.addError(this, 1001, nameOfNewTargetPkg, targetPkgParentFullName, e.getMessage());
	    }

	} else {

	    if (pkgToRelocateOpt.isEmpty()) {
		result.addError(this, 1002, pkgToRelocateFullName);
	    }
	    if (targetPkgParentOpt.isEmpty()) {
		result.addError(this, 1003, pkgToRelocateFullName, targetPkgParentFullName);
	    }
	}
    }

    private void transformAaaUnions() {

	result.addInfo(this, 900);

	if (dependenciesBySchema.containsKey(AAA_SCHEMA_FULL_NAME)) {

	    EAPackage aaaSchemaPkg = eaRepo.lookupPackage(AAA_SCHEMA_FULL_NAME).get();
	    EAPackage gidSchemaPkg = eaRepo.lookupPackage(GEOINFODOK_PKG_FULL_NAME).get();

	    result.addInfo(this, 901);
	    SortedSet<String> aaaUnionsPropertyChoice = new TreeSet<>(
		    Arrays.asList("AA_Modellart", "AA_Fachdatenobjekt", "AX_Reservierungsauftrag_Gebietskennung",
			    "AA_Empfaenger", "DCP", "AX_Lagebezeichnung", "AX_Listenelement3D"));
	    aaaPropertyChoiceUnionTransformation(aaaUnionsPropertyChoice, aaaSchemaPkg);

	    result.addInfo(this, 902);
	    SortedSet<String> aaaUnionsGeometryChoices = new TreeSet<>(
		    Arrays.asList("AA_Punktgeometrie", "AA_Liniengeometrie", "AA_Flaechengeometrie", "AU_Geometrie",
			    "AG_Geometrie", "AA_Punktgeometrie_3D", "AA_MehrfachLinienGeometrie_3D",
			    "AA_MehrfachFlaechenGeometrie_3D", "AU_Geometrie_3D"));
	    aaaGeometryUnionTransformation(aaaUnionsGeometryChoices, aaaSchemaPkg, gidSchemaPkg);

	    result.addInfo(this, 903);
	    SortedSet<String> aaaUnionsToReplaceWithCharacterString = new TreeSet<>(Arrays.asList("AA_UUID"));
	    aaaTypeMapping(aaaUnionsToReplaceWithCharacterString, "CharacterString", aaaSchemaPkg, gidSchemaPkg);

	} else {
	    // TBD
	}
    }

    private void aaaGeometryUnionTransformation(SortedSet<String> namesOfGeometryChoiceUnions, EAPackage aaaSchemaPkg,
	    EAPackage gidSchemaPkg) {

	java.util.Collection<EAElement> aaaSchemaElements = eaRepo.elementsAll(aaaSchemaPkg).values();
	java.util.Collection<EAElement> gidSchemaElements = eaRepo.elementsAll(gidSchemaPkg).values();

	// identify direct geometry type choices
	SortedMap<String, SortedSet<String>> directGeometryTypeChoicesByUnionName = new TreeMap<>();
	for (String unionName : namesOfGeometryChoiceUnions) {

	    List<EAElement> unionElements = aaaSchemaElements.stream().filter(elmt -> elmt.getName().equals(unionName))
		    .collect(Collectors.toList());

	    if (unionElements.size() > 1) {
		result.addError(this, 908, unionName);
	    } else if (unionElements.size() == 0) {
		result.addWarning(this, 909, unionName);
	    } else {

		EAElement unionEaElmt = unionElements.get(0);
		Element unionElmt = rep.GetElementByID(unionEaElmt.getElementId());

		Collection<Attribute> atts = unionElmt.GetAttributes();
		atts.Refresh();

		SortedSet<String> directGeometryTypeChoices = new TreeSet<>();
		for (Attribute att : atts) {
		    directGeometryTypeChoices.add(att.GetType());
		}
		directGeometryTypeChoicesByUnionName.put(unionName, directGeometryTypeChoices);
	    }
	}

	// now evaluate which GM type choices there really are per geometry union
	SortedMap<String, SortedSet<String>> allGMGeometryTypeChoicesByUnionName = new TreeMap<>();
	for (String geometryUnionName : directGeometryTypeChoicesByUnionName.keySet()) {
	    SortedSet<String> gmGeometryTypeChoices = identifyAllGMGeometryTypeChoices(geometryUnionName,
		    directGeometryTypeChoicesByUnionName, new HashSet<String>());
	    allGMGeometryTypeChoicesByUnionName.put(geometryUnionName, gmGeometryTypeChoices);
	}

	/*
	 * look up elements (classes and data types) in all GeoInfoDok elements, which
	 * have attributes with one of the geometry unions as type - ignore elements
	 * that are these unions
	 */

	for (EAElement eaElmt : gidSchemaElements) {

	    if (StringUtils.equalsAnyIgnoreCase(eaElmt.getMetaType(), "class", "datatype")
		    && !namesOfGeometryChoiceUnions.contains(eaElmt.getName())) {

		Element elmt = rep.GetElementByID(eaElmt.getElementId());

		Collection<Attribute> atts = elmt.GetAttributes();
		atts.Refresh();

		for (Attribute att : atts) {
		    String attType = att.GetType();
		    if (namesOfGeometryChoiceUnions.contains(attType)) {
			String attName = att.GetName();
			/*
			 * add constraint to owning element, which defines the allowed geometry types
			 * for this attribute
			 */
			SortedSet<String> allowedGMGeometryTypes = allGMGeometryTypeChoicesByUnionName.get(attType);
			String conTextLine = allowedGMGeometryTypes.size() > 1
				? "/*Attribut " + attName + " darf nur Werte der folgenden Typen enthalten: "
					+ StringUtils.join(allowedGMGeometryTypes, ", ") + "*/"
				: "/*Attribut " + attName + " darf nur Werte des folgenden Typs enthalten: "
					+ StringUtils.join(allowedGMGeometryTypes, ", ") + "*/";
			try {
			    aaaAddToAlleConstraint(elmt, conTextLine);
			} catch (EAException ex) {
			    result.addError(this, 911, "Alle", attName, elmt.GetName(), ex.getMessage());
			}
		    }
		}
	    } else {
		// ignore other element types
	    }
	}

	// finally, map the geometry type choice unions to GM_Object
	aaaTypeMapping(namesOfGeometryChoiceUnions, "GM_Object", aaaSchemaPkg, gidSchemaPkg);
    }

    private SortedSet<String> identifyAllGMGeometryTypeChoices(String geometryUnionName,
	    SortedMap<String, SortedSet<String>> directGeometryTypeChoicesByUnionName,
	    Set<String> evaluatedGeometryUnions) {

	evaluatedGeometryUnions.add(geometryUnionName);

	SortedSet<String> res = new TreeSet<>();

	if (directGeometryTypeChoicesByUnionName.containsKey(geometryUnionName)) {

	    SortedSet<String> directGeometryTypeChoices = directGeometryTypeChoicesByUnionName.get(geometryUnionName);

	    for (String choice : directGeometryTypeChoices) {
		if (choice.startsWith("GM")) {
		    res.add(choice);
		} else if (!evaluatedGeometryUnions.contains(choice)) {
		    res.addAll(identifyAllGMGeometryTypeChoices(choice, directGeometryTypeChoicesByUnionName,
			    evaluatedGeometryUnions));
		}
	    }
	} else {
	    result.addError(this, 910, geometryUnionName);
	}

	return res;
    }

    private void aaaPropertyChoiceUnionTransformation(SortedSet<String> namesOfPropertyChoiceUnions,
	    EAPackage aaaSchemaPkg) {

	/*
	 * Assumptions:
	 * 
	 * - union meta type is DataType
	 * 
	 * - union stereotype already correct
	 */

	java.util.Collection<EAElement> schemaElements = eaRepo.elementsAll(aaaSchemaPkg).values();

	for (String unionName : namesOfPropertyChoiceUnions) {

	    List<EAElement> unionDataTypes = schemaElements.stream()
		    .filter(elmt -> elmt.getMetaType().equalsIgnoreCase("datatype") && elmt.getName().equals(unionName))
		    .collect(Collectors.toList());

	    if (unionDataTypes.size() > 1) {
		result.addError(this, 904, unionName);
	    } else if (unionDataTypes.size() == 0) {
		result.addWarning(this, 905, unionName);
	    } else {

		EAElement eaElmt = unionDataTypes.get(0);
		Element elmt = rep.GetElementByID(eaElmt.getElementId());
		Collection<Attribute> atts = elmt.GetAttributes();
		atts.Refresh();

		for (Attribute att : atts) {
		    try {
			EAAttributeUtil.setEALowerBound(att, "0");
		    } catch (EAException ex) {
			result.addError(this, 906, att.GetName(), unionName, ex.getMessage());
		    }
		}

		String conTextLine = "/*Genau ein Attribut muss einen oder ggfs. mehrere Werte enthalten. Alle anderen Attribute dürfen keine Werte enthalten.*/";
		try {
		    aaaAddToAlleConstraint(elmt, conTextLine);
		} catch (EAException ex) {
		    result.addError(this, 907, "Alle", unionName, ex.getMessage());
		}
	    }
	}
    }

    private void aaaAddToAlleConstraint(Element elmt, String conTextLine) throws EAException {
	Optional<Constraint> conOpt = EAElementUtil.getConstraint(elmt, "Alle");
	if (conOpt.isPresent()) {
	    Constraint con = conOpt.get();
	    String conNotes = con.GetNotes();
	    String newConNotes = conNotes + "\n\n" + conTextLine;
	    EAConstraintUtil.setEANotes(con, newConNotes);
	} else {
	    EAElementUtil.addConstraint(elmt, "Alle", "OCL", conTextLine);
	}
    }

    private void aaaMeasureTypeTransformation() {

	result.addInfo(this, 801);

	if (dependenciesBySchema.containsKey(AAA_SCHEMA_FULL_NAME)) {

	    EAPackage aaaSchemaPkg = eaRepo.lookupPackage(AAA_SCHEMA_FULL_NAME).get();
	    EAPackage gidSchemaPkg = eaRepo.lookupPackage(GEOINFODOK_PKG_FULL_NAME).get();

	    aaaTypeMapping(aaaMeasureTypesToTransform, "Measure", aaaSchemaPkg, gidSchemaPkg);

	} else {
	    // TBD
	}
    }

    private void aaaTypeMapping(SortedSet<String> typesToMap, String targetTypeName, EAPackage aaaSchemaPkg,
	    EAPackage gidSchemaPkg) {

	java.util.Collection<EAElement> aaaSchemaElements = eaRepo.elementsAll(aaaSchemaPkg).values();
	java.util.Collection<EAElement> gidSchemaElements = eaRepo.elementsAll(gidSchemaPkg).values();

	// lookup target type
	Optional<EAElement> targetElmtOpt = lookupAllowedElement(targetTypeName, allowedElements(aaaSchemaPkg));

	if (targetElmtOpt.isPresent()) {

	    EAElement targetElmt = targetElmtOpt.get();

	    List<EAElement> aaaElmtsToTransform = aaaSchemaElements.stream()
		    .filter(elmt -> typesToMap.contains(elmt.getName())).collect(Collectors.toList());

	    /*
	     * update attributes in all GeoInfoDok schema elements: if a AAA type is set (as
	     * classifier ID and/or type), use the target type
	     */
	    for (EAElement eaElmt : gidSchemaElements) {

		if (StringUtils.equalsAnyIgnoreCase(eaElmt.getMetaType(), "class", "datatype")) {

		    Element elmt = rep.GetElementByID(eaElmt.getElementId());
		    Collection<Attribute> atts = elmt.GetAttributes();
		    atts.Refresh();

		    for (Attribute att : atts) {

			int attClassifierId = att.GetClassifierID();
			String attType = att.GetType();

			if (aaaElmtsToTransform.stream()
				.anyMatch(e -> e.getElementId() == attClassifierId || e.getName().equals(attType))) {

			    String attFullName = attFullName(eaElmt, att.GetName());
			    try {
				EAAttributeUtil.setEAClassifierID(att, targetElmt.getElementId());
				EAAttributeUtil.setEAType(att, targetElmt.getName());
				MessageContext mc = result.addInfo(this, 802, targetTypeName);
				if (mc != null) {
				    mc.addDetail(this, 3, attFullName);
				}
			    } catch (EAException ex) {
				MessageContext mc = result.addError(this, 800, ex.getMessage());
				if (mc != null) {
				    mc.addDetail(this, 3, attFullName);
				}
			    }
			}
		    }
		}
	    }

	    // delete aaaElmtsToTransform in the actual repository
	    for (EAElement eaElmt : aaaElmtsToTransform) {
		Element elmt = rep.GetElementByID(eaElmt.getElementId());
		Package pkg = rep.GetPackageByID(elmt.GetPackageID());
		EAPackageUtil.deleteElement(pkg, eaElmt.getElementId());
	    }

	    /*
	     * finally, delete the aaaElmtsToTransform in relevant places
	     */
	    eaRepo.deleteElements(aaaElmtsToTransform);

	} else {
	    // TBD
	}
    }

    private void reportPackageDependencies(String schemaFullName) {

	if (packageDependenciesBySchema.containsKey(schemaFullName)) {
	    List<PackageDependency> dependencies = packageDependenciesBySchema.get(schemaFullName);

	    dependencies.sort(Comparator.comparing(PackageDependency::getCaseInfo)
		    .thenComparing(PackageDependency::getSchemaFullName));

	    for (PackageDependency pd : dependencies) {

		MessageContext mc = result.addInfo(this, 121, pd.getCaseInfo());
		if (mc != null) {
		    mc.addDetail(this, 2, pd.getSchemaFullName());
		    mc.addDetail(this, 6, pd.getExternalFullName());
		}
	    }
	}
    }

    private void checkForDuplicateElementsInSchemasAndDependencies() {

	result.addInfo(this, 106);
	result.addInfo(this, 122, StringUtils.join(metaTypesToIgnoreInDuplicateElementCheck, ", "));

	for (String schemaFullName : dependenciesBySchema.keySet()) {

	    result.addInfo(this, 107, schemaFullName);

	    Optional<EAPackage> schemaPkgOpt = eaRepo.lookupPackage(schemaFullName);

	    if (schemaPkgOpt.isPresent()) {

		EAPackage schemaPkg = schemaPkgOpt.get();

		Set<EAElement> allowedElements = allowedElements(schemaPkg);

		Set<EAElement> tmp = new HashSet<>();
		Set<EAElement> duplicates = new HashSet<>();

		for (EAElement elmt : allowedElements) {

		    if (StringUtils.isNotBlank(elmt.getName())
			    && !hasMetatypeToIgnore(elmt, metaTypesToIgnoreInDuplicateElementCheck)) {

			Optional<EAElement> existingElmt = tmp.stream()
				.filter(e -> e.getName().equals(elmt.getName())
					&& !hasMetatypeToIgnore(elmt, metaTypesToIgnoreInDuplicateElementCheck))
				.findAny();
			if (existingElmt.isPresent()) {
			    duplicates.add(existingElmt.get());
			    duplicates.add(elmt);
			} else {
			    tmp.add(elmt);
			}
		    }
		}

		SortedMap<String, List<EAElement>> duplicateElementsByName = duplicates.stream()
			.collect(Collectors.groupingBy(EAElement::getName, TreeMap::new, Collectors.toList()));

		for (Entry<String, List<EAElement>> e : duplicateElementsByName.entrySet()) {

		    MessageContext mc = result.addWarning(this, 105, e.getKey());
		    if (mc != null) {
			List<String> duplicateElementsFullNameSorted = e.getValue().stream()
				.map(elmt -> "(" + elmt.getMetaType() + ") " + elmt.getFullName()).sorted()
				.collect(Collectors.toList());
			for (String fn : duplicateElementsFullNameSorted) {
			    mc.addDetail(this, 2, fn);
			}
		    }
		}
	    }
	}
    }

    private boolean hasMetatypeToIgnore(EAElement elmt, List<String> metaTypesToIgnore) {
	return StringUtils.equalsAnyIgnoreCase(elmt.getMetaType(),
		metaTypesToIgnore.toArray(new String[metaTypesToIgnore.size()]));
    }

    private void identifyPackageDependencies() {

	result.addInfo(this, 113);

	for (String schemaFullName : dependenciesBySchema.keySet()) {

	    result.addInfo(this, 114, schemaFullName);

	    currentSchemaForProcessingSchemaDependencies = schemaFullName;
	    Optional<EAPackage> schemaPkgOpt = eaRepo.lookupPackage(schemaFullName);

	    if (schemaPkgOpt.isPresent()) {

		EAPackage schemaPkg = schemaPkgOpt.get();

		// erlaubte Ziel-Pakete und Elemente für Menge der definierten
		// Abhängigkeiten ermitteln
		Set<EAPackage> allowedPackages = allowedPackages(schemaPkg);
		Set<EAElement> allowedElements = allowedElements(schemaPkg);

		Set<EAPackage> schemaPackages = packagesOfSchema(schemaPkg);
		List<EAElement> schemaElements = new ArrayList<>(eaRepo.elementsAll(schemaPkg).values());

		result.addInfo(this, 115, schemaFullName);

		for (EAPackage pkg : packagesOfSchema(schemaPkg)) {
		    analyzeConnectors(schemaFullName, pkg, schemaPackages, schemaElements, allowedPackages,
			    allowedElements);
		}

		result.addInfo(this, 116, schemaFullName);

		for (EAElement elmt : schemaElements) {
		    analyzeConnectors(schemaFullName, elmt, schemaPackages, schemaElements, allowedPackages,
			    allowedElements);
		}

		result.addInfo(this, 118, schemaFullName);

		for (EAElement elmt : schemaElements) {
		    analyzeElements(schemaFullName, elmt, allowedElements);
		}

		result.addInfo(this, 117, schemaFullName);

		for (EAElement elmt : schemaElements) {
		    analyzeAttributes(schemaFullName, elmt, allowedElements);
		}

		result.addInfo(this, 119, schemaFullName);

		for (EAElement elmt : schemaElements) {
		    analyzeOperationsAndParameters(schemaFullName, elmt, allowedElements);
		}

		result.addInfo(this, 120, schemaFullName);
		analyzeDiagrams(schemaFullName, allowedPackages, allowedElements);

		reportPackageDependencies(schemaFullName);
	    }
	}
    }

    private void analyzeDiagrams(String schemaFullName, Set<EAPackage> allowedPackages,
	    Set<EAElement> allowedElements) {

	Set<EAPackage> eaPkgs = packagesOfSchema(eaRepo.lookupPackage(schemaFullName).get());

	for (EAPackage eaPkg : eaPkgs) {
	    Package pkg = rep.GetPackageByID(eaPkg.getPkgId());
	    Collection<Diagram> diagrams = pkg.GetDiagrams();
	    diagrams.Refresh();
	    for (Diagram d : diagrams) {
		String diagramName = d.GetName();
		Collection<DiagramObject> diagramObjects = d.GetDiagramObjects();
		diagramObjects.Refresh();
		for (DiagramObject dobj : diagramObjects) {
		    int targetElmtId = dobj.GetElementID();
		    Optional<EAElement> eaTargetElmtOpt = eaRepo.lookupElement(targetElmtId);
		    Optional<EAPackage> eaTargetPkgOpt = eaRepo.lookupPackageByElementId(targetElmtId);
		    /*
		     * check if the target element that is referenced by the diagram object is
		     * present (i.e., was identified while loading the model), and if it is allowed
		     */
		    if ((eaTargetElmtOpt.isPresent() || eaTargetPkgOpt.isPresent())
			    && !(inPackageElementIds(targetElmtId, allowedPackages)
				    || inElementIds(targetElmtId, allowedElements))) {

			AbstractEAModelElement eaModelElmt = eaTargetElmtOpt.isPresent() ? eaTargetElmtOpt.get()
				: eaTargetPkgOpt.get();
			String caseInfo = "Diagram Object - diagram '" + diagramName + "', external model element: '"
				+ eaModelElmt.getName() + "'";
			String schemaElementFullName = eaPkg.getFullName() + " - diagram '" + diagramName + "'";
			String externalElementFullName = eaModelElmt.getFullName();
			boolean externalAllowed = false;

			PackageDependency pd = new PackageDependency(caseInfo, schemaElementFullName,
				externalElementFullName, externalAllowed);

			addPackageDependency(schemaFullName, pd);
		    }
		}
	    }
	}
    }

    private void analyzeOperationsAndParameters(String schemaFullName, EAElement eaElmt,
	    Set<EAElement> allowedElements) {

	Element elmt = rep.GetElementByID(eaElmt.getElementId());

	Collection<Method> operations = elmt.GetMethods();
	operations.Refresh();

	for (Method op : operations) {

	    String opName = op.GetName();
	    String opFullName = eaElmt.getFullName() + "." + opName;
	    try {
		int classifierId = Integer.parseInt(op.GetClassifierID());
		if (classifierId != 0) {
		    // check linked classifier
		    if (!inElementIds(classifierId, allowedElements)) {

			String caseInfo = "Operation (ClassifierID)";
			String schemaElementFullName = opFullName;
			String externalElementFullName = eaRepo.getFullName(classifierId)
				.orElse(FULL_NAME_FOR_MISSING_ELEMENT);
			boolean externalAllowed = false;

			PackageDependency pd = new PackageDependency(caseInfo, schemaElementFullName,
				externalElementFullName, externalAllowed);

			addPackageDependency(schemaFullName, pd);
		    }
		}
	    } catch (NumberFormatException e) {
		// ignore
	    }

	    Collection<Parameter> parameters = op.GetParameters();
	    parameters.Refresh();

	    for (Parameter p : parameters) {
		String pName = p.GetName();
		String pFullName = opFullName + "(" + pName + ")";

		try {
		    int classifierId = Integer.parseInt(p.GetClassifierID());
		    if (classifierId != 0) {
			// check linked classifier
			if (!inElementIds(classifierId, allowedElements)) {

			    String caseInfo = "Operation parameter (ClassifierID)";
			    String schemaElementFullName = pFullName;
			    String externalElementFullName = eaRepo.getFullName(classifierId)
				    .orElse(FULL_NAME_FOR_MISSING_ELEMENT);
			    boolean externalAllowed = false;

			    PackageDependency pd = new PackageDependency(caseInfo, schemaElementFullName,
				    externalElementFullName, externalAllowed);

			    addPackageDependency(schemaFullName, pd);
			}
		    }
		} catch (NumberFormatException e) {
		    // ignore
		}
	    }
	}
    }

    private void analyzeElements(String schemaFullName, EAElement eaElmt, Set<EAElement> allowedElements) {

	Element elmt = rep.GetElementByID(eaElmt.getElementId());

	// check linked classifier
	int classifierId = elmt.GetClassifierID();
	if (classifierId != 0) {
	    if (!inElementIds(classifierId, allowedElements)) {
		String caseInfo = "Element (metaType: " + elmt.GetMetaType() + ") ClassifierID";
		String schemaElementFullName = eaElmt.getFullName();
		String externalElementFullName = eaRepo.getFullName(classifierId).orElse(FULL_NAME_FOR_MISSING_ELEMENT);
		boolean externalAllowed = false;
		PackageDependency pd = new PackageDependency(caseInfo, schemaElementFullName, externalElementFullName,
			externalAllowed);
		addPackageDependency(schemaFullName, pd);
	    }
	}

    }

    private void processSchemaDependencies() {

	result.addInfo(this, 108);

	for (String schemaFullName : dependenciesBySchema.keySet()) {

	    result.addInfo(this, 109, schemaFullName);

	    Optional<EAPackage> schemaPkgOpt = eaRepo.lookupPackage(schemaFullName);

	    if (schemaPkgOpt.isPresent()) {

		EAPackage schemaPkg = schemaPkgOpt.get();

		// erlaubte Ziel-Pakete und Elemente für Menge der definierten
		// Abhängigkeiten ermitteln
		Set<EAPackage> allowedPackages = allowedPackages(schemaPkg);
		Set<EAElement> allowedElements = allowedElements(schemaPkg);

		Set<EAPackage> schemaPackages = packagesOfSchema(schemaPkg);
		List<EAElement> schemaElements = new ArrayList<>(eaRepo.elementsAll(schemaPkg).values());

		if (PROCESS_CONNECTORS) {

		    /*
		     * Links checken (nur auf IDs erlaubter Ziel-Pakete und Elemente)
		     */

		    result.addInfo(this, 110, schemaFullName);

		    for (EAPackage pkg : packagesOfSchema(schemaPkg)) {
			processConnectors(pkg, schemaPackages, schemaElements, allowedPackages, allowedElements);
		    }

		    // ensure that all defined schema dependencies are explicitly modeled
		    ensureSchemaDependenciesModeled(schemaFullName);

		    result.addInfo(this, 111, schemaFullName);

		    for (EAElement elmt : schemaElements) {
			processConnectors(elmt, schemaPackages, schemaElements, allowedPackages, allowedElements);
		    }
		}

		if (PROCESS_ATTRIBUTES) {

		    result.addInfo(this, 112, schemaFullName);

		    for (EAElement elmt : schemaElements) {
			processAttributes(elmt, allowedPackages, allowedElements);
		    }
		}

//		if (PROCESS_OPERATIONS) {
//		    
//		    result.addInfo(this, 123, schemaFullName);
//
//		    for (EAElement elmt : schemaElements) {
//			processOperations(elmt, allowedPackages, allowedElements);
//		    }
//		}
	    }
	}
    }

//    private void processOperations(EAElement eaElmt, Set<EAPackage> allowedPackages, Set<EAElement> allowedElements) {
//
//	Element elmt = rep.GetElementByID(eaElmt.getElementId());
//
//	Collection<Method> operations = elmt.GetMethods();
//	operations.Refresh();
//	
//	for (Method op : operations) {
//	    
//	    // check and, if relevant, update the return type
//	    String returnType = op.GetReturnType();
//	    String returnClassifierId = op.GetClassifierID();
//	    
//	    Collection<Parameter> parameters = op.GetParameters();
//	    parameters.Refresh();
//	    
//	    for(Parameter param : parameters) {
//		
//		String paramType = param.GetType();
//		String paramClassifierId = param.GetClassifierID();
//	    }
//	}
//    }

    private void ensureSchemaDependenciesModeled(String schemaFullName) {

	EAPackage eaSchemaPkg = eaRepo.lookupPackage(schemaFullName).get();
	Package schemaPkg = rep.GetPackageByID(eaSchemaPkg.getPkgId());

	// identify which dependencies are already modeled and which are missing
	List<EAPackage> dependenciesEa = this.dependenciesBySchema.get(schemaFullName);
	SortedMap<String, Integer> dependencyPkgElementIdToAddByDepName = new TreeMap<>();
	for (EAPackage depEa : dependenciesEa) {
	    if (!isTargetOfDependencyConnector(depEa.getPkgElementId(), schemaPkg.GetConnectors())) {
		dependencyPkgElementIdToAddByDepName.put(depEa.getName(), depEa.getPkgElementId());
	    }
	}

	// add missing dependencies
	for (Entry<String, Integer> e : dependencyPkgElementIdToAddByDepName.entrySet()) {
	    try {
		result.addInfo(this, 702, eaSchemaPkg.getName(), e.getKey());
		EAPackageUtil.createDependencyConnector(schemaPkg, e.getValue());
	    } catch (EAException ex) {
		result.addError(this, 703, eaSchemaPkg.getName(), e.getKey(), ex.getMessage());
	    }
	}
    }

    private boolean isTargetOfDependencyConnector(int depPkgElementId, Collection<Connector> connectors) {
	connectors.Refresh();
	for (Connector conn : connectors) {
	    if (conn.GetType().equalsIgnoreCase("dependency") && conn.GetSupplierID() == depPkgElementId) {
		return true;
	    }
	}
	return false;
    }

    private void analyzeConnectors(String schemaFullName, EAPackage eaPkg, Set<EAPackage> schemaPackages,
	    List<EAElement> schemaElements, Set<EAPackage> allowedPackages, Set<EAElement> allowedElements) {
	Package pkg = rep.GetPackageByID(eaPkg.getPkgId());
	analyzeConnectors(schemaFullName, true, eaPkg.getFullName(), pkg.GetConnectors(), schemaPackages,
		schemaElements, allowedPackages, allowedElements);
    }

    private void analyzeConnectors(String schemaFullName, EAElement eaElmt, Set<EAPackage> schemaPackages,
	    List<EAElement> schemaElements, Set<EAPackage> allowedPackages, Set<EAElement> allowedElements) {
	Element elmt = rep.GetElementByID(eaElmt.getElementId());
	analyzeConnectors(schemaFullName, false, eaElmt.getFullName(), elmt.GetConnectors(), schemaPackages,
		schemaElements, allowedPackages, allowedElements);
    }

    private void processConnectors(EAPackage eaPkg, Set<EAPackage> schemaPackages, List<EAElement> schemaElements,
	    Set<EAPackage> allowedPackages, Set<EAElement> allowedElements) {
	Package pkg = rep.GetPackageByID(eaPkg.getPkgId());
	processConnectors(true, eaPkg.getFullName(), pkg.GetConnectors(), schemaPackages, schemaElements,
		allowedPackages, allowedElements);
    }

    private void processConnectors(EAElement eaElmt, Set<EAPackage> schemaPackages, List<EAElement> schemaElements,
	    Set<EAPackage> allowedPackages, Set<EAElement> allowedElements) {
	Element elmt = rep.GetElementByID(eaElmt.getElementId());
	processConnectors(false, eaElmt.getFullName(), elmt.GetConnectors(), schemaPackages, schemaElements,
		allowedPackages, allowedElements);
    }

    private void processConnectors(boolean isCheckForPackageConnectors, String fullNameOfContext,
	    Collection<Connector> conns, Set<EAPackage> schemaPackages, List<EAElement> schemaElements,
	    Set<EAPackage> allowedPackages, Set<EAElement> allowedElements) {

	conns.Refresh();

	List<Integer> connectorIdsOfSchemaDependenciesToRemove = new ArrayList<>();

	for (Connector conn : conns) {

	    int clientId = conn.GetClientID();
	    int supplierId = conn.GetSupplierID();

	    if ((inPackageElementIds(clientId, schemaPackages) || inElementIds(clientId, schemaElements))
		    && (inPackageElementIds(supplierId, allowedPackages)
			    || inElementIds(supplierId, allowedElements))) {

		// both ends of the connector are elements of the schema - fine

	    } else {

		boolean connectorSourceAndTargetNeedToBeSwitched = false;
		boolean isIllegalDependency = false;
		String externalElementInfo;
		int externalElementId;

		if (inPackageElementIds(clientId, schemaPackages) || inElementIds(clientId, schemaElements)) {

		    // the supplier/target is not a schema element

		    // connector from schema element to external element (in allowed dependencies or
		    // outside)

		    externalElementInfo = "target";
		    externalElementId = supplierId;

		    /*
		     * So we have a package dependency on the external package. That is fine, if the
		     * external element is in allowed dependencies. Otherwise, it would constitute
		     * an incorrect package dependency.
		     */

		    if (inPackageElementIds(supplierId, allowedPackages) || inElementIds(supplierId, allowedElements)) {
			// target is in allowed dependencies
		    } else {
			// target is in illegal package
			isIllegalDependency = true;
		    }

		    /*
		     * If the connector is an association or aggregation, and ONLY the source end is
		     * navigable, the connector would be wrong. The connector should be modeled the
		     * other way round, so that the external package has a package dependency on the
		     * schema package.
		     */

		    String metaType = conn.GetMetaType();

		    if (StringUtils.equalsAnyIgnoreCase(metaType, "association", "aggregation")
			    && EAConnectorEndUtil.isNavigable(conn.GetClientEnd(), conn, true)
			    && !EAConnectorEndUtil.isNavigable(conn.GetSupplierEnd(), conn, true)) {
			connectorSourceAndTargetNeedToBeSwitched = true;
		    }

		} else {

		    // the client/source is outside of allowed elements

		    // connector from external element (in allowed dependencies or
		    // outside) to schema element

		    externalElementInfo = "source";
		    externalElementId = clientId;

		    // so we would have a package dependency from the external package

		    /*
		     * If the connector is an association or aggregation, and ONLY the source end is
		     * navigable, the connector would be wrong. The connector should be modeled the
		     * other way round, so that the schema package has a package dependency on the
		     * target package.
		     */

		    String metaType = conn.GetMetaType();

		    if (StringUtils.equalsAnyIgnoreCase(metaType, "association", "aggregation")
			    && EAConnectorEndUtil.isNavigable(conn.GetClientEnd(), conn, true)
			    && !EAConnectorEndUtil.isNavigable(conn.GetSupplierEnd(), conn, true)) {

			connectorSourceAndTargetNeedToBeSwitched = true;

			/*
			 * In this case, we would actually have a dependency on the external package.
			 * That is fine, if the external element is in allowed dependencies. Otherwise,
			 * it would constitute an incorrect package dependency.
			 */

			if (inPackageElementIds(clientId, allowedPackages) || inElementIds(clientId, allowedElements)) {
			    // source is in allowed dependencies
			} else {
			    // source is in illegal package
			    isIllegalDependency = true;
			}
		    }
		}

		// first, switch source and target of association/aggregation, if necessary
		if (connectorSourceAndTargetNeedToBeSwitched) {

		    Optional<String> externalFullNameOpt = eaRepo.getFullName(externalElementId);

		    MessageContext mc = result.addError(this, 701, externalElementInfo,
			    EAConnectorUtil.connectorInfo(conn, rep));
		    if (mc != null) {
			mc.addDetail(this, isCheckForPackageConnectors ? 5 : 2, fullNameOfContext);
			mc.addDetail(this, 6,
				externalFullNameOpt.isPresent() ? externalFullNameOpt.get() : "no value present");
		    }
		}

		/*
		 * second, handle an illegal dependency (TODO fix, if possible - for now, just
		 * report it)
		 */
		if (isIllegalDependency) {

		    if (externalElementInfo.equalsIgnoreCase("target")) {

			// if this is a dependency of a schema package, note it for removal
			if (isDependency(conn) && isSchema(fullNameOfContext)) {

			    connectorIdsOfSchemaDependenciesToRemove.add(conn.GetConnectorID());

			} else {

			    Optional<String> externalFullNameOpt = eaRepo.getFullName(externalElementId);

			    MessageContext mc = result.addError(this, 700, externalElementInfo,
				    EAConnectorUtil.connectorInfo(conn, rep));
			    if (mc != null) {
				mc.addDetail(this, isCheckForPackageConnectors ? 5 : 2, fullNameOfContext);
				mc.addDetail(this, 6, externalFullNameOpt.isPresent() ? externalFullNameOpt.get()
					: "no value present");
			    }
			}
		    }
		}
	    }
	}

	if (!connectorIdsOfSchemaDependenciesToRemove.isEmpty()) {
	    EAPackage eaSchemaPkg = eaRepo.lookupPackage(fullNameOfContext).get();
	    Package schemaPkg = rep.GetPackageByID(eaSchemaPkg.getPkgId());
	    for (Integer connId : connectorIdsOfSchemaDependenciesToRemove) {
		EAPackageUtil.deleteConnector(schemaPkg, connId);
	    }
	}
    }

    private boolean isSchema(String fullName) {
	return dependenciesBySchema.containsKey(fullName);
    }

    private boolean isDependency(Connector conn) {
	return conn.GetType().equalsIgnoreCase("dependency");
    }

    private void analyzeConnectors(String schemaFullName, boolean isCheckForPackageConnectors, String fullNameOfContext,
	    Collection<Connector> conns, Set<EAPackage> schemaPackages, List<EAElement> schemaElements,
	    Set<EAPackage> allowedPackages, Set<EAElement> allowedElements) {

	conns.Refresh();

	for (Connector conn : conns) {

	    int clientId = conn.GetClientID();
	    int supplierId = conn.GetSupplierID();

	    if ((inPackageElementIds(clientId, schemaPackages) || inElementIds(clientId, schemaElements))
		    && (inPackageElementIds(supplierId, allowedPackages)
			    || inElementIds(supplierId, allowedElements))) {

		// both ends of the connector are elements of the schema - fine

	    } else {

		if (inPackageElementIds(clientId, schemaPackages) || inElementIds(clientId, schemaElements)) {

		    // the supplier/target is not a schema element

		    // connector from schema element to external element (in allowed dependencies or
		    // outside)

		    int externalElementId = supplierId;

		    /*
		     * So we have a package dependency on the external package. That is fine, if the
		     * external element is in allowed dependencies. Otherwise, it would constitute
		     * an incorrect package dependency.
		     */

		    String caseInfo = "Connector - " + EAConnectorUtil.connectorInfo(conn, rep);
		    String schemaElementFullName = fullNameOfContext;
		    String externalElementFullName = eaRepo.getFullName(externalElementId)
			    .orElse(FULL_NAME_FOR_MISSING_ELEMENT);
		    boolean externalAllowed = inPackageElementIds(externalElementId, allowedPackages)
			    || inElementIds(externalElementId, allowedElements);

		    PackageDependency pd = new PackageDependency(caseInfo, schemaElementFullName,
			    externalElementFullName, externalAllowed);

		    addPackageDependency(schemaFullName, pd);

		} else {

		    // the client/source is outside of allowed elements -> does not constitute a
		    // package dependency of the schema
		}
	    }
	}

    }

    private void addPackageDependency(String schemaFullName, PackageDependency pd) {

	List<PackageDependency> pds;
	if (this.packageDependenciesBySchema.containsKey(schemaFullName)) {
	    pds = this.packageDependenciesBySchema.get(schemaFullName);
	} else {
	    pds = new ArrayList<>();
	    this.packageDependenciesBySchema.put(schemaFullName, pds);
	}
	pds.add(pd);
    }

    private void processAttributes(EAElement eaElmt, Set<EAPackage> allowedPackages, Set<EAElement> allowedElements) {

	Element elmt = rep.GetElementByID(eaElmt.getElementId());

	Collection<Attribute> attributes = elmt.GetAttributes();
	attributes.Refresh();

	for (Attribute att : attributes) {

	    String attName = att.GetName();
	    String attFullName = attFullName(eaElmt, attName);
	    int classifierId = att.GetClassifierID();
	    String type = att.GetType();
	    String originalType = type;

	    boolean updateType = false;
	    boolean updateClassifier = false;
	    boolean updateTypeSetAndSequenceInfos = false;

	    if (StringUtils.startsWithAny(type, "Set<", "Sequence<")) {

		MessageContext mc = result.addInfo(this, 307, originalType);
		if (mc != null) {
		    mc.addDetail(this, 3, attFullName);
		}

		if (type.startsWith("Set<")) {
		    updateTypeSetAndSequenceInfos = true;
		    type = StringUtils.substringBetween(type, "Set<", ">");
		} else if (type.startsWith("Sequence<")) {
		    updateTypeSetAndSequenceInfos = true;
		    type = StringUtils.substringBetween(type, "Sequence<", ">");
		}
	    }

	    if (classMappings.containsKey(type)) {

		String mappingTarget = classMappings.get(type);
		updateType = true;
		updateClassifier = true;

		MessageContext mc = result.addInfo(this, 306, type, mappingTarget);
		if (mc != null) {
		    mc.addDetail(this, 3, attFullName);
		}

		type = mappingTarget;

	    } else if (classifierId == 0) {

		if (type.equals("<undefined>") || StringUtils.isBlank(type)) {
		    /*
		     * Should be enum or code. TBD: Should we check more, for example to see if a
		     * type for a normal attribute is missing?
		     * 
		     * Regarding linking, there is nothing we can do if the type is undefined.
		     */

		} else {

		    MessageContext mc;
		    if (ONLY_LINK_ANALYSIS) {
			mc = result.addInfo(this, 300, type);
		    } else {
			mc = result.addDebug(this, 300, type);
			updateClassifier = true;
		    }
		    if (mc != null) {
			mc.addDetail(this, 3, attFullName);
		    }
		}

	    } else {

		// check linked classifier
		if (!inElementIds(classifierId, allowedElements)) {

		    Optional<EAElement> externalElmt = eaRepo.lookupElement(classifierId);
		    String externalElmtFullName = externalElmt.isPresent() ? externalElmt.get().getFullName()
			    : "<external element not found>";
		    MessageContext mc;
		    if (ONLY_LINK_ANALYSIS) {
			mc = result.addInfo(this, 301);
		    } else {
			mc = result.addDebug(this, 301);
			updateClassifier = true;
		    }

		    if (mc != null) {
			mc.addDetail(this, 6, externalElmtFullName);
		    }
		}
	    }

	    if (updateTypeSetAndSequenceInfos) {

		try {
		    EAAttributeUtil.setEAType(att, type);
		    EAAttributeUtil.setEALowerBound(att, "0");
		    EAAttributeUtil.setEAUpperBound(att, "*");
		    if (originalType.startsWith("Set")) {
			EAAttributeUtil.setEAIsOrdered(att, false);
			EAAttributeUtil.setEAAllowDuplicates(att, false);
		    } else {
			// assuming Sequence
			EAAttributeUtil.setEAIsOrdered(att, true);
			EAAttributeUtil.setEAAllowDuplicates(att, true);
		    }
		} catch (EAException e) {
		    MessageContext mc = result.addInfo(this, 304, e.getMessage());
		    if (mc != null) {
			mc.addDetail(this, 3, attFullName);
		    }
		}
	    }

	    if (updateType) {
		try {
		    EAAttributeUtil.setEAType(att, type);
		} catch (EAException e) {
		    MessageContext mc = result.addInfo(this, 305, e.getMessage());
		    if (mc != null) {
			mc.addDetail(this, 3, attFullName);
		    }
		}
	    }

	    if (updateClassifier) {

		Optional<EAElement> allowedElmt = lookupAllowedElement(type, allowedElements);

		if (allowedElmt.isPresent()) {

		    try {
			EAAttributeUtil.setEAClassifierID(att, allowedElmt.get().getElementId());
		    } catch (EAException e) {
			MessageContext mc = result.addInfo(this, 303, e.getMessage());
			if (mc != null) {
			    mc.addDetail(this, 3, attFullName);
			}
		    }

		} else {

		    MessageContext mc = result.addInfo(this, 302, type);
		    if (mc != null) {
			mc.addDetail(this, 3, attFullName);
		    }
		}
	    }
	}
    }

    private String attFullName(EAElement eaElmt, String attName) {
	return eaElmt.getFullName() + "." + attName;
    }

    private void analyzeAttributes(String schemaFullName, EAElement eaElmt, Set<EAElement> allowedElements) {

	Element elmt = rep.GetElementByID(eaElmt.getElementId());

	Collection<Attribute> attributes = elmt.GetAttributes();
	attributes.Refresh();

	for (Attribute att : attributes) {

	    String attName = att.GetName();
	    String attFullName = eaElmt.getFullName() + "." + attName;
	    int classifierId = att.GetClassifierID();

	    if (classifierId != 0) {

		// check linked classifier
		if (!inElementIds(classifierId, allowedElements)) {

		    String caseInfo = "Attribute (ClassifierID)";
		    String schemaElementFullName = attFullName;
		    String externalElementFullName = eaRepo.getFullName(classifierId)
			    .orElse(FULL_NAME_FOR_MISSING_ELEMENT);
		    boolean externalAllowed = false;

		    PackageDependency pd = new PackageDependency(caseInfo, schemaElementFullName,
			    externalElementFullName, externalAllowed);

		    addPackageDependency(schemaFullName, pd);
		}
	    }
	}
    }

    private Optional<EAElement> lookupAllowedElement(int elementId, Set<EAElement> allowedElements) {
	return allowedElements.stream().filter(elmt -> elmt.getElementId() == elementId).findAny();
    }

    private Optional<EAElement> lookupAllowedElement(String elementName, Set<EAElement> allowedElements) {
	return allowedElements.stream().filter(elmt -> elmt.getName().equals(elementName)).findAny();
    }

    private boolean inElementIds(int elementId, java.util.Collection<EAElement> elements) {

	Optional<EAElement> opt = elements.stream().filter(elmt -> elmt.getElementId() == elementId).findAny();

	if (opt.isPresent()) {
	    EAElement elmt = opt.get();
	    addVisitedSchemaDependency(elmt.getFullName());
	    return true;
	} else {
	    return false;
	}
    }

    private void addVisitedSchemaDependency(String fullName) {

	if (!inIdentifyPackageDependencies) {
	    return;
	}

	if (fullName.startsWith(currentSchemaForProcessingSchemaDependencies)) {
	    /*
	     * fullName belongs to the schema that is being processed, not one of its
	     * dependencies
	     */
	} else {

	    List<String> dependencyList = this.dependenciesBySchemaIn.get(currentSchemaForProcessingSchemaDependencies);
	    for (String dependency : dependencyList) {
		if (fullName.startsWith(dependency)) {
		    SortedSet<String> visitedDependencies;
		    if (this.visitedDependenciesBySchemaIn.containsKey(currentSchemaForProcessingSchemaDependencies)) {
			visitedDependencies = visitedDependenciesBySchemaIn
				.get(currentSchemaForProcessingSchemaDependencies);
		    } else {
			visitedDependencies = new TreeSet<>();
			visitedDependenciesBySchemaIn.put(currentSchemaForProcessingSchemaDependencies,
				visitedDependencies);
		    }
		    visitedDependencies.add(dependency);
		}
	    }
	}
    }

    private boolean inPackageElementIds(int elementId, java.util.Collection<EAPackage> packages) {
	return packages.stream().anyMatch(pkg -> pkg.getPkgElementId() == elementId);
    }

    private Set<EAPackage> allowedPackages(EAPackage schemaPkg) {
	Set<EAPackage> res = new HashSet<>();
	res.addAll(packagesOfSchema(schemaPkg));
	for (EAPackage dependency : dependenciesBySchema.get(schemaPkg.getFullName())) {
	    res.addAll(packagesOfSchema(dependency));
	}
	return res;
    }

    /**
     * @param schemaPkg
     * @return set with the schema package and all of its direct and indirect
     *         children
     */
    private Set<EAPackage> packagesOfSchema(EAPackage schemaPkg) {
	Set<EAPackage> res = new HashSet<>();
	res.add(schemaPkg);
	res.addAll(eaRepo.childrenAll(schemaPkg).values());
	return res;
    }

    /**
     * @param schemaPkg
     * @return the elements of the schema and its dependencies, excluding those with
     *         (ignoring case) 'informative' or 'example in their full name
     */
    private Set<EAElement> allowedElements(EAPackage schemaPkg) {

	Set<EAElement> tmp = new HashSet<>();

	tmp.addAll(eaRepo.elementsAll(schemaPkg).values());

	for (EAPackage dependency : dependenciesBySchema.get(schemaPkg.getFullName())) {
	    tmp.addAll(eaRepo.elementsAll(dependency).values());
	}

	Set<EAElement> res = tmp.stream()
		.filter(elmt -> !StringUtils.containsAnyIgnoreCase(elmt.getFullName(), "informative", "example"))
		.collect(Collectors.toSet());

	return res;
    }

    private void printIdentifiedSchemas() {

	for (String schemaFullName : dependenciesBySchema.keySet()) {

	    Optional<EAPackage> eaSchemaPkgOpt = eaRepo.lookupPackage(schemaFullName);

	    if (eaSchemaPkgOpt.isPresent()) {

		EAPackage schemaPkg = eaSchemaPkgOpt.get();

		System.out.println(schemaFullName);

		System.out.println("--- Packages:");
		SortedMap<String, EAPackage> childrenByFullName = eaRepo.childrenAll(schemaPkg);
		for (String fn : childrenByFullName.keySet()) {
		    System.out.println("   - " + fn);
		}

		System.out.println("--- Elements:");
		SortedMap<Integer, EAElement> elementsById = eaRepo.elementsAll(schemaPkg);
		for (EAElement elmt : elementsById.values()) {
		    System.out.println("   - " + elmt.getFullName());
		}
	    }
	}

    }

    private boolean identifySchemas() {

	boolean allSchemasAndDependenciesFound = true;

	for (String schemaFullName : this.dependenciesBySchemaIn.keySet()) {

	    // lookup schema package
	    Optional<EAPackage> eaSchemaPkgOpt = eaRepo.lookupPackage(schemaFullName);

	    if (eaSchemaPkgOpt.isEmpty()) {
		allSchemasAndDependenciesFound = false;
		result.addError(this, 101, schemaFullName);
	    } else {

		// lookup the schema dependencies
		for (String dependencyFullName : this.dependenciesBySchemaIn.get(schemaFullName)) {

		    // lookup dependency package
		    Optional<EAPackage> eaDepPkgOpt = eaRepo.lookupPackage(dependencyFullName);
		    if (eaDepPkgOpt.isEmpty()) {
			allSchemasAndDependenciesFound = false;
			result.addError(this, 102, dependencyFullName);
		    } else {

			EAPackage dependency = eaDepPkgOpt.get();

			List<EAPackage> dependencies;
			if (dependenciesBySchema.containsKey(schemaFullName)) {
			    dependencies = dependenciesBySchema.get(schemaFullName);
			} else {
			    dependencies = new ArrayList<>();
			    dependenciesBySchema.put(schemaFullName, dependencies);
			}
			dependencies.add(dependency);
		    }
		}
	    }
	}

	return allSchemasAndDependenciesFound;
    }

    @Override
    public String message(int mnr) {

	switch (mnr) {

	case 0:
	    return "Context: class '$1$'";
	case 1:
	    return "Context: property '$1$'";
	case 2:
	    return "Context: model element '$1$'";
	case 3:
	    return "Context: attribute '$1$'";
	case 4:
	    return "Context: association role '$1$' of association '$2$'";
	case 5:
	    return "Context: package '$1$'";
	case 6:
	    return "Context external element: '$1$'";

	case 100:
	    return "Duplicate model element detected: $1$";
	case 101:
	    return "Schema was not found: $1$";
	case 102:
	    return "Dependency package was not found: $1$";
	case 103:
	    return "??Model element '$1$' has unknown meta type '$2$'.";
	case 104:
	    return "Not all configured schemas and their dependencies were found. Consult the log for further details. Processing will proceed, but results may be incorrect.";
	case 105:
	    return "Elements with equal name '$1$' found in allowed elements (of the schema and its dependencies)! Re-assignment of links to element with this name will not be deterministic!";
	case 106:
	    return "=== === === CHECKING FOR DUPLICATE ELEMENTS IN SCHEMAS AND DEPENDENCIES === === ===";
	case 107:
	    return "--- --- Performing duplicate check for schema: $1$";
	case 108:
	    return "=== === === PROCESSING SCHEMA DEPENDENCIES === === ===";
	case 109:
	    return "--- --- Processing dependencies for schema: $1$";
	case 110:
	    return "--- --- --- Now processing connectors for packages of schema: $1$";
	case 111:
	    return "--- --- --- Now processing connectors for elements in schema: $1$";
	case 112:
	    return "--- --- --- Now processing attributes of elements in schema: $1$";
	case 113:
	    return "=== === === ANALYZING PACKAGE DEPENDENCIES === === ===";
	case 114:
	    return "--- --- Performing dependency analysis for schema: $1$";
	case 115:
	    return "--- --- --- Now analyzing connectors for packages of schema: $1$";
	case 116:
	    return "--- --- --- Now analyzing connectors for elements in schema: $1$";
	case 117:
	    return "--- --- --- Now analyzing attributes of elements in schema: $1$";
	case 118:
	    return "--- --- --- Now analyzing elements in schema: $1$";
	case 119:
	    return "--- --- --- Now analyzing operations and their parameters in schema: $1$";
	case 120:
	    return "--- --- --- Now analyzing diagrams in schema: $1$";
	case 121:
	    return "Package dependency, case: $1$";
	case 122:
	    return "--- --- Elements with the following meta types will be ignored: $1$";
	case 123:
	    return "--- --- --- Now processing operations of elements in schema: $1$";
	case 124:
	    return "=== === === CHECKING ATTRIBUTE LINKAGE === === ===";
	case 125:
	    return "--- --- Processing attributes in schema: $1$";
	case 126:
	    return "=== === === REPORTING UNVISITED SCHEMA DEPENDENCIES === === ===";
	case 127:
	    return "--- --- unvisited (and thus probably to be removed) dependencies for schema: $1$";
	case 128:
	    return "--- --- --- $1$";
	// element messages: 2xx

	// attribute messages: 3xx
	case 300:
	    return "Attribute without linking (type: '$1$')";
	case 301:
	    return "Attribute classifier is external element.";
	case 302:
	    return "Could not update classifier of attribute, because type '$1$' was not found in allowed schema elements.";
	case 303:
	    return "Exception occurred while updating classifier of attribute. Exception message is: $1$";
	case 304:
	    return "Exception occurred while updating type and multiplicity of attribute. Exception message is: $1$";
	case 305:
	    return "Exception occurred while updating type of attribute. Exception message is: $1$";
	case 306:
	    return "Type mapping applies (from '$1$' to '$2$').";
	case 307:
	    return "Attribute with set/sequence type (type: '$1$').";
	// operation messages: 4xx

	// operation parameter messages: 5xx

	// diagram messages: 6xx

	// connector messages: 7xx
	case 700:
	    return "Connector to/from external model element ($1$ is external): $2$";
	case 701:
	    return "Source and target of association/aggregation to/from external model element need to be switched ($1$ is external to the schema): $2$";
	case 702:
	    return "Adding dependency connector from schema package '$1$' to package '$2$'.";
	case 703:
	    return "Exception occurred while adding dependency connector from schema package '$1$' to package '$2$'. Exception message is: $3$";

	// 800 AAA type mapping
	case 800:
	    return "Exception occurred while updating attribute classifier and type. Exception message is: $1$";
	case 801:
	    return "=== === === TRANSFORMING AAA MEASURE TYPES === === ===";
	case 802:
	    return "Switched attribute type (and classifier) to '$1$'.";

	// 900 AAA union transformation
	case 900:
	    return "=== === === TRANSFORMING AAA UNIONS === === ===";
	case 901:
	    return "--- --- --- Now transforming unions with property choice to data types";
	case 902:
	    return "--- --- --- Now transforming geometry unions";
	case 903:
	    return "--- --- --- Now applying mapping to CharacterString";
	case 904:
	    return "Multiple data types with name '$1$' found in the model, where at most one is expected. Union property choice transformation is not applied for that type.";
	case 905:
	    return "No data type with name '$1$' found in the model. Union property choice transformation cannot be applied for that type.";
	case 906:
	    return "Exception occurred while updating lower bound of attribute '$1$' in union '$2$'. Exception message is: $3$";
	case 907:
	    return "Exception occurred while adding new property choice constraint '$1$' in union '$2$'. Exception message is: $3$";
	case 908:
	    return "??Multiple unions with name '$1$' found in the model, where at most one is expected. Transformation for union with geometry type choice is not applied for that type.";
	case 909:
	    return "??No union with name '$1$' found in the model. Transformation for union with geometry type choice cannot be applied for that type.";
	case 910:
	    return "??Type '$1$' is not one of the defined unions with geometry type choices! It will be ignored. Have all such unions been configured or is the type missing?";
	case 911:
	    return "Exception occurred while adding new geometry type choice constraint for attribute '$1$' in element '$2$'. Exception message is: $3$";

	// 1000 model structur transformation
	case 1000:
	    return "=== === === TRANSFORMING MODEL STRUCTURE === === ===";
	case 1001:
	    return "Exception occurred while trying to create new package '$1$' in package '$2$'. Exception message is: $3$";
	case 1002:
	    return "Cannot relocate package '$1$, because it was not found.";
	case 1003:
	    return "Cannot relocate package '$1$, because the parent package ('$2$') of the designated target package was not found.";
	case 1004:
	    return "Exception occurred while trying to rename package '$1$' to '$2$'. Exception message is: $3$";
	case 1005:
	    return "Exception occurred while trying to move package '$1$' to new package '$2$'. Exception message is: $3$";

	default:
	    return "(" + LinkTransformer.class.getName() + ") Unknown message with number: " + mnr;
	}
    }
}
