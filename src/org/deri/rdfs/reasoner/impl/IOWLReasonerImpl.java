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
package org.deri.rdfs.reasoner.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.deri.rdfs.reasoner.RDFS2DatalogTransformer;
import org.deri.rdfs.reasoner.api.IOWLReasoner;
import org.deri.rdfs.reasoner.api.ReasonerFacade;
import org.deri.rdfs.reasoner.api.factory.RDFSReasonerFactory.BuiltInReasoner;
import org.deri.rdfs.reasoner.api.terms.Rule;
import org.deri.rdfs.reasoner.builtin.iris.IrisFacade;
import org.deri.rdfs.reasoner.exception.ExternalToolException;
import org.deri.rdfs.reasoner.exception.NonStandardRDFSUseException;
import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Variable;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.wsml.reasoner.ConjunctiveQuery;

/**
 * A prototypical implementation of an iOWL reasoner.
 * 
 * <pre>
 *  Created on April 6th, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/impl/IOWLReasonerImpl.java,v $,
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.4 $ $Date: 2007-08-24 14:05:41 $
 */
public class IOWLReasonerImpl extends ReasonerImpl implements IOWLReasoner {

	private ReasonerFacade builtInFacade = null;
	
	private String defaultNS = "";
	
	public IOWLReasonerImpl(BuiltInReasoner builtInType) {
		switch (builtInType) {
		case IRIS:
			builtInFacade = new IrisFacade();
			break;
		default:
			throw new UnsupportedOperationException("Reasoning with "
					+ builtInType.toString() + " is not supported!");
		}
	}

	public void registerOntologies(Map<String, Graph> ontologies) 
			throws NonStandardRDFSUseException {
		Set<Entry<String, Graph>> entrySet = ontologies.entrySet();
		for(Entry<String, Graph> entry : entrySet) {
			Graph ontology = entry.getValue();
			defaultNS = entry.getKey();
			
			// Convert the ontology to Datalog Program:
			Set<Rule> kb = new HashSet<Rule>();
			kb.addAll(convertOntology(ontology, defaultNS));
			
			// Register the program at the built-in reasoner:
			try {
				builtInFacade.register(defaultNS, kb);
			} catch (ExternalToolException e) {
				e.printStackTrace();
                throw new IllegalArgumentException(
                        "This set of ontologies could not be registered at the " +
                        "built-in reasoner", e);	
			}
		}
	}

	public void registerOntology(Graph ontology, String ontologyURI) 
			throws NonStandardRDFSUseException {
		Map<String, Graph> ontologies = new HashMap<String, Graph>();
		ontologies.put(ontologyURI, ontology);
		registerOntologies(ontologies);
	}
	
	public void deRegisterOntology(Graph ontology, String ontologyURI) {
		Map<String, Graph> ontologies = new HashMap<String, Graph>();
        ontologies.put(ontologyURI, ontology);
        deRegisterOntology(ontologies);
	}

	public void deRegisterOntology(Map<String, Graph> ontologies) {
		Set<Entry<String, Graph>> entrySet = ontologies.entrySet();
		for (Entry<String, Graph> entry : entrySet) {
			String ontologyURI = entry.getKey();
            try {
                builtInFacade.deregister(ontologyURI);
            } catch (ExternalToolException e) {
                e.printStackTrace();
                throw new IllegalArgumentException(
                        "This set of ontologies could not be deregistered at the " +
                        "built-in reasoner", e);
            }
        }
	}
	
	/*
	 * Method to convert an RDF ontology to Datalog rules.
	 * 
	 * @param ontology Graph to be converted
	 * @return rules Set of F-Logic rules
	 */
	protected Set<Rule> convertOntology(Graph ontology, String defaultNS) 
			throws NonStandardRDFSUseException {
		Set<Rule> rules = super.convertOntology(ontology, defaultNS); 
		RDFS2DatalogTransformer rdfs2datalog = new RDFS2DatalogTransformer(defaultNS);
        rules.add(rdfs2datalog.generateAuxiliaryRDFRule());
        rules.addAll(rdfs2datalog.generateAuxiliaryRDFSRules());
        rules.addAll(rdfs2datalog.generateAuxiliaryERDFSRules(ontology));
        rules.addAll(rdfs2datalog.generateAuxiliaryIOWLRules(ontology, rules));
//         System.out.println("f-logic rules:");
//         for (Rule r : rules)
//        	 System.out.println(r.toString());
//         System.out.println("-*");
		return rules;
	}

	public String getDefaultNS(Graph ontology) {
		return defaultNS;
	}
	
	public boolean entails(Graph ontology, Statement expression) {
		throw new UnsupportedOperationException();
	}

	public boolean entails(Graph ontology, Set<Statement> expressions) {
		throw new UnsupportedOperationException();
	}

	public boolean executeGroundQuery(Graph ontology, LogicalExpression query) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	public Set<Map<Variable, Term>> executeQuery(Graph ontology, 
			LogicalExpression query) throws ExternalToolException {
		Set<Map<Variable, Term>> result = new HashSet<Map<Variable, Term>>();
		Set<org.wsml.reasoner.ConjunctiveQuery> set = convertQuery(query);
		for (ConjunctiveQuery datalogQuery : set) {
            result.addAll(builtInFacade.evaluate(datalogQuery, ""));
            
        }
        return result;
	}

}
/*
 * $Log: IOWLReasonerImpl.java,v $
 * Revision 1.4  2007-08-24 14:05:41  nathalie
 * *** empty log message ***
 *
 * Revision 1.3  2007-08-23 09:09:01  nathalie
 * added non standard rdfs use exception
 *
 * Revision 1.2  2007-08-08 16:23:56  nathalie
 * some documentation/maintenance addings
 *
 * Revision 1.1  2007/05/24 07:02:20  nathalie
 * created rdfs reasoner
 *
 * 
 */
