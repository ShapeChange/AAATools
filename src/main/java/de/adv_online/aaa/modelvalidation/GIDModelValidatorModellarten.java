/**
 * GeoInfoDok Model Validators (Modellarten)
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
package de.adv_online.aaa.modelvalidation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import de.interactive_instruments.shapechange.core.Options;
import de.interactive_instruments.shapechange.core.ProcessRuleSet;
import de.interactive_instruments.shapechange.core.ShapeChangeAbortException;
import de.interactive_instruments.shapechange.core.ShapeChangeResult;
import de.interactive_instruments.shapechange.core.ValidatorConfiguration;
import de.interactive_instruments.shapechange.core.model.ClassInfo;
import de.interactive_instruments.shapechange.core.model.Model;
import de.interactive_instruments.shapechange.core.model.PropertyInfo;
import de.interactive_instruments.shapechange.core.modelvalidation.AbstractModelValidator;

/**
 * Checks model requirements that originate from the General Feature Model.
 * 
 * @author Johannes Echterhoff (echterhoff at interactive-instruments dot de)
 *
 */
public class GIDModelValidatorModellarten extends AbstractModelValidator {

    private static final String MAT_ALL = "ALL";

    @Override
    public boolean isValid(Model model, ValidatorConfiguration validatorConfig) throws ShapeChangeAbortException {

	boolean modelIsValid = true;

	Map<String, ProcessRuleSet> ruleSets = validatorConfig.getRuleSets();

	// get the set of all rules defined for the validator
	Set<String> rules = new HashSet<String>();
	if (!ruleSets.isEmpty()) {
	    for (ProcessRuleSet ruleSet : ruleSets.values()) {
		if (ruleSet.getAdditionalRules() != null) {
		    rules.addAll(ruleSet.getAdditionalRules());
		}
	    }
	}

	/*
	 * because there are no mandatory - in other words default - rules for this
	 * validator simply return the result if no rules are defined in the rule sets
	 * (which the schema allows)
	 */
	if (rules.isEmpty())
	    return modelIsValid;

	// apply pre-processing (nothing to do right now)

	// execute rules

	ShapeChangeResult scr = model.result();

	if (rules.contains(GIDModelValidatorModellartenConstants.RULE_GID_MODELLART_FOR_ENUM_VALUED_PROPERTIES)) {
	    scr.addProcessFlowInfo(null, 20103,
		    GIDModelValidatorModellartenConstants.RULE_GID_MODELLART_FOR_ENUM_VALUED_PROPERTIES);
	    modelIsValid = modelIsValid & gidModellartForEnumValuedProperties(model, validatorConfig);
	}

	// TODO weitere Prüfung: die Attribute einer Klasse dürfen keine Modellarten
	// haben die die Klasse nicht auch hat.
// Achtung: wenn keine Modellart angegeben ist gilt das Modellelement für alle Modellarten, bzw. bei Attributen für alle Modellarten der Klasse

	// further validation tasks to be added as needed in the future

	return modelIsValid;
    }

    private boolean gidModellartForEnumValuedProperties(Model model, ValidatorConfiguration validatorConfig) {

	ShapeChangeResult scr = model.result();

	boolean result = true;

	/*
	 * For all enumerations from the schemas selected for processing, compute the
	 * set of properties that make use of them.
	 */
	SortedMap<ClassInfo, SortedSet<PropertyInfo>> enumValuedPropertiesByTheEnum = new TreeMap<>();

	for (ClassInfo ci : model.selectedSchemaClasses()) {
	    if (ci.category() == Options.ENUMERATION) {
		enumValuedPropertiesByTheEnum.put(ci, new TreeSet<>());
	    }
	}

	for (PropertyInfo pi : model.selectedSchemaProperties()) {
	    ClassInfo typeCi = pi.typeClass();
	    if (typeCi != null && enumValuedPropertiesByTheEnum.containsKey(typeCi)) {
		SortedSet<PropertyInfo> pis = enumValuedPropertiesByTheEnum.get(typeCi);
		pis.add(pi);
	    }
	}

	for (ClassInfo enumCi : enumValuedPropertiesByTheEnum.keySet()) {

	    SortedSet<PropertyInfo> pis = enumValuedPropertiesByTheEnum.get(enumCi);

	    if (enumCi.properties().isEmpty() || pis.isEmpty()) {
		continue;
	    }

	    /*
	     * Determine the set of MAT (tagged value AAA:Modellart) that the properties in
	     * pis have (if no value is set, use it from the owning class - if no value is
	     * set there, it means 'all' MAT values). Then also determine the MAT for the
	     * enums of the enumeration. Diff the sets to identify the MAT values that none
	     * of the properties have. For failure cases, log the properties that make use
	     * of the enumeration.
	     */

	    Set<String> pisModellarten = new HashSet<>();

	    for (PropertyInfo pi : pis) {
		pisModellarten.addAll(determineModellarten(pi));
	    }

	    if (pisModellarten.contains(MAT_ALL)) {
		continue;
	    }

	    Set<String> enumsModellarten = new HashSet<>();

	    for (PropertyInfo enumPi : enumCi.properties().values()) {
		enumsModellarten.addAll(determineModellarten(enumPi));
	    }

	    // For enums, ignore MAT_ALL, since it does not matter for this check
	    enumsModellarten.remove(MAT_ALL);

	    /*
	     * Compute MAT values from enums that are not present in the properties.
	     */
	    enumsModellarten.removeAll(pisModellarten);

	    if (!enumsModellarten.isEmpty()) {

		result = false;

		SortedSet<String> pisInfos = new TreeSet<>();
		for (PropertyInfo pi : pis) {
		    pisInfos.add(pi.inClass().name() + "." + pi.name());
		}

		this.report(enumCi, this, 102, enumCi.name(), StringUtils.join(pisInfos, ", "),
			StringUtils.join(enumsModellarten, ", "), validatorConfig.getValidationMode());
	    }
	}

//	/*
//	 * First approach
//	 */
//
//	SortedSet<? extends PackageInfo> selSchemas = model.selectedSchemas();
//
//	for (PackageInfo schema : selSchemas.stream().sorted(Comparator.comparing(PackageInfo::name)).toList()) {
//
//	    scr.addInfo(this, 101, schema.name());
//
//	    for (ClassInfo ci : model.classes(schema)) {
//
//		for (PropertyInfo pi : ci.properties().values()) {
//
//		    if (pi.categoryOfValue() == Options.ENUMERATION) {
//
//			ClassInfo typeCi = pi.typeClass();
//
//			if (typeCi != null && model.isInSelectedSchemas(typeCi) && !typeCi.properties().isEmpty()) {
//
//			    SortedSet<String> gidModellartenVonWertearten = new TreeSet<>();
//
//			    for (PropertyInfo tPi : typeCi.properties().values()) {
//
//				String enumGidModellarten = tPi
//					.taggedValue(GIDModelValidatorModellartenConstants.TV_GID_MODELLART);
//
//				if (StringUtils.isNotBlank(enumGidModellarten)) {
//				    gidModellartenVonWertearten
//					    .addAll(Arrays.asList(StringUtils.split(enumGidModellarten, ", ")));
//				}
//			    }
//
//			    if (!gidModellartenVonWertearten.isEmpty()) {
//
//				String attidModellarten = pi
//					.taggedValue(GIDModelValidatorModellartenConstants.TV_GID_MODELLART);
//
//				SortedSet<String> gidModellartenVonAttributart = new TreeSet<>();
//
//				if (StringUtils.isNotBlank(attidModellarten)) {
//				    gidModellartenVonAttributart
//					    .addAll(Arrays.asList(StringUtils.split(attidModellarten, ", ")));
//				}
//
//				/*
//				 * Compute GID:Modellart values from enums that are not present in the
//				 * attribute.
//				 */
//				gidModellartenVonWertearten.removeAll(gidModellartenVonAttributart);
//
//				if (!gidModellartenVonWertearten.isEmpty()) {
//				    this.report(pi, this, 100, ci.name(), pi.name(),
//					    StringUtils.join(gidModellartenVonWertearten, ", "),
//					    validatorConfig.getValidationMode());
//				    result = false;
//				}
//			    }
//			}
//		    }
//		}
//	    }
//	}

	return result;
    }

    private SortedSet<String> determineModellarten(PropertyInfo pi) {

	SortedSet<String> gidModellarten = new TreeSet<>();

	String tvPi = pi.taggedValue(GIDModelValidatorModellartenConstants.TV_GID_MODELLART);

	if (StringUtils.isNotBlank(tvPi)) {
	    gidModellarten.addAll(Arrays.asList(StringUtils.split(tvPi, ", ")));
	} else {
	    // Determine from owning class and all its subtypes
	    SortedSet<ClassInfo> typeAndAllSubtypesInCompleteHierarchy = new TreeSet<>();
	    typeAndAllSubtypesInCompleteHierarchy.add(pi.inClass());
	    typeAndAllSubtypesInCompleteHierarchy.addAll(pi.inClass().subtypesInCompleteHierarchy());

	    for (ClassInfo ci : typeAndAllSubtypesInCompleteHierarchy) {
		String tvOwner = ci.taggedValue(GIDModelValidatorModellartenConstants.TV_GID_MODELLART);
		if (StringUtils.isNotBlank(tvOwner)) {
		    gidModellarten.addAll(Arrays.asList(StringUtils.split(tvOwner, ", ")));
		} else {

		    gidModellarten.add(MAT_ALL);
		}
	    }
	}

	return gidModellarten;
    }

    @Override
    public String message(int mnr) {

	return switch (mnr) {

	case 0 -> "Context: class '$1$'";
	case 1 -> "Context: property '$1$'";
	case 2 -> "Context: $1$";

	case 100 -> "Property '$1$.$2$' does not have the following values for tag '"
		+ GIDModelValidatorModellartenConstants.TV_GID_MODELLART
		+ "' that appear in the enums of its value type: $3$";
	case 101 -> "------ Now checking schema package '$1$' ------";
	case 102 -> "Enumeration '$1$': the properties that use it ($2$) do not have the following values for tag '"
		+ GIDModelValidatorModellartenConstants.TV_GID_MODELLART
		+ "' that appear in the enums of the enumeration: $3$";

	default -> "(" + this.getClass().getName() + ") Unknown message with number: " + mnr;
	};
    }
}
