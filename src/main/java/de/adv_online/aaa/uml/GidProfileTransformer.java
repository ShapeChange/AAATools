/**
 * GeoInfoDok Transformations (Profile Transformer)
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

package de.adv_online.aaa.uml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
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
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
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

import de.interactive_instruments.shapechange.core.MessageSource;
import de.interactive_instruments.shapechange.core.Options;
import de.interactive_instruments.shapechange.core.ShapeChangeAbortException;
import de.interactive_instruments.shapechange.core.ShapeChangeResult;
import de.interactive_instruments.shapechange.core.ShapeChangeResult.MessageContext;
import de.interactive_instruments.shapechange.core.model.Transformer;
import de.interactive_instruments.shapechange.ea.util.EAAttributeUtil;
import de.interactive_instruments.shapechange.ea.util.EAConnectorEndUtil;
import de.interactive_instruments.shapechange.ea.util.EAConnectorUtil;
import de.interactive_instruments.shapechange.ea.util.EAElementUtil;
import de.interactive_instruments.shapechange.ea.util.EAException;
import de.interactive_instruments.shapechange.ea.util.EAPackageUtil;
import de.interactive_instruments.shapechange.ea.util.EATaggedValue;

/**
 * @author Johannes Echterhoff (echterhoff at interactive-instruments dot de)
 *
 */
public class GidProfileTransformer implements Transformer, MessageSource {

//    public static final String RELEVANT_PACKAGE_NAME = "TestSchema";
    public static final String RELEVANT_PACKAGE_NAME = "GeoInfoDok";

    public static final String ORIGINAL_GEOINFODOK_PACKAGE_NAME = "GeoInfoDok";

    public static final boolean CLONE_PACKAGE = false;
    public static final String CLONE_PACKAGE_NAME = "NAS";

    public static final boolean KEEP_OLD_PROFILE = false;

    public static final boolean UPDATE_ASSOCIATION_ROLES_WITHOUT_NAME = true;

    public static final boolean PERFORM_ORIGINAL_GID_ANALYSIS = false;
    public static final boolean PERFORM_UML_PROFILE_UPDATE = true;

    public static final EnumSet<MetaType> WARN_ON_FALLBACK_ASSIGNMENT = EnumSet.noneOf(MetaType.class);

    public static final SortedSet<String> PACKAGES_TO_EXCLUDE_IN_UML_PROFILE_UPDATE = new TreeSet<>(
	    Stream.of("AAA_Signaturenkatalog", "AAA_Signaturenkatalog 1.1").collect(Collectors.toSet()));

    public static final String ID_FOR_ASSOCIATION_ROLE_WITHOUT_NAME = "{noname}";

    // for development only:
    public static final boolean PRINT_TAGGED_VALUES = false;

    private ShapeChangeResult result = null;

    private Repository rep = null;

    protected List<String> geoinfodokClassPrefixes = new ArrayList<>();

    protected SortedSet<Integer> gidPkgElementIds = new TreeSet<>();
    protected SortedSet<Integer> gidPkgIds = new TreeSet<>();
    protected SortedSet<Integer> gidElementIds = new TreeSet<>();

    protected Set<StereotypeMappingInfo> stereotypeMappingInfos = new HashSet<>();
    protected List<String> targetValuesForTagMapping = null;
    protected SortedMap<String, String> tagsWithFixedValue = new TreeMap<>();

    protected SortedMap<String, Integer> numberOfNonBlankValuesBySourceTaggedValueFQName = new TreeMap<>();

    public void initialise(Options o, ShapeChangeResult r, String repositoryFileName) throws ShapeChangeAbortException {

	WARN_ON_FALLBACK_ASSIGNMENT.add(MetaType.CLASS);

	result = r;

	/** Make sure repository file exists */
	java.io.File repfile = new java.io.File(repositoryFileName);
	boolean ex = true;
	if (!repfile.exists()) {
	    ex = false;
	    if (!repositoryFileName.toLowerCase().endsWith(".qea")) {
		repositoryFileName += ".qea";
		repfile = new java.io.File(repositoryFileName);
		ex = repfile.exists();
	    }
	}
	if (!ex) {
	    r.addFatalError(null, 31, repositoryFileName);
	    throw new ShapeChangeAbortException();
	}

	/** Connect to EA Repository */
	String absname = repfile.getAbsolutePath();
	rep = new Repository();
	if (!rep.OpenFile(absname)) {
	    String errormsg = rep.GetLastError();
	    r.addFatalError(null, 30, errormsg, repositoryFileName);
	    throw new ShapeChangeAbortException();
	}

	geoinfodokClassPrefixes = Arrays.asList("AA_", "AC_", "AD_", "AG_", "AP_", "AU_", "AX_", "BR_", "GN_", "GV_",
		"LB_", "LN_", "TA_");

	targetValuesForTagMapping = Arrays.asList("true", "false", "inline", "byReference", "inlineOrByReference");

	tagsWithFixedValue.put("byValuePropertyType", "");
	tagsWithFixedValue.put("asDictionary", "true");
	tagsWithFixedValue.put("isCollection", "");
	tagsWithFixedValue.put("noPropertyType", "");
	tagsWithFixedValue.put("reverseRoleNAS", "");
	tagsWithFixedValue.put("xsdEncodingRule", "");

//	EITHER
	// Mapping the old UML profile to the new
//	initialiseProfileMappingAaaToGid();

	// OR
	/*
	 * Fixing up the new UML profile - especially for duplicated schema packages
	 * (where EA 17.0 apparently loses track of fully qualified stereotypes for
	 * association role stereotypes and / or the tags assigned to such association
	 * roles)
	 */
	initialiseProfileMappingGidToGidFix();

	for (StereotypeMappingInfo smi : this.stereotypeMappingInfos) {
	    for (String sourceTVFQName : smi.getTvNameToSourceFQNameMap().values()) {
		this.numberOfNonBlankValuesBySourceTaggedValueFQName.put(sourceTVFQName, 0);
	    }
	}
    }

    private void initialiseProfileMappingGidToGidFix() {
	{
	    // retired

	    /*
	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
	     */
	    SortedMap<String, String> retTVNameToSourceFQNameMap = new TreeMap<>();
	    SortedMap<String, String> retTVSourceToTargetFQNameMap = new TreeMap<>();

	    retTVNameToSourceFQNameMap.put("GID:GueltigBis", "GID::GID_Retired::GID:GueltigBis");

	    retTVSourceToTargetFQNameMap.put("GID::GID_Retired::GID:GueltigBis", "GID::GID_Retired::GID:GueltigBis");

	    StereotypeMappingInfo smi = new StereotypeMappingInfo("GID::GID_Retired", "GID::GID_Retired",
		    retTVNameToSourceFQNameMap, retTVSourceToTargetFQNameMap, MetaType.ANY, false);
	    this.stereotypeMappingInfos.add(smi);
	}

	{
	    // applicationSchema

	    /*
	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
	     */
	    SortedMap<String, String> pkgTVNameToSourceFQNameMap = new TreeMap<>();
	    SortedMap<String, String> pkgTVSourceToTargetFQNameMap = new TreeMap<>();

	    pkgTVNameToSourceFQNameMap.put("GID:Modellart", "GID::GID_ElementMitModellart::GID:Modellart");
	    pkgTVNameToSourceFQNameMap.put("GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");

	    pkgTVNameToSourceFQNameMap.put("version", "GID::ApplicationSchema::version");

	    pkgTVNameToSourceFQNameMap.put("GID:AAAVersion", "GID::GID_ApplicationSchema::GID:AAAVersion");
	    pkgTVNameToSourceFQNameMap.put("GID:Organisation", "GID::GID_ApplicationSchema::GID:Organisation");
	    pkgTVNameToSourceFQNameMap.put("GID:Datum", "GID::GID_ApplicationSchema::GID:Datum");
	    pkgTVNameToSourceFQNameMap.put("gmlProfileSchema", "GID::GID_ApplicationSchema::gmlProfileSchema");
	    pkgTVNameToSourceFQNameMap.put("targetNamespace", "GID::GID_ApplicationSchema::targetNamespace");
	    pkgTVNameToSourceFQNameMap.put("xmlns", "GID::GID_ApplicationSchema::xmlns");
	    pkgTVNameToSourceFQNameMap.put("xsdDocument", "GID::GID_ApplicationSchema::xsdDocument");
	    pkgTVNameToSourceFQNameMap.put("xsdEncodingRule", "GID::GID_ApplicationSchema::xsdEncodingRule");

	    pkgTVNameToSourceFQNameMap.put("definition", "GID::GI_Element::definition");
	    pkgTVNameToSourceFQNameMap.put("description", "GID::GI_Element::description");
	    pkgTVNameToSourceFQNameMap.put("designation", "GID::GI_Element::designation");

	    // -------------

	    pkgTVSourceToTargetFQNameMap.put("GID::GI_Element::definition", "GID::GI_Element::definition");
	    pkgTVSourceToTargetFQNameMap.put("GID::GI_Element::description", "GID::GI_Element::description");
	    pkgTVSourceToTargetFQNameMap.put("GID::GI_Element::designation", "GID::GI_Element::designation");

	    pkgTVSourceToTargetFQNameMap.put("GID::GID_ElementMitModellart::GID:Modellart",
		    "GID::GID_ElementMitModellart::GID:Modellart");
	    pkgTVSourceToTargetFQNameMap.put("GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");

	    pkgTVSourceToTargetFQNameMap.put("GID::ApplicationSchema::version", "GID::ApplicationSchema::version");

	    pkgTVSourceToTargetFQNameMap.put("GID::GID_ApplicationSchema::GID:AAAVersion",
		    "GID::GID_ApplicationSchema::GID:AAAVersion");
	    pkgTVSourceToTargetFQNameMap.put("GID::GID_ApplicationSchema::GID:Organisation",
		    "GID::GID_ApplicationSchema::GID:Organisation");
	    pkgTVSourceToTargetFQNameMap.put("GID::GID_ApplicationSchema::GID:Datum",
		    "GID::GID_ApplicationSchema::GID:Datum");
	    pkgTVSourceToTargetFQNameMap.put("GID::GID_ApplicationSchema::gmlProfileSchema",
		    "GID::GID_ApplicationSchema::gmlProfileSchema");
	    pkgTVSourceToTargetFQNameMap.put("GID::GID_ApplicationSchema::targetNamespace",
		    "GID::GID_ApplicationSchema::targetNamespace");
	    pkgTVSourceToTargetFQNameMap.put("GID::GID_ApplicationSchema::xmlns", "GID::GID_ApplicationSchema::xmlns");
	    pkgTVSourceToTargetFQNameMap.put("GID::GID_ApplicationSchema::xsdDocument",
		    "GID::GID_ApplicationSchema::xsdDocument");
	    pkgTVSourceToTargetFQNameMap.put("GID::GID_ApplicationSchema::xsdEncodingRule",
		    "GID::GID_ApplicationSchema::xsdEncodingRule");

	    StereotypeMappingInfo smiAppSchema = new StereotypeMappingInfo("GID::GID_ApplicationSchema",
		    "GID::GID_ApplicationSchema", pkgTVNameToSourceFQNameMap, pkgTVSourceToTargetFQNameMap,
		    MetaType.PACKAGE, false);
	    this.stereotypeMappingInfos.add(smiAppSchema);
	}

	{
	    // package (without stereotype)

	    /*
	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
	     */
	    SortedMap<String, String> pkgTVNameToSourceFQNameMap = new TreeMap<>();
	    SortedMap<String, String> pkgTVSourceToTargetFQNameMap = new TreeMap<>();

	    pkgTVNameToSourceFQNameMap.put("GID:Kennung", "GID::GID_ElementMitKennung::GID:Kennung");
	    pkgTVNameToSourceFQNameMap.put("GID:Modellart", "GID::GID_ElementMitModellart::GID:Modellart");
	    pkgTVNameToSourceFQNameMap.put("GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");

	    pkgTVNameToSourceFQNameMap.put("xsdDocument", "GID::GID_Package::xsdDocument");
	    pkgTVNameToSourceFQNameMap.put("xsdEncodingRule", "GID::GID_Package::xsdEncodingRule");

	    pkgTVNameToSourceFQNameMap.put("definition", "GID::GI_Element::definition");
	    pkgTVNameToSourceFQNameMap.put("description", "GID::GI_Element::description");
	    pkgTVNameToSourceFQNameMap.put("designation", "GID::GI_Element::designation");

	    // -------------

	    pkgTVSourceToTargetFQNameMap.put("GID::GI_Element::definition", "GID::GI_Element::definition");
	    pkgTVSourceToTargetFQNameMap.put("GID::GI_Element::description", "GID::GI_Element::description");
	    pkgTVSourceToTargetFQNameMap.put("GID::GI_Element::designation", "GID::GI_Element::designation");

	    pkgTVSourceToTargetFQNameMap.put("GID::GID_ElementMitKennung::GID:Kennung",
		    "GID::GID_ElementMitKennung::GID:Kennung");
	    pkgTVSourceToTargetFQNameMap.put("GID::GID_ElementMitModellart::GID:Modellart",
		    "GID::GID_ElementMitModellart::GID:Modellart");
	    pkgTVSourceToTargetFQNameMap.put("GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");

	    pkgTVSourceToTargetFQNameMap.put("GID::GID_Package::xsdDocument", "GID::GID_Package::xsdDocument");
	    pkgTVSourceToTargetFQNameMap.put("GID::GID_Package::xsdEncodingRule", "GID::GID_Package::xsdEncodingRule");

	    StereotypeMappingInfo smiPackage = new StereotypeMappingInfo("GID::GID_Package", "GID::GID_Package",
		    pkgTVNameToSourceFQNameMap, pkgTVSourceToTargetFQNameMap, MetaType.PACKAGE, true);
	    this.stereotypeMappingInfos.add(smiPackage);
	}

	{
	    // type

	    /*
	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
	     */
	    SortedMap<String, String> tTVNameToSourceFQNameMap = new TreeMap<>();
	    SortedMap<String, String> tTVSourceToTargetFQNameMap = new TreeMap<>();

	    tTVNameToSourceFQNameMap.put("definition", "GID::GI_Element::definition");
	    tTVNameToSourceFQNameMap.put("description", "GID::GI_Element::description");
	    tTVNameToSourceFQNameMap.put("designation", "GID::GI_Element::designation");

	    tTVNameToSourceFQNameMap.put("GID:Grunddatenbestand",
		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
	    tTVNameToSourceFQNameMap.put("GID:Kennung", "GID::GID_ElementMitKennung::GID:Kennung");
	    tTVNameToSourceFQNameMap.put("GID:Modellart", "GID::GID_ElementMitModellart::GID:Modellart");
	    tTVNameToSourceFQNameMap.put("AAA:Profile", "GID::AAA_ProfilElement::AAA:Profile");
	    tTVNameToSourceFQNameMap.put("GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
	    tTVNameToSourceFQNameMap.put("byValuePropertyType", "GID::GID_Mixin::byValuePropertyType");
	    tTVNameToSourceFQNameMap.put("isCollection", "GID::GID_Mixin::isCollection");
	    tTVNameToSourceFQNameMap.put("noPropertyType", "GID::GID_Mixin::noPropertyType");
	    tTVNameToSourceFQNameMap.put("xmlSchemaType", "GID::GID_Mixin::xmlSchemaType");
	    tTVNameToSourceFQNameMap.put("xsdEncodingRule", "GID::GID_Mixin::xsdEncodingRule");

	    // -------------

	    tTVSourceToTargetFQNameMap.put("GID::GI_Element::definition", "GID::GI_Element::definition");
	    tTVSourceToTargetFQNameMap.put("GID::GI_Element::description", "GID::GI_Element::description");
	    tTVSourceToTargetFQNameMap.put("GID::GI_Element::designation", "GID::GI_Element::designation");

	    tTVSourceToTargetFQNameMap.put("GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand",
		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
	    tTVSourceToTargetFQNameMap.put("GID::GID_ElementMitKennung::GID:Kennung",
		    "GID::GID_ElementMitKennung::GID:Kennung");
	    tTVSourceToTargetFQNameMap.put("GID::GID_ElementMitModellart::GID:Modellart",
		    "GID::GID_ElementMitModellart::GID:Modellart");
	    tTVSourceToTargetFQNameMap.put("GID::AAA_ProfilElement::AAA:Profile",
		    "GID::AAA_ProfilElement::AAA:Profile");
	    tTVSourceToTargetFQNameMap.put("GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
	    tTVSourceToTargetFQNameMap.put("GID::GID_Mixin::byValuePropertyType",
		    "GID::GID_Mixin::byValuePropertyType");
	    tTVSourceToTargetFQNameMap.put("GID::GID_Mixin::isCollection", "GID::GID_Mixin::isCollection");
	    tTVSourceToTargetFQNameMap.put("GID::GID_Mixin::noPropertyType", "GID::GID_Mixin::noPropertyType");
	    tTVSourceToTargetFQNameMap.put("GID::GID_Mixin::xmlSchemaType", "GID::GID_Mixin::xmlSchemaType");
	    tTVSourceToTargetFQNameMap.put("GID::GID_Mixin::xsdEncodingRule", "GID::GID_Mixin::xsdEncodingRule");

	    StereotypeMappingInfo smi = new StereotypeMappingInfo("GID::GID_Mixin", "GID::GID_Mixin",
		    tTVNameToSourceFQNameMap, tTVSourceToTargetFQNameMap, MetaType.CLASS, false);
	    this.stereotypeMappingInfos.add(smi);
	}

	{
	    // featureType

	    /*
	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
	     */
	    SortedMap<String, String> ftTVNameToSourceFQNameMap = new TreeMap<>();
	    SortedMap<String, String> ftTVSourceToTargetFQNameMap = new TreeMap<>();

	    ftTVNameToSourceFQNameMap.put("GID:Grunddatenbestand",
		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
	    ftTVNameToSourceFQNameMap.put("GID:Kennung", "GID::GID_ElementMitKennung::GID:Kennung");
	    ftTVNameToSourceFQNameMap.put("GID:Modellart", "GID::GID_ElementMitModellart::GID:Modellart");
	    ftTVNameToSourceFQNameMap.put("AAA:Nutzungsart", "GID::GID_FeatureType::AAA:Nutzungsart");
	    ftTVNameToSourceFQNameMap.put("AAA:Nutzungsartkennung",
		    "GID::AAA_NutzungsartkennungElement::AAA:Nutzungsartkennung");
	    ftTVNameToSourceFQNameMap.put("AAA:Profile", "GID::AAA_ProfilElement::AAA:Profile");
	    ftTVNameToSourceFQNameMap.put("GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
	    ftTVNameToSourceFQNameMap.put("byValuePropertyType", "GID::GID_FeatureType::byValuePropertyType");
	    ftTVNameToSourceFQNameMap.put("isCollection", "GID::GID_FeatureType::isCollection");
	    ftTVNameToSourceFQNameMap.put("noPropertyType", "GID::GID_FeatureType::noPropertyType");
	    ftTVNameToSourceFQNameMap.put("xsdEncodingRule", "GID::GID_FeatureType::xsdEncodingRule");

	    ftTVNameToSourceFQNameMap.put("definition", "GID::GI_Element::definition");
	    ftTVNameToSourceFQNameMap.put("description", "GID::GI_Element::description");
	    ftTVNameToSourceFQNameMap.put("designation", "GID::GI_Element::designation");

	    // -------------

	    ftTVSourceToTargetFQNameMap.put("GID::GI_Element::definition", "GID::GI_Element::definition");
	    ftTVSourceToTargetFQNameMap.put("GID::GI_Element::description", "GID::GI_Element::description");
	    ftTVSourceToTargetFQNameMap.put("GID::GI_Element::designation", "GID::GI_Element::designation");

	    ftTVSourceToTargetFQNameMap.put("GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand",
		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
	    ftTVSourceToTargetFQNameMap.put("GID::GID_ElementMitKennung::GID:Kennung",
		    "GID::GID_ElementMitKennung::GID:Kennung");
	    ftTVSourceToTargetFQNameMap.put("GID::GID_ElementMitModellart::GID:Modellart",
		    "GID::GID_ElementMitModellart::GID:Modellart");
	    ftTVSourceToTargetFQNameMap.put("GID::GID_FeatureType::AAA:Nutzungsart",
		    "GID::GID_FeatureType::AAA:Nutzungsart");
	    ftTVSourceToTargetFQNameMap.put("GID::AAA_NutzungsartkennungElement::AAA:Nutzungsartkennung",
		    "GID::AAA_NutzungsartkennungElement::AAA:Nutzungsartkennung");
	    ftTVSourceToTargetFQNameMap.put("GID::AAA_ProfilElement::AAA:Profile",
		    "GID::AAA_ProfilElement::AAA:Profile");
	    ftTVSourceToTargetFQNameMap.put("GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
	    ftTVSourceToTargetFQNameMap.put("GID::GID_FeatureType::byValuePropertyType",
		    "GID::GID_FeatureType::byValuePropertyType");
	    ftTVSourceToTargetFQNameMap.put("GID::GID_FeatureType::isCollection", "GID::GID_FeatureType::isCollection");
	    ftTVSourceToTargetFQNameMap.put("GID::GID_FeatureType::noPropertyType",
		    "GID::GID_FeatureType::noPropertyType");
	    ftTVSourceToTargetFQNameMap.put("GID::GID_FeatureType::xsdEncodingRule",
		    "GID::GID_FeatureType::xsdEncodingRule");

	    StereotypeMappingInfo smi = new StereotypeMappingInfo("GID::GID_FeatureType", "GID::GID_FeatureType",
		    ftTVNameToSourceFQNameMap, ftTVSourceToTargetFQNameMap, MetaType.CLASS, false);
	    this.stereotypeMappingInfos.add(smi);
	}

	{
	    // dataType

	    /*
	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
	     */
	    SortedMap<String, String> dtTVNameToSourceFQNameMap = new TreeMap<>();
	    SortedMap<String, String> dtTVSourceToTargetFQNameMap = new TreeMap<>();

	    dtTVNameToSourceFQNameMap.put("definition", "GID::GI_Element::definition");
	    dtTVNameToSourceFQNameMap.put("description", "GID::GI_Element::description");
	    dtTVNameToSourceFQNameMap.put("designation", "GID::GI_Element::designation");

	    dtTVNameToSourceFQNameMap.put("GID:Grunddatenbestand",
		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
	    dtTVNameToSourceFQNameMap.put("GID:Kennung", "GID::GID_ElementMitKennung::GID:Kennung");
	    dtTVNameToSourceFQNameMap.put("GID:Modellart", "GID::GID_ElementMitModellart::GID:Modellart");
	    dtTVNameToSourceFQNameMap.put("AAA:Profile", "GID::AAA_ProfilElement::AAA:Profile");
	    dtTVNameToSourceFQNameMap.put("GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
	    dtTVNameToSourceFQNameMap.put("isCollection", "GID::GID_DataType::isCollection");
	    dtTVNameToSourceFQNameMap.put("noPropertyType", "GID::GID_DataType::noPropertyType");
	    dtTVNameToSourceFQNameMap.put("xsdEncodingRule", "GID::GID_DataType::xsdEncodingRule");

	    // -------------

	    dtTVSourceToTargetFQNameMap.put("GID::GI_Element::definition", "GID::GI_Element::definition");
	    dtTVSourceToTargetFQNameMap.put("GID::GI_Element::description", "GID::GI_Element::description");
	    dtTVSourceToTargetFQNameMap.put("GID::GI_Element::designation", "GID::GI_Element::designation");

	    dtTVSourceToTargetFQNameMap.put("GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand",
		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
	    dtTVSourceToTargetFQNameMap.put("GID::GID_ElementMitKennung::GID:Kennung",
		    "GID::GID_ElementMitKennung::GID:Kennung");
	    dtTVSourceToTargetFQNameMap.put("GID::GID_ElementMitModellart::GID:Modellart",
		    "GID::GID_ElementMitModellart::GID:Modellart");
	    dtTVSourceToTargetFQNameMap.put("GID::AAA_ProfilElement::AAA:Profile",
		    "GID::AAA_ProfilElement::AAA:Profile");
	    dtTVSourceToTargetFQNameMap.put("GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
	    dtTVSourceToTargetFQNameMap.put("GID::GID_DataType::isCollection", "GID::GID_DataType::isCollection");
	    dtTVSourceToTargetFQNameMap.put("GID::GID_DataType::noPropertyType", "GID::GID_DataType::noPropertyType");
	    dtTVSourceToTargetFQNameMap.put("GID::GID_DataType::xsdEncodingRule", "GID::GID_DataType::xsdEncodingRule");

	    StereotypeMappingInfo smi = new StereotypeMappingInfo("GID::GID_DataType", "GID::GID_DataType",
		    dtTVNameToSourceFQNameMap, dtTVSourceToTargetFQNameMap, MetaType.DATATYPE, true);
	    this.stereotypeMappingInfos.add(smi);
	}

	{
	    // code list

	    /*
	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
	     */
	    SortedMap<String, String> clTVNameToSourceFQNameMap = new TreeMap<>();
	    SortedMap<String, String> clTVSourceToTargetFQNameMap = new TreeMap<>();

	    clTVNameToSourceFQNameMap.put("definition", "GID::GI_Element::definition");
	    clTVNameToSourceFQNameMap.put("description", "GID::GI_Element::description");
	    clTVNameToSourceFQNameMap.put("designation", "GID::GI_Element::designation");

	    clTVNameToSourceFQNameMap.put("GID:Modellart", "GID::GID_ElementMitModellart::GID:Modellart");
	    clTVNameToSourceFQNameMap.put("AAA:Profile", "GID::AAA_ProfilElement::AAA:Profile");
	    clTVNameToSourceFQNameMap.put("asDictionary", "GID::GID_CodeSet::asDictionary");
	    clTVNameToSourceFQNameMap.put("codeList", "GID::GID_CodeSet::codeList");
	    clTVNameToSourceFQNameMap.put("xsdEncodingRule", "GID::GID_CodeSet::xsdEncodingRule");

	    // -------------

	    clTVSourceToTargetFQNameMap.put("GID::GI_Element::definition", "GID::GI_Element::definition");
	    clTVSourceToTargetFQNameMap.put("GID::GI_Element::description", "GID::GI_Element::description");
	    clTVSourceToTargetFQNameMap.put("GID::GI_Element::designation", "GID::GI_Element::designation");

	    clTVSourceToTargetFQNameMap.put("GID::GID_ElementMitModellart::GID:Modellart",
		    "GID::GID_ElementMitModellart::GID:Modellart");
	    clTVSourceToTargetFQNameMap.put("GID::AAA_ProfilElement::AAA:Profile",
		    "GID::AAA_ProfilElement::AAA:Profile");
	    clTVSourceToTargetFQNameMap.put("GID::GID_CodeSet::asDictionary", "GID::GID_CodeSet::asDictionary");
	    clTVSourceToTargetFQNameMap.put("GID::GID_CodeSet::codeList", "GID::GID_CodeSet::codeList");
	    clTVSourceToTargetFQNameMap.put("GID::GID_CodeSet::xsdEncodingRule", "GID::GID_CodeSet::xsdEncodingRule");

	    StereotypeMappingInfo smi = new StereotypeMappingInfo("GID::GID_CodeSet", "GID::GID_CodeSet",
		    clTVNameToSourceFQNameMap, clTVSourceToTargetFQNameMap, MetaType.DATATYPE, false);
	    this.stereotypeMappingInfos.add(smi);
	}

	{
	    // enumeration

	    /*
	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
	     */
	    SortedMap<String, String> eTVNameToSourceFQNameMap = new TreeMap<>();
	    SortedMap<String, String> eTVSourceToTargetFQNameMap = new TreeMap<>();

	    eTVNameToSourceFQNameMap.put("definition", "GID::GI_Element::definition");
	    eTVNameToSourceFQNameMap.put("description", "GID::GI_Element::description");
	    eTVNameToSourceFQNameMap.put("designation", "GID::GI_Element::designation");

	    eTVNameToSourceFQNameMap.put("GID:Grunddatenbestand",
		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
	    eTVNameToSourceFQNameMap.put("GID:Modellart", "GID::GID_ElementMitModellart::GID:Modellart");
	    eTVNameToSourceFQNameMap.put("AAA:Profile", "GID::AAA_ProfilElement::AAA:Profile");
	    eTVNameToSourceFQNameMap.put("GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
	    eTVNameToSourceFQNameMap.put("xsdEncodingRule", "GID::GID_Enumeration::xsdEncodingRule");

	    // -------------

	    eTVSourceToTargetFQNameMap.put("GID::GI_Element::definition", "GID::GI_Element::definition");
	    eTVSourceToTargetFQNameMap.put("GID::GI_Element::description", "GID::GI_Element::description");
	    eTVSourceToTargetFQNameMap.put("GID::GI_Element::designation", "GID::GI_Element::designation");

	    eTVSourceToTargetFQNameMap.put("GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand",
		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
	    eTVSourceToTargetFQNameMap.put("GID::GID_ElementMitModellart::GID:Modellart",
		    "GID::GID_ElementMitModellart::GID:Modellart");
	    eTVSourceToTargetFQNameMap.put("GID::AAA_ProfilElement::AAA:Profile",
		    "GID::AAA_ProfilElement::AAA:Profile");
	    eTVSourceToTargetFQNameMap.put("GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
	    eTVSourceToTargetFQNameMap.put("GID::GID_Enumeration::xsdEncodingRule",
		    "GID::GID_Enumeration::xsdEncodingRule");

	    StereotypeMappingInfo smi = new StereotypeMappingInfo("GID::GID_Enumeration", "GID::GID_Enumeration",
		    eTVNameToSourceFQNameMap, eTVSourceToTargetFQNameMap, MetaType.ENUMERATION, true);
	    this.stereotypeMappingInfos.add(smi);
	}

	{
	    // property (for attributes and association roles)

	    /*
	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
	     */
	    SortedMap<String, String> propTVNameToSourceFQNameMap = new TreeMap<>();
	    SortedMap<String, String> propTVSourceToTargetFQNameMap = new TreeMap<>();

	    propTVNameToSourceFQNameMap.put("GID:Kennung", "GID::GID_ElementMitKennung::GID:Kennung");
	    propTVNameToSourceFQNameMap.put("GID:Modellart", "GID::GID_ElementMitModellart::GID:Modellart");
	    propTVNameToSourceFQNameMap.put("GID:Grunddatenbestand",
		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
	    propTVNameToSourceFQNameMap.put("GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
	    propTVNameToSourceFQNameMap.put("AAA:Profile", "GID::AAA_ProfilElement::AAA:Profile");

	    propTVNameToSourceFQNameMap.put("AAA:Landnutzung", "GID::AAA_LandnutzungElement::AAA:Landnutzung");

	    propTVNameToSourceFQNameMap.put("GID:UnitOfMeasure", "GID::GID_Property::GID:UnitOfMeasure");
	    propTVNameToSourceFQNameMap.put("inlineOrByReference", "GID::GID_Property::inlineOrByReference");
	    propTVNameToSourceFQNameMap.put("GID:objektbildend", "GID::GID_Property::GID:objektbildend");
	    propTVNameToSourceFQNameMap.put("reverseRoleNAS", "GID::GID_Property::reverseRoleNAS");
	    propTVNameToSourceFQNameMap.put("sequenceNumber", "GID::GID_Property::sequenceNumber");

	    propTVNameToSourceFQNameMap.put("definition", "GID::GI_Element::definition");
	    propTVNameToSourceFQNameMap.put("description", "GID::GI_Element::description");
	    propTVNameToSourceFQNameMap.put("designation", "GID::GI_Element::designation");

	    // -------------

	    propTVSourceToTargetFQNameMap.put("GID::GI_Element::definition", "GID::GI_Element::definition");
	    propTVSourceToTargetFQNameMap.put("GID::GI_Element::description", "GID::GI_Element::description");
	    propTVSourceToTargetFQNameMap.put("GID::GI_Element::designation", "GID::GI_Element::designation");

	    propTVSourceToTargetFQNameMap.put("GID::GID_ElementMitKennung::GID:Kennung",
		    "GID::GID_ElementMitKennung::GID:Kennung");
	    propTVSourceToTargetFQNameMap.put("GID::GID_ElementMitModellart::GID:Modellart",
		    "GID::GID_ElementMitModellart::GID:Modellart");
	    propTVSourceToTargetFQNameMap.put("GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand",
		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
	    propTVSourceToTargetFQNameMap.put("GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
	    propTVSourceToTargetFQNameMap.put("GID::AAA_ProfilElement::AAA:Profile",
		    "GID::AAA_ProfilElement::AAA:Profile");

	    propTVSourceToTargetFQNameMap.put("GID::AAA_LandnutzungElement::AAA:Landnutzung",
		    "GID::AAA_LandnutzungElement::AAA:Landnutzung");

	    propTVSourceToTargetFQNameMap.put("GID::GID_Property::GID:UnitOfMeasure",
		    "GID::GID_Property::GID:UnitOfMeasure");
	    propTVSourceToTargetFQNameMap.put("GID::GID_Property::inlineOrByReference",
		    "GID::GID_Property::inlineOrByReference");
	    propTVSourceToTargetFQNameMap.put("GID::GID_Property::GID:objektbildend",
		    "GID::GID_Property::GID:objektbildend");
	    propTVSourceToTargetFQNameMap.put("GID::GID_Property::reverseRoleNAS", "GID::GID_Property::reverseRoleNAS");
	    propTVSourceToTargetFQNameMap.put("GID::GID_Property::sequenceNumber", "GID::GID_Property::sequenceNumber");

	    StereotypeMappingInfo smi = new StereotypeMappingInfo("GID::GID_Property", "GID::GID_Property",
		    propTVNameToSourceFQNameMap, propTVSourceToTargetFQNameMap, MetaType.PROPERTY, true);
	    this.stereotypeMappingInfos.add(smi);
	}

	{
	    // enumeration literal

	    /*
	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
	     */
	    SortedMap<String, String> elTVNameToSourceFQNameMap = new TreeMap<>();
	    SortedMap<String, String> elTVSourceToTargetFQNameMap = new TreeMap<>();

	    elTVNameToSourceFQNameMap.put("definition", "GID::GI_Element::definition");
	    elTVNameToSourceFQNameMap.put("description", "GID::GI_Element::description");
	    elTVNameToSourceFQNameMap.put("designation", "GID::GI_Element::designation");

	    elTVNameToSourceFQNameMap.put("GID:Modellart", "GID::GID_ElementMitModellart::GID:Modellart");
	    elTVNameToSourceFQNameMap.put("GID:Grunddatenbestand",
		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
	    elTVNameToSourceFQNameMap.put("GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
	    elTVNameToSourceFQNameMap.put("AAA:Nutzungsartkennung",
		    "GID::AAA_NutzungsartkennungElement::AAA:Nutzungsartkennung");
	    elTVNameToSourceFQNameMap.put("AAA:Profile", "GID::AAA_ProfilElement::AAA:Profile");
	    elTVNameToSourceFQNameMap.put("AAA:Landnutzung", "GID::AAA_LandnutzungElement::AAA:Landnutzung");

	    // -------------

	    elTVSourceToTargetFQNameMap.put("GID::GI_Element::definition", "GID::GI_Element::definition");
	    elTVSourceToTargetFQNameMap.put("GID::GI_Element::description", "GID::GI_Element::description");
	    elTVSourceToTargetFQNameMap.put("GID::GI_Element::designation", "GID::GI_Element::designation");

	    elTVSourceToTargetFQNameMap.put("GID::GID_ElementMitModellart::GID:Modellart",
		    "GID::GID_ElementMitModellart::GID:Modellart");
	    elTVSourceToTargetFQNameMap.put("GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand",
		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
	    elTVSourceToTargetFQNameMap.put("GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer",
		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
	    elTVSourceToTargetFQNameMap.put("GID::AAA_NutzungsartkennungElement::AAA:Nutzungsartkennung",
		    "GID::AAA_NutzungsartkennungElement::AAA:Nutzungsartkennung");
	    elTVSourceToTargetFQNameMap.put("GID::AAA_ProfilElement::AAA:Profile",
		    "GID::AAA_ProfilElement::AAA:Profile");
	    elTVSourceToTargetFQNameMap.put("GID::AAA_LandnutzungElement::AAA:Landnutzung",
		    "GID::AAA_LandnutzungElement::AAA:Landnutzung");

	    StereotypeMappingInfo smi = new StereotypeMappingInfo("GID::GID_EnumerationLiteral",
		    "GID::GID_EnumerationLiteral", elTVNameToSourceFQNameMap, elTVSourceToTargetFQNameMap,
		    MetaType.ENUMERATIONLITERAL, true);
	    this.stereotypeMappingInfos.add(smi);
	}
    }

    private void initialiseProfileMappingAaaToGid() {
//	{
//	    // retired
//
//	    /*
//	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
//	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
//	     */
//	    SortedMap<String, String> retTVNameToSourceFQNameMap = new TreeMap<>();
//	    SortedMap<String, String> retTVSourceToTargetFQNameMap = new TreeMap<>();
//
//	    retTVNameToSourceFQNameMap.put("AAA:GueltigBis", "AAA::retired::AAA:GueltigBis");
//
//	    retTVSourceToTargetFQNameMap.put("AAA::retired::AAA:GueltigBis", "GID::GID_Retired::GID:GueltigBis");
//
//	    StereotypeMappingInfo smi = new StereotypeMappingInfo("GID::GID_Retired", "GID::GID_Retired",
//		    retTVNameToSourceFQNameMap, retTVSourceToTargetFQNameMap, MetaType.ANY, false);
//	    this.stereotypeMappingInfos.add(smi);
//	}
//
//	{
//	    // applicationSchema
//
//	    /*
//	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
//	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
//	     */
//	    SortedMap<String, String> pkgTVNameToSourceFQNameMap = new TreeMap<>();
//	    SortedMap<String, String> pkgTVSourceToTargetFQNameMap = new TreeMap<>();
//
//	    pkgTVNameToSourceFQNameMap.put("AAA:Kennung", "AAA::applicationSchema::AAA:Kennung");
//	    pkgTVNameToSourceFQNameMap.put("AAA:Modellart", "AAA::applicationSchema::AAA:Modellart");
//	    pkgTVNameToSourceFQNameMap.put("AAA:Revisionsnummer", "AAA::applicationSchema::AAA:Revisionsnummer");
//
//	    pkgTVNameToSourceFQNameMap.put("version", "AAA::applicationSchema::version");
//
//	    pkgTVNameToSourceFQNameMap.put("AAA:AAAVersion", "AAA::applicationSchema::AAA:AAAVersion");
//	    pkgTVNameToSourceFQNameMap.put("AAA:Organisation", "AAA::applicationSchema::AAA:Organisation");
//	    pkgTVNameToSourceFQNameMap.put("AAA:Datum", "AAA::applicationSchema::AAA:Datum");
//	    pkgTVNameToSourceFQNameMap.put("gmlProfileSchema", "AAA::applicationSchema::gmlProfileSchema");
//	    pkgTVNameToSourceFQNameMap.put("targetNamespace", "AAA::applicationSchema::targetNamespace");
//	    pkgTVNameToSourceFQNameMap.put("xmlns", "AAA::applicationSchema::xmlns");
//	    pkgTVNameToSourceFQNameMap.put("xsdDocument", "AAA::applicationSchema::xsdDocument");
//	    pkgTVNameToSourceFQNameMap.put("xsdEncodingRule", "AAA::applicationSchema::xsdEncodingRule");
//
//	    // -------------
//
//	    pkgTVSourceToTargetFQNameMap.put("AAA::applicationSchema::AAA:Modellart",
//		    "GID::GID_ElementMitModellart::GID:Modellart");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::applicationSchema::AAA:Revisionsnummer",
//		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
//
//	    pkgTVSourceToTargetFQNameMap.put("AAA::applicationSchema::version", "GID::ApplicationSchema::version");
//
//	    pkgTVSourceToTargetFQNameMap.put("AAA::applicationSchema::AAA:AAAVersion",
//		    "GID::GID_ApplicationSchema::GID:AAAVersion");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::applicationSchema::AAA:Organisation",
//		    "GID::GID_ApplicationSchema::GID:Organisation");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::applicationSchema::AAA:Datum",
//		    "GID::GID_ApplicationSchema::GID:Datum");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::applicationSchema::gmlProfileSchema",
//		    "GID::GID_ApplicationSchema::gmlProfileSchema");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::applicationSchema::targetNamespace",
//		    "GID::GID_ApplicationSchema::targetNamespace");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::applicationSchema::xmlns", "GID::GID_ApplicationSchema::xmlns");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::applicationSchema::xsdDocument",
//		    "GID::GID_ApplicationSchema::xsdDocument");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::applicationSchema::xsdEncodingRule",
//		    "GID::GID_ApplicationSchema::xsdEncodingRule");
//	        
//	    StereotypeMappingInfo smiAppSchema = new StereotypeMappingInfo("AAA::applicationSchema",
//		    "GID::GID_ApplicationSchema", pkgTVNameToSourceFQNameMap, pkgTVSourceToTargetFQNameMap,
//		    MetaType.PACKAGE, false);
//	    this.stereotypeMappingInfos.add(smiAppSchema);
//	}
//
//	{
//	    // schema
//
//	    /*
//	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
//	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
//	     */
//	    SortedMap<String, String> pkgTVNameToSourceFQNameMap = new TreeMap<>();
//	    SortedMap<String, String> pkgTVSourceToTargetFQNameMap = new TreeMap<>();
//
//	    pkgTVNameToSourceFQNameMap.put("AAA:Kennung", "AAA::schema::AAA:Kennung");
//	    pkgTVNameToSourceFQNameMap.put("AAA:Modellart", "AAA::schema::AAA:Modellart");
//	    pkgTVNameToSourceFQNameMap.put("AAA:Revisionsnummer", "AAA::schema::AAA:Revisionsnummer");
//
//	    pkgTVNameToSourceFQNameMap.put("version", "AAA::schema::version");
//
//	    pkgTVNameToSourceFQNameMap.put("AAA:AAAVersion", "AAA::schema::AAA:AAAVersion");
//	    pkgTVNameToSourceFQNameMap.put("AAA:Organisation", "AAA::schema::AAA:Organisation");
//	    pkgTVNameToSourceFQNameMap.put("AAA:Datum", "AAA::schema::AAA:Datum");
//	    pkgTVNameToSourceFQNameMap.put("gmlProfileSchema", "AAA::schema::gmlProfileSchema");
//	    pkgTVNameToSourceFQNameMap.put("targetNamespace", "AAA::schema::targetNamespace");
//	    pkgTVNameToSourceFQNameMap.put("xmlns", "AAA::schema::xmlns");
//	    pkgTVNameToSourceFQNameMap.put("xsdDocument", "AAA::schema::xsdDocument");
//	    pkgTVNameToSourceFQNameMap.put("xsdEncodingRule", "AAA::schema::xsdEncodingRule");
//
//	    // -------------
//
//	    pkgTVSourceToTargetFQNameMap.put("AAA::schema::AAA:Modellart",
//		    "GID::GID_ElementMitModellart::GID:Modellart");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::schema::AAA:Revisionsnummer",
//		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
//
//	    pkgTVSourceToTargetFQNameMap.put("AAA::schema::version", "GID::ApplicationSchema::version");
//
//	    pkgTVSourceToTargetFQNameMap.put("AAA::schema::AAA:AAAVersion",
//		    "GID::GID_ApplicationSchema::GID:AAAVersion");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::schema::AAA:Organisation",
//		    "GID::GID_ApplicationSchema::GID:Organisation");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::schema::AAA:Datum", "GID::GID_ApplicationSchema::GID:Datum");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::schema::gmlProfileSchema",
//		    "GID::GID_ApplicationSchema::gmlProfileSchema");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::schema::targetNamespace",
//		    "GID::GID_ApplicationSchema::targetNamespace");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::schema::xmlns", "GID::GID_ApplicationSchema::xmlns");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::schema::xsdDocument", "GID::GID_ApplicationSchema::xsdDocument");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::schema::xsdEncodingRule",
//		    "GID::GID_ApplicationSchema::xsdEncodingRule");
//
//	    StereotypeMappingInfo smiSchema = new StereotypeMappingInfo("AAA::schema", "GID::GID_ApplicationSchema",
//		    pkgTVNameToSourceFQNameMap, pkgTVSourceToTargetFQNameMap, MetaType.PACKAGE, false);
//	    this.stereotypeMappingInfos.add(smiSchema);
//	}
//
//	{
//	    // package (without stereotype)
//
//	    /*
//	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
//	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
//	     */
//	    SortedMap<String, String> pkgTVNameToSourceFQNameMap = new TreeMap<>();
//	    SortedMap<String, String> pkgTVSourceToTargetFQNameMap = new TreeMap<>();
//
//	    pkgTVNameToSourceFQNameMap.put("AAA:Kennung", "AAA::package::AAA:Kennung");
//	    pkgTVNameToSourceFQNameMap.put("AAA:Modellart", "AAA::package::AAA:Modellart");
//	    pkgTVNameToSourceFQNameMap.put("AAA:Revisionsnummer", "AAA::package::AAA:Revisionsnummer");
//
//	    pkgTVNameToSourceFQNameMap.put("xsdDocument", "AAA::package::xsdDocument");
//	    pkgTVNameToSourceFQNameMap.put("xsdEncodingRule", "AAA::package::xsdEncodingRule");
//
//	    // -------------
//
//	    pkgTVSourceToTargetFQNameMap.put("AAA::package::AAA:Kennung", "GID::GID_ElementMitKennung::GID:Kennung");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::package::AAA:Modellart",
//		    "GID::GID_ElementMitModellart::GID:Modellart");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::package::AAA:Revisionsnummer",
//		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
//
//	    pkgTVSourceToTargetFQNameMap.put("AAA::package::xsdDocument", "GID::GID_Package::xsdDocument");
//	    pkgTVSourceToTargetFQNameMap.put("AAA::package::xsdEncodingRule", "GID::GID_Package::xsdEncodingRule");
//
//	    StereotypeMappingInfo smiPackage = new StereotypeMappingInfo("AAA::package", "GID::GID_Package",
//		    pkgTVNameToSourceFQNameMap, pkgTVSourceToTargetFQNameMap, MetaType.PACKAGE, true);
//	    this.stereotypeMappingInfos.add(smiPackage);
//	}
//
//	{
//	    // type
//
//	    /*
//	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
//	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
//	     */
//	    SortedMap<String, String> tTVNameToSourceFQNameMap = new TreeMap<>();
//	    SortedMap<String, String> tTVSourceToTargetFQNameMap = new TreeMap<>();
//
//	    tTVNameToSourceFQNameMap.put("AAA:Grunddatenbestand", "AAA::type::AAA:Grunddatenbestand");
//	    tTVNameToSourceFQNameMap.put("AAA:Kennung", "AAA::type::AAA:Kennung");
//	    tTVNameToSourceFQNameMap.put("AAA:LetzteAenderung", "AAA::type::AAA:LetzteAenderung");
//	    tTVNameToSourceFQNameMap.put("AAA:Modellart", "AAA::type::AAA:Modellart");
//	    tTVNameToSourceFQNameMap.put("AAA:Nutzungsart", "AAA::type::AAA:Nutzungsart");
//	    tTVNameToSourceFQNameMap.put("AAA:Nutzungsartkennung", "AAA::type::AAA:Nutzungsartkennung");
//	    tTVNameToSourceFQNameMap.put("AAA:Profile", "AAA::type::AAA:Profile");
//	    tTVNameToSourceFQNameMap.put("AAA:Revisionsnummer", "AAA::type::AAA:Revisionsnummer");
//	    tTVNameToSourceFQNameMap.put("AAA:Themen", "AAA::type::AAA:Themen");
//	    tTVNameToSourceFQNameMap.put("byValuePropertyType", "AAA::type::byValuePropertyType");
//	    tTVNameToSourceFQNameMap.put("isCollection", "AAA::type::isCollection");
//	    tTVNameToSourceFQNameMap.put("noPropertyType", "AAA::type::noPropertyType");
//	    tTVNameToSourceFQNameMap.put("xmlSchemaType", "AAA::type::xmlSchemaType");
//	    tTVNameToSourceFQNameMap.put("xsdEncodingRule", "AAA::type::xsdEncodingRule");
//
//	    // -------------
//
//	    tTVSourceToTargetFQNameMap.put("AAA::type::AAA:Grunddatenbestand",
//		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
//	    tTVSourceToTargetFQNameMap.put("AAA::type::AAA:Kennung", "GID::GID_ElementMitKennung::GID:Kennung");
//	    tTVSourceToTargetFQNameMap.put("AAA::type::AAA:Modellart", "GID::GID_ElementMitModellart::GID:Modellart");
//	    tTVSourceToTargetFQNameMap.put("AAA::type::AAA:Profile", "GID::AAA_ProfilElement::AAA:Profile");
//	    tTVSourceToTargetFQNameMap.put("AAA::type::AAA:Revisionsnummer",
//		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
//	    tTVSourceToTargetFQNameMap.put("AAA::type::byValuePropertyType", "GID::GID_Mixin::byValuePropertyType");
//	    tTVSourceToTargetFQNameMap.put("AAA::type::isCollection", "GID::GID_Mixin::isCollection");
//	    tTVSourceToTargetFQNameMap.put("AAA::type::noPropertyType", "GID::GID_Mixin::noPropertyType");
//	    tTVSourceToTargetFQNameMap.put("AAA::type::xmlSchemaType", "GID::GID_Mixin::xmlSchemaType");
//	    tTVSourceToTargetFQNameMap.put("AAA::type::xsdEncodingRule", "GID::GID_Mixin::xsdEncodingRule");
//
//	    StereotypeMappingInfo smi = new StereotypeMappingInfo("AAA::type", "GID::GID_Mixin",
//		    tTVNameToSourceFQNameMap, tTVSourceToTargetFQNameMap, MetaType.CLASS, false);
//	    this.stereotypeMappingInfos.add(smi);
//	}
//
//	{
//	    // featureType
//
//	    /*
//	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
//	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
//	     */
//	    SortedMap<String, String> ftTVNameToSourceFQNameMap = new TreeMap<>();
//	    SortedMap<String, String> ftTVSourceToTargetFQNameMap = new TreeMap<>();
//
//	    ftTVNameToSourceFQNameMap.put("AAA:Grunddatenbestand", "AAA::featureType::AAA:Grunddatenbestand");
//	    ftTVNameToSourceFQNameMap.put("AAA:Kennung", "AAA::featureType::AAA:Kennung");
//	    ftTVNameToSourceFQNameMap.put("AAA:LetzteAenderung", "AAA::featureType::AAA:LetzteAenderung");
//	    ftTVNameToSourceFQNameMap.put("AAA:Modellart", "AAA::featureType::AAA:Modellart");
//	    ftTVNameToSourceFQNameMap.put("AAA:Nutzungsart", "AAA::featureType::AAA:Nutzungsart");
//	    ftTVNameToSourceFQNameMap.put("AAA:Nutzungsartkennung", "AAA::featureType::AAA:Nutzungsartkennung");
//	    ftTVNameToSourceFQNameMap.put("AAA:Profile", "AAA::featureType::AAA:Profile");
//	    ftTVNameToSourceFQNameMap.put("AAA:Revisionsnummer", "AAA::featureType::AAA:Revisionsnummer");
//	    ftTVNameToSourceFQNameMap.put("AAA:Themen", "AAA::type::AAA:Themen");
//	    ftTVNameToSourceFQNameMap.put("byValuePropertyType", "AAA::featureType::byValuePropertyType");
//	    ftTVNameToSourceFQNameMap.put("isCollection", "AAA::featureType::isCollection");
//	    ftTVNameToSourceFQNameMap.put("noPropertyType", "AAA::featureType::noPropertyType");
//	    ftTVNameToSourceFQNameMap.put("xsdEncodingRule", "AAA::featureType::xsdEncodingRule");
//
//	    // -------------
//
//	    ftTVSourceToTargetFQNameMap.put("AAA::featureType::AAA:Grunddatenbestand",
//		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
//	    ftTVSourceToTargetFQNameMap.put("AAA::featureType::AAA:Kennung", "GID::GID_ElementMitKennung::GID:Kennung");
//	    ftTVSourceToTargetFQNameMap.put("AAA::featureType::AAA:Modellart",
//		    "GID::GID_ElementMitModellart::GID:Modellart");
//	    ftTVSourceToTargetFQNameMap.put("AAA::featureType::AAA:Nutzungsart",
//		    "GID::GID_FeatureType::AAA:Nutzungsart");
//	    ftTVSourceToTargetFQNameMap.put("AAA::featureType::AAA:Nutzungsartkennung",
//		    "GID::AAA_NutzungsartkennungElement::AAA:Nutzungsartkennung");
//	    ftTVSourceToTargetFQNameMap.put("AAA::featureType::AAA:Profile", "GID::AAA_ProfilElement::AAA:Profile");
//	    ftTVSourceToTargetFQNameMap.put("AAA::featureType::AAA:Revisionsnummer",
//		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
//	    ftTVSourceToTargetFQNameMap.put("AAA::featureType::byValuePropertyType",
//		    "GID::GID_FeatureType::byValuePropertyType");
//	    ftTVSourceToTargetFQNameMap.put("AAA::featureType::isCollection", "GID::GID_FeatureType::isCollection");
//	    ftTVSourceToTargetFQNameMap.put("AAA::featureType::noPropertyType", "GID::GID_FeatureType::noPropertyType");
//	    ftTVSourceToTargetFQNameMap.put("AAA::featureType::xsdEncodingRule",
//		    "GID::GID_FeatureType::xsdEncodingRule");
//
//	    StereotypeMappingInfo smi = new StereotypeMappingInfo("AAA::featureType", "GID::GID_FeatureType",
//		    ftTVNameToSourceFQNameMap, ftTVSourceToTargetFQNameMap, MetaType.CLASS, false);
//	    this.stereotypeMappingInfos.add(smi);
//	}
//
//	{
//	    // dataType
//
//	    /*
//	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
//	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
//	     */
//	    SortedMap<String, String> dtTVNameToSourceFQNameMap = new TreeMap<>();
//	    SortedMap<String, String> dtTVSourceToTargetFQNameMap = new TreeMap<>();
//
//	    dtTVNameToSourceFQNameMap.put("AAA:Grunddatenbestand", "AAA::dataType::AAA:Grunddatenbestand");
//	    dtTVNameToSourceFQNameMap.put("AAA:Kennung", "AAA::dataType::AAA:Kennung");
//	    dtTVNameToSourceFQNameMap.put("AAA:LetzteAenderung", "AAA::dataType::AAA:LetzteAenderung");
//	    dtTVNameToSourceFQNameMap.put("AAA:Modellart", "AAA::dataType::AAA:Modellart");
//	    dtTVNameToSourceFQNameMap.put("AAA:Nutzungsart", "AAA::dataType::AAA:Nutzungsart");
//	    dtTVNameToSourceFQNameMap.put("AAA:Nutzungsartkennung", "AAA::dataType::AAA:Nutzungsartkennung");
//	    dtTVNameToSourceFQNameMap.put("AAA:Profile", "AAA::dataType::AAA:Profile");
//	    dtTVNameToSourceFQNameMap.put("AAA:Revisionsnummer", "AAA::dataType::AAA:Revisionsnummer");
//	    dtTVNameToSourceFQNameMap.put("AAA:Themen", "AAA::type::AAA:Themen");
//	    dtTVNameToSourceFQNameMap.put("isCollection", "AAA::dataType::isCollection");
//	    dtTVNameToSourceFQNameMap.put("noPropertyType", "AAA::dataType::noPropertyType");
//	    dtTVNameToSourceFQNameMap.put("xsdEncodingRule", "AAA::dataType::xsdEncodingRule");
//
//	    // -------------
//
//	    dtTVSourceToTargetFQNameMap.put("AAA::dataType::AAA:Grunddatenbestand",
//		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
//	    dtTVSourceToTargetFQNameMap.put("AAA::dataType::AAA:Kennung", "GID::GID_ElementMitKennung::GID:Kennung");
//	    dtTVSourceToTargetFQNameMap.put("AAA::dataType::AAA:Modellart",
//		    "GID::GID_ElementMitModellart::GID:Modellart");
//	    dtTVSourceToTargetFQNameMap.put("AAA::dataType::AAA:Profile", "GID::AAA_ProfilElement::AAA:Profile");
//	    dtTVSourceToTargetFQNameMap.put("AAA::dataType::AAA:Revisionsnummer",
//		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
//	    dtTVSourceToTargetFQNameMap.put("AAA::dataType::isCollection", "GID::GID_DataType::isCollection");
//	    dtTVSourceToTargetFQNameMap.put("AAA::dataType::noPropertyType", "GID::GID_DataType::noPropertyType");
//	    dtTVSourceToTargetFQNameMap.put("AAA::dataType::xsdEncodingRule", "GID::GID_DataType::xsdEncodingRule");
//
//	    StereotypeMappingInfo smi = new StereotypeMappingInfo("AAA::dataType", "GID::GID_DataType",
//		    dtTVNameToSourceFQNameMap, dtTVSourceToTargetFQNameMap, MetaType.DATATYPE, true);
//	    this.stereotypeMappingInfos.add(smi);
//	}
//
//	{
//	    // union (wird umgewandelt)
//
//	    /*
//	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
//	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
//	     */
//	    SortedMap<String, String> unionTVNameToSourceFQNameMap = new TreeMap<>();
//	    SortedMap<String, String> unionTVSourceToTargetFQNameMap = new TreeMap<>();
//
//	    unionTVNameToSourceFQNameMap.put("AAA:Grunddatenbestand", "AAA::union::AAA:Grunddatenbestand");
//	    unionTVNameToSourceFQNameMap.put("AAA:Kennung", "AAA::union::AAA:Kennung");
//	    unionTVNameToSourceFQNameMap.put("AAA:LetzteAenderung", "AAA::union::AAA:LetzteAenderung");
//	    unionTVNameToSourceFQNameMap.put("AAA:Modellart", "AAA::union::AAA:Modellart");
//	    unionTVNameToSourceFQNameMap.put("AAA:Nutzungsart", "AAA::union::AAA:Nutzungsart");
//	    unionTVNameToSourceFQNameMap.put("AAA:Nutzungsartkennung", "AAA::union::AAA:Nutzungsartkennung");
//	    unionTVNameToSourceFQNameMap.put("AAA:Profile", "AAA::union::AAA:Profile");
//	    unionTVNameToSourceFQNameMap.put("AAA:Revisionsnummer", "AAA::union::AAA:Revisionsnummer");
//	    unionTVNameToSourceFQNameMap.put("AAA:Themen", "AAA::type::AAA:Themen");
//	    unionTVNameToSourceFQNameMap.put("isCollection", "AAA::union::isCollection");
//	    unionTVNameToSourceFQNameMap.put("noPropertyType", "AAA::union::noPropertyType");
//	    unionTVNameToSourceFQNameMap.put("xsdEncodingRule", "AAA::union::xsdEncodingRule");
//
//	    // -------------
//
//	    unionTVSourceToTargetFQNameMap.put("AAA::union::AAA:Grunddatenbestand",
//		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
//	    unionTVSourceToTargetFQNameMap.put("AAA::union::AAA:Kennung", "GID::GID_ElementMitKennung::GID:Kennung");
//	    unionTVSourceToTargetFQNameMap.put("AAA::union::AAA:Modellart",
//		    "GID::GID_ElementMitModellart::GID:Modellart");
//	    unionTVSourceToTargetFQNameMap.put("AAA::union::AAA:Profile", "GID::AAA_ProfilElement::AAA:Profile");
//	    unionTVSourceToTargetFQNameMap.put("AAA::union::AAA:Revisionsnummer",
//		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
//	    unionTVSourceToTargetFQNameMap.put("AAA::union::isCollection", "GID::GID_DataType::isCollection");
//	    unionTVSourceToTargetFQNameMap.put("AAA::union::noPropertyType", "GID::GID_DataType::noPropertyType");
//	    unionTVSourceToTargetFQNameMap.put("AAA::union::xsdEncodingRule", "GID::GID_DataType::xsdEncodingRule");
//
//	    StereotypeMappingInfo smi = new StereotypeMappingInfo("AAA::union", "GID::GID_DataType",
//		    unionTVNameToSourceFQNameMap, unionTVSourceToTargetFQNameMap, MetaType.DATATYPE, false);
//	    this.stereotypeMappingInfos.add(smi);
//	}
//
//	{
//	    // code list
//
//	    /*
//	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
//	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
//	     */
//	    SortedMap<String, String> clTVNameToSourceFQNameMap = new TreeMap<>();
//	    SortedMap<String, String> clTVSourceToTargetFQNameMap = new TreeMap<>();
//
//	    clTVNameToSourceFQNameMap.put("AAA:Grunddatenbestand", "AAA::codeList::AAA:Grunddatenbestand");
//	    clTVNameToSourceFQNameMap.put("AAA:Kennung", "AAA::codeList::AAA:Kennung");
//	    clTVNameToSourceFQNameMap.put("AAA:LetzteAenderung", "AAA::codeList::AAA:LetzteAenderung");
//	    clTVNameToSourceFQNameMap.put("AAA:Modellart", "AAA::codeList::AAA:Modellart");
//	    clTVNameToSourceFQNameMap.put("AAA:Nutzungsart", "AAA::codeList::AAA:Nutzungsart");
//	    clTVNameToSourceFQNameMap.put("AAA:Nutzungsartkennung", "AAA::codeList::AAA:Nutzungsartkennung");
//	    clTVNameToSourceFQNameMap.put("AAA:Profile", "AAA::codeList::AAA:Profile");
//	    clTVNameToSourceFQNameMap.put("AAA:Revisionsnummer", "AAA::codeList::AAA:Revisionsnummer");
//	    clTVNameToSourceFQNameMap.put("AAA:Themen", "AAA::type::AAA:Themen");
//	    clTVNameToSourceFQNameMap.put("asDictionary", "AAA::codeList::asDictionary");
//	    clTVNameToSourceFQNameMap.put("codeList", "AAA::codeList::codeList");
//	    clTVNameToSourceFQNameMap.put("xsdEncodingRule", "AAA::codeList::xsdEncodingRule");
//
//	    // -------------
//
//	    clTVSourceToTargetFQNameMap.put("AAA::codeList::AAA:Modellart",
//		    "GID::GID_ElementMitModellart::GID:Modellart");
//	    clTVSourceToTargetFQNameMap.put("AAA::codeList::AAA:Profile", "GID::AAA_ProfilElement::AAA:Profile");
//	    clTVSourceToTargetFQNameMap.put("AAA::codeList::asDictionary", "GID::GID_CodeSet::asDictionary");
//	    clTVSourceToTargetFQNameMap.put("AAA::codeList::codeList", "GID::GID_CodeSet::codeList");
//	    clTVSourceToTargetFQNameMap.put("AAA::codeList::xsdEncodingRule", "GID::GID_CodeSet::xsdEncodingRule");
//
//	    StereotypeMappingInfo smi = new StereotypeMappingInfo("AAA::codeList", "GID::GID_CodeSet",
//		    clTVNameToSourceFQNameMap, clTVSourceToTargetFQNameMap, MetaType.DATATYPE, false);
//	    this.stereotypeMappingInfos.add(smi);
//	}
//
//	{
//	    // enumeration
//
//	    /*
//	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
//	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
//	     */
//	    SortedMap<String, String> eTVNameToSourceFQNameMap = new TreeMap<>();
//	    SortedMap<String, String> eTVSourceToTargetFQNameMap = new TreeMap<>();
//
//	    eTVNameToSourceFQNameMap.put("AAA:Grunddatenbestand", "AAA::enumeration::AAA:Grunddatenbestand");
//	    eTVNameToSourceFQNameMap.put("AAA:Kennung", "AAA::enumeration::AAA:Kennung");
//	    eTVNameToSourceFQNameMap.put("AAA:LetzteAenderung", "AAA::enumeration::AAA:LetzteAenderung");
//	    eTVNameToSourceFQNameMap.put("AAA:Modellart", "AAA::enumeration::AAA:Modellart");
//	    eTVNameToSourceFQNameMap.put("AAA:Nutzungsart", "AAA::enumeration::AAA:Nutzungsart");
//	    eTVNameToSourceFQNameMap.put("AAA:Nutzungsartkennung", "AAA::enumeration::AAA:Nutzungsartkennung");
//	    eTVNameToSourceFQNameMap.put("AAA:Profile", "AAA::enumeration::AAA:Profile");
//	    eTVNameToSourceFQNameMap.put("AAA:Revisionsnummer", "AAA::enumeration::AAA:Revisionsnummer");
//	    eTVNameToSourceFQNameMap.put("AAA:Themen", "AAA::type::AAA:Themen");
//	    eTVNameToSourceFQNameMap.put("xsdEncodingRule", "AAA::enumeration::xsdEncodingRule");
//
//	    // -------------
//
//	    eTVSourceToTargetFQNameMap.put("AAA::enumeration::AAA:Grunddatenbestand",
//		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
//	    eTVSourceToTargetFQNameMap.put("AAA::enumeration::AAA:Modellart",
//		    "GID::GID_ElementMitModellart::GID:Modellart");
//	    eTVSourceToTargetFQNameMap.put("AAA::enumeration::AAA:Profile", "GID::AAA_ProfilElement::AAA:Profile");
//	    eTVSourceToTargetFQNameMap.put("AAA::enumeration::AAA:Revisionsnummer",
//		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
//	    eTVSourceToTargetFQNameMap.put("AAA::enumeration::xsdEncodingRule",
//		    "GID::GID_Enumeration::xsdEncodingRule");
//
//	    StereotypeMappingInfo smi = new StereotypeMappingInfo("AAA::enumeration", "GID::GID_Enumeration",
//		    eTVNameToSourceFQNameMap, eTVSourceToTargetFQNameMap, MetaType.ENUMERATION, true);
//	    this.stereotypeMappingInfos.add(smi);
//	}
//
//	{
//	    // property (for attributes and association roles)
//
//	    /*
//	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
//	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
//	     */
//	    SortedMap<String, String> propTVNameToSourceFQNameMap = new TreeMap<>();
//	    SortedMap<String, String> propTVSourceToTargetFQNameMap = new TreeMap<>();
//
//	    propTVNameToSourceFQNameMap.put("AAA:Kennung", "AAA::property::AAA:Kennung");
//	    propTVNameToSourceFQNameMap.put("AAA:Modellart", "AAA::property::AAA:Modellart");
//	    propTVNameToSourceFQNameMap.put("AAA:Grunddatenbestand", "AAA::property::AAA:Grunddatenbestand");
//	    propTVNameToSourceFQNameMap.put("AAA:LetzteAenderung", "AAA::property::AAA:LetzteAenderung");
//	    propTVNameToSourceFQNameMap.put("AAA:Revisionsnummer", "AAA::property::AAA:Revisionsnummer");
//	    propTVNameToSourceFQNameMap.put("AAA:Profile", "AAA::property::AAA:Profile");
//	    propTVNameToSourceFQNameMap.put("AAA:UnitOfMeasure", "AAA::property::AAA:UnitOfMeasure");
//
//	    propTVNameToSourceFQNameMap.put("AAA:Landnutzung", "AAA::property::AAA:Landnutzung");
//	    propTVNameToSourceFQNameMap.put("allowedTypesNAS", "AAA::property::allowedTypesNAS");
//
//	    propTVNameToSourceFQNameMap.put("AAA:Nutzungsart", "AAA::property::AAA:Nutzungsart");
//	    propTVNameToSourceFQNameMap.put("AAA:Nutzungsartkennung", "AAA::property::AAA:Nutzungsartkennung");
//
//	    propTVNameToSourceFQNameMap.put("inlineOrByReference", "AAA::property::inlineOrByReference");
//	    propTVNameToSourceFQNameMap.put("AAA:objektbildend", "AAA::property::AAA:objektbildend");
//	    propTVNameToSourceFQNameMap.put("reverseRoleNAS", "AAA::property::reverseRoleNAS");
//	    propTVNameToSourceFQNameMap.put("sequenceNumber", "AAA::property::sequenceNumber");
//
//	    // -------------
//
//	    propTVSourceToTargetFQNameMap.put("AAA::property::AAA:Kennung", "GID::GID_ElementMitKennung::GID:Kennung");
//	    propTVSourceToTargetFQNameMap.put("AAA::property::AAA:Modellart",
//		    "GID::GID_ElementMitModellart::GID:Modellart");
//	    propTVSourceToTargetFQNameMap.put("AAA::property::AAA:Grunddatenbestand",
//		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
//	    propTVSourceToTargetFQNameMap.put("AAA::property::AAA:Revisionsnummer",
//		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
//	    propTVSourceToTargetFQNameMap.put("AAA::property::AAA:Profile", "GID::AAA_ProfilElement::AAA:Profile");
//
//	    propTVSourceToTargetFQNameMap.put("AAA::property::AAA:Landnutzung",
//		    "GID::AAA_LandnutzungElement::AAA:Landnutzung");
//
//	    propTVSourceToTargetFQNameMap.put("AAA::property::AAA:UnitOfMeasure",
//		    "GID::GID_Property::GID:UnitOfMeasure");
//	    propTVSourceToTargetFQNameMap.put("AAA::property::inlineOrByReference",
//		    "GID::GID_Property::inlineOrByReference");
//	    propTVSourceToTargetFQNameMap.put("AAA::property::AAA:objektbildend",
//		    "GID::GID_Property::GID:objektbildend");
//	    propTVSourceToTargetFQNameMap.put("AAA::property::reverseRoleNAS", "GID::GID_Property::reverseRoleNAS");
//	    propTVSourceToTargetFQNameMap.put("AAA::property::sequenceNumber", "GID::GID_Property::sequenceNumber");
//
//	    StereotypeMappingInfo smi = new StereotypeMappingInfo("AAA::property", "GID::GID_Property",
//		    propTVNameToSourceFQNameMap, propTVSourceToTargetFQNameMap, MetaType.PROPERTY, true);
//	    this.stereotypeMappingInfos.add(smi);
//	}
//
//	{
//	    // enumeration literal
//
//	    /*
//	     * Die xxxTVNameToSourceFQNameMap sollte alle im Source-UML-Profil enthaltenen
//	     * Tags umfassen - selbst die, die im neuen Profil entfallen.
//	     */
//	    SortedMap<String, String> elTVNameToSourceFQNameMap = new TreeMap<>();
//	    SortedMap<String, String> elTVSourceToTargetFQNameMap = new TreeMap<>();
//
//	    elTVNameToSourceFQNameMap.put("AAA:Kennung", "AAA::enum::AAA:Kennung");
//	    elTVNameToSourceFQNameMap.put("AAA:Modellart", "AAA::enum::AAA:Modellart");
//	    elTVNameToSourceFQNameMap.put("AAA:Grunddatenbestand", "AAA::enum::AAA:Grunddatenbestand");
//	    elTVNameToSourceFQNameMap.put("AAA:LetzteAenderung", "AAA::enum::AAA:LetzteAenderung");
//	    elTVNameToSourceFQNameMap.put("AAA:Revisionsnummer", "AAA::enum::AAA:Revisionsnummer");
//	    elTVNameToSourceFQNameMap.put("AAA:Nutzungsart", "AAA::enum::AAA:Nutzungsart");
//	    elTVNameToSourceFQNameMap.put("AAA:Nutzungsartkennung", "AAA::enum::AAA:Nutzungsartkennung");
//	    elTVNameToSourceFQNameMap.put("AAA:Profile", "AAA::enum::AAA:Profile");
//	    elTVNameToSourceFQNameMap.put("AAA:Landnutzung", "AAA::enum::AAA:Landnutzung");
//
//	    // -------------
//
//	    elTVSourceToTargetFQNameMap.put("AAA::enum::AAA:Modellart", "GID::GID_ElementMitModellart::GID:Modellart");
//	    elTVSourceToTargetFQNameMap.put("AAA::enum::AAA:Grunddatenbestand",
//		    "GID::GID_ElementMitGrunddatenbestand::GID:Grunddatenbestand");
//	    elTVSourceToTargetFQNameMap.put("AAA::enum::AAA:Revisionsnummer",
//		    "GID::GID_ElementMitRevisionsnummer::GID:Revisionsnummer");
//	    elTVSourceToTargetFQNameMap.put("AAA::enum::AAA:Nutzungsartkennung",
//		    "GID::AAA_NutzungsartkennungElement::AAA:Nutzungsartkennung");
//	    elTVSourceToTargetFQNameMap.put("AAA::enum::AAA:Profile", "GID::AAA_ProfilElement::AAA:Profile");
//	    elTVSourceToTargetFQNameMap.put("AAA::enum::AAA:Landnutzung",
//		    "GID::AAA_LandnutzungElement::AAA:Landnutzung");
//
//	    StereotypeMappingInfo smi = new StereotypeMappingInfo("AAA::enum", "GID::GID_EnumerationLiteral",
//		    elTVNameToSourceFQNameMap, elTVSourceToTargetFQNameMap, MetaType.ENUMERATIONLITERAL, true);
//	    this.stereotypeMappingInfos.add(smi);
//	}
    }

    public void shutdown() {
	rep.CloseFile();
	rep.Exit();
	rep = null;
    }

    public void transform() throws ShapeChangeAbortException {

	if (PERFORM_ORIGINAL_GID_ANALYSIS) {

	    // perform analysis of original GeoInfoDok package

	    result.addProcessFlowInfo(this, 1000);

	    Package originalGidPkg = null;

	    for (Package p : rep.GetModels()) {

		Package px = EAPackageUtil.findPackage(p, ORIGINAL_GEOINFODOK_PACKAGE_NAME);

		if (px != null) {
		    originalGidPkg = px;
		    break;
		}
	    }

	    if (originalGidPkg == null) {
		result.addError("Could not find package " + ORIGINAL_GEOINFODOK_PACKAGE_NAME + " in the model!");
	    } else {
		analyzeOriginalGeoInfoDok(originalGidPkg);
	    }
	}

	if (PERFORM_UML_PROFILE_UPDATE) {

	    // transform the relevant package (process UML profile)

	    result.addProcessFlowInfo(this, 1001);

	    Package relevantPackage = null;

	    for (Package p : rep.GetModels()) {

		Package px = EAPackageUtil.findPackage(p, RELEVANT_PACKAGE_NAME);

		if (px != null) {
		    relevantPackage = px;
		    break;
		}
	    }

	    if (relevantPackage == null) {
		result.addError("Could not find package " + RELEVANT_PACKAGE_NAME + " in the model!");
	    } else {
		try {
		    transformPackage(relevantPackage);
		} catch (ShapeChangeAbortException e) {
		    e.printStackTrace();
		} catch (EAException e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    private void analyzeOriginalGeoInfoDok(Package originalGidPkg) {

	// gather IDs of GeoInfoDok elements
	collectGeoInfoDokIDs(originalGidPkg);

	// actual analysis
	checkGeoInfoDokContainment(originalGidPkg);
    }

    private void collectGeoInfoDokIDs(Package pkg) {

	gidPkgIds.add(pkg.GetParentID());
	gidPkgElementIds.add(pkg.GetElement().GetElementID());

	Collection<Element> c = pkg.GetElements();
	c.Refresh();
	for (Element elmt : c) {
	    gidElementIds.add(elmt.GetElementID());
	}

	// drill down into child packages
	Collection<Package> childPackages = pkg.GetPackages();
	childPackages.Refresh();
	for (Package cp : childPackages) {
	    collectGeoInfoDokIDs(cp);
	}
    }

    private void transformPackage(Package p) throws ShapeChangeAbortException, EAException {

	if (CLONE_PACKAGE) {

	    Package relPackage = p.Clone();
	    p = relPackage;

	    if (p == null) {
		result.addFatalError("Fehler beim Klonen des Pakets. Ggf. ist es noch mit dem SVN verbunden.");
		throw new ShapeChangeAbortException();
	    } else {
		p.SetName(CLONE_PACKAGE_NAME);
		if (!p.Update())
		    result.addError("Fehler beim Klonen des Pakets: " + p.GetLastError());
	    }
	}

	if (p != null) {
	    // add GID stereotypes to cloned package
	    updateProfile(p);

	    // synchronize every target stereotype defined in stereotype mapping infos
	    for (StereotypeMappingInfo smi : this.stereotypeMappingInfos) {
		String tsFQName = smi.getTargetStereotypeFQName();
		String[] parts = tsFQName.split("::");
		if (parts.length != 2) {
		    result.addError(this, 109, tsFQName);
		} else {
		    String profile = parts[0];
		    String stereotype = parts[1];
		    /* boolean synchSuccess = */rep.SynchProfile(profile, stereotype);
		    /*
		     * 20240808 JE: In Tests, the return value of SynchProfile was always false.
		     * Maybe because no tagged values were added by the operation? There is no
		     * documentation for the returned boolean value in the EA Automation
		     * documentation. However, the modified target tagged values have apparently
		     * been affected by SynchProfile, because TVs with boolean or enumeration value
		     * were shown with dropdown boxes in EA (after opening the transformed model).
		     */
//		    if(!synchSuccess) {
//			result.addError(this,110,tsFQName,rep.GetLastError());
//		    }
		}
	    }

	    logProfileUpdateStatistics();
	}
    }

    private void logProfileUpdateStatistics() {

	result.addInfo(this, 1002);

	SortedMap<String, Integer> totalNumberOfNonBlankValuesByTVName = new TreeMap<>();
	SortedMap<String, SortedMap<String, Integer>> numberOfNonBlankValuesBySourceTaggedValueFQNameByTVName = new TreeMap<>();

	for (Entry<String, Integer> e : numberOfNonBlankValuesBySourceTaggedValueFQName.entrySet()) {

	    String sourceTVFQName = e.getKey();
	    String tvName = sourceTVFQName.contains("::") ? StringUtils.substringAfterLast(sourceTVFQName, "::")
		    : sourceTVFQName;
	    Integer numberOfNonBlankValues = e.getValue();

	    // logging per stereotype
	    result.addInfo(this, 1003, sourceTVFQName, "" + e.getValue());

	    // update total
	    int total = 0;
	    if (totalNumberOfNonBlankValuesByTVName.containsKey(tvName)) {
		total = totalNumberOfNonBlankValuesByTVName.get(tvName);
	    }
	    total = total + numberOfNonBlankValues;
	    totalNumberOfNonBlankValuesByTVName.put(tvName, total);

	    SortedMap<String, Integer> map2;
	    if (numberOfNonBlankValuesBySourceTaggedValueFQNameByTVName.containsKey(tvName)) {
		map2 = numberOfNonBlankValuesBySourceTaggedValueFQNameByTVName.get(tvName);
	    } else {
		map2 = new TreeMap<>();
		numberOfNonBlankValuesBySourceTaggedValueFQNameByTVName.put(tvName, map2);
	    }
	    map2.put(sourceTVFQName, numberOfNonBlankValues);
	}

	result.addInfo("--------------------------");
	for (String tvName : totalNumberOfNonBlankValuesByTVName.keySet()) {
	    result.addInfo(this, 1004, tvName, "" + totalNumberOfNonBlankValuesByTVName.get(tvName));
	}
	result.addInfo("--------------------------");
	for (String tvName : totalNumberOfNonBlankValuesByTVName.keySet()) {
	    result.addInfo(this, 1004, tvName, "" + totalNumberOfNonBlankValuesByTVName.get(tvName));
	    if (numberOfNonBlankValuesBySourceTaggedValueFQNameByTVName.containsKey(tvName)) {
		SortedMap<String, Integer> map = numberOfNonBlankValuesBySourceTaggedValueFQNameByTVName.get(tvName);
		for (Entry<String, Integer> e : map.entrySet()) {
		    result.addInfo(this, 1003, e.getKey(), "" + e.getValue());
		}
	    }
	}
    }

    protected void updateProfile(Package p) throws EAException {

	Element pkgElmt = p.GetElement();

	String pkgName = pkgElmt.GetName();

//	if (!StringUtils.containsAnyIgnoreCase(pkgName, "NAS", "AFIS-ALKIS-ATKIS Anwendungsschema", "AAA Basisschema",
//		"AAA_Basisklassen")) {
//	    return;
//	}

	if (PACKAGES_TO_EXCLUDE_IN_UML_PROFILE_UPDATE.contains(pkgName)) {
	    result.addInfo(this, 120, pkgName);
	} else {

	    result.addInfo(this, 123, pkgName);

//	printElementTypeInfo(pkgElmt);

	    List<StereotypeMappingInfo> smis = identifyMappingInfos(pkgElmt);

	    if (smis.isEmpty()) {
		result.addInfo(this, 101, pkgElmt.GetName());
	    } else {

		/*
		 * get values for all relevant source TVs, to later on set them in the target
		 * TVs
		 */
		SortedMap<String, List<String>> sourceTVs = getSourceTVs(EAElementUtil.getEATaggedValues(pkgElmt),
			smis);

		// update the stereotype(s)
		updateStereotypes(pkgElmt, smis);

		// copy source tagged values (i.e., from old profile TVs) to target tagged
		// values (i.e., to new profile TVs)
		setTargetTVs(pkgElmt, smis, sourceTVs);

		/*
		 * remove old stereotype; in case of old tagged values without stereotype,
		 * remove those as well
		 */
		removeOldProfile(pkgElmt, smis);
	    }

	    // handle other elements contained in the package

	    Collection<Element> c = p.GetElements();
	    c.Refresh();
	    for (Element e : c) {
		updateProfile(e);
	    }

	    // drill down into child packages

	    Collection<Package> childPackages = p.GetPackages();
	    childPackages.Refresh();
	    for (Package cp : childPackages) {
		updateProfile(cp);
	    }
	}
    }

    protected void updateProfile(Element elmt) throws EAException {

	MetaType elmtMetaType = metaType(elmt);
	if (elmtMetaType == MetaType.UNKNOWN || elmtMetaType == MetaType.IGNORED) {
	    // do not process unknown or ignored model elements
	    return;
	}

	String elmtName = elmt.GetName();

	result.addDebug(this, 102, elmtName);

//	printElementTypeInfo(elmt);

	List<StereotypeMappingInfo> smis = identifyMappingInfos(elmt);

	if (smis.isEmpty()) {
	    result.addInfo(this, 101, elmtName, elmt.GetStereotypeEx());
	} else {

	    /*
	     * get values for all relevant source TVs, to later on set them in the target
	     * TVs
	     */
	    SortedMap<String, List<String>> sourceTVs = getSourceTVs(EAElementUtil.getEATaggedValues(elmt), smis);

	    // update the stereotype(s)
	    updateStereotypes(elmt, smis);

	    // copy source tagged values (i.e., from old profile TVs) to target tagged
	    // values (i.e., to new profile TVs)
	    setTargetTVs(elmt, smis, sourceTVs);

	    /*
	     * remove old stereotype; in case of old tagged values without stereotype,
	     * remove those as well
	     */
	    removeOldProfile(elmt, smis);

	    Collection<Attribute> cAtts = elmt.GetAttributes();
	    cAtts.Refresh();
	    for (Attribute a : cAtts) {
		updateProfile(a, elmtMetaType == MetaType.ENUMERATION);
	    }

	    if (elmtMetaType == MetaType.CLASS || elmtMetaType == MetaType.DATATYPE) {

		Collection<Connector> cConns = elmt.GetConnectors();
		cConns.Refresh();

		for (Connector conn : cConns) {

		    // only process "Association" connectors
		    String type = conn.GetType();
		    if (!type.equalsIgnoreCase("Association") && !type.equalsIgnoreCase("Aggregation")) {
			continue;
		    }

		    if (conn.GetClientID() == conn.GetSupplierID()) {

			// reflexive association
			processConnectorEnd("target", conn.GetSupplierEnd(), conn);
			processConnectorEnd("source", conn.GetClientEnd(), conn);

		    } else {

			boolean connectorEndOwnedByElementIsSupplierEnd = conn.GetClientID() == elmt.GetElementID();
			ConnectorEnd connectorEndOwnedByElmt = connectorEndOwnedByElementIsSupplierEnd
				? conn.GetSupplierEnd()
				: conn.GetClientEnd();
			String connectorEndOwnedByElmtIdentifier = connectorEndOwnedByElementIsSupplierEnd ? "target"
				: "source";
			processConnectorEnd(connectorEndOwnedByElmtIdentifier, connectorEndOwnedByElmt, conn);
		    }
		}
	    }
	}
    }

    private void processConnectorEnd(String connectorEndIdentifier, ConnectorEnd ce, Connector conn)
	    throws EAException {

	String connectorEndName = StringUtils.stripToEmpty(ce.GetRole());

	// check if the connector end has no name
	if (connectorEndName.length() == 0) {
	    result.addInfo(this, 114, connectorEndIdentifier, associationInfo(conn));
	}

	if (!UPDATE_ASSOCIATION_ROLES_WITHOUT_NAME && connectorEndName.length() == 0) {
	    result.addInfo(this, 115, connectorEndIdentifier, associationInfo(conn));
	} else {
	    updateProfile(ce, conn);
	}
    }

    private void checkGeoInfoDokContainment(Package gidPkg) {

	Collection<Connector> pkgConns = gidPkg.GetConnectors();
	pkgConns.Refresh();

	for (Connector pkgConn : pkgConns) {

	    if (isIgnored(pkgConn)) {
		continue;
	    }

	    int clientId = pkgConn.GetClientID();
	    int supplierId = pkgConn.GetSupplierID();

	    if (!idOfGeoInfoDok(clientId) || !idOfGeoInfoDok(supplierId)) {
		result.addInfo(this, 121, EAConnectorUtil.connectorInfo(pkgConn, rep));
	    }
	}

	Collection<Element> c = gidPkg.GetElements();
	c.Refresh();
	for (Element elmt : c) {

//	    MetaType elmtMetaType = metaType(elmt);
//
//	    if (elmtMetaType == MetaType.CLASS || elmtMetaType == MetaType.DATATYPE) {

	    Collection<Connector> cConns = elmt.GetConnectors();
	    cConns.Refresh();

	    for (Connector conn : cConns) {

		if (isIgnored(conn)) {
		    continue;
		}

		int clientId = conn.GetClientID();
		int supplierId = conn.GetSupplierID();

//		String sourceClassName = rep.GetElementByID(conn.GetClientID()).GetName();
//		String targetClassName = rep.GetElementByID(conn.GetSupplierID()).GetName();

		if (clientId != supplierId && (!idOfGeoInfoDok(clientId) || !idOfGeoInfoDok(supplierId))) {

		    String type = conn.GetType();

		    if (!type.equalsIgnoreCase("Association") && !type.equalsIgnoreCase("Aggregation")) {

			result.addInfo(this, 122, EAConnectorUtil.connectorInfo(conn, rep));

		    } else {

//				/*
//				 * log associations where not both ends are GeoInfoDok classes (with according
//				 * name prefix)
//				 */
//				String sourceClassName = rep.GetElementByID(conn.GetClientID()).GetName();
//				String targetClassName = rep.GetElementByID(conn.GetSupplierID()).GetName();
//
//				if (!startsWithGeoInfoDokPrefix(sourceClassName)
//					|| !startsWithGeoInfoDokPrefix(targetClassName)) {
//				    result.addInfo(this, 116, modelInfo(conn));
//				}

			result.addInfo(this, 116, associationInfo(conn));
		    }
		}
	    }
//	    }
	}

	// drill down into child packages

	Collection<Package> childPackages = gidPkg.GetPackages();
	childPackages.Refresh();
	for (Package cp : childPackages) {
	    checkGeoInfoDokContainment(cp);
	}
    }

    private boolean isIgnored(Connector conn) {
	return conn.GetMetaType().equalsIgnoreCase("NoteLink");
    }

    private boolean idOfGeoInfoDok(int id) {
	return gidElementIds.contains(id) /* || gidPkgIds.contains(id) */ || gidPkgElementIds.contains(id);
    }

    private boolean startsWithGeoInfoDokPrefix(String className) {

	for (String gidClassPrefix : this.geoinfodokClassPrefixes) {
	    if (className.startsWith(gidClassPrefix)) {
		return true;
	    }
	}
	return false;
    }

    private String associationInfo(Connector conn) {

	String sourceClassName = rep.GetElementByID(conn.GetClientID()).GetName();
	String targetClassName = rep.GetElementByID(conn.GetSupplierID()).GetName();

	ConnectorEnd sourceEnd = conn.GetClientEnd();
	String sourceEndName = StringUtils.defaultIfBlank(sourceEnd.GetRole(), ID_FOR_ASSOCIATION_ROLE_WITHOUT_NAME);
	boolean sourceIsNavigable = EAConnectorEndUtil.isNavigable(sourceEnd, conn, true);

	ConnectorEnd targetEnd = conn.GetSupplierEnd();
	String targetEndName = StringUtils.defaultIfBlank(targetEnd.GetRole(), ID_FOR_ASSOCIATION_ROLE_WITHOUT_NAME);
	boolean targetIsNavigable = EAConnectorEndUtil.isNavigable(targetEnd, conn, true);

	return sourceClassName + "|" + sourceEndName + (sourceIsNavigable ? "<" : "") + "---"
		+ (targetIsNavigable ? ">" : "") + targetEndName + "|" + targetClassName;
    }

    private void removeOldProfile(Element elmt, List<StereotypeMappingInfo> smis) throws EAException {

	if (KEEP_OLD_PROFILE) {
	    return;
	}

	// remove source stereotypes
	String stereotypeEx = elmt.GetStereotypeEx();

	if (StringUtils.isNotBlank(stereotypeEx)) {

	    Optional<String> finalStereotypeExOpt = determineRemainingStereotypes(stereotypeEx, smis);
	    if (finalStereotypeExOpt.isPresent()) {
//		System.out.println("   final StereotypeEx: " + finalStereotypeExOpt.get());
		EAElementUtil.setEAStereotypeEx(elmt, finalStereotypeExOpt.get());
	    }
	}

	// remove any remaining source tagged values

	Set<String> fqNamesOfTargetTVsToKeep = new HashSet<>();
	Set<String> namesOfSourceTVsToRemove = new HashSet<>();
	for (StereotypeMappingInfo smi : smis) {
	    fqNamesOfTargetTVsToKeep.addAll(smi.getTvSourceToTargetFQNameMap().values());
	    namesOfSourceTVsToRemove.addAll(smi.getTvNameToSourceFQNameMap().keySet());
	}

	Collection<TaggedValue> cTV = elmt.GetTaggedValues();
	cTV.Refresh();
	for (short i = 0; i < cTV.GetCount(); i++) {
	    TaggedValue tv = cTV.GetAt(i);
	    if (!fqNamesOfTargetTVsToKeep.contains(tv.GetFQName()) && namesOfSourceTVsToRemove.contains(tv.GetName())) {
		cTV.Delete(i);
	    }
	}
	cTV.Refresh();
    }

    private Optional<String> determineRemainingStereotypes(String oldStereotypeEx, List<StereotypeMappingInfo> smis) {

	SortedSet<String> remainingStereotypes = new TreeSet<>();

	String[] oldStereotypes = oldStereotypeEx.split("\\s*,\\s*");

	outer: for (String stOld : oldStereotypes) {

	    boolean notFoundAsSourceStereotype = true;

	    inner: for (StereotypeMappingInfo smi : smis) {
		if (StringUtils.substringAfterLast(smi.getTargetStereotypeFQName(), "::").equals(stOld)) {
		    remainingStereotypes.add(smi.getTargetStereotypeFQName());
		    continue outer;
		}

		if (StringUtils.substringAfterLast(smi.getSourceStereotypeFQName(), "::").equalsIgnoreCase(stOld)) {
		    notFoundAsSourceStereotype = false;
		    break inner;
		}
	    }

	    if (notFoundAsSourceStereotype) {
		remainingStereotypes.add(stOld);
	    }
	}

	if (!remainingStereotypes.isEmpty()) {
	    return Optional.of(StringUtils.join(remainingStereotypes, ","));
	} else {
	    return Optional.empty();
	}
    }

    protected void updateProfile(ConnectorEnd connectorEnd, Connector conn) throws EAException {

	String name = StringUtils.defaultIfBlank(connectorEnd.GetRole(), ID_FOR_ASSOCIATION_ROLE_WITHOUT_NAME);

	result.addDebug(this, 111, name);

	List<StereotypeMappingInfo> smis = identifyMappingInfos(connectorEnd);

	if (smis.isEmpty()) {
	    result.addInfo(this, 113, name);
	} else {

	    /*
	     * get values for all relevant source TVs, to later on set them in the target
	     * TVs
	     */

	    SortedMap<String, List<String>> sourceTVs = getSourceTVs(EAConnectorEndUtil.getEATaggedValues(connectorEnd),
		    smis);
	    /*
	     * For association roles, we remove all source tagged values. The reason is that
	     * apparently EA has an issue when a new stereotype is assigned to an
	     * association role, where the stereotype has a tag with same name as an already
	     * existing tag that does not belong to any stereotype (like tags that were
	     * added manually).
	     */
	    removeSourceTaggedValues(connectorEnd, smis);

	    // update the stereotype(s)
	    updateStereotypes(connectorEnd, smis);

	    // copy source tagged values (i.e., from old profile TVs) to target tagged
	    // values (i.e., to new profile TVs)
	    setTargetTVs(connectorEnd, conn, smis, sourceTVs);

	    /*
	     * remove old stereotype; in case of old tagged values without stereotype,
	     * remove those as well
	     */
	    removeOldProfile(connectorEnd, smis);
	}

    }

    private void removeOldProfile(ConnectorEnd connectorEnd, List<StereotypeMappingInfo> smis) throws EAException {

	if (KEEP_OLD_PROFILE) {
	    return;
	}

	// remove source stereotypes
	String stereotypeEx = connectorEnd.GetStereotypeEx();

	if (StringUtils.isNotBlank(stereotypeEx)) {

	    Optional<String> finalStereotypeExOpt = determineRemainingStereotypes(stereotypeEx, smis);
	    if (finalStereotypeExOpt.isPresent()) {
//		System.out.println("   final StereotypeEx: " + finalStereotypeExOpt.get());
		EAConnectorEndUtil.setEAStereotypeEx(connectorEnd, finalStereotypeExOpt.get());
	    }
	}

	// remove any remaining source tagged values
	removeSourceTaggedValues(connectorEnd, smis);
    }

    private void removeSourceTaggedValues(ConnectorEnd connectorEnd, List<StereotypeMappingInfo> smis) {

	Set<String> fqNamesOfTargetTVsToKeep = new HashSet<>();
	Set<String> namesOfSourceTVsToRemove = new HashSet<>();
	for (StereotypeMappingInfo smi : smis) {
	    fqNamesOfTargetTVsToKeep.addAll(smi.getTvSourceToTargetFQNameMap().values());
	    namesOfSourceTVsToRemove.addAll(smi.getTvNameToSourceFQNameMap().keySet());
	}

	Collection<RoleTag> cTV = connectorEnd.GetTaggedValues();
	cTV.Refresh();

	for (short i = 0; i < cTV.GetCount(); i++) {
	    RoleTag tv = cTV.GetAt(i);
	    if (!fqNamesOfTargetTVsToKeep.contains(tv.GetFQName()) && namesOfSourceTVsToRemove.contains(tv.GetTag())) {
		cTV.Delete(i);
	    }
	}
	cTV.Refresh();
    }

    protected void updateProfile(Attribute att, boolean isEnumerationLiteral) throws EAException {

	String attName = att.GetName();

	result.addDebug(this, 106, attName);

//	printAttributeTypeInfo(att);

	List<StereotypeMappingInfo> smis = identifyMappingInfos(att, isEnumerationLiteral);

	if (smis.isEmpty()) {
	    result.addInfo(this, 108, attName);
	} else {

	    /*
	     * get values for all relevant source TVs, to later on set them in the target
	     * TVs
	     */
	    SortedMap<String, List<String>> sourceTVs = getSourceTVs(
		    EAAttributeUtil.getEATaggedValuesWithCombinedKeys(att), smis);

	    // update the stereotype(s)
	    updateStereotypes(att, smis);

	    // copy source tagged values (i.e., from old profile TVs) to target tagged
	    // values (i.e., to new profile TVs)
	    setTargetTVs(att, smis, sourceTVs);

	    /*
	     * remove old stereotype; in case of old tagged values without stereotype,
	     * remove those as well
	     */
	    removeOldProfile(att, smis);
	}

    }

    private void addStatisticsFromSourceTVs(SortedMap<String, List<String>> sourceTVs) {

	for (Entry<String, List<String>> e : sourceTVs.entrySet()) {
	    String sourceTaggedValueFQName = e.getKey();
	    for (String v : e.getValue()) {
		if (StringUtils.isNotBlank(v)) {

		    int counter = 0;
		    if (numberOfNonBlankValuesBySourceTaggedValueFQName.containsKey(sourceTaggedValueFQName)) {
			counter = numberOfNonBlankValuesBySourceTaggedValueFQName.get(sourceTaggedValueFQName);
		    }
		    counter++;
		    numberOfNonBlankValuesBySourceTaggedValueFQName.put(sourceTaggedValueFQName, counter);

		    continue;
		}
	    }
	}
    }

    private void removeOldProfile(Attribute att, List<StereotypeMappingInfo> smis) throws EAException {

	if (KEEP_OLD_PROFILE) {
	    return;
	}

	// remove source stereotypes
	String stereotypeEx = att.GetStereotypeEx();

	if (StringUtils.isNotBlank(stereotypeEx)) {

	    Optional<String> finalStereotypeExOpt = determineRemainingStereotypes(stereotypeEx, smis);
	    if (finalStereotypeExOpt.isPresent()) {
//		System.out.println("   final StereotypeEx: " + finalStereotypeExOpt.get());
		EAAttributeUtil.setEAStereotypeEx(att, finalStereotypeExOpt.get());
	    }
	}

	// remove any remaining source tagged values

	Set<String> fqNamesOfTargetTVsToKeep = new HashSet<>();
	Set<String> namesOfSourceTVsToRemove = new HashSet<>();
	for (StereotypeMappingInfo smi : smis) {
	    fqNamesOfTargetTVsToKeep.addAll(smi.getTvSourceToTargetFQNameMap().values());
	    namesOfSourceTVsToRemove.addAll(smi.getTvNameToSourceFQNameMap().keySet());
	}

	Collection<AttributeTag> cTV = att.GetTaggedValues();
	cTV.Refresh();

	for (short i = 0; i < cTV.GetCount(); i++) {
	    AttributeTag tv = cTV.GetAt(i);
	    if (!fqNamesOfTargetTVsToKeep.contains(tv.GetFQName()) && namesOfSourceTVsToRemove.contains(tv.GetName())) {
		cTV.Delete(i);
	    }
	}
	cTV.Refresh();
    }

    private void setTargetTVs(Element elmt, List<StereotypeMappingInfo> smis, SortedMap<String, List<String>> sourceTVs)
	    throws EAException {

	SortedMap<String, EATaggedValue> tvs = EAElementUtil.getEATaggedValues(elmt);

	for (StereotypeMappingInfo smi : smis) {

	    SortedMap<String, String> elmtTVSourceToTargetFQNameMap = smi.getTvSourceToTargetFQNameMap();

	    for (String sourceFQName : sourceTVs.keySet()) {

		if (!elmtTVSourceToTargetFQNameMap.containsKey(sourceFQName)) {
		    continue;
		}

		List<String> sourceValues = determineSourceValuesForTag(sourceFQName, sourceTVs);

		String targetFQName = elmtTVSourceToTargetFQNameMap.get(sourceFQName);

		SortedSet<String> targetLookupKeys = eaTaggedValuesLookupKey(targetFQName);

		for (String targetLookupKey : targetLookupKeys) {

		    if (tvs.containsKey(targetLookupKey)) {

			for (String sourceValue : sourceValues) {

			    /*
			     * Ensure that fixed value is set, even if it is the empty string.
			     */
			    if (StringUtils.isNotBlank(sourceValue) || isTagWithFixedValue(sourceFQName)) {
				EAElementUtil.updateTaggedValue(elmt, targetFQName, applyTargetTagMapping(sourceValue),
					false);
			    }
			}

		    } else {
			MessageContext mc = result.addError(this, 100, targetFQName);
			if (mc != null) {
			    mc.addDetail(this, 2, elmt.GetName());
			}
		    }
		}
	    }
	}

	// JE: for development only
	printTaggedValues(tvs);
    }

    private void printTaggedValues(SortedMap<String, EATaggedValue> tvs) {
	if (PRINT_TAGGED_VALUES) {
	    for (Entry<String, EATaggedValue> e : tvs.entrySet()) {
		System.out.println(e.getKey());
	    }
	}
    }

    private String applyTargetTagMapping(String sourceTagValue) {

	for (String targetValue : targetValuesForTagMapping) {
	    if (sourceTagValue.equalsIgnoreCase(targetValue)) {
		return targetValue;
	    }
	}

	return sourceTagValue;
    }

    private void setTargetTVs(Attribute att, List<StereotypeMappingInfo> smis,
	    SortedMap<String, List<String>> sourceTVs) throws EAException {

	SortedMap<String, EATaggedValue> tvs = EAAttributeUtil.getEATaggedValuesWithCombinedKeys(att);

	for (StereotypeMappingInfo smi : smis) {
	    SortedMap<String, String> elmtTVSourceToTargetFQNameMap = smi.getTvSourceToTargetFQNameMap();

	    for (String sourceFQName : sourceTVs.keySet()) {

		if (!elmtTVSourceToTargetFQNameMap.containsKey(sourceFQName)) {
		    continue;
		}

		List<String> sourceValues = determineSourceValuesForTag(sourceFQName, sourceTVs);

		String targetFQName = elmtTVSourceToTargetFQNameMap.get(sourceFQName);

		SortedSet<String> targetLookupKeys = eaTaggedValuesLookupKey(targetFQName);

		for (String targetLookupKey : targetLookupKeys) {

		    if (tvs.containsKey(targetLookupKey)) {

			for (String sourceValue : sourceValues) {

			    /*
			     * Ensure that fixed value is set, even if it is the empty string.
			     */
			    if (StringUtils.isNotBlank(sourceValue) || isTagWithFixedValue(sourceFQName)) {
				EAAttributeUtil.updateTaggedValue(att, targetFQName, applyTargetTagMapping(sourceValue),
					false);
			    }
			}

		    } else {
			MessageContext mc = result.addError(this, 100, targetFQName);
			if (mc != null) {
			    mc.addDetail(this, 3, att.GetName());
			}
		    }
		}
	    }
	}

	// JE: for development only
	printTaggedValues(tvs);
    }

    private boolean isTagWithFixedValue(String sourceTagFQName) {
	return StringUtils.containsAnyIgnoreCase(sourceTagFQName, tagsWithFixedValue.keySet().toArray(new String[0]));
    }

    private Optional<String> getFixedValue(String sourceTagFQName) {

	for (Entry<String, String> e : tagsWithFixedValue.entrySet()) {
	    if (StringUtils.containsIgnoreCase(sourceTagFQName, e.getKey())) {
		return Optional.of(e.getValue());
	    }
	}

	return Optional.empty();
    }

    private void setTargetTVs(ConnectorEnd ce, Connector conn, List<StereotypeMappingInfo> smis,
	    SortedMap<String, List<String>> sourceTVs) throws EAException {

	SortedMap<String, EATaggedValue> tvs = EAConnectorEndUtil.getEATaggedValues(ce);

	for (StereotypeMappingInfo smi : smis) {

	    SortedMap<String, String> elmtTVSourceToTargetFQNameMap = smi.getTvSourceToTargetFQNameMap();

	    for (String sourceFQName : sourceTVs.keySet()) {

		if (!elmtTVSourceToTargetFQNameMap.containsKey(sourceFQName)) {
		    continue;
		}

		List<String> sourceValues = determineSourceValuesForTag(sourceFQName, sourceTVs);

		String targetFQName = elmtTVSourceToTargetFQNameMap.get(sourceFQName);

		SortedSet<String> targetLookupKeys = eaTaggedValuesLookupKey(targetFQName);

		for (String targetLookupKey : targetLookupKeys) {

		    if (tvs.containsKey(targetLookupKey)) {

			for (String sourceValue : sourceValues) {

			    /*
			     * Ensure that fixed value is set, even if it is the empty string.
			     */
			    if (StringUtils.isNotBlank(sourceValue) || isTagWithFixedValue(sourceFQName)) {
				EAConnectorEndUtil.updateTaggedValue(ce, targetFQName,
					applyTargetTagMapping(sourceValue), false);
			    }
			}

		    } else {
			MessageContext mc = result.addError(this, 100, targetFQName);
			if (mc != null) {
			    mc.addDetail(this, 4,
				    StringUtils.defaultIfBlank(ce.GetRole(), ID_FOR_ASSOCIATION_ROLE_WITHOUT_NAME),
				    associationInfo(conn));
			}
		    }
		}
	    }
	}

	// JE: for development only
	printTaggedValues(tvs);
    }

    private List<String> determineSourceValuesForTag(String sourceFQName, SortedMap<String, List<String>> sourceTVs) {

	List<String> sourceValues;

	Optional<String> fixedValueOpt = getFixedValue(sourceFQName);

	if (fixedValueOpt.isPresent()) {
	    sourceValues = new ArrayList<>();
	    sourceValues.add(fixedValueOpt.get());
	} else {
	    sourceValues = sourceTVs.get(sourceFQName);
	}

	return sourceValues;
    }

    private void updateStereotypes(Element elmt, List<StereotypeMappingInfo> smis) throws EAException {

	String stex = elmt.GetStereotypeEx();
	String newStex = determineNewStereotypeEx(stex, smis);
	EAElementUtil.setEAStereotypeEx(elmt, newStex);
    }

    private String determineNewStereotypeEx(String oldStereotypeEx, List<StereotypeMappingInfo> smis) {

//	System.out.println("   old StereotypeEx: " + oldStereotypeEx);

	String newStex = smis.stream().map(smi -> smi.getTargetStereotypeFQName()).collect(Collectors.joining(","));
	if (!newStex.contains(oldStereotypeEx)) {
	    newStex = oldStereotypeEx + (oldStereotypeEx.length() > 0 ? "," : "") + newStex;
	}
//	System.out.println("   new StereotypeEx: " + newStex);

	return newStex;
    }

    private void updateStereotypes(Attribute att, List<StereotypeMappingInfo> smis) throws EAException {

	String stex = att.GetStereotypeEx();
	String newStex = determineNewStereotypeEx(stex, smis);
	EAAttributeUtil.setEAStereotypeEx(att, newStex);
    }

    private void updateStereotypes(ConnectorEnd ce, List<StereotypeMappingInfo> smis) throws EAException {

	String stex = ce.GetStereotypeEx();
	String newStex = determineNewStereotypeEx(stex, smis);

	// TEST: remove new stereotype before setting it again
	if (newStex.contains(newStex)) {
	    EAConnectorEndUtil.setEAStereotypeEx(ce, "");
	}

	EAConnectorEndUtil.setEAStereotypeEx(ce, newStex);
    }

    private MetaType metaType(Element elmt) {

	switch (elmt.GetMetaType()) {
	case "Package":
	    return MetaType.PACKAGE;
	case "Class":
	    return MetaType.CLASS;
	case "DataType":
	    return MetaType.DATATYPE;
	case "Enumeration":
	    return MetaType.ENUMERATION;
	case "Note":
	    return MetaType.IGNORED;
	case "Metaclass":
	    return MetaType.IGNORED;
	default:
	    result.addWarning(this, 103, elmt.GetName(), elmt.GetMetaType());
	    return MetaType.UNKNOWN;
	}
    }

    private List<StereotypeMappingInfo> identifyMappingInfos(Element elmt) {

	List<StereotypeMappingInfo> res = new ArrayList<>();

	MetaType met = metaType(elmt);

	/*
	 * für meta type des elements die Stereotypen suchen; falls keine gefunden,
	 * fallback für den meta type suchen
	 */

	for (StereotypeMappingInfo smi : this.stereotypeMappingInfos) {

	    if (smi.getApplicableMetaType() != met) {
		continue;
	    }

	    String sourceStereotypeFQName = smi.getSourceStereotypeFQName();
	    List<String> sourceStereotypeFQNameLookupList = createFQNameLookupList(sourceStereotypeFQName);

	    if (EAElementUtil.hasStereotype(elmt,
		    sourceStereotypeFQNameLookupList.toArray(new String[sourceStereotypeFQNameLookupList.size()]))) {
		res.add(smi);
	    }
	}

	if (res.isEmpty()) {
	    StereotypeMappingInfo fallbackSmi = identifyFallbackMappingInfo(elmt);
	    if (fallbackSmi != null) {
		res.add(fallbackSmi);

		if (met == MetaType.CLASS && WARN_ON_FALLBACK_ASSIGNMENT.contains(MetaType.CLASS)) {
		    result.addWarning(this, 104, elmt.GetName());
		} else if (met == MetaType.DATATYPE && WARN_ON_FALLBACK_ASSIGNMENT.contains(MetaType.DATATYPE)) {
		    result.addWarning(this, 105, elmt.GetName());
		} else if (met == MetaType.ENUMERATION && WARN_ON_FALLBACK_ASSIGNMENT.contains(MetaType.ENUMERATION)) {
		    result.addWarning(this, 118, elmt.GetName());
		} else if (met == MetaType.PACKAGE && WARN_ON_FALLBACK_ASSIGNMENT.contains(MetaType.PACKAGE)) {
		    result.addWarning(this, 119, elmt.GetName());
		}
	    }
	}

	// zusätzlich Mappings für meta type ANY suchen und mit aufnehmen (dabei aber
	// kein fallback)

	for (StereotypeMappingInfo smi : this.stereotypeMappingInfos) {

	    if (smi.getApplicableMetaType() == MetaType.ANY) {

		String sourceStereotypeFQName = smi.getSourceStereotypeFQName();
		List<String> sourceStereotypeFQNameLookupList = createFQNameLookupList(sourceStereotypeFQName);

		if (EAElementUtil.hasStereotype(elmt, sourceStereotypeFQNameLookupList
			.toArray(new String[sourceStereotypeFQNameLookupList.size()]))) {
		    res.add(smi);
		}
	    }
	}

	return res;
    }

    private List<StereotypeMappingInfo> identifyMappingInfos(Attribute att, boolean isEnumerationLiteral) {

	String attName = att.GetName();

	List<StereotypeMappingInfo> res = new ArrayList<>();

	for (StereotypeMappingInfo smi : this.stereotypeMappingInfos) {

	    if (!((isEnumerationLiteral && smi.getApplicableMetaType() == MetaType.ENUMERATIONLITERAL)
		    || (!isEnumerationLiteral && smi.getApplicableMetaType() == MetaType.PROPERTY))) {
		continue;
	    }

	    String sourceStereotypeFQName = smi.getSourceStereotypeFQName();
	    List<String> sourceStereotypeFQNameLookupList = createFQNameLookupList(sourceStereotypeFQName);

	    if (EAAttributeUtil.hasStereotype(att,
		    sourceStereotypeFQNameLookupList.toArray(new String[sourceStereotypeFQNameLookupList.size()]))) {
		res.add(smi);
	    }
	}

	if (res.isEmpty()) {
	    StereotypeMappingInfo fallbackSmi = identifyFallbackMappingInfoForProperties(isEnumerationLiteral);
	    if (fallbackSmi != null) {
		res.add(fallbackSmi);
		if (isEnumerationLiteral && WARN_ON_FALLBACK_ASSIGNMENT.contains(MetaType.ENUMERATIONLITERAL)) {
		    result.addWarning(this, 117, attName);
		} else if (!isEnumerationLiteral && WARN_ON_FALLBACK_ASSIGNMENT.contains(MetaType.PROPERTY)) {
		    result.addWarning(this, 107, attName);
		}
	    }
	}

	// zusätzlich Mappings für meta type ANY suchen und mit aufnehmen (dabei aber
	// kein fallback)

	for (StereotypeMappingInfo smi : this.stereotypeMappingInfos) {

	    if (smi.getApplicableMetaType() == MetaType.ANY) {

		String sourceStereotypeFQName = smi.getSourceStereotypeFQName();
		List<String> sourceStereotypeFQNameLookupList = createFQNameLookupList(sourceStereotypeFQName);

		if (EAAttributeUtil.hasStereotype(att, sourceStereotypeFQNameLookupList
			.toArray(new String[sourceStereotypeFQNameLookupList.size()]))) {
		    res.add(smi);
		}
	    }
	}

	return res;
    }

    private List<StereotypeMappingInfo> identifyMappingInfos(ConnectorEnd ce) {

	List<StereotypeMappingInfo> res = new ArrayList<>();

	for (StereotypeMappingInfo smi : this.stereotypeMappingInfos) {

	    if (smi.getApplicableMetaType() != MetaType.PROPERTY) {
		continue;
	    }

	    String sourceStereotypeFQName = smi.getSourceStereotypeFQName();
	    List<String> sourceStereotypeFQNameLookupList = createFQNameLookupList(sourceStereotypeFQName);

	    if (EAConnectorEndUtil.hasStereotype(ce,
		    sourceStereotypeFQNameLookupList.toArray(new String[sourceStereotypeFQNameLookupList.size()]))) {
		res.add(smi);
	    }
	}

	if (res.isEmpty()) {
	    StereotypeMappingInfo fallbackSmi = identifyFallbackMappingInfoForProperties(false);
	    if (fallbackSmi != null) {
		res.add(fallbackSmi);
		if (WARN_ON_FALLBACK_ASSIGNMENT.contains(MetaType.PROPERTY)) {
		    result.addWarning(this, 112,
			    StringUtils.defaultIfBlank(ce.GetRole(), ID_FOR_ASSOCIATION_ROLE_WITHOUT_NAME));
		}
	    }
	}

	// zusätzlich Mappings für meta type ANY suchen und mit aufnehmen (dabei aber
	// kein fallback)

	for (StereotypeMappingInfo smi : this.stereotypeMappingInfos) {

	    if (smi.getApplicableMetaType() == MetaType.ANY) {

		String sourceStereotypeFQName = smi.getSourceStereotypeFQName();
		List<String> sourceStereotypeFQNameLookupList = createFQNameLookupList(sourceStereotypeFQName);

		if (EAConnectorEndUtil.hasStereotype(ce, sourceStereotypeFQNameLookupList
			.toArray(new String[sourceStereotypeFQNameLookupList.size()]))) {
		    res.add(smi);
		}
	    }
	}

	return res;
    }

    private StereotypeMappingInfo identifyFallbackMappingInfo(Element elmt) {

	MetaType met = metaType(elmt);
	StereotypeMappingInfo res = null;

	for (StereotypeMappingInfo smi : this.stereotypeMappingInfos) {
	    if (smi.getApplicableMetaType() == met && smi.isFallbackMappingForApplicableMetaType()) {
		res = smi;
		break;
	    }
	}

	return res;
    }

    private StereotypeMappingInfo identifyFallbackMappingInfoForProperties(boolean isEnumerationLiteral) {

	StereotypeMappingInfo res = null;

	for (StereotypeMappingInfo smi : this.stereotypeMappingInfos) {
	    if (((isEnumerationLiteral && smi.getApplicableMetaType() == MetaType.ENUMERATIONLITERAL)
		    || (!isEnumerationLiteral && smi.getApplicableMetaType() == MetaType.PROPERTY))
		    && smi.isFallbackMappingForApplicableMetaType()) {
		res = smi;
		break;
	    }
	}

	return res;
    }

    /**
     * @param tvs  tbd
     * @param smis tbd
     * @return Map with key: source tag fqname, value: list of values found for this
     *         tag
     * @throws EAException
     */
    private SortedMap<String, List<String>> getSourceTVs(SortedMap<String, EATaggedValue> tvs,
	    List<StereotypeMappingInfo> smis) throws EAException {

	SortedMap<String, List<String>> res = new TreeMap<>();

	for (StereotypeMappingInfo smi : smis) {

	    SortedMap<String, String> elmtTVNameToSourceFQNameMap = smi.getTvNameToSourceFQNameMap();

	    for (String sourceTVName : elmtTVNameToSourceFQNameMap.keySet()) {

		String sourceFQName = elmtTVNameToSourceFQNameMap.get(sourceTVName);

		/*
		 * lookup the TV with sourceFQName, beginning with most specific qualified name
		 * (profile, stereotype, tv), then decreasing specificity (stereotype, tv and
		 * lastly just tv)
		 */

		List<String> fqNameLookupList = createFQNameLookupList(sourceFQName);

		fqNameLookupLoop: for (String lookupFQName : fqNameLookupList) {

//		System.out.println("Looking up sourceFQName: " + lookupFQName);

		    SortedSet<String> sourceLookupKeys = eaTaggedValuesLookupKey(lookupFQName);

		    for (String sourceLookupKey : sourceLookupKeys) {

			boolean foundTVWithSourceFQName = tvs.containsKey(sourceLookupKey);
//		System.out.println("element has tv with sourceFQName: " + foundTVWithSourceFQName);

			if (foundTVWithSourceFQName) {

//			System.out.println("  Found tag " + sourceFQName + " with lookupFQName " + lookupFQName);

			    EATaggedValue sourceTV = tvs.get(sourceLookupKey);
			    List<String> sourceValues = sourceTV.getValues();

			    if (sourceValues.isEmpty() || sourceValues.stream().allMatch(v -> StringUtils.isBlank(v))) {
				/*
				 * nothing to do - ignore tags without value here, to allow for tagged values
				 * with same tag name but less specific lookupFQName (typically a plain tag that
				 * does not belong to any stereotype) to provide a value
				 */
			    } else {
				res.put(sourceFQName, sourceValues);
				// found and processed source TV (with at least one value), break lookup loop
				break fqNameLookupLoop;
			    }
			}
		    }
		}
	    }
	}

	// JE: for development only
//	for (Entry<String, EATaggedValue> e : tvs.entrySet()) {
//	    System.out.println(e.getKey());
//	}

	addStatisticsFromSourceTVs(res);

	return res;
    }

    private List<String> createFQNameLookupList(String fqName) {

	List<String> res = new ArrayList<>();

	String name = fqName;

	while (StringUtils.isNotBlank(name)) {
	    res.add(name);
	    if (name.contains("::")) {
		name = name.substring(name.indexOf("::") + 2);
	    } else {
		name = null;
	    }
	}

	return res;
    }

    private SortedSet<String> eaTaggedValuesLookupKey(String fqName) {

	SortedSet<String> res = new TreeSet<>();

	if (fqName.contains("::")) {
	    String[] parts = fqName.split("::");
	    res.add(parts[parts.length - 1] + "#" + fqName);
	} else {
	    /*
	     * There is no fqName; lookup key is either just {tag}+'#', or {tag}+'#'+{tag}
	     * (both versions were observed while developing the UML profile transformer,
	     * and testing profile transfer for classifiers, packages, connector ends, and
	     * attributes).
	     */
	    res.add(fqName + "#" + fqName);
	    res.add(fqName + "#");
	}

	return res;
    }

    private void printElementTypeInfo(Element elmt) {

	String elementName = elmt.GetName();

	String metaType = elmt.GetMetaType();
	String type = elmt.GetType();
	int subtype = elmt.GetSubtype();

	System.out.println("Type info " + elementName + ": " + metaType + ", " + type + ", " + subtype);

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

	case 100:
	    return "Did not find target TV with FQName '$1$' to copy tag value(s) to.";
	case 101:
	    return "No stereotype mapping info found for model element '$1$' (StereotypeEx is: '$2$'). The profile of the element is not updated.";
	case 102:
	    return "Updating profile for model element '$1$'.";
	case 103:
	    return "??Model element '$1$' has unknown meta type '$2$'.";
	case 104:
	    return "Using fallback stereotype mapping info for class '$1$'. That may be unintended! Extend the mapping definition if default mapping is not applicable.";
	case 105:
	    return "Using fallback stereotype mapping info for datatype '$1$'. That may be unintended! Extend the mapping definition if default mapping is not applicable.";
	case 106:
	    return "Updating profile for attribute '$1$'.";
	case 107:
	    return "Using fallback stereotype mapping info for attribute '$1$'. That may be unintended! Extend the mapping definition if default mapping is not applicable.";
	case 108:
	    return "No stereotype mapping info found for attribute '$1$'. The profile of the attribute cannot be updated.";
	case 109:
	    return "??Encountered stereotype mapping with invalid target stereotype identifier '$1$'. The identifier must be defined according to the pattern: {profile id}::{stereotype name}!";
	case 110:
	    return "??Synchronizing profile for target stereotype '$1$' did not succeed. Last error info from EA Repository: $2$";
	case 111:
	    return "Updating profile for association end '$1$'.";
	case 112:
	    return "Using fallback stereotype mapping info for association role '$1$'. That may be unintended! Extend the mapping definition if default mapping is not applicable.";
	case 113:
	    return "No stereotype mapping info found for association role '$1$'. The profile of the association role cannot be updated.";
	case 114:
	    return "Association role without name: $1$ end of association $2$.";
	case 115:
	    return "Skipping UML profile update of $1$ end of association $2$ because that end has no name, and profile updates for such associations is disabled.";
	case 116:
	    return "??GeoInfoDok association or aggregation to/from external element: $1$";
	case 117:
	    return "Using fallback stereotype mapping info for enum '$1$'. That may be unintended! Extend the mapping definition if default mapping is not applicable.";
	case 118:
	    return "Using fallback stereotype mapping info for enumeration '$1$'. That may be unintended! Extend the mapping definition if default mapping is not applicable.";
	case 119:
	    return "Using fallback stereotype mapping info for package '$1$'. That may be unintended! Extend the mapping definition if default mapping is not applicable.";
	case 120:
	    return "Ignoring package '$1$' (and its children) because it is listed on the exclude list.";
	case 121:
	    return "??GeoInfoDok package connector to/from external element: $1$";
	case 122:
	    return "??GeoInfoDok classifier connector to/from external element: $1$";
	case 123:
	    return "Updating profile for package '$1$'.";

	case 1000:
	    return "Analyzing original GeoInfoDok package";
	case 1001:
	    return "Transforming UML profile";
	case 1002:
	    return "Statistics - occurrences of source tag with actual value(s):";
	case 1003:
	    return "- $1$: $2$";
	case 1004:
	    return "tag '$1$', total number of actual values: $2$";

	default:
	    return "(" + GidProfileTransformer.class.getName() + ") Unknown message with number: " + mnr;
	}
    }
}
