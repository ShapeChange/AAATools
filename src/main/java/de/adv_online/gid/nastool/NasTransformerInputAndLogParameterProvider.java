/**
 * NAS-Tool (schema transformer)
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
package de.adv_online.gid.nastool;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.interactive_instruments.shapechange.core.InputAndLogParameterProvider;

/**
 * @author Johannes Echterhoff (echterhoff at interactive-instruments dot de)
 *
 */
public class NasTransformerInputAndLogParameterProvider implements InputAndLogParameterProvider {

    protected SortedSet<String> allowedInputParametersWithStaticNames = new TreeSet<>(
	    Stream.of(NasTransformer_7_GID.PARAM_IMPLEMENTATION_SCHEMAS).collect(Collectors.toSet()));

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
