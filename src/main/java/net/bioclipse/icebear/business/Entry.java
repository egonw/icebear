/* Copyright (c) 2012       Ola Spjuth <ola.spjuth@farmbio.uu.se>
 *               2012,2021  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.icebear.business;

/**
 * Class to hold the molecular properties.
 */
public class Entry {

	// the next two fields are for provenance

	/** A URI for the resource for which this property applies. */
	public String resource;

	public String predicate;
	public String predicateLabel;
	public String object;
	
	public Entry(String resource, String predicateLabel, String predicateURI, String object) {
		super();
		this.resource = resource;
		this.predicate = predicateURI;
		this.predicateLabel = predicateLabel;
		this.object = object;
	}

	public String toString() {
		return "[" + predicateLabel + ": " + object + "]";
	}

	public Object getEditableValue() {
		return this;
	}

}
