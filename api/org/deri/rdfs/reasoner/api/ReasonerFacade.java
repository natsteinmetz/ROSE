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

import java.util.Set;

import org.deri.rdfs.reasoner.api.terms.Rule;
import org.deri.rdfs.reasoner.exception.ExternalToolException;

/**
 * This interface represents a facade to various datalog engines that allow to 
 * perform query answering, e.g. KAON2, MINS, IRIS.
 * 
 * For each such system a specific facade must be implemented to integrate the
 * component into the system.
 * 
 * <pre>
 *  Created on April 6th, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/api/org/deri/rdfs/reasoner/api/ReasonerFacade.java,v $,
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:21 $
 */
public interface ReasonerFacade {
	/**
     * Registers the Datalog knowledge base representing the ontology at the
     * external reasoner
     * 
     * @param ontologyURI
     *            the orginal logical ontology URI
     * @param kb
     *            the knowledge base describing the ontology
     * @throws ExternalToolException
     *             if some exception happens during ontology registration
     */
    public void register(String ontologyURI, Set<Rule> kb) 
    		throws ExternalToolException;

    /**
     * Removes the ontology from the external reasoner
     * 
     * @param ontologyURI
     *            the original logical ontology URI
     * @throws ExternalToolException
     *             if exception happens during ontology removal
     */
    public void deregister(String ontologyURI) throws ExternalToolException;

    /**
     * Evaluates a given query on a particular external tool.
     * 
     * @param q
     *            the query to be evaluated.
     * @return a set of variable bindings (map with variables as keys, and the
     *         bindings: IRIs or DataValues as values)
     * @throws ExternalToolException
     *             in case that some error occurs during the execution of the
     *             query
     */
    public Set evaluate(org.wsml.reasoner.ConjunctiveQuery q, String ontologyURI) 
    		throws ExternalToolException;

}
/*
 * $Log: ReasonerFacade.java,v $
 * Revision 1.1  2007-05-24 07:02:21  nathalie
 * created rdfs reasoner
 *
 * 
 */
