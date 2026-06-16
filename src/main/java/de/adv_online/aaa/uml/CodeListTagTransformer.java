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
 * Bundeskanzlerplatz 2d
 * 53113 Bonn
 * Germany
 */

package de.adv_online.aaa.uml;

import java.io.File;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sparx.Element;
import org.sparx.Repository;

import de.interactive_instruments.shapechange.core.MessageSource;
import de.interactive_instruments.shapechange.core.Options;
import de.interactive_instruments.shapechange.core.ShapeChangeAbortException;
import de.interactive_instruments.shapechange.core.ShapeChangeResult;
import de.interactive_instruments.shapechange.core.model.Transformer;
import de.interactive_instruments.shapechange.ea.util.EAElementUtil;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EAElement;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EAPackage;
import de.interactive_instruments.shapechange.ea.util.modelhelper.EARepository;

/**
 * @author Johannes Echterhoff (echterhoff at interactive-instruments dot de)
 *
 */
public class CodeListTagTransformer implements Transformer, MessageSource {

    public static final String GEOINFODOK_PKG_FULL_NAME = "Model::GeoInfoDok";

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

	    EAPackage gidSchemaPkg = eaRepo.lookupPackage(GEOINFODOK_PKG_FULL_NAME).get();

	    java.util.Collection<EAElement> gidDataTypeElements = eaRepo.elementsAll(gidSchemaPkg).values().stream()
		    .filter(e -> "datatype".equalsIgnoreCase(e.getMetaType())).collect(Collectors.toList());

	    for (EAElement gidElmt : gidDataTypeElements) {

		Element elmt = rep.GetElementByID(gidElmt.getElementId());
		String stList = elmt.GetStereotypeList();

		if (StringUtils.equalsAnyIgnoreCase("gid_codeset", stList)) {
		    String codeListTV = "https://registry.gdi-de.org/codelist/de.adv-online.gid/" + gidElmt.getName();
		    EAElementUtil.updateTaggedValue(elmt, "codeList", codeListTV, false);
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}
    }

    @Override
    public String message(int mnr) {

	switch (mnr) {

	case 0:
	    return "Context: class '$1$'";
	case 1:
	    return "Context: property '$1$'";

	case 100:
	    return "";

	default:
	    return "(" + CodeListTagTransformer.class.getName() + ") Unknown message with number: " + mnr;
	}
    }
}
