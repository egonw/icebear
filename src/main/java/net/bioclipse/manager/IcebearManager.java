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

import java.util.ArrayList;
import java.util.List;

import io.github.egonw.bacting.IBactingManager;

/**
 * Manager for crawling chemical information on the internet. 
 */
public class IcebearManager implements IBactingManager {

	private String workspaceRoot;

	/**
     * Creates a new IcebearManager.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public IcebearManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

	@Override
	public String getManagerName() {
		return "icebear";
	}

	public List<String> doi() {
		List<String> dois = new ArrayList<String>();
		return dois;
	}
}
