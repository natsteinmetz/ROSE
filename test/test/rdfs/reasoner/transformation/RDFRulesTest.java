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
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.vocabulary.RDF;
import org.openrdf.vocabulary.RDFS;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;

import junit.framework.TestCase;

/**
 * Test for the transformation of RDF to F-Logic and of adding 
 * the supplementary rules for RDF entailment.
 *
 * <pre>
 *  Created on May 2, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/test/test/rdfs/reasoner/transformation/RDFRulesTest.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-08-24 14:04:31 $
 */
public class RDFRulesTest extends TestCase {
	
	private RDFS2DatalogTransformer transformer = null;
	
	private Graph graph = null;
	
	private String namespace = "http://www.example.com/";
	
	private EntityFactory factory = null;
	
	private DataFactory dataFactory = Factory.createDataFactory(null);
	
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
	 * Test if the transformation from RDFS to F-Logic molecules 
	 * is complete and whether the simple entailment can be 
	 * captured.
	 * 
	 * The following statements are taken as RDFS test statements:
	 * Person rdfs:subClassOf Animal .
	 * john rdf:type Person .
	 * john hasName "John" .
	 */	
	public void testSimpleTransformation() throws Exception {	
		Set<Rule> rules = new HashSet<Rule>();
		
		StatementIterator it = graph.getStatements();
		while (it.hasNext()) {
			rules.add(transformer.transform(it.next()));
		}

		Term subject = factory.createIdentifier(namespace + "Person");
		Term predicate = factory.createIdentifier(RDFS.SUBCLASSOF);
		Term object = factory.createIdentifier(namespace + "Animal");
		FMolecule molecule = factory.createFMolecule(subject, predicate, object);
		Rule rule = factory.createRule(molecule);
		assertTrue(rules.contains(rule));
		
		subject = factory.createIdentifier(namespace + "john");
		predicate = factory.createIdentifier(RDF.TYPE);
		object = factory.createIdentifier(namespace + "Person");
		molecule = factory.createFMolecule(subject, predicate, object);
		rule = factory.createRule(molecule);
		assertTrue(rules.contains(rule));
		
		subject = factory.createIdentifier(namespace + "john");
		predicate = factory.createIdentifier(namespace + "hasName");
		object = dataFactory.createWsmlString("John");
		molecule = factory.createFMolecule(subject, predicate, object);
		rule = factory.createRule(molecule);
		assertTrue(rules.contains(rule));
		
		assertEquals(rules.size(), 3);
		
//		for (Rule rul : rules)
//			System.out.println(rul.toString());
	}
	
	/**
	 * Test if the transformation from RDFS to F-Logic molecules 
	 * is complete and whether the RDF entailment can be 
	 * captured.
	 * 
	 * The following statements are taken as RDFS test statements:
	 * Person rdfs:subClassOf Animal .
	 * john rdf:type Person .
	 * john hasName "John" .
	 */	
	public void testRDFTransformation() throws Exception {	
		Set<Rule> rules = new HashSet<Rule>();
		
		StatementIterator it = graph.getStatements();
		while (it.hasNext()) {
			Statement statement = it.next();
			rules.add(transformer.transform(statement));
		}
		rules.add(transformer.generateAuxiliaryRDFRule());
		
		// forall x (exists y, u(y[x ->> u]) IMPLIES x[type ->> Property])
		Term subject = factory.createVariable("?x");
		Term predicate = factory.createIdentifier(RDF.TYPE);
		Term object = factory.createIdentifier(RDF.PROPERTY);
		FMolecule head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?y");
		predicate = factory.createVariable("?x");
		object = factory.createVariable("?u");
		FMolecule mol = factory.createFMolecule(subject, predicate, object);
		List<FMolecule> body = new LinkedList<FMolecule>();
		body.add(mol);
		Rule rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
		
		assertEquals(rules.size(), 4);
	}
	
	/*
	 * The following statements are taken as RDFS test statements:
	 * Person rdfs:subClassOf Animal .
	 * john rdf:type Person .
	 * john hasName "John" .
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
