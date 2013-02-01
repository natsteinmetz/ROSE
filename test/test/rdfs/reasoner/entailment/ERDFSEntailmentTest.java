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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.deri.rdfs.reasoner.api.Reasoner;
import org.deri.rdfs.reasoner.api.factory.EntityFactory;
import org.deri.rdfs.reasoner.exception.NonStandardRDFSUseException;
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
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.vocabulary.RDF;
import org.openrdf.vocabulary.RDFS;
import org.wsml.reasoner.api.inconsistency.InconsistencyException;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;

import junit.framework.TestCase;

/**
 * Test for eRDFS entailment.
 *
 * <pre>
 *  Created on August 9, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/test/test/rdfs/reasoner/entailment/ERDFSEntailmentTest.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-08-24 14:04:31 $
 */
public class ERDFSEntailmentTest extends TestCase {
	
	private EntityFactory factory = null;
	
	private Reasoner reasoner = null;
	
	private Graph graph = null;
	
	private String namespace = "http://www.example.com/";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		factory = new EntityFactoryImpl();
		// create reasoner with default reasoning engine IRIS
		reasoner = new RDFSReasonerFactoryImpl().createERDFSReasoner();
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
	 * Test non-standard use of RDFS vocabulary exception.
	 */
	public void testNonStandardRDFSUse() throws Exception {
		// create RDFParser
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RDFParser.RDF_SYNTAX, RDFParser.RDF_XML_SYNTAX);
		RDFParser parser = new RDFParser(properties);
		InputStreamReader reader = new InputStreamReader(ClassLoader
                .getSystemResourceAsStream("test/rdfs/reasoner/entailment/non-standard-use-test.rdfs"));
		
		// parse file
		Map<String, Graph> parsedResult = parser.parse(reader, "");
		
		// get graph
		Entry<String, Graph> entry = parsedResult.entrySet().iterator().next();
		Graph graph = entry.getValue();
		
//		// print out graph
//		StatementIterator it = graph.getStatements();
//		while (it.hasNext()) {
//			System.out.println("- " + it.next());
//		}
		
		// get default namespace
		String defaultNS = parser.getDefaultNS();
		
		List<String> nonStandardUsages = null;
		try {
			// register graph
			reasoner.registerOntology(graph, defaultNS);
			fail("Should fail because the given graph has non-standard use " +
					"of the RDFS vocabulary.");
		} catch (NonStandardRDFSUseException e) {
			e.getMessage();
			nonStandardUsages = NonStandardRDFSUseException.getNonStandardRDFSUsages();
		}
		
		assertTrue(!nonStandardUsages.isEmpty());
		
		assertTrue(nonStandardUsages.contains("(http://test.example.org/test#Animal, " +
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#type, " +
				"http://www.w3.org/2000/01/rdf-schema#Class)"));
		
		assertTrue(nonStandardUsages.contains("(http://test.example.org/test#hasParent, " +
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#type, " +
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property)"));
		
		assertTrue(nonStandardUsages.contains("(http://www.w3.org/2000/01/rdf-schema#domain, " +
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#type, " +
				"http://test.example.org/test#testType)"));
		
		assertTrue(nonStandardUsages.contains("(http://www.w3.org/2000/01/rdf-schema#range, " +
				"http://www.w3.org/2000/01/rdf-schema#subClassOf, " +
				"http://test.example.org/test#testType)"));
		
	}
	
	/**
	 * Test the eRDFS entailment with simple queries.
	 */	
	public void testDomainANDSubProperty() throws Exception {	
		// setup test ontology
		setupTestOntology1();
		
		// create dummy wsml ontology to add namespace
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		Ontology ontology = wsmoFactory.createOntology(
				wsmoFactory.createIRI(namespace + "dummy"));
		ontology.setDefaultNamespace(wsmoFactory.createIRI(namespace));
		
		// create query
		Set<Map<Variable, Term>> result = new HashSet<Map<Variable, Term>>();
		LogicalExpression query = Factory.createLogicalExpressionFactory(null).
		createLogicalExpression("?x[_\"" + RDFS.DOMAIN + "\" hasValue ?y]", 
				ontology);
		
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
        	if (vBinding.containsKey(factory.createVariable("y")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "Person"))) {
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
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("y")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "Person"))) {
        		in = true;
        	}
        }
        assertTrue(in);
	}
	
	/**
	 * Test the eRDFS entailment with simple queries.
	 */	
	public void testRangeANDSubProperty() throws Exception {	
		// setup test ontology
		setupTestOntology1();
		
		// create dummy wsml ontology to add namespace
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		Ontology ontology = wsmoFactory.createOntology(
				wsmoFactory.createIRI(namespace + "dummy"));
		ontology.setDefaultNamespace(wsmoFactory.createIRI(namespace));
		
		// create query
		Set<Map<Variable, Term>> result = new HashSet<Map<Variable, Term>>();
		LogicalExpression query = Factory.createLogicalExpressionFactory(null).
		createLogicalExpression("?x[_\"" + RDFS.RANGE + "\" hasValue ?y]", 
				ontology);
		
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
        	if (vBinding.containsKey(factory.createVariable("y")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "Person"))) {
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
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("y")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "Person"))) {
        		in = true;
        	}
        }
        assertTrue(in);
	}
	
	/**
	 * Test the eRDFS entailment with simple queries.
	 */	
	public void testDomainANDSubClass() throws Exception {	
		// setup test ontology
		setupTestOntology2();
		
		// create dummy wsml ontology to add namespace
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		Ontology ontology = wsmoFactory.createOntology(
				wsmoFactory.createIRI(namespace + "dummy"));
		ontology.setDefaultNamespace(wsmoFactory.createIRI(namespace));
		
		// create query
		Set<Map<Variable, Term>> result = new HashSet<Map<Variable, Term>>();
		LogicalExpression query = Factory.createLogicalExpressionFactory(null).
		createLogicalExpression("?x[_\"" + RDFS.DOMAIN + "\" hasValue ?y]", 
				ontology);
		
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
        
        assertEquals(result.size(), 3);
        boolean in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "hasParent"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("y")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "Person"))) {
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
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("y")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "Animal"))) {
        		in = true;
        	}
        }
        assertTrue(in);
	}
	
	/**
	 * Test the eRDFS entailment with simple queries.
	 */	
	public void testRangeANDSubClass() throws Exception {	
		// setup test ontology
		setupTestOntology2();
		
		// create dummy wsml ontology to add namespace
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		Ontology ontology = wsmoFactory.createOntology(
				wsmoFactory.createIRI(namespace + "dummy"));
		ontology.setDefaultNamespace(wsmoFactory.createIRI(namespace));
		
		// create query
		Set<Map<Variable, Term>> result = new HashSet<Map<Variable, Term>>();
		LogicalExpression query = Factory.createLogicalExpressionFactory(null).
		createLogicalExpression("?x[_\"" + RDFS.RANGE + "\" hasValue ?y]", 
				ontology);
		
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
        
        assertEquals(result.size(), 3);
        boolean in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("x")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "hasParent"))) {
        		in = true;
        	}
        }
        assertTrue(in);
        in = false;
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("y")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "Person"))) {
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
        for (Map<Variable, Term> vBinding : result) {
        	if (vBinding.containsKey(factory.createVariable("y")) 
        			&& vBinding.containsValue(factory.createIdentifier(namespace + "Animal"))) {
        		in = true;
        	}
        }
        assertTrue(in);
	}
	
	/*
	 * The following statements are taken as RDFS test statements.
	 */
	private void setupTestOntology1() {
		Resource resource = new URIImpl(namespace + "hasParent");
		URI uri = new URIImpl(RDFS.DOMAIN);
		Value value = new URIImpl(namespace + "Person");
		Statement statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "hasParent");
		uri = new URIImpl(RDFS.RANGE);
		value = new URIImpl(namespace + "Person");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "hasMother");
		uri = new URIImpl(RDFS.SUBPROPERTYOF);
		value = new URIImpl(namespace + "hasParent");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
	}
	
	/*
	 * The following statements are taken as RDFS test statements.
	 */
	private void setupTestOntology2() {
		Resource resource = new URIImpl(namespace + "hasParent");
		URI uri = new URIImpl(RDFS.DOMAIN);
		Value value = new URIImpl(namespace + "Person");
		Statement statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "hasParent");
		uri = new URIImpl(RDFS.RANGE);
		value = new URIImpl(namespace + "Person");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "Person");
		uri = new URIImpl(RDFS.SUBCLASSOF);
		value = new URIImpl(namespace + "Animal");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
	}
	
}
/*
 * $log: $
 * 
 */