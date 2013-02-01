/*
 * RDFS Reasoner Implementation.
 *
 * Copyright (c) 2007, University of Innsbruck, Austria.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */
package org.deri.rdfs.reasoner.api;

import java.util.Map;
import java.util.Set;

import org.deri.rdfs.reasoner.exception.ExternalToolException;
import org.deri.rdfs.reasoner.exception.NonStandardRDFSUseException;
import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Variable;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;

/**
 * An interface for invoking an RDFS reasoner with a particular reasoning task.
 * 
 * <pre>
 *  Created on April 6th, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/api/org/deri/rdfs/reasoner/api/Reasoner.java,v $,
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.2 $ $Date: 2007-08-23 09:09:01 $
 */
public interface Reasoner {

	public void registerOntologies(Map<String, Graph> ontologies) 
			throws ExternalToolException, NonStandardRDFSUseException;

	/**
	 * Registers the ontology. If the ontology is already registered, updates
	 * the ontology content.
	 * 
	 * @param ontology
	 * @throws NonStandardRDFSUseException 
	 * @throws Exception 
	 */
	public void registerOntology(Graph ontology, String ontologyURI) 
			throws ExternalToolException, NonStandardRDFSUseException;

	/**
	* Deregisters the ontology. Any further request using this ontologyID will
	* result in an exception.
	* 
	* @param ontologyID
	*/
	public void deRegisterOntology(Graph ontology, String ontologyURI);

	public void deRegisterOntology(Map<String, Graph> ontologies);

	public boolean executeGroundQuery(Graph ontology, LogicalExpression query);

	/**
	 * Method to execute a conjunctive query over the given ontology
	 * 
	 * @param ontology
	 * @param query
	 * @return a set with a variable binding
	 * @throws ExternalToolException
	 */
	public Set<Map<Variable, Term>> executeQuery(Graph ontology,
			LogicalExpression query) throws ExternalToolException;

	public boolean entails(Graph ontology, Statement expression);
	
	public boolean entails(Graph ontology, Set<Statement> expressions);
	
	/**
	 * Method to get the default namespace of the given ontology.
	 * 
	 * @param ontology
	 * @return default namespace as string
	 */
	public String getDefaultNS(Graph ontology);
	
}
/*
 * $Log: Reasoner.java,v $
 * Revision 1.2  2007-08-23 09:09:01  nathalie
 * added non standard rdfs use exception
 *
 * Revision 1.1  2007/05/24 07:02:21  nathalie
 * created rdfs reasoner
 *
 * 
 */