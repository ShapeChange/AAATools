/**
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

package de.adv_online.aaa.uml.model;

import org.sparx.Element;

public class EAElement extends AbstractEAModelElement {

    private int elementId;
    private String metaType;

    public EAElement(Element elmt, String pathToElement) {

	super(elmt.GetName(), pathToElement + elmt.GetName());
	this.elementId = elmt.GetElementID();
	this.metaType = elmt.GetMetaType();
    }

    /**
     * @return the elementId
     */
    public int getElementId() {
	return elementId;
    }

    public String getMetaType() {
	return metaType;
    }

}
