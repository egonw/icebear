/* Copyright (c) 2012,2021  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.IMolecule.Property;
import net.bioclipse.core.domain.IStringMatrix;
import net.bioclipse.icebear.business.Entry;
import net.bioclipse.icebear.extractors.INextURIExtractor;
import net.bioclipse.icebear.extractors.IPropertyExtractor;
import net.bioclipse.icebear.extractors.links.OwlEquivalentClassExtractor;
import net.bioclipse.icebear.extractors.links.OwlSameAsExtractor;
import net.bioclipse.icebear.extractors.links.SkosExactMatchExtractor;
import net.bioclipse.rdf.business.IRDFStore;

/**
 * Manager for crawling chemical information on the internet. 
 */
public class IcebearManager implements IBactingManager {

	private String workspaceRoot;
	private CDKManager cdk;
	private RDFManager rdf;

	Map<String,String> extraHeaders = new HashMap<String, String>() {
		private static final long serialVersionUID = 2825983879781792266L;
	{
	  put("Content-Type", "application/rdf+xml");
	  put("Accept", "application/rdf+xml"); // Both Accept and Content-Type are needed for PubChem 
	}};

	private List<IPropertyExtractor> extractors = new ArrayList<IPropertyExtractor>() {
		private static final long serialVersionUID = 2825983879781792266L; {
	}};
	private List<INextURIExtractor> spiders = new ArrayList<INextURIExtractor>() {
		private static final long serialVersionUID = 7089854109617759948L; {
		add(new OwlSameAsExtractor());
		add(new OwlEquivalentClassExtractor());
		add(new SkosExactMatchExtractor());
	}};

	/**
     * Creates a new IcebearManager.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public IcebearManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		this.cdk = new CDKManager(this.workspaceRoot);
		this.rdf = new RDFManager(this.workspaceRoot);
	}

	public List<IRDFStore> findInfo(IMolecule mol) throws BioclipseException {
    	ICDKMolecule cdkMol = cdk.asCDKMolecule(mol);
    	String inchikey = cdkMol.getInChIKey(Property.USE_CACHED_OR_CALCULATED);
    	IcebearWorkload workload = new IcebearWorkload();
    	String hasMoleculeByInChI =
   			"PREFIX wdt: <http://www.wikidata.org/prop/direct/>"
				+ "SELECT ?compound WHERE {"
				+ "  ?compound wdt:P235  \"" + inchikey + "\" ."
				+ "}";
        IStringMatrix results = rdf.sparqlRemote(
        	"https://query.wikidata.org/sparql", hasMoleculeByInChI
        );
        if (results.getRowCount() == 0)
        	throw new BioclipseException("No molecule in Wikidata with the InChIKey: " + inchikey);
        if (results.getRowCount() > 1)
        	throw new BioclipseException("Too many molecules in Wikidata with the InChIKey: " + inchikey);
        String entityID = results.get(1, "compound");
        if (entityID == null || entityID.length() == 0)
        	throw new BioclipseException("No Wikidata entity found for the molecule with the InChIKey: " + inchikey);
    	workload.addNewURI(entityID);

    	List<IRDFStore> stores = new ArrayList<IRDFStore>();
    	while (workload.hasMoreWork()) {
    		stores.add(findInfoForOneURI(workload));
    	}
    	return stores;
	}

    public List<Entry> getProperties(IRDFStore store) throws BioclipseException, CoreException {
    	String resource = rdf.getForPredicate(store,
    		"http://www.bioclipse.org/PrimaryObject",
			"http://www.bioclipse.org/hasURI").get(0);
    	
		List<Entry> props = new ArrayList<Entry>();
		for (IPropertyExtractor extractor : extractors) {
			props.addAll(extractor.extractProperties(store, resource));
		}
		return props;
    }

    private IRDFStore findInfoForOneURI(IcebearWorkload workload) {
    	IRDFStore store = rdf.createInMemoryStore();
    	URI nextURI = workload.getNextURI();
		String nextURIString = nextURI.toString();
    	try {
			rdf.addObjectProperty(store,
				"http://www.bioclipse.org/PrimaryObject", "http://www.bioclipse.org/hasURI",
				nextURI.toString()
			);
			rdf.importURL(store, nextURIString, extraHeaders);
			System.out.println(rdf.asTurtle(store));
			for (INextURIExtractor spider : spiders) {
				for (String uri : spider.extractURIs(store, nextURI.toString())) {
					workload.addNewURI(uri);
				}
			}
		} catch (Exception exception) {
			System.out.println("Error while downloading " + nextURIString + ": " + exception.getMessage());
		}
    	return store;
    }

	@Override
	public String getManagerName() {
		return "isbjørn";
	}

	public List<String> doi() {
		List<String> dois = new ArrayList<String>();
		return dois;
	}

	class IcebearWorkload {
		
		Set<URI> todo = new HashSet<URI>();
		Set<URI> done = new HashSet<URI>();

		public boolean hasMoreWork() {
			System.out.println("work left todo: " + todo.size());
			return todo.size() != 0;
		}

		public URI getNextURI() {
			URI nextURI = todo.iterator().next();
			System.out.println("next URI: " + nextURI);
			todo.remove(nextURI);
			done.add(nextURI);
			return nextURI;
		}

		/**
		 * Returns false when the URI was already processed or is already scheduled.
		 */
		public boolean addNewURI(String newURI) {
			System.out.println("Adding URI: " + newURI);
			try {
				URI uri = new URI(newURI);
				if (done.contains(uri) || todo.contains(uri)) {
					System.out.println("Already got it...");
					return false;
				}

				todo.add(uri);
				return true;
			} catch (URISyntaxException e) {
				System.out.println("Failed to add the new URI: " + e.getMessage());
				return false;
			}
		}
	}
}
