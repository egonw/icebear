/* Copyright (c) 2012,2021  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.icebear.extractors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.bioclipse.core.domain.StringMatrix;
import net.bioclipse.icebear.business.Entry;
import net.bioclipse.managers.RDFManager;
import net.bioclipse.rdf.business.IRDFStore;

public class AbstractExtractor {

	protected RDFManager rdf = new RDFManager(".");

	protected List<String> getPredicate(IRDFStore store, String resource, String predicate) {
		try {
			return rdf.getForPredicate(store, resource, predicate);
		} catch (Throwable exception) {
			exception.printStackTrace();
		};
		return Collections.emptyList();
	}

	protected List<String> allOwlSameAs(IRDFStore store, String resource) {
		try {
			return rdf.allOwlSameAs(store, resource);
		} catch (Throwable exception) {
			exception.printStackTrace();
		}
		return Collections.emptyList();
	}

	protected List<String> allOwlEquivalentClass(IRDFStore store, String resource) {
		try {
			return rdf.allOwlEquivalentClass(store, resource);
		} catch (Throwable exception) {
			exception.printStackTrace();
		}
		return Collections.emptyList();
	}

	protected List<String> allSkosExactmatch(IRDFStore store, String resource) {
		try {
			return rdf.getForPredicate(
				store, resource, "http://www.w3.org/2004/02/skos/core#exactMatch"
			);
		} catch (Throwable exception) {
			exception.printStackTrace();
		}
		return Collections.emptyList();
	}

	protected StringMatrix sparql(IRDFStore store, String query) {
		try {
			return rdf.sparql(store, query);
		} catch (Throwable exception) {
			exception.printStackTrace();
		};
		return new StringMatrix();
	}

	protected void addPredicateToMap(IRDFStore store, Map<String, String> resultMap, String label, String resource, String predicate) {
		List<String> props = getPredicate(store, resource, predicate);
		if (props.size() > 0) {
			resultMap.put(label, props.get(0));
		}
	}

	protected List<Entry> extractEntries(IRDFStore store, String label, String resource, String predicate) {
		List<Entry> entries = new ArrayList<Entry>();
		List<String> props = getPredicate(store, resource, predicate);
		for (String prop : props) {
			entries.add(new Entry(resource, label, predicate, prop));
		}
		return entries;
	}

	protected String stripDataType(String id) {
		if (id.contains("^^"))
			return id.substring(0, id.indexOf("^^"));
		return id;
	}
}
