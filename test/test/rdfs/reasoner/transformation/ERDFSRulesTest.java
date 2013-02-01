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
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.vocabulary.RDFS;

import junit.framework.TestCase;

/**
 * Test for the transformation of RDF to F-Logic and of adding 
 * the supplementary rules for eRDFS entailment.
 *
 * <pre>
 *  Created on May 2, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/test/test/rdfs/reasoner/transformation/ERDFSRulesTest.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-08-24 14:04:31 $
 */
public class ERDFSRulesTest extends TestCase {
	
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
	 * Test if the transformation from RDFS to F-Logic molecules 
	 * is complete and whether the RDFS entailment can be 
	 * captured.
	 */	
	public void testERDFSTransformation() throws Exception {	
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
		
		assertEquals(rules.size(), 2);		
//		for (Rule rul : rules)
//			System.out.println(rul.toString());
		
		rules.addAll(transformer.generateAuxiliaryERDFSRules(graph));
		
		// forall x,y,u(u[domain ->> x] AND x[subClassOf ->> y] 
		// IMPLIES u[domain ->> y]
		subject = factory.createVariable("?u");
		predicate = factory.createIdentifier(RDFS.DOMAIN);
		object = factory.createVariable("?y");
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?u");
		predicate = factory.createIdentifier(RDFS.DOMAIN);
		object = factory.createVariable("?x");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBCLASSOF);
		object = factory.createVariable("?y");
		mol2 = factory.createFMolecule(subject, predicate, object);
		body.add(mol2);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
		
		// forall x,y,u(u[range ->> x] AND x[subClassOf ->> y] 
		// IMPLIES u[range ->> y]
		subject = factory.createVariable("?u");
		predicate = factory.createIdentifier(RDFS.RANGE);
		object = factory.createVariable("?y");
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?u");
		predicate = factory.createIdentifier(RDFS.RANGE);
		object = factory.createVariable("?x");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBCLASSOF);
		object = factory.createVariable("?y");
		mol2 = factory.createFMolecule(subject, predicate, object);
		body.add(mol2);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
		
		// forall x,y,u(y[domain ->> u] AND x[subPropertyOf ->> y] 
		// IMPLIES x[domain ->> u]
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.DOMAIN);
		object = factory.createVariable("?u");
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?y");
		predicate = factory.createIdentifier(RDFS.DOMAIN);
		object = factory.createVariable("?u");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBPROPERTYOF);
		object = factory.createVariable("?y");
		mol2 = factory.createFMolecule(subject, predicate, object);
		body.add(mol2);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
		
		// forall x,y,u(y[range ->> u] AND x[subPropertyOf ->> y] 
		// IMPLIES x[range ->> u]
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.RANGE);
		object = factory.createVariable("?u");
		head = factory.createFMolecule(subject, predicate, object);
		subject = factory.createVariable("?y");
		predicate = factory.createIdentifier(RDFS.RANGE);
		object = factory.createVariable("?u");
		mol = factory.createFMolecule(subject, predicate, object);
		body = new LinkedList<FMolecule>();
		body.add(mol);
		subject = factory.createVariable("?x");
		predicate = factory.createIdentifier(RDFS.SUBPROPERTYOF);
		object = factory.createVariable("?y");
		mol2 = factory.createFMolecule(subject, predicate, object);
		body.add(mol2);
		rule = factory.createRule(head, body);
		assertTrue(rules.contains(rule));
		
//		for (Rule rul : rules)
//			System.out.println(rul.toString());
		assertEquals(rules.size(), 6);
	}
	
	
	/*
	 * The following statements are taken as RDFS test statements.
	 */
	private void setupTestOntology() {
		Resource resource = new URIImpl(namespace + "parent");
		URI uri = new URIImpl(RDFS.DOMAIN);
		Value value = new URIImpl(namespace + "Person");
		Statement statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
		
		resource = new URIImpl(namespace + "mother");
		uri = new URIImpl(RDFS.SUBPROPERTYOF);
		value = new URIImpl(namespace + "parent");
		statement = new StatementImpl(resource, uri, value);
		graph.add(statement);
	}
	
}
/*
 * $log: $
 * 
 */
