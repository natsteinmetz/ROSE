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
package test.rdfs.reasoner.transformation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deri.rdfs.reasoner.RDFS2DatalogTransformer;
import org.deri.rdfs.reasoner.api.factory.EntityFactory;
import org.deri.rdfs.reasoner.api.terms.FMolecule;
import org.deri.rdfs.reasoner.api.terms.Rule;
import org.deri.rdfs.reasoner.factory.EntityFactoryImpl;
import org.omwg.logicalexpression.terms.Term;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.vocabulary.RDF;
import org.openrdf.vocabulary.RDFS;

import junit.framework.TestCase;

/**
 * Test for the transformation of RDF to F-Logic and of adding 
 * the supplementary rules for RDFS entailment.
 *
 * <pre>
 *  Created on May 2, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/test/test/rdfs/reasoner/transformation/RDFSRulesTest.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-08-24 14:04:31 $
 */
public class RDFSRulesTest extends TestCase {

	private RDFS2DatalogTransformer transformer = null;
	
	private Graph graph = null;
	
	private String namespace = "http://www.example.com/";
	
	private EntityFactory factory = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		transformer = new RDFS2DatalogTransformer(namespace);
		factory = new EntityFactoryImpl();
		graph = new GraphImpl();
		setupTestOntology();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		transformer = null;
		factory = null;
		graph = null;
		System.gc();
	}
	
	/**
	 * Test the transformation of blank nodes.
	 */
	public void testBlankNodes() throws Exception {
		Set<Rule> rules = new HashSet<Rule>();
		
		StatementIterator it = graph.getStatements();
		while (it.hasNext()) {
			Statement s = it.next();
			rules.add(transformer.transform(s));
		}
		
		Resource resource = new BNodeImpl("_:BNode1");
		URI uri = new URIImpl(RDFS.SUBCLASSOF);
		Value value = new URIImpl(namespace + "Car");
		Statement statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "SmallCar");
		uri = new URIImpl(RDFS.SUBCLASSOF);
		value = new BNodeImpl("_:BNode1");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new BNodeImpl("_:BNode2");
		uri = new URIImpl(RDF.TYPE);
		value = new URIImpl(namespace + "SmallCar");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		rules.add(transformer.generateAuxiliaryRDFRule());
		rules.addAll(transformer.generateAuxiliaryRDFSRules());
		//TODO add the actual test????!!!
	}
	
	/**
	 * Test if the transformation from RDFS to F-Logic molecules 
	 * is complete and whether the RDFS entailment can be 
	 * captured.
	 */	
	public void testRDFSTransformation() throws Exception {	
		Set<Rule> rules = new HashSet<Rule>();
		
		Term subject;
		Term predicate;
		Term object;
		FMolecule head;
		FMolecule mol;
		FMolecule mol2;
		List<FMolecule> body;
		Rule rule;
		
		StatementIterator it = graph.getStatements();
		while (it.hasNext()) {
			Statement statement = it.next();
			rules.add(transformer.transform(statement));
		}
		rules.add(transformer.generateAuxiliaryRDFRule());
		
		assertEquals(rules.size(), 4);		
//		for (Rule rul : rules)
//			System.out.println(rul.toString());
		Set<Rule> rdfRules = new HashSet<Rule>();
		for (Rule r : rules)
			rdfRules.add(r);
			
		rules.addAll(transformer.generateAuxiliaryRDFSRules());
		
		// forall x,y,u(x[y ->> u] IMPLIES x[type ->> Resource])
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDF.TYPE);
		object = factory.createIdentifier(RDFS.RESOURCE);
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createVariable("?y");
		object = factory.createVariable("?u");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
		
		// forall x,y,u(x[y ->> u] IMPLIES u[type ->> Resource])
		subject = factory.createVariable("?u");
		predicate = factory.createIdentifier(RDF.TYPE);
		object = factory.createIdentifier(RDFS.RESOURCE);
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createVariable("?y");
		object = factory.createVariable("?u");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
		
		// forall u,v,x,y(x[domain ->> y] AND u[x ->> v] IMPLIES u[type ->> y]
		subject = factory.createVariable("?u");
		predicate = factory.createIdentifier(RDF.TYPE);
		object = factory.createVariable("?y");
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.DOMAIN);
		object = factory.createVariable("?y");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		subject = factory.createVariable("?u");
		predicate = factory.createVariable("?x");
		object = factory.createVariable("?v");
		mol2 = factory.createFMolecule(subject, predicate, object);
		body.add(mol2);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
		
		// forall u,v,x,y(x[range ->> y] AND u[x ->> v] IMPLIES v[type ->> y]
		subject = factory.createVariable("?v");
		predicate = factory.createIdentifier(RDF.TYPE);
		object = factory.createVariable("?y");
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.RANGE);
		object = factory.createVariable("?y");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		subject = factory.createVariable("?u");
		predicate = factory.createVariable("?x");
		object = factory.createVariable("?v");
		mol2 = factory.createFMolecule(subject, predicate, object);
		body.add(mol2);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));

		// forall x(x[type ->> ContainerMembershipProperty] IMPLIES 
		// x[subPropertyOf ->> member]
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBPROPERTYOF);
		object = factory.createIdentifier(RDFS.MEMBER);
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDF.TYPE);
		object = factory.createIdentifier(RDFS.CONTAINERMEMBERSHIPPROPERTY);
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
	
		// forall x(x[type ->> Property] IMPLIES x[subPropertyOf ->> x])
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBPROPERTYOF);
		object = factory.createVariable("?x");
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDF.TYPE);
		object = factory.createIdentifier(RDF.PROPERTY);
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
				
		// forall x,y,u(x[subPropertyOf ->> y] AND y[subPropertyOf ->> u] 
		// IMPLIES x[subPropertyOf ->> u])
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBPROPERTYOF);
		object = factory.createVariable("?u");
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBPROPERTYOF);
		object = factory.createVariable("?y");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		subject = factory.createVariable("?y");
		predicate = factory.createIdentifier(RDFS.SUBPROPERTYOF);
		object = factory.createVariable("?u");
		mol2 = factory.createFMolecule(subject, predicate, object);
		body.add(mol2);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
				
		// forall x,y(x[subPropertyOf ->> y] IMPLIES x[type ->> Property] 
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDF.TYPE);
		object = factory.createIdentifier(RDF.PROPERTY);
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBPROPERTYOF);
		object = factory.createVariable("?y");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
		
		// forall x,y(x[subPropertyOf ->> y] IMPLIES y[type ->> Property]
		subject = factory.createVariable("?y");
		predicate = factory.createIdentifier(RDF.TYPE);
		object = factory.createIdentifier(RDF.PROPERTY);
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBPROPERTYOF);
		object = factory.createVariable("?y");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
		
		// forall x,y(x[subPropertyOf ->> y] IMPLIES forall u, v 
		// (u[x ->> v] IMPLIES u[y ->> v]))
		subject = factory.createVariable("?u");
		predicate = factory.createVariable("?y");
		object = factory.createVariable("?v");
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBPROPERTYOF);
		object = factory.createVariable("?y");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		subject = factory.createVariable("?u");
		predicate = factory.createVariable("?x");
		object = factory.createVariable("?v");
		mol2 = factory.createFMolecule(subject, predicate, object);
		body.add(mol2);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));


		// forall x(x[type ->> Class] IMPLIES x[subClassOf ->> Resource])
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBCLASSOF);
		object = factory.createIdentifier(RDFS.RESOURCE);
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDF.TYPE);
		object = factory.createIdentifier(RDFS.CLASS);
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
				
		// forall x(x[type ->> Class] IMPLIES x[subClassOf ->> x])
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBCLASSOF);
		object = factory.createVariable("?x");
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDF.TYPE);
		object = factory.createIdentifier(RDFS.CLASS);
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));

		// forall x,y,u(x[subClassOf ->> y] AND y[subClassOf ->> u] 
		// IMPLIES x[subClassOf ->> u])
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBCLASSOF);
		object = factory.createVariable("?u");
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBCLASSOF);
		object = factory.createVariable("?y");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		subject = factory.createVariable("?y");
		predicate = factory.createIdentifier(RDFS.SUBCLASSOF);
		object = factory.createVariable("?u");
		mol2 = factory.createFMolecule(subject, predicate, object);
		body.add(mol2);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
		
		// forall x,y(x[subClassOf ->> y] IMPLIES x[type ->> Class]
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDF.TYPE);
		object = factory.createIdentifier(RDFS.CLASS);
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBCLASSOF);
		object = factory.createVariable("?y");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
		
		// forall x,y(x[subClassOf ->> y] IMPLIES y[type ->> Class]
		subject = factory.createVariable("?y");
		predicate = factory.createIdentifier(RDF.TYPE);
		object = factory.createIdentifier(RDFS.CLASS);
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBCLASSOF);
		object = factory.createVariable("?y");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));	

		// forall x,y(x[subClassOf ->> y] IMPLIES forall u
		// (u[type ->> x] IMPLIES u[type ->> y])) 
		subject = factory.createVariable("?u");
		predicate = factory.createIdentifier(RDF.TYPE);
		object = factory.createVariable("?y");
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBCLASSOF);
		object = factory.createVariable("?y");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		subject = factory.createVariable("?u");
		predicate = factory.createIdentifier(RDF.TYPE);
		object = factory.createVariable("?x");
		mol2 = factory.createFMolecule(subject, predicate, object);
		body.add(mol2);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
		
		rules.removeAll(rdfRules);
//		for (Rule rul : rules)
//			System.out.println(rul.toString());
		assertEquals(rules.size(), 16);
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
	}

}

