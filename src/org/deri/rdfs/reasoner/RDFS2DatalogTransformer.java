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
package org.deri.rdfs.reasoner;

import java.rmi.server.UID;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deri.rdfs.reasoner.api.factory.EntityFactory;
import org.deri.rdfs.reasoner.api.terms.FMolecule;
import org.deri.rdfs.reasoner.api.terms.Rule;
import org.deri.rdfs.reasoner.factory.EntityFactoryImpl;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Variable;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.vocabulary.RDF;
import org.openrdf.vocabulary.RDFS;
import org.wsmo.common.Identifier;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;

/**
 * Transformation from RDF triples to F-Logic rules.
 *
 * <pre>
 *  Created on April 6, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/RDFS2DatalogTransformer.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.3 $ $Date: 2007-08-24 14:04:59 $
 */
public class RDFS2DatalogTransformer {
	
	private EntityFactory factory = new EntityFactoryImpl();
	
	private WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
	
	private DataFactory dataFactory = Factory.createDataFactory(null);
	
	private Map<BNode, Identifier> bNodes = new HashMap<BNode, Identifier>();
	
	private String defaultNS = "";
	
	public RDFS2DatalogTransformer(String defaultNS) {
		this.defaultNS = defaultNS;
	}
	
	/**
	 * An RDF statement of the form <s,p,o> is transformed to a rule, containing 
	 * the F-Logic molecule of the form s[p ->> o] as head.
	 */
	public Rule transform (Statement statement) {
		Resource subject = statement.getSubject();
		URI predicate = statement.getPredicate();
		Value object = statement.getObject();
		
		return transform(subject, predicate, object);
	}
	
	/**
	 * A combination of RDF subject, predicate and object is transformed to a rule, 
	 * containing the F-Logic molecule of the form s[p ->> o] as head.
	 */
	public Rule transform (Resource subject, URI predicate, Value object) {
		Term subj;
		Term pred;
		Term obj;
		
		// BNodes are skolemized
		if (subject instanceof BNode) {
			if (bNodes.containsKey((BNode) subject)) {
				subj = bNodes.get((BNode) subject);
			}
			else {
				UID uid = new java.rmi.server.UID();
				Identifier uidIRI = wsmoFactory.createIRI(defaultNS + uid.toString());
				bNodes.put((BNode) subject, uidIRI);
				subj = uidIRI;
			}
		}
		else {
			subj = wsmoFactory.createIRI(subject.toString());
		}
		
		pred = wsmoFactory.createIRI(predicate.getURI());
		
		// BNodes are skolemized
		if (object instanceof BNode) {
			if (bNodes.containsKey((BNode) object)) {
				obj = bNodes.get((BNode) object);
			}
			else {
				UID uid = new java.rmi.server.UID();
				Identifier uidIRI = wsmoFactory.createIRI(defaultNS + uid.toString());
				bNodes.put((BNode) object, uidIRI);
				obj = uidIRI;
			}
		}
		else if (object instanceof Resource) {
			obj = wsmoFactory.createIRI(object.toString());
		}
		else if (object instanceof Literal) {
			try { 
				obj = wsmoFactory.createIRI(((Literal) object).getLabel());
			}
			catch (IllegalArgumentException e) {
				try {
					obj = dataFactory.createWsmlInteger(object.toString());
				}
				catch (NumberFormatException ne) {
					obj = dataFactory.createWsmlString(object.toString());
				}
			}
		}
		else {
			obj = wsmoFactory.createIRI(object.toString());
		}
		return factory.createRule(factory.createFMolecule(subj, pred, obj));
	}
	
	/**
	 * To capture RDF entailment, one supplementary rule needs to be taken into 
	 * account:
	 * for the RDF statement <s,p,o>, an F-Logic rule containing the F-Logic 
	 * molecule of the form p[type ->> Property] as head is added.
	 */
	public Rule generateAuxiliaryRDFRule() {
		Variable x = factory.createVariable("?x");
		Variable y = factory.createVariable("?y");
		Variable u = factory.createVariable("?u");
		List<FMolecule> body = new LinkedList<FMolecule>();
		// forall x (exists y, u(y[x ->> u]) IMPLIES x[type ->> Property])
		FMolecule head = factory.createFMolecule(x, factory.createIdentifier(RDF.TYPE), 
				factory.createIdentifier(RDF.PROPERTY));
		body.add(factory.createFMolecule(y, x, u));
		return factory.createRule(head, body);
	}
	
	/**
	 * To capture RDFS entailment, supplementary rules need to be taken into 
	 * account
	 */
	public Set<Rule> generateAuxiliaryRDFSRules() {
		Set<Rule> rules = new HashSet<Rule>();
		Variable x = factory.createVariable("?x");
		Variable y = factory.createVariable("?y");
		Variable u = factory.createVariable("?u");
		Variable v = factory.createVariable("?v");
		List<FMolecule> body = new LinkedList<FMolecule>();
		FMolecule head = null;
		
		// forall x,y,u(x[y ->> u] IMPLIES x[type ->> Resource] AND 
		// u[type ->> Resource])
		head = factory.createFMolecule(x, factory.createIdentifier(RDF.TYPE), 
				factory.createIdentifier(RDFS.RESOURCE));
		body.add(factory.createFMolecule(x, y, u));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		head = factory.createFMolecule(u, factory.createIdentifier(RDF.TYPE), 
				factory.createIdentifier(RDFS.RESOURCE));
		body.add(factory.createFMolecule(x, y, u));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		
		/*
		 * - for each rules x[domain ->> y] AND u[x ->> v], the rule 
		 * 	 u[type ->> y] is implied.
		 * - for each rules x[range ->> y] AND u[x ->> v], the rule 
		 * 	 v[type ->> y] is added.
		 * - for each rule x[type ->> ContainerMembershipProperty], the rule 
		 * 	 x[subPropertyOf ->> member] is added.
		 */

		// forall u,v,x,y(x[domain ->> y] AND u[x ->> v] IMPLIES u[type ->> y]
		head = factory.createFMolecule(u, factory.createIdentifier(RDF.TYPE), y);
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDFS.DOMAIN), y));
		body.add(factory.createFMolecule(u, x, v));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		
		// forall u,v,x,y(x[range ->> y] AND u[x ->> v] IMPLIES v[type ->> y]
		head = factory.createFMolecule(v, factory.createIdentifier(RDF.TYPE), y);
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDFS.RANGE), y));
		body.add(factory.createFMolecule(u, x, v));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		
		// forall x(x[type ->> ContainerMembershipProperty] IMPLIES 
		// x[subPropertyOf ->> member]
		head = factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBPROPERTYOF), 
				factory.createIdentifier(RDFS.MEMBER));
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDF.TYPE), 
				factory.createIdentifier(RDFS.CONTAINERMEMBERSHIPPROPERTY)));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		
		/*
		 * - for each rule x[type ->> Property], the rule x[subPropertyOf ->> x] 
		 *	 is added.
		 * - for each rule x[subPropertyOf ->> y], the rules x[type ->> Property] 
		 *   and y[type ->> Property] are added. For each rule z1[x ->> z2], the 
		 *   rule z1[y ->> z2] is added.
		 * - for each rules x[subPropertyOf ->> y] and y[subPropertyOf ->> z], 
		 *   the rule x[subPropertyOf ->> z] is added. 
		 */
		
		// forall x(x[type ->> Property] IMPLIES x[subPropertyOf ->> x])
		head = factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBPROPERTYOF), x);
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDF.TYPE), 
				factory.createIdentifier(RDF.PROPERTY)));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		
		// forall x,y,u(x[subPropertyOf ->> y] AND y[subPropertyOf ->> u] 
		// IMPLIES x[subPropertyOf ->> u])
		head = factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBPROPERTYOF), u);
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBPROPERTYOF), y));
		body.add(factory.createFMolecule(y, factory.createIdentifier(RDFS.SUBPROPERTYOF), u));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		
		// forall x,y(x[subPropertyOf ->> y] IMPLIES x[type ->> Property] 
		// AND y[type ->> Property] AND forall u, v (u[x ->> v] IMPLIES u[y ->> v]))
		head = factory.createFMolecule(x, factory.createIdentifier(RDF.TYPE), 
				factory.createIdentifier(RDF.PROPERTY));
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBPROPERTYOF), y));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		head = factory.createFMolecule(y, factory.createIdentifier(RDF.TYPE), 
				factory.createIdentifier(RDF.PROPERTY));
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBPROPERTYOF), y));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		head = factory.createFMolecule(u, y, v);
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBPROPERTYOF), y));
		body.add(factory.createFMolecule(u, x, v));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		
		/*
		 * - for each rule x[type ->> Class], the rules x[subClassOf ->> Resource]
		 * 	 and x[subClassOf ->> x] are added.
		 * - for each rule x[subClassOf ->> y], the rules x[type ->> Class] and 
		 *   y[type ->> Class] are added. For each rule z[type ->> x], the 
		 *   rule z[type ->> y] is added.
		 * - for each rules x[subClassOf ->> y] and y[subClassOf ->> z], 
		 *   the rule x[subClassOf ->> z] is added.
		 */
		
		// forall x(x[type ->> Class] IMPLIES x[subClassOf ->> Resource])
		head = factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBCLASSOF), 
				factory.createIdentifier(RDFS.RESOURCE));
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDF.TYPE), 
				factory.createIdentifier(RDFS.CLASS)));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		
		// forall x(x[type ->> Class] IMPLIES x[subClassOf ->> x])
		head = factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBCLASSOF), x);
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDF.TYPE), 
				factory.createIdentifier(RDFS.CLASS)));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		
		// forall x,y,u(x[subClassOf ->> y] AND y[subClassOf ->> u] 
		// IMPLIES x[subClassOf ->> u])
		head = factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBCLASSOF), u);
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBCLASSOF), y));
		body.add(factory.createFMolecule(y, factory.createIdentifier(RDFS.SUBCLASSOF), u));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		
		// forall x,y(x[subClassOf ->> y] IMPLIES x[type ->> Class] AND 
		// y[type ->> Class] AND forall u(u[type ->> x] IMPLIES u[type ->> y])) 
		// IMPLIES x[subClassOf ->> u])
		head = factory.createFMolecule(x, factory.createIdentifier(RDF.TYPE), 
				factory.createIdentifier(RDFS.CLASS));
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBCLASSOF), y));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		head = factory.createFMolecule(y, factory.createIdentifier(RDF.TYPE), 
				factory.createIdentifier(RDFS.CLASS));
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBCLASSOF), y));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		head = factory.createFMolecule(u, factory.createIdentifier(RDF.TYPE), y);
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBCLASSOF), y));
		body.add(factory.createFMolecule(u, factory.createIdentifier(RDF.TYPE), x));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		
        return rules;
    }
	
	/**
	 * To capture eRDFS entailment, supplementary rules need to be taken into 
	 * account
	 */
	public Set<Rule> generateAuxiliaryERDFSRules(Graph graph) {
		Set<Rule> rules = new HashSet<Rule>();
		Variable x = factory.createVariable("?x");
		Variable y = factory.createVariable("?y");
		Variable u = factory.createVariable("?u");
//		Variable v = factory.createVariable("?v");	
		List<FMolecule> body = new LinkedList<FMolecule>();
		FMolecule head = null;
		
		// forall x,y,u(u[domain ->> x] AND x[subClassOf ->> y] 
		// IMPLIES u[domain ->> y]
		head = factory.createFMolecule(u, factory.createIdentifier(RDFS.DOMAIN), y);
		body.add(factory.createFMolecule(u, factory.createIdentifier(RDFS.DOMAIN), x));
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBCLASSOF), y));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		
		// forall x,y,u(u[range ->> x] AND x[subClassOf ->> y] 
		// IMPLIES u[range ->> y]
		head = factory.createFMolecule(u, factory.createIdentifier(RDFS.RANGE), y);
		body.add(factory.createFMolecule(u, factory.createIdentifier(RDFS.RANGE), x));
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBCLASSOF), y));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		
		// forall x,y,u(y[domain ->> u] AND x[subPropertyOf ->> y] 
		// IMPLIES x[domain ->> u]
		head = factory.createFMolecule(x, factory.createIdentifier(RDFS.DOMAIN), u);
		body.add(factory.createFMolecule(y, factory.createIdentifier(RDFS.DOMAIN), u));
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBPROPERTYOF), y));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		
		// forall x,y,u(y[range ->> u] AND x[subPropertyOf ->> y] 
		// IMPLIES x[range ->> u]
		head = factory.createFMolecule(x, factory.createIdentifier(RDFS.RANGE), u);
		body.add(factory.createFMolecule(y, factory.createIdentifier(RDFS.RANGE), u));
		body.add(factory.createFMolecule(x, factory.createIdentifier(RDFS.SUBPROPERTYOF), y));
		rules.add(factory.createRule(head, body));
		body = new LinkedList<FMolecule>();
		
		// TODO: check whether these formulas are still needed, they should not 
		// be needed anymore...
		
		/*
		 * direct embedding of the extensional RDFS entailment regime in F-Logic
		 * Def. 3, page 6 of "Logical Foundations of (e)RDF(S): Complexity, 
		 * Reasoning, and Extension" by Jos de Bruijn and Stijn Heymans
		 */ 
//		StatementIterator iterator = graph.getStatements();
//		while (iterator.hasNext()) {
//			Statement statement = iterator.next();
//			Resource subject = statement.getSubject();
//			URI predicate = statement.getPredicate();
//			Value object = statement.getObject();
//			
//			// <s,subClassOf,o> = forall x(x[type ->> s] IMPLIES x[type ->> o])
//			if (predicate.getURI().equals(RDFS.SUBCLASSOF)) {
//				head = factory.createFMolecule(x, factory.createIdentifier(RDF.TYPE), 
//						wsmoFactory.createIRI(object.toString()));
//				body.add(factory.createFMolecule(x, factory.createIdentifier(RDF.TYPE), 
//						wsmoFactory.createIRI(subject.toString())));
//				rules.add(factory.createRule(head, body));
//				System.out.println(factory.createRule(head, body));
//				body = new LinkedList<FMolecule>();
//			}
//			
//			// <s,subPropertyOf,o> = forall x,y([x[s ->> y] IMPLIES x[o ->> y])
//			if (predicate.getURI().equals(RDFS.SUBPROPERTYOF)) {
//				head = factory.createFMolecule(x, 
//						wsmoFactory.createIRI(object.toString()), y);
//				body.add(factory.createFMolecule(x, 
//						wsmoFactory.createIRI(subject.toString()), y));
//				rules.add(factory.createRule(head, body));
//				System.out.println(factory.createRule(head, body));
//				body = new LinkedList<FMolecule>();
//			}
//			// <s,domain,o> = forall x,y([x[s ->> y] IMPLIES x[type ->> o])
//			if (predicate.getURI().equals(RDFS.DOMAIN)) {
//				head = factory.createFMolecule(x, factory.createIdentifier(RDF.TYPE), 
//						wsmoFactory.createIRI(object.toString()));
//				body.add(factory.createFMolecule(x, 
//						factory.createIdentifier(subject.toString()), y));
//				rules.add(factory.createRule(head, body));
//				System.out.println(factory.createRule(head, body));
//				body = new LinkedList<FMolecule>();
//			}
//			// <s,range,o> = forall x,y([x[s ->> y] IMPLIES y[type ->> o])
//			if (predicate.getURI().equals(RDFS.RANGE)) {
//				head = factory.createFMolecule(y, factory.createIdentifier(RDF.TYPE), 
//						wsmoFactory.createIRI(object.toString()));
//				body.add(factory.createFMolecule(x, 
//						wsmoFactory.createIRI(subject.toString()), y));
//				rules.add(factory.createRule(head, body));
//				System.out.println(factory.createRule(head, body));
//				body = new LinkedList<FMolecule>();
//			}
//		}
//
		/*
		 * Def. 1 p. 5 of "Logical Foundations of (e)RDF(S): Complexity, 
		 * Reasoning, and Extension" by Jos de Bruijn and Stijn Heymans
		 */ 
//		// forall x,y(forall u,v(u[x ->> v] IMPLIES u[type ->> y]) 
//		// IMPLIES x[domain ->> y])
//		head = factory.createFMolecule(x, factory.createIdentifier(RDFS.DOMAIN), y);
//		body.add(factory.createFMolecule(x, x, y));
//		body.add(factory.createFMolecule(x, factory.createIdentifier(RDF.TYPE), y));
//		rules.add(factory.createRule(head, body));
//		System.out.println(factory.createRule(head, body));
//		body = new LinkedList<FMolecule>();
//		
//		// forall x,y(forall u,v(u[x ->> v] IMPLIES v[type ->> y])
//		// IMPLIES x[range ->> y])
//		head = factory.createFMolecule(x, factory.createIdentifier(RDFS.RANGE), y);
//		body.add(factory.createFMolecule(u, x, v));
//		body.add(factory.createFMolecule(v, factory.createIdentifier(RDF.TYPE), y));
//		rules.add(factory.createRule(head, body));
//		System.out.println(factory.createRule(head, body));
//		body = new LinkedList<FMolecule>();
//		
//		// forall x,y(x[type ->> Property] AND y[type ->> Property] 
//		// AND forall u,v(u[x ->> v] IMPLIES u[y ->> v]) IMPLIES 
//		// x[subPropertyOf ->> y])
//		
//		// forall x,y(x[type ->> Class] AND y[type ->> Class] 
//		// AND forall u(u[type ->> x] IMPLIES u[type ->> y]) IMPLIES
//		// x[subClassOf ->> y])
		
		return rules;
		
	}
	
	/**
	 * To capture iOWL entailment, supplementary rules need to be taken into 
	 * account
	 */
	public Set<Rule> generateAuxiliaryIOWLRules(Graph graph, Set<Rule> rules) {

		return rules;
	}
}
/*
 * $log: $
 * 
 */