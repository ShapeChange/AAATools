/**
 * GeoInfoDok Transformations (Profile Transformer)
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
 * Bundeskanzlerplatz 2d
 * 53113 Bonn
 * Germany
 */
package de.adv_online.aaa.uml;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Johannes Echterhoff (echterhoff at interactive-instruments dot de)
 *
 */
public class StereotypeMappingInfo {

    protected String sourceStereotypeFQName = null;
    protected String targetStereotypeFQName = null;

    protected MetaType applicableMetaType;
    protected boolean isFallbackMappingForSourceMetaType;

    protected SortedMap<String, String> tvNameToSourceFQNameMap = new TreeMap<>();
    protected SortedMap<String, String> tvSourceToTargetFQNameMap = new TreeMap<>();

    public StereotypeMappingInfo(String sourceStereotypeFQName, String targetStereotypeFQName,
	    SortedMap<String, String> tvNameToSourceFQNameMap, SortedMap<String, String> tvSourceToTargetFQNameMap,
	    MetaType applicableMetaType, boolean isFallbackMappingForSourceMetaType) {

	this.sourceStereotypeFQName = sourceStereotypeFQName;
	this.targetStereotypeFQName = targetStereotypeFQName;
	this.tvNameToSourceFQNameMap = tvNameToSourceFQNameMap;
	this.tvSourceToTargetFQNameMap = tvSourceToTargetFQNameMap;
	this.applicableMetaType = applicableMetaType;
	this.isFallbackMappingForSourceMetaType = isFallbackMappingForSourceMetaType;
    }

    /**
     * @return the sourceStereotypeFQName
     */
    public String getSourceStereotypeFQName() {
	return sourceStereotypeFQName;
    }

    /**
     * @return the targetStereotypeFQName
     */
    public String getTargetStereotypeFQName() {
	return targetStereotypeFQName;
    }

    /**
     * @return the tvNameToSourceFQNameMap
     */
    public SortedMap<String, String> getTvNameToSourceFQNameMap() {
	return tvNameToSourceFQNameMap;
    }

    /**
     * @return the tvSourceToTargetFQNameMap
     */
    public SortedMap<String, String> getTvSourceToTargetFQNameMap() {
	return tvSourceToTargetFQNameMap;
    }

    /**
     * @return the applicableMetaType
     */
    public MetaType getApplicableMetaType() {
	return applicableMetaType;
    }

    /**
     * @return the isFallbackMappingForSourceMetaType
     */
    public boolean isFallbackMappingForApplicableMetaType() {
	return isFallbackMappingForSourceMetaType;
    }

}
