/* Copyright (c) 2012,2021  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.icebear.extractors.links;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.icebear.extractors.AbstractExtractor;
import net.bioclipse.icebear.extractors.INextURIExtractor;
import net.bioclipse.rdf.business.IRDFStore;

public class SkosExactMatchExtractor extends AbstractExtractor implements INextURIExtractor {

	@Override
	public List<String> extractURIs(IRDFStore store, String resource) {
		List<String> sameResources = allSkosExactmatch(store, resource);
		List<String> properResources = new ArrayList<String>();
		for (String sameResource : sameResources) {
			properResources.add(sameResource);
		}
		return properResources;
	}


}
