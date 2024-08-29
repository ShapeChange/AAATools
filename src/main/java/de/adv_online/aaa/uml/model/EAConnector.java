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

import org.apache.commons.lang3.StringUtils;
import org.sparx.Connector;
import org.sparx.ConnectorEnd;
import org.sparx.Repository;

import de.interactive_instruments.shapechange.ea.util.EAConnectorUtil;

public class EAConnector {

    public static String connectorInfo(Connector conn, Repository rep) {

	String sourceElementName = rep.GetElementByID(conn.GetClientID()).GetName();
	String targetElementName = rep.GetElementByID(conn.GetSupplierID()).GetName();

	String connMetaType = StringUtils.defaultIfBlank(conn.GetMetaType(), "NA");
	String connType = StringUtils.defaultIfBlank(conn.GetType(), "NA");
	String connSubtype = StringUtils.defaultIfBlank(conn.GetSubtype(), "NA");

	String connectorInfo = "(";

	if (!connMetaType.equals(connType)) {
	    connectorInfo += "meta type: " + connMetaType + ", ";
	}

	connectorInfo += "type: " + connType;

	if (!connSubtype.equals("NA")) {
	    connectorInfo += ", subtype: " + connSubtype;
	}

	connectorInfo += ") " + sourceElementName + "---" + targetElementName;

	return connectorInfo;
    }

    /**
     * NOTE: connector end name is ignored!
     * 
     * @param ce
     * @param con
     * @return <code>true</code>, if the connector end is navigable or if
     *         navigability is unspecified; else <code>false</code>
     */
    public static boolean isNavigable(ConnectorEnd ce, Connector con) {

	boolean nav = ce.GetIsNavigable();

	/*
	 * If not explicitly set, also accept unspecified navigability, if present in
	 * both directions.
	 */
	if (!nav) {
	    nav = EAConnectorUtil.navigability(con) == 0;
	}

	return nav;
    }
}
