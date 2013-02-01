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
import java.util.Vector;
import java.util.Map.Entry;

import org.deri.rdfs.reasoner.RDFS2DatalogTransformer;
import org.deri.rdfs.reasoner.api.ERDFSReasoner;
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
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.vocabulary.RDF;
import org.openrdf.vocabulary.RDFS;
import org.wsml.reasoner.ConjunctiveQuery;

/**
 * A prototypical implementation of an eRDFS reasoner. Doing 
 * extensional RDFS entailment is only possible if there is no 
 * non-standard use of the RDFS vocabulary in the given ontology. 
 * 
 * Non-standard use of the RDFS vocabulary corresponds to using the 
 * vocabulary in locations where it has not been intendeded 
 * (e.g. 	&lt;type, subPropertyOf, a	&gt;, an occurrence of 
 * <tt>Class<tt> in an RDFS graph, ...).
 * 
 * To avoid the creation of such non-standard use of the RDFS 
 * vocabulary during parsing of an RDF file, it is recommended to use 
 * the following RDF/XML writing style: <br /><br />
 * &lt;rdf:Description rdf:about="&ex;Man"&gt; <br />
 * &lt;rdfs:subClassOf rdf:resource="&ex;Person"/&gt; <br />
 * &lt;/rdf:Description&gt; <br /><br />
 * rather than: <br /><br />
 * &lt;rdfs:Class rdf:about="&ex;Man"&gt; <br />
 * &lt;rdfs:subClassOf rdf:resource="&ex;Person"/&gt; <br />
 * &lt;/rdfs:Class&gt; <br />
 * 
 * <pre>
 *  Created on April 6th, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/impl/ERDFSReasonerImpl.java,v $,
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.4 $ $Date: 2007-08-24 14:06:04 $
 */
public class ERDFSReasonerImpl extends ReasonerImpl implements ERDFSReasoner {

	private ReasonerFacade builtInFacade = null;
	
	private String defaultNS = "";
	
	private Vector<String> usages = new Vector<String>();
	
	public ERDFSReasonerImpl(BuiltInReasoner builtInType) {
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

	/**
	 * Method to convert an RDF ontology to Datalog rules.
	 * 
	 * @param ontology Graph to be converted
	 * @return rules Set of F-Logic rules
	 */
	protected Set<Rule> convertOntology(Graph ontology, String defaultNS) 
			throws NonStandardRDFSUseException {
		if (checkForNonStandardRDFSUse(ontology)) {
			throw new NonStandardRDFSUseException(usages);
		}
		Set<Rule> rules = super.convertOntology(ontology, defaultNS); 
		RDFS2DatalogTransformer rdfs2datalog = new RDFS2DatalogTransformer(defaultNS);
        
        rules.add(rdfs2datalog.generateAuxiliaryRDFRule());
        rules.addAll(rdfs2datalog.generateAuxiliaryRDFSRules());
        rules.addAll(rdfs2datalog.generateAuxiliaryERDFSRules(ontology));
//        System.out.println("f-logic rules:");
//        for (Rule r : rules)
//        	System.out.println(r.toString());
//        System.out.println("-*");
		return rules;
	}
	
	public String getDefaultNS(Graph ontology) {
		return defaultNS;
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
    
    /**
     * Checks for a non-standard use of the RDFS vocabulary in the given RDF 
     * graph. Non-standard use of the RDFS vocabulary corresponds to using the 
     * vocabulary in locations where it has not been intended 
     * (e.g. 	&lt;type, subPropertyOf, a&gt;, an occurrence of 
     * <tt>Class<tt> in an RDFS graph, ...).
     * 
     * @param graph - the RDF graph to check for non-standard use of RDFS vocabulary
     * @return true if the graph includes non-standard use of the RDFS 
     * 		   vocabulary and false otherwise
     */
    private boolean checkForNonStandardRDFSUse(Graph graph) {
    	StatementIterator it = graph.getStatements();
    	while (it.hasNext()) {
    		checkForNonStandardRDFSUse(it.next());
    	}
    	if (usages.size() > 0) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    /**
     * Check RDF statement for a non-standard usage of the RDFS vocabulary
     */
    private void checkForNonStandardRDFSUse(Statement statement) {
    	Resource subject = statement.getSubject();
    	URI predicate = statement.getPredicate();
    	Value object = statement.getObject();
    	/*
    	 * check whether type, subClassOf, domain, range or subPropertyOf occur 
    	 * in a non-property position in the graph.
    	 */ 
    	if (subject.toString().equals(RDF.TYPE) || 
    			subject.toString().equals(RDFS.SUBCLASSOF) ||
    			subject.toString().equals(RDFS.DOMAIN) || 
    			subject.toString().equals(RDFS.RANGE) || 
    			subject.toString().equals(RDFS.SUBPROPERTYOF)) {
    		
    		if (!isInPropertyPosition(subject, predicate, object)) {
    			usages.add(statement.toString());
    		}
    	}
    	if (object.toString().equals(RDF.TYPE) || 
    			object.toString().equals(RDFS.SUBCLASSOF) ||
    			object.toString().equals(RDFS.DOMAIN) || 
    			object.toString().equals(RDFS.RANGE) || 
    			object.toString().equals(RDFS.SUBPROPERTYOF)) {
    		if (!isInPropertyPosition(object, predicate)) {
    			usages.add(statement.toString());
    		}
    	}
    	
    	/*
    	 * check whether ContainerMembershipProperty, Resource, Class or Property 
    	 * occur in the graph.
    	 */
    	if (subject.toString().equals(RDFS.CONTAINERMEMBERSHIPPROPERTY) || 
    			predicate.toString().equals(RDFS.CONTAINERMEMBERSHIPPROPERTY) ||
    			object.toString().equals(RDFS.CONTAINERMEMBERSHIPPROPERTY) ||
    			subject.toString().equals(RDFS.RESOURCE) ||
    			predicate.toString().equals(RDFS.RESOURCE) ||
    			object.toString().equals(RDFS.RESOURCE) ||
    			subject.toString().equals(RDFS.CLASS) ||
    			predicate.toString().equals(RDFS.CLASS) ||
    			object.toString().equals(RDFS.CLASS) ||
    			subject.toString().equals(RDF.PROPERTY) ||
    			predicate.toString().equals(RDF.PROPERTY) ||
    			object.toString().equals(RDF.PROPERTY)) {
    		usages.add(statement.toString());
    	}
    }
	
    private boolean isInPropertyPosition(Resource subject, URI predicate, Value object) {
    	if (predicate.toString().equals(RDFS.SUBPROPERTYOF) || 
    			predicate.toString().equals(RDFS.DOMAIN) || 
    			predicate.toString().equals(RDFS.RANGE)) {
    		return true;
    	}
    	else if (predicate.toString().equals(RDF.TYPE) && 
    			(object.toString().equals(RDF.PROPERTY) || 
    			 object.toString().equals(RDFS.CONTAINERMEMBERSHIPPROPERTY))) {
    		return true;
    	}
    	return false;
    }
    
    private boolean isInPropertyPosition(Value object, URI predicate) {
    	return predicate.toString().equals(RDFS.SUBPROPERTYOF);
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
	
}
/*
 * $Log: ERDFSReasonerImpl.java,v $
 * Revision 1.4  2007-08-24 14:06:04  nathalie
 * added check for non-standard use of RDFS vocabulary
 *
 * Revision 1.3  2007-08-23 09:14:51  nathalie
 * added check for non standard use of rdfs vocabulary
 *
 * Revision 1.2  2007-08-08 16:23:56  nathalie
 * some documentation/maintenance addings
 *
 * Revision 1.1  2007/05/24 07:02:20  nathalie
 * created rdfs reasoner
 *
 * 
 */
