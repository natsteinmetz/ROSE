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
package test.rdfs.reasoner.entailment;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.deri.rdfs.reasoner.api.Reasoner;
import org.deri.rdfs.reasoner.api.factory.EntityFactory;
import org.deri.rdfs.reasoner.factory.EntityFactoryImpl;
import org.deri.rdfs.reasoner.factory.RDFSReasonerFactoryImpl;
import org.deri.rdfs.reasoner.io.RDFParser;
import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Variable;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.vocabulary.RDF;
import org.openrdf.vocabulary.RDFS;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;

import junit.framework.TestCase;

/**
 * Test for RDFS entailment.
 *
 * <pre>
 *  Created on May 7, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/test/test/rdfs/reasoner/entailment/RDFSEntailmentTest.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:22 $
 */
public class RDFSEntailmentTest extends TestCase {
	
	private EntityFactory factory = null;
	
	private Reasoner reasoner = null;
	
	private Graph graph = null;
	
	private String namespace = "http://www.example.com/";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		factory = new EntityFactoryImpl();
		// create reasoner with default reasoning engine IRIS
		reasoner = new RDFSReasonerFactoryImpl().createRDFSReasoner();
		graph = new GraphImpl();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		factory = null;
		graph = null;
		reasoner = null;
		System.gc();
	}
	
	/**
	 * Test the RDFS entailment with a parsed example file.
	 */
	public void testFile() throws Exception {
		// create RDFParser
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RDFParser.RDF_SYNTAX, RDFParser.RDF_XML_SYNTAX);
		RDFParser parser = new RDFParser(properties);
		InputStreamReader reader = new InputStreamReader(ClassLoader
                .getSystemResourceAsStream("test/rdfs/reasoner/entailment/test.rdfs"));
		
		// parse file
		Map<String, Graph> parsedResult = parser.parse(reader, "");
		
		// get graph
		Entry<String, Graph> entry = parsedResult.entrySet().iterator().next();
		Graph graph = entry.getValue();
		
		// get namespaces and default namespace
		Map<String, String> namespaces = parser.getNamespaces();
		String defaultNS = parser.getDefaultNS();
		
		// register graph
		reasoner.registerOntology(graph, defaultNS);
		
		// create dummy wsml ontology to add namespaces
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		Ontology ontology = wsmoFactory.createOntology(
				wsmoFactory.createIRI(defaultNS + "dummy"));
		ontology.setDefaultNamespace(wsmoFactory.createIRI(defaultNS));
		for (Entry<String, String> entryNS : namespaces.entrySet()) {
			ontology.addNamespace(wsmoFactory.createNamespace(entryNS.getKey(), 
					wsmoFactory.createIRI(entryNS.getValue())));
		}
		
		// create query 1
		Set<Map<Variable, Term>> result = new HashSet<Map<Variable, Term>>();
		LogicalExpression query = Factory.createLogicalExpressionFactory(null).
			createLogicalExpression("?x[hasMother hasValue ?y]", ontology);
		
		// send query to the reasoner
		result = reasoner.executeQuery(graph, query);
		
//		// print out the results:
//        System.out.println("The query '" + query + "' has the following " + 
//        		result.size() + " result(s):");
//        for (Map<Variable, Term> vBinding : result) {
//            for (Variable var : vBinding.keySet()) {
//                System.out.println("  " + var + ": " + vBinding.get(var).toString() + "\t ");
//            }
//        }
//        System.out.println();
        
        assertEquals(result.size(), 1);
        boolean in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) 
        			&& vBinding.containsValue(factory.createIdentifier(defaultNS + "john"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("y")) 
        			&& vBinding.containsValue(factory.createIdentifier(defaultNS + "anna"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        
        // create query 2
		result = new HashSet<Map<Variable, Term>>();
		query = Factory.createLogicalExpressionFactory(null).
			createLogicalExpression("?x[hasName hasValue ?y]", ontology);
		
		// send query to the reasoner
		result = reasoner.executeQuery(graph, query);
		
//		// print out the results:
//        System.out.println("The query '" + query + "' has the following " + 
//        		result.size() + " result(s):");
//        for (Map<Variable, Term> vBinding : result) {
//            for (Variable var : vBinding.keySet()) {
//                System.out.println("  " + var + ": " + vBinding.get(var).toString() + "\t ");
//            }
//        }
//        System.out.println();
        
        assertEquals(result.size(), 2);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) 
        			&& vBinding.containsValue(factory.createIdentifier(defaultNS + "john"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("y")) && vBinding.containsValue(
        			Factory.createDataFactory(null).createWsmlString("John"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) 
        			&& vBinding.containsValue(factory.createIdentifier(defaultNS + "anna"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("y")) && vBinding.containsValue(
        			Factory.createDataFactory(null).createWsmlString("Anna"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        
        // create query 3
		result = new HashSet<Map<Variable, Term>>();
		query = Factory.createLogicalExpressionFactory(null).
			createLogicalExpression("hasMother[rdfs#subPropertyOf hasValue ?y]", ontology);

		// send query to the reasoner
		result = reasoner.executeQuery(graph, query);
		
//		// print out the results:
//        System.out.println("The query '" + query + "' has the following " + 
//        		result.size() + " result(s):");
//        for (Map<Variable, Term> vBinding : result) {
//            for (Variable var : vBinding.keySet()) {
//                System.out.println("  " + var + ": " + vBinding.get(var).toString() + "\t ");
//            }
//        }
//        System.out.println();
        
        assertEquals(result.size(), 3);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("y")) 
        			&& vBinding.containsValue(factory.createIdentifier(defaultNS + "hasParent"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("y")) 
        			&& vBinding.containsValue(factory.createIdentifier(defaultNS + "hasRelative"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("y")) 
        			&& vBinding.containsValue(factory.createIdentifier(defaultNS + "hasMother"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        
        // deregister graph
        reasoner.deRegisterOntology(graph, defaultNS);
	}
	
	/**
	 * Test the RDFS entailment with simple queries.
	 */	
	public void testStatementSubjAndObj() throws Exception {	
		// setup test ontology
		setupTestOntology();
		
		// create dummy wsml ontology to add namespace
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		Ontology ontology = wsmoFactory.createOntology(
				wsmoFactory.createIRI(namespace + "dummy"));
		ontology.setDefaultNamespace(wsmoFactory.createIRI(namespace));
		
		// create query
		Set<Map<Variable, Term>> result = new HashSet<Map<Variable, Term>>();
		LogicalExpression query = Factory.createLogicalExpressionFactory(null).
		createLogicalExpression("?x[hasParent hasValue ?y]", ontology);
		
		reasoner.registerOntology(graph, "");
		result = reasoner.executeQuery(graph, query);
		
//		// print out the results:
//        System.out.println("The query '" + query + "' has the following " + 
//        		result.size() + " result(s):");
//        for (Map<Variable, Term> vBinding : result) {
//            for (Variable var : vBinding.keySet()) {
//                System.out.println("  " + var + ": " + vBinding.get(var).toString() + "\t ");
//            }
//        }
//        System.out.println();
        
        reasoner.deRegisterOntology(graph, "");
        
        assertEquals(result.size(), 1);
        boolean in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "anna"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("y")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "john"))) {
        		in = true;
        	}
        }
        assertTrue(in);
	}
	
	/**
	 * Test the RDFS entailment with simple a query.
	 */	
	public void testClassType() throws Exception {	
		// setup test ontology
		setupTestOntology();
		
		// create dummy wsml ontology to add namespace
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		Ontology ontology = wsmoFactory.createOntology(
				wsmoFactory.createIRI(namespace + "dummy"));
		ontology.setDefaultNamespace(wsmoFactory.createIRI(namespace));
		
		// create query
		Set<Map<Variable, Term>> result = new HashSet<Map<Variable, Term>>();
		LogicalExpression query = Factory.createLogicalExpressionFactory(null).
				createLogicalExpression("Person[_\"" + RDF.TYPE + 
						"\" hasValue ?x]", ontology);
		
		reasoner.registerOntology(graph, "");
		result = reasoner.executeQuery(graph, query);
		
//		// print out the results:
//        System.out.println("The query '" + query + "' has the following " + 
//        		result.size() + " result(s):");
//        for (Map<Variable, Term> vBinding : result) {
//            for (Variable var : vBinding.keySet()) {
//                System.out.println(var + ": " + vBinding.get(var).toString() + "\t ");
//            }
//        }
//        System.out.println();
        
        reasoner.deRegisterOntology(graph, "");
        
        assertEquals(result.size(), 2);
        boolean in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) 
        			&& vBinding.containsValue(factory.createIdentifier(RDFS.RESOURCE))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) 
        			&& vBinding.containsValue(factory.createIdentifier(RDFS.CLASS))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
	}
	
	/**
	 * Test the RDFS entailment with a simple query.
	 */	
	public void testSubProperty() throws Exception {	
		// setup test ontology
		setupTestOntology();
		
		// create dummy wsml ontology to add namespace
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		Ontology ontology = wsmoFactory.createOntology(
				wsmoFactory.createIRI(namespace + "dummy"));
		ontology.setDefaultNamespace(wsmoFactory.createIRI(namespace));
		
		// create query
		Set<Map<Variable, Term>> result = new HashSet<Map<Variable, Term>>();
		LogicalExpression query = Factory.createLogicalExpressionFactory(null).
				createLogicalExpression("?x[_\"" + RDFS.SUBPROPERTYOF + 
						"\" hasValue hasParent]", ontology);
		
		reasoner.registerOntology(graph, "");
		result = reasoner.executeQuery(graph, query);
		
//		// print out the results:
//        System.out.println("The query '" + query + "' has the following " + 
//        		result.size() + " result(s):");
//        for (Map<Variable, Term> vBinding : result) {
//            for (Variable var : vBinding.keySet()) {
//                System.out.println(var + ": " + vBinding.get(var).toString() + "\t ");
//            }
//        }
//        System.out.println();
        
        reasoner.deRegisterOntology(graph, "");
        
        assertEquals(result.size(), 2);
        boolean in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "hasMother"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "hasParent"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
	}
	
	/**
	 * Test the RDFS entailment with a simple query.
	 */	
	public void testWithStringValue() throws Exception {	
		// setup test ontology
		setupTestOntology();
		
		// create dummy wsml ontology to add namespace
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		Ontology ontology = wsmoFactory.createOntology(
				wsmoFactory.createIRI(namespace + "dummy"));
		ontology.setDefaultNamespace(wsmoFactory.createIRI(namespace));
		
		// create query
		Set<Map<Variable, Term>> result = new HashSet<Map<Variable, Term>>();
		LogicalExpression query = Factory.createLogicalExpressionFactory(null).
				createLogicalExpression("?x[hasName hasValue \"John\"]", 
						ontology);
		
		reasoner.registerOntology(graph, "");
		result = reasoner.executeQuery(graph, query);
		
//		// print out the results:
//        System.out.println("The query '" + query + "' has the following " + 
//        		result.size() + " result(s):");
//        for (Map<Variable, Term> vBinding : result) {
//            for (Variable var : vBinding.keySet()) {
//                System.out.println(var + ": " + vBinding.get(var).toString() + "\t ");
//            }
//        }
//        System.out.println();
        
        reasoner.deRegisterOntology(graph, "");
        
        assertEquals(result.size(), 1);
        boolean in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "john"))) {
        		in = true;
        	}
        }
        assertTrue(in);
	}
	
	/**
	 * Test the RDFS entailment with a simple query.
	 */	
	public void testGetStringValue() throws Exception {	
		// setup test ontology
		setupTestOntology();
		
		// create dummy wsml ontology to add namespace
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		Ontology ontology = wsmoFactory.createOntology(
				wsmoFactory.createIRI(namespace + "dummy"));
		ontology.setDefaultNamespace(wsmoFactory.createIRI(namespace));
		
		// create query
		Set<Map<Variable, Term>> result = new HashSet<Map<Variable, Term>>();
		LogicalExpression query = Factory.createLogicalExpressionFactory(null).
				createLogicalExpression("john[hasName hasValue ?x]", 
						ontology);
		
		reasoner.registerOntology(graph, "");
		result = reasoner.executeQuery(graph, query);
		
//		// print out the results:
//        System.out.println("The query '" + query + "' has the following " + 
//        		result.size() + " result(s):");
//        for (Map<Variable, Term> vBinding : result) {
//            for (Variable var : vBinding.keySet()) {
//                System.out.println(var + ": " + vBinding.get(var).toString() + "\t ");
//            }
//        }
//        System.out.println();
        
        reasoner.deRegisterOntology(graph, "");
        
        assertEquals(result.size(), 1);
        boolean in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) && vBinding.containsValue(
        			Factory.createDataFactory(null).createWsmlString("John"))) {
        		in = true;
        	}
        }
        assertTrue(in);
	}
	
	/**
	 * Test the RDFS entailment with a simple query.
	 */	
	public void testGetSubPropertyOf() throws Exception {	
		// setup test ontology
		setupTestOntology();
		
		// create dummy wsml ontology to add namespace
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		Ontology ontology = wsmoFactory.createOntology(
				wsmoFactory.createIRI(namespace + "dummy"));
		ontology.setDefaultNamespace(wsmoFactory.createIRI(namespace));
		
		// create query
		Set<Map<Variable, Term>> result = new HashSet<Map<Variable, Term>>();
		LogicalExpression query = Factory.createLogicalExpressionFactory(null).
				createLogicalExpression("?x[_\"" + RDFS.SUBPROPERTYOF + 
						"\" hasValue hasParent]", ontology);
		
		reasoner.registerOntology(graph, "");
		result = reasoner.executeQuery(graph, query);
		
//		// print out the results:
//        System.out.println("The query '" + query + "' has the following " + 
//        		result.size() + " result(s):");
//        for (Map<Variable, Term> vBinding : result) {
//            for (Variable var : vBinding.keySet()) {
//                System.out.println(var + ": " + vBinding.get(var).toString() + "\t ");
//            }
//        }
//        System.out.println();
        
        reasoner.deRegisterOntology(graph, "");
        
        assertEquals(result.size(), 2);
        boolean in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "hasMother"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "hasParent"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
	}
	
	/**
	 * Test the RDFS entailment with a conjunctive query.
	 */	
	public void testGetClassesOfInstance() throws Exception {	
		// setup test ontology
		setupTestOntology();
		
		// create dummy wsml ontology to add namespace
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		Ontology ontology = wsmoFactory.createOntology(
				wsmoFactory.createIRI(namespace + "dummy"));
		ontology.setDefaultNamespace(wsmoFactory.createIRI(namespace));
		
		// create query
		Set<Map<Variable, Term>> result = new HashSet<Map<Variable, Term>>();
		LogicalExpression query = Factory.createLogicalExpressionFactory(null).
			createLogicalExpression("anna[hasParent hasValue ?x] and " +
					"?x[_\"" + RDF.TYPE + "\" hasValue ?class]", ontology);
		
		reasoner.registerOntology(graph, "");
		result = reasoner.executeQuery(graph, query);
		
//		// print out the results:
//        System.out.println("The query '" + query + "' has the following " + 
//        		result.size() + " result(s):");
//        for (Map<Variable, Term> vBinding : result) {
//            for (Variable var : vBinding.keySet()) {
//                System.out.println(var + ": " + vBinding.get(var).toString() + "\t ");
//            }
//        }
//        System.out.println();
        
        reasoner.deRegisterOntology(graph, "");
        
        assertEquals(result.size(), 3);
        boolean in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "john"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("class")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "Person"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("class")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "Animal"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("class")) 
        			&& vBinding.containsValue(factory.createIdentifier(RDFS.RESOURCE))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
	}
	
	/**
	 * Test the RDFS entailment with a conjunctive query.
	 */	
	public void testGetClassesOfInstance2() throws Exception {	
		// setup test ontology
		setupTestOntology();
		
		// create dummy wsml ontology to add namespace
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		Ontology ontology = wsmoFactory.createOntology(
				wsmoFactory.createIRI(namespace + "dummy"));
		ontology.setDefaultNamespace(wsmoFactory.createIRI(namespace));
		
		// create query
		Set<Map<Variable, Term>> result = new HashSet<Map<Variable, Term>>();
		LogicalExpression query = Factory.createLogicalExpressionFactory(null).
			createLogicalExpression("?x[hasName hasValue \"John\"] and " +
					"?x[_\"" + RDF.TYPE + "\" hasValue ?class]", ontology);
		
		reasoner.registerOntology(graph, "");
		result = reasoner.executeQuery(graph, query);
		
//		// print out the results:
//        System.out.println("The query '" + query + "' has the following " + 
//        		result.size() + " result(s):");
//        for (Map<Variable, Term> vBinding : result) {
//            for (Variable var : vBinding.keySet()) {
//                System.out.println(var + ": " + vBinding.get(var).toString() + "\t ");
//            }
//        }
//        System.out.println();
        
        reasoner.deRegisterOntology(graph, "");
        
        assertEquals(result.size(), 3);
        boolean in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "john"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("class")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "Person"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("class")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "Animal"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("class")) 
        			&& vBinding.containsValue(factory.createIdentifier(RDFS.RESOURCE))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
	}
	
	/*
	 * The following statements are taken as RDFS test statements.
	 */
	private void setupTestOntology() {
		Resource resource = new URIImpl(namespace + "Person");
		URI uri = new URIImpl(RDFS.SUBCLASSOF);
		Value value = new URIImpl(namespace + "Animal");
		Statement statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "john");
		uri = new URIImpl(RDF.TYPE);
		value = new URIImpl(namespace + "Person");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "john");
		uri = new URIImpl(namespace + "hasName");
		value = new LiteralImpl("John");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "john");
		uri = new URIImpl(namespace + "hasAge");
		value = new LiteralImpl("30");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "anna");
		uri = new URIImpl(namespace + "hasName");
		value = new LiteralImpl("Anna");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "hasName");
		uri = new URIImpl(RDFS.DOMAIN);
		value = new LiteralImpl(namespace + "Person");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "hasName");
		uri = new URIImpl(RDFS.RANGE);
		value = new LiteralImpl(RDFS.LITERAL);
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "anna");
		uri = new URIImpl(RDF.TYPE);
		value = new LiteralImpl(RDFS.CONTAINERMEMBERSHIPPROPERTY);
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "hasMother");
		uri = new URIImpl(RDFS.SUBPROPERTYOF);
		value = new LiteralImpl(namespace + "hasParent");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "hasParent");
		uri = new URIImpl(RDFS.SUBPROPERTYOF);
		value = new LiteralImpl(namespace + "hasRelative");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "anna");
		uri = new URIImpl(namespace + "hasParent");
		value = new LiteralImpl(namespace + "john");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "Child");
		uri = new URIImpl(RDFS.SUBCLASSOF);
		value = new LiteralImpl(namespace + "Person");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
	}
	
}
/*
 * $log: $
 * 
 */