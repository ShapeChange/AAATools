/**
 * GeoInfoDok Transformations (CodeList Tag Transformer)
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sparx.Attribute;
import org.sparx.Constraint;
import org.sparx.Element;
import org.sparx.Repository;

import de.interactive_instruments.shapechange.core.MessageSource;
import de.interactive_instruments.shapechange.core.Options;
import de.interactive_instruments.shapechange.core.ShapeChangeAbortException;
import de.interactive_instruments.shapechange.core.ShapeChangeResult;
import de.interactive_instruments.shapechange.core.ShapeChangeResult.MessageContext;
import de.interactive_instruments.shapechange.core.model.Transformer;
import de.interactive_instruments.shapechange.ea.util.EAAttributeUtil;
import de.interactive_instruments.shapechange.ea.util.EAConstraintUtil;
import de.interactive_instruments.shapechange.ea.util.EAElementUtil;
import de.interactive_instruments.shapechange.ea.util.EAException;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EAElement;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EAPackage;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EARepository;

/**
 * @author Johannes Echterhoff (echterhoff at interactive-instruments dot de)
 *
 */
public class Iso19107Edition2Transformer implements Transformer, MessageSource {

    public static final String RELEVANT_SCHEMA_FULL_NAME = "Model::GeoInfoDok::AFIS-ALKIS-ATKIS Anwendungsschema::AFIS-ALKIS-ATKIS Anwendungsschema DEV";

    public static final String ISO_19107_ED2_REQCLASS_GEOMETRY_FULL_NAME = "Model::ISO/TC 211::ISO 19107 Spatial schema::ISO 19107 Edition 2::Geometry";
    public static final String ISO_19107_ED2_REQCLASS_POLYGON_FULL_NAME = "Model::ISO/TC 211::ISO 19107 Spatial schema::ISO 19107 Edition 2::Surfaces::Polygon";
    public static final String ISO_19107_ED2_REQCLASS_COORDINATES_FULL_NAME = "Model::ISO/TC 211::ISO 19107 Spatial schema::ISO 19107 Edition 2::Coordinates";

    private ShapeChangeResult result = null;

    private Repository rep = null;
    private EARepository eaRepo = null;

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

	/** Connect to EA Repository */
	String absname = repfile.getAbsolutePath();
	rep = new Repository();
	if (!rep.OpenFile(absname)) {
	    String errormsg = rep.GetLastError();
	    r.addFatalError(null, 30, errormsg, repositoryFileName);
	    throw new ShapeChangeAbortException();
	}
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

	    EAPackage relevantSchemaPkg = eaRepo.lookupPackage(RELEVANT_SCHEMA_FULL_NAME).get();

	    Collection<EAElement> relevantElements = eaRepo.elementsAll(relevantSchemaPkg).values().stream()
		    .filter(e -> StringUtils.equalsAnyIgnoreCase(e.getMetaType(), "class", "datatype"))
		    .collect(Collectors.toList());

	    // identify the elements from relevant ISO 19107 Edition 2 requirements classes

	    EAPackage iso19107Ed2SchemaPkg1 = eaRepo.lookupPackage(ISO_19107_ED2_REQCLASS_GEOMETRY_FULL_NAME).get();
	    EAPackage iso19107Ed2SchemaPkg2 = eaRepo.lookupPackage(ISO_19107_ED2_REQCLASS_POLYGON_FULL_NAME).get();
	    EAPackage iso19107Ed2SchemaPkg3 = eaRepo.lookupPackage(ISO_19107_ED2_REQCLASS_COORDINATES_FULL_NAME).get();

	    List<EAElement> iso19107Ed2Elements = new ArrayList<>();
	    iso19107Ed2Elements.addAll(eaRepo.elementsAll(iso19107Ed2SchemaPkg1).values());
	    iso19107Ed2Elements.addAll(eaRepo.elementsAll(iso19107Ed2SchemaPkg2).values());
	    iso19107Ed2Elements.addAll(eaRepo.elementsAll(iso19107Ed2SchemaPkg3).values());

	    Map<String, EAElement> iso19107Ed2ElementsByName = iso19107Ed2Elements.stream()
		    .filter(e -> StringUtils.equalsAnyIgnoreCase(e.getMetaType(), "interface", "class", "datatype")
			    && !StringUtils.containsIgnoreCase(rep.GetElementByID(e.getElementId()).GetStereotypeList(),
				    "codelist"))
		    .collect(Collectors.toMap(e -> e.getName(), e -> e));

	    transformStandardCases(relevantElements, iso19107Ed2ElementsByName);

	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}
    }

    private void transformStandardCases(Collection<EAElement> relevantElements,
	    Map<String, EAElement> iso19107Ed2ElementsByName) {

	// Lookup relevant elements from ISO 19107 Edition 2

	EAElement collectionEaElmt = iso19107Ed2ElementsByName.get("Collection");
	EAElement curveEaElmt = iso19107Ed2ElementsByName.get("Curve");
	EAElement envelopeEaElmt = iso19107Ed2ElementsByName.get("Envelope");
	EAElement geometryEaElmt = iso19107Ed2ElementsByName.get("Geometry");
	EAElement pointEaElmt = iso19107Ed2ElementsByName.get("Point");
	EAElement polyhedralSurfaceEaElmt = iso19107Ed2ElementsByName.get("PolyhedralSurface");
	EAElement solidEaElmt = iso19107Ed2ElementsByName.get("Solid");
	EAElement surfaceEaElmt = iso19107Ed2ElementsByName.get("Surface");
	EAElement triangulatedSurfaceEaElmt = iso19107Ed2ElementsByName.get("TriangulatedSurface");

	boolean isoElementsFound = true;

	if (collectionEaElmt == null) {
	    isoElementsFound = false;
	    result.addError(this, 100, "Collection");
	}
	if (curveEaElmt == null) {
	    isoElementsFound = false;
	    result.addError(this, 100, "Curve");
	}
	if (envelopeEaElmt == null) {
	    isoElementsFound = false;
	    result.addError(this, 100, "Envelope");
	}
	if (geometryEaElmt == null) {
	    isoElementsFound = false;
	    result.addError(this, 100, "Geometry");
	}
	if (pointEaElmt == null) {
	    isoElementsFound = false;
	    result.addError(this, 100, "Point");
	}
	if (polyhedralSurfaceEaElmt == null) {
	    isoElementsFound = false;
	    result.addError(this, 100, "PolyhedralSurface");
	}
	if (solidEaElmt == null) {
	    isoElementsFound = false;
	    result.addError(this, 100, "Solid");
	}
	if (surfaceEaElmt == null) {
	    isoElementsFound = false;
	    result.addError(this, 100, "Surface");
	}
	if (triangulatedSurfaceEaElmt == null) {
	    isoElementsFound = false;
	    result.addError(this, 100, "TriangulatedSurface");
	}

	if (!isoElementsFound) {
	    return;
	}

	Element collectionElmt = rep.GetElementByID(collectionEaElmt.getElementId());
	Element curveElmt = rep.GetElementByID(curveEaElmt.getElementId());
	Element envelopeElmt = rep.GetElementByID(envelopeEaElmt.getElementId());
	Element geometryElmt = rep.GetElementByID(geometryEaElmt.getElementId());
	Element pointElmt = rep.GetElementByID(pointEaElmt.getElementId());
	Element polyhedralSurfaceElmt = rep.GetElementByID(polyhedralSurfaceEaElmt.getElementId());
	Element solidElmt = rep.GetElementByID(solidEaElmt.getElementId());
	Element surfaceElmt = rep.GetElementByID(surfaceEaElmt.getElementId());
	Element triangulatedSurfaceElmt = rep.GetElementByID(triangulatedSurfaceEaElmt.getElementId());

	for (EAElement eaElmt : relevantElements) {

	    Element elmt = rep.GetElementByID(eaElmt.getElementId());
	    String elmtName = eaElmt.getName();

	    org.sparx.Collection<Attribute> atts = elmt.GetAttributes();
	    atts.Refresh();

	    for (Attribute att : atts) {

		String attType = att.GetType();
		String attName = att.GetName();
		String attFullName = attFullName(eaElmt, attName);

		try {

		    Element newType = null;
		    String alleConstraint = null;
		    boolean overrideAlleConstraint = false;
		    boolean deleteAlleConstraint = false;

		    if (StringUtils.equalsAnyIgnoreCase(attType, "GM_MultiPoint", "GM_MultiCurve", "GM_MultiSurface")) {

			newType = collectionElmt;

			alleConstraint = switch (attType) {
			case "GM_MultiPoint" -> {
			    yield "Attribut position.elementType darf nur \"point\" enthalten.";
			}
			case "GM_MultiCurve" -> {
			    yield "Attribut position.elementType darf nur \"curve\" enthalten.";
			}
			case "GM_MultiSurface" -> {
			    yield "Attribut position.elementType darf nur \"surface\" enthalten.";
			}
			default -> {
			    yield null;
			}
			};

		    } else if (StringUtils.equalsAnyIgnoreCase(attType, "GM_Curve", "GM_CompositeCurve",
			    "GM_SurfaceBoundary"/* , "AA_Liniengeometrie" */)) {

			newType = curveElmt;

		    } else if ("GM_Envelope".equalsIgnoreCase(attType)) {

			newType = envelopeElmt;

		    } else if (StringUtils.equalsAnyIgnoreCase(attType,
			    "GM_Object"/* , "AA_Punktgeometrie", "AA_Flaechengeometrie" */)) {

			newType = geometryElmt;

			/*
			 * alleConstraint = switch (attType) { case "AA_Punktgeometrie" -> { yield
			 * "Attribut position darf nur Werte der folgenden Typen enthalten: " +
			 * "Point oder Collection mit elementType = point."; } case
			 * "AA_Flaechengeometrie" -> { yield
			 * "Attribut position darf nur Werte der folgenden Typen enthalten: " +
			 * "PolyhedralSurface oder Collection mit elementType = surface."; } default ->
			 * { yield null; } };
			 */

			if (attName.equals("position")) {

			    overrideAlleConstraint = true;

			    if (StringUtils.equalsAnyIgnoreCase(elmtName, "AG_Flaechenobjekt", "AU_Flaechenobjekt")) {
				alleConstraint = "Attribut position darf nur Werte der folgenden Typen enthalten: "
					+ "PolyhedralSurface oder Collection mit elementType = surface.";
			    } else if (elmtName.equalsIgnoreCase("AG_Objekt")) {
				alleConstraint = "Attribut position darf nur Werte der folgenden Typen enthalten: "
					+ "Curve, Point, PolyhedralSurface "
					+ "oder Collection mit elementType = surface";
			    } else if (elmtName.equalsIgnoreCase("AU_GeometrieObjekt_3D")) {
				alleConstraint = "Attribut position darf nur Werte der folgenden Typen enthalten: "
					+ "Curve, Point, Solid, Surface, TriangulatedSurface "
					+ "oder Collection mit elementType = curve, point, surface";
			    } else if (elmtName.equals("AU_KontinuierlichesLinienobjekt")) {
				newType = curveElmt;
				deleteAlleConstraint = true;
			    } else if (elmtName.equalsIgnoreCase("AU_MehrfachFlaechenObjekt_3D")) {
				alleConstraint = "Attribut position darf nur Werte der folgenden Typen enthalten: "
					+ "Surface oder Collection mit elementType = surface";
			    } else if (elmtName.equalsIgnoreCase("AU_MehrfachLinienObjekt_3D")) {
				alleConstraint = "Attribut position darf nur Werte der folgenden Typen enthalten: "
					+ "Curve oder Collection mit elementType = curve";
			    } else if (elmtName.equalsIgnoreCase("AU_Objekt")) {
				alleConstraint = "Attribut position darf nur Werte der folgenden Typen enthalten: "
					+ "Curve, Point, PolyhedralSurface "
					+ "oder Collection mit elementType = curve, surface";
			    } else if (StringUtils.equalsAnyIgnoreCase(elmtName, "AU_Punkthaufenobjekt",
				    "AU_PunkthaufenObjekt_3D")) {
				alleConstraint = "Attribut position darf nur Werte der folgenden Typen enthalten: "
					+ "Point oder Collection mit elementType = point";
			    } else {
				overrideAlleConstraint = false;
			    }
			}

		    } else if (StringUtils.equalsAnyIgnoreCase(attType, "GM_Point", "GM_PointRef")) {

			newType = pointElmt;

		    } else if ("GM_PolyhedralSurface".equalsIgnoreCase(attType)) {

			newType = polyhedralSurfaceElmt;

		    } else if ("GM_Solid".equalsIgnoreCase(attType)) {

			newType = solidElmt;

		    } else if (StringUtils.equalsAnyIgnoreCase(attType, "GM_Surface", "GM_OrientableSurface")) {

			newType = surfaceElmt;

		    } else if ("GM_TriangulatedSurface".equalsIgnoreCase(attType)) {

			newType = triangulatedSurfaceElmt;
		    }

		    if (newType != null) {

			EAAttributeUtil.setEAType(att, newType.GetName());
			EAAttributeUtil.setEAClassifierID(att, newType.GetElementID());
			
			if(deleteAlleConstraint) {
			    
			    EAElementUtil.deleteConstraint(elmt, "Alle");

			} else if (StringUtils.isNotBlank(alleConstraint)) {

			    String alleConstraintFull = "/* " + alleConstraint + " */";

			    Optional<Constraint> conOpt = EAElementUtil.getConstraint(elmt, "Alle");

			    if (conOpt.isPresent()) {
				Constraint c = conOpt.get();
				if (overrideAlleConstraint) {
				    EAConstraintUtil.setEANotes(c, alleConstraintFull);
				} else {
				    EAConstraintUtil.appendToEANotes(c, alleConstraintFull);
				}
			    } else {
				EAElementUtil.addConstraint(elmt, "Alle", "OCL", alleConstraintFull);
			    }
			}

			MessageContext mc = result.addInfo(this, 101, newType.GetName());
			if (mc != null) {
			    mc.addDetail(this, 3, attFullName);
			}
		    }

		} catch (EAException ex) {
		    MessageContext mc = result.addError(this, 102, ex.getMessage());
		    if (mc != null) {
			mc.addDetail(this, 3, attFullName);
		    }
		}
	    }

	    atts.Refresh();
	}
    }

    private String attFullName(EAElement eaElmt, String attName) {
	return eaElmt.getFullName() + "." + attName;
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

	case 100:
	    return "Could not find required ISO 19107 Edition 2 element '$1$'. The transformation of standard cases is skipped.";
	case 101:
	    return "Switched attribute type (and classifier) to '$1$'.";
	case 102:
	    return "Exception occurred while updating attribute classifier and type. Exception message is: $1$";

	default:
	    return "(" + Iso19107Edition2Transformer.class.getName() + ") Unknown message with number: " + mnr;
	}
    }
}
