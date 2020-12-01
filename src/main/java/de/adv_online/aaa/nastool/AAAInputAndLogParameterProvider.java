/**
 * NAS-Tool (schema transformer)
 *
 * The class in this file implements the ShapeChange InputAndLogParameterProvider
 * interface to register additional input and log parameters.
 *
 * (c) 2009-2020 Arbeitsgemeinschaft der Vermessungsverwaltungen der 
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
package de.adv_online.aaa.nastool;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.interactive_instruments.ShapeChange.InputAndLogParameterProvider;

/**
 * @author Johannes Echterhoff (echterhoff at interactive-instruments dot de)
 *
 */
public class AAAInputAndLogParameterProvider implements InputAndLogParameterProvider {

    protected SortedSet<String> allowedInputParametersWithStaticNames = new TreeSet<>(Stream.of("transformerTargetPath")
	    .collect(Collectors.toSet()));

    protected List<Pattern> regexesForAllowedInputParametersWithDynamicNames = null;

    protected SortedSet<String> allowedLogParametersWithStaticNames = null;

    protected List<Pattern> regexesForAllowedLogParametersWithDynamicNames = null;

    @Override
    public SortedSet<String> allowedInputParametersWithStaticNames() {
	return allowedInputParametersWithStaticNames;
    }

    @Override
    public List<Pattern> regexesForAllowedInputParametersWithDynamicNames() {
	return regexesForAllowedInputParametersWithDynamicNames;
    }

    @Override
    public SortedSet<String> allowedLogParametersWithStaticNames() {
	return allowedLogParametersWithStaticNames;
    }

    @Override
    public List<Pattern> regexesForAllowedLogParametersWithDynamicNames() {
	return regexesForAllowedLogParametersWithDynamicNames;
    }

}
