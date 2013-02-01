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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deri.rdfs.reasoner.RDFS2DatalogTransformer;
import org.deri.rdfs.reasoner.WSML2DatalogTransformer;
import org.deri.rdfs.reasoner.api.Reasoner;
import org.deri.rdfs.reasoner.api.terms.Rule;
import org.deri.rdfs.reasoner.exception.NonStandardRDFSUseException;
import org.omwg.logicalexpression.Atom;
import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.terms.Term;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.sesame.sail.StatementIterator;
import org.wsml.reasoner.ConjunctiveQuery;
import org.wsml.reasoner.Literal;
import org.wsml.reasoner.impl.LogicalExpressionVariableVisitor;
import org.wsml.reasoner.impl.WSMO4JManager;
import org.wsml.reasoner.transformation.le.LloydToporRules;
import org.wsml.reasoner.transformation.le.LogicalExpressionTransformer;
import org.wsml.reasoner.transformation.le.TopDownLESplitter;
import org.wsml.reasoner.transformation.le.TransformationRule;
import org.wsmo.common.Identifier;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;

/**
 * An abstract Reasoner class, providing the methods for converting RDFS Graphs, 
 * RDFS statements and WSML Logical Expressions to F-Logic rules.
 * 
 * <pre>
 *  Created on April 27th, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/impl/ReasonerImpl.java,v $,
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.2 $ $Date: 2007-08-23 09:09:01 $
 */
public abstract class ReasonerImpl implements Reasoner {
	
	public final static String PRED_HAS_VALUE = "f-logic-has-value";
	
	protected final static String WSML_RESULT_PREDICATE = "http://www.wsmo.org/reasoner/"
        + "wsml_query_result";
	
	private Set<Rule> ruleSet = null;
	
	/*
	 * Method to convert an RDFS ontology to Datalog rules.
	 * 
	 * @param ontology Graph to be converted
	 * @return rules Set of F-Logic rules
	 */
	protected Set<Rule> convertOntology(Graph ontology, String defaultNS) 
			throws NonStandardRDFSUseException {
		Set<Rule> rules = new HashSet<Rule>();
		ruleSet = new HashSet<Rule>();
		RDFS2DatalogTransformer rdfs2datalog = new RDFS2DatalogTransformer(defaultNS);
        StatementIterator it = ontology.getStatements();
        while (it.hasNext()) {
        	Statement statement = it.next();
        	Rule rule = rdfs2datalog.transform(statement);
        	rules.add(rule);
        	ruleSet.add(rule);
        }
		return rules;
	}
	
	/*
	 * Method to convert an RDFS statement to an F-Logic rule.
	 * 
	 * @param statement RDFS statement to be converted
	 * @return rule F-Logic rule
	 */
	protected Rule convertStatement(Statement statement, String defaultNS) 
			throws Exception {
		RDFS2DatalogTransformer rdfs2datalog = new RDFS2DatalogTransformer(defaultNS);
		return rdfs2datalog.transform(statement);
	}
	
	/*
	 * Method to convert a WSML query to an datalog conjunctive query.
	 * 
	 * @param query WSML query
	 * @return query Conjunctive query
	 */
	@SuppressWarnings("unchecked")
	protected Set<ConjunctiveQuery> convertQuery(LogicalExpression q) {
//System.out.println(q.toString());
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
	    LogicalExpressionFactory leFactory = Factory.createLogicalExpressionFactory(null);
        WSML2DatalogTransformer wsml2datalog = new WSML2DatalogTransformer();
        
        List<Term> params = new LinkedList<Term>();
        LogicalExpressionVariableVisitor varVisitor = new LogicalExpressionVariableVisitor();
        q.accept(varVisitor);
        params.addAll(varVisitor.getFreeVariables(q));
        Atom rHead = leFactory.createAtom((Identifier) wsmoFactory
                .createIRI(WSML_RESULT_PREDICATE), params);

        LogicalExpression resultDefRule = leFactory.createInverseImplication(rHead, q);
        List<TransformationRule> lloydToporRules = (List<TransformationRule>) new LloydToporRules(
                new WSMO4JManager());
        LogicalExpressionTransformer lloydToporNormalizer = new TopDownLESplitter(
                lloydToporRules);
        Set<LogicalExpression> conjunctiveQueries = lloydToporNormalizer
                .transform(resultDefRule);

        Set<org.wsml.reasoner.Rule> rules = new HashSet<org.wsml.reasoner.Rule>();

        for (LogicalExpression query : conjunctiveQueries) {
        	rules.add(wsml2datalog.transformLogExpr(query, ruleSet));
        }

        Set<ConjunctiveQuery> result = new HashSet<ConjunctiveQuery>();

        for (org.wsml.reasoner.Rule rule : rules) {
            if (!rule.getHead().getPredicateUri().equals(WSML_RESULT_PREDICATE))
                throw new IllegalArgumentException("Could not transform query " + q);

            List<Literal> body = new LinkedList<Literal>();
            for (Literal l : rule.getBody()) {
                body.add(l);
            }
            result.add(new ConjunctiveQuery(body));
//            System.out.println(new ConjunctiveQuery(body).toString());
        }
        return result;
    }

}
/*
 * $Log: ReasonerImpl.java,v $
 * Revision 1.2  2007-08-23 09:09:01  nathalie
 * added non standard rdfs use exception
 *
 * Revision 1.1  2007/05/24 07:02:20  nathalie
 * created rdfs reasoner
 *
 * 
 */
