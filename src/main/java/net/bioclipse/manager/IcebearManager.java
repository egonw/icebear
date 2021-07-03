/* Copyright (c) 2012,2021  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.manager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.IMolecule.Property;
import net.bioclipse.managers.CDKManager;
import net.bioclipse.managers.RDFManager;
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

	public void findInfo(IMolecule mol) throws BioclipseException {
    	ICDKMolecule cdkMol = cdk.asCDKMolecule(mol);
    	String inchi = cdkMol.getInChI(Property.USE_CACHED_OR_CALCULATED);
    	IcebearWorkload workload = new IcebearWorkload();
    	workload.addNewURI("http://rdf.openmolecules.net/?" + inchi);
    	inchi = inchi.replace("=1S/", "=1/");
    	workload.addNewURI("http://rdf.openmolecules.net/?" + inchi);
    	while (workload.hasMoreWork()) {
    		findInfoForOneURI(workload);
    	}
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
//			for (INextURIExtractor spider : spiders) {
//				for (String uri : spider.extractURIs(store, nextURI.toString())) {
//					workload.addNewURI(uri);
//				}
//			}
		} catch (Exception exception) {
			System.out.println("Error while downloading " + nextURIString + ": " + exception.getMessage());
		}
    	return store;
    }

	@Override
	public String getManagerName() {
		return "icebear";
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
