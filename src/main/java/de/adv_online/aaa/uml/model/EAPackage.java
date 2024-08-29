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

public class EAPackage extends AbstractEAModelElement {

    private int pkgElementId;
    private int pkgId;

    public EAPackage(String name, String fullName, int pkgElementId, int pkgId) {
	super(name, fullName);
	this.pkgElementId = pkgElementId;
	this.pkgId = pkgId;
    }

    public int getPkgElementId() {
	return pkgElementId;
    }

    public int getPkgId() {
	return pkgId;
    }
}
