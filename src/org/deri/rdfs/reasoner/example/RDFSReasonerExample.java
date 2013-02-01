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
package org.deri.rdfs.reasoner.example;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.deri.rdfs.reasoner.api.Reasoner;
import org.deri.rdfs.reasoner.api.factory.RDFSReasonerFactory;
import org.deri.rdfs.reasoner.factory.RDFSReasonerFactoryImpl;
import org.deri.rdfs.reasoner.io.RDFParser;
import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Variable;
import org.openrdf.model.Graph;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;

/**
 * Usage example for the RDFS reasoner framework. The example shows how to 
 * parse an RDFS file, how to create an RDFS reasoner (we use the ERDFS 
 * Reasoner in this example, to use extensional RDFS entailment), how to 
 * register the RDFS ontology at the reasoner and how to execute queries 
 * over it.
 *
 * <pre>
 *  Created on April 25, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/example/RDFSReasonerExample.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-08-24 14:05:14 $
 */
public class RDFSReasonerExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RDFSReasonerExample ex = new RDFSReasonerExample();
        try {
            ex.doTestRun();
            System.exit(0);
        } catch (Throwable e) {
            e.printStackTrace();
        }
	}
	
	public void doTestRun() throws Exception {
		// create RDFParser
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RDFParser.RDF_SYNTAX, RDFParser.RDF_XML_SYNTAX);
		RDFParser parser = new RDFParser(properties);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(
        		"org/deri/rdfs/reasoner/example/test.rdfs");
        
        // parse file
        Map<String, Graph> parsedResult = parser.parse(is, "");
        
        // get ontology
		Entry<String, Graph> entry = parsedResult.entrySet().iterator().next();
		Graph graph = entry.getValue();
		
		// get namespaces and default namespace
		Map<String, String> namespaces = parser.getNamespaces();
		String ontologyURI = parser.getDefaultNS();
		
        // create a reasoner with IRIS as underlying reasoning engine
		// if no map is given, IRIS is taken as default reasoner
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(RDFSReasonerFactory.PARAM_BUILT_IN_REASONER,
                RDFSReasonerFactory.BuiltInReasoner.IRIS);
        Reasoner reasoner = RDFSReasonerFactoryImpl.getFactory().
        		createERDFSReasoner(params);
        
        // Register ontology
        reasoner.registerOntology(graph, ontologyURI);
		
		// create dummy wsml ontology to add namespaces
        // these namespaces are used for creating correct WSML queries
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		Ontology ontology = wsmoFactory.createOntology(
				wsmoFactory.createIRI(ontologyURI + "dummy"));
		ontology.setDefaultNamespace(wsmoFactory.createIRI(ontologyURI));
		for (Entry<String, String> entryNS : namespaces.entrySet()) {
			ontology.addNamespace(wsmoFactory.createNamespace(entryNS.getKey(), 
					wsmoFactory.createIRI(entryNS.getValue())));
		}
		
		// create query 1 (rdfs entailment)
		Set<Map<Variable, Term>> result = new HashSet<Map<Variable, Term>>();
		LogicalExpression query = Factory.createLogicalExpressionFactory(null).
			createLogicalExpression("?x[hasMother hasValue ?y]", ontology);
		
		// send query to the reasoner
		result = reasoner.executeQuery(graph, query);
		
		// print out the results:
        System.out.println("The query '" + query + "' has the following " + 
        		result.size() + " result(s):");
        for (Map<Variable, Term> vBinding : result) {
            for (Variable var : vBinding.keySet()) {
                System.out.println("  " + var + ": " + vBinding.get(var).toString() + "\t ");
            }
        }
        System.out.println("\n");
        
        // create query 2 (rdfs entailment)
		result = new HashSet<Map<Variable, Term>>();
		query = Factory.createLogicalExpressionFactory(null).
			createLogicalExpression("?x[hasName hasValue ?y]", ontology);
		
		// send query to the reasoner
		result = reasoner.executeQuery(graph, query);
		
		// print out the results:
        System.out.println("The query '" + query + "' has the following " + 
        		result.size() + " result(s):");
        for (Map<Variable, Term> vBinding : result) {
            for (Variable var : vBinding.keySet()) {
                System.out.println("  " + var + ": " + vBinding.get(var).toString() + "\t ");
            }
        }
        System.out.println("\n");
        
        // create query 3 (rdfs entailment)
		result = new HashSet<Map<Variable, Term>>();
		query = Factory.createLogicalExpressionFactory(null).
			createLogicalExpression("hasMother[rdfs#subPropertyOf hasValue ?y]", ontology);

		// send query to the reasoner
		result = reasoner.executeQuery(graph, query);
		
		// print out the results:
        System.out.println("The query '" + query + "' has the following " + 
        		result.size() + " result(s):");
        for (Map<Variable, Term> vBinding : result) {
            for (Variable var : vBinding.keySet()) {
                System.out.println("  " + var + ": " + vBinding.get(var).toString() + "\t ");
            }
        }
        System.out.println("\n");
        
		// create query 4 (eRDFS entailment)
		result = new HashSet<Map<Variable, Term>>();
		query = Factory.createLogicalExpressionFactory(null).
			createLogicalExpression("isAncestorOf[rdfs#domain hasValue ?x]", ontology);
		
		// send query to the reasoner
		result = reasoner.executeQuery(graph, query);
		
		// print out the results:
        System.out.println("The query '" + query + "' has the following " + 
        		result.size() + " result(s):");
        for (Map<Variable, Term> vBinding : result) {
            for (Variable var : vBinding.keySet()) {
                System.out.println("  " + var + ": " + vBinding.get(var).toString() + "\t ");
            }
        }
        System.out.println("\n");
		
        // deregister graph
        reasoner.deRegisterOntology(graph, ontologyURI);
	}

}
/*
 * $log: $
 * 
 */