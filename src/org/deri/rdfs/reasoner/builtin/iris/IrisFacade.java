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
package org.deri.rdfs.reasoner.builtin.iris;

import static org.deri.iris.factory.Factory.BASIC;
import static org.deri.iris.factory.Factory.BUILTIN;
import static org.deri.iris.factory.Factory.CONCRETE;
import static org.deri.iris.factory.Factory.PROGRAM;
import static org.deri.iris.factory.Factory.TERM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.deri.iris.Executor;
import org.deri.iris.api.IExecutor;
import org.deri.iris.api.IProgram;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.storage.IRelation;
import org.deri.iris.api.terms.IConstructedTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.api.terms.concrete.IIri;
import org.deri.iris.evaluation.algebra.ExpressionEvaluator;
import org.deri.iris.terms.StringTerm;
import org.deri.iris.terms.concrete.IntegerTerm;
import org.deri.rdfs.reasoner.api.ReasonerFacade;
import org.deri.rdfs.reasoner.api.factory.EntityFactory;
import org.deri.rdfs.reasoner.api.terms.FMolecule;
import org.deri.rdfs.reasoner.api.terms.Rule;
import org.deri.rdfs.reasoner.exception.DatalogException;
import org.deri.rdfs.reasoner.exception.ExternalToolException;
import org.deri.rdfs.reasoner.factory.EntityFactoryImpl;
import org.omwg.logicalexpression.Constants;
import org.omwg.logicalexpression.terms.BuiltInConstructedTerm;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.DataValue;
import org.omwg.ontology.Variable;
import org.omwg.ontology.WsmlDataType;
import org.wsml.reasoner.ConjunctiveQuery;
import org.wsml.reasoner.Literal;
import org.wsmo.common.IRI;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;

/**
 *
 * <pre>
 *  Created on April 6, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/builtin/iris/IrisFacade.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @author Richard Pöttler, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:21 $
 */
public class IrisFacade implements ReasonerFacade{
	
	public final static String PRED_HAS_VALUE = "f-logic-has-value";
	
	public final static String PRED_HAS_STRING_VALUE = "has-string-value";
	
	public final static String PRED_HAS_INT_VALUE = "has-int-value";
	
	public final static String PRED_HAS_BOOL_VALUE = "has-bool-value";
	
	public final static String PRED_HAS_DOUBLE_VALUE = "has-double-value";
	
	// The iris program to evaluate the queries.
	private IProgram p = PROGRAM.createProgram();

	/*
	 * Records whether the ontology changed since the last calculation of the
	 * fixed point.
	 */
	private boolean factsChanged = true;

	/*
	 * Records whether the ontology changed since the last calculation of the
	 * fixed point.
	 */
	private boolean rulesChanged = true;

	// Excutor that executes the queries.
	private IExecutor e = new Executor(p, new ExpressionEvaluator());
	
	/*
	 * Map to determine whether a rule for a conjunctive query was already created. 
	 * <ul>
	 * <li>key = the <code>ConjunctiveQuery</code> object which was substituted</li>
	 * <li>value = the literal uesd to substitute the query</li>
	 * </ul>
	 */
	private Map<ConjunctiveQuery, IQuery> conjunctiveQueries = 
			new HashMap<ConjunctiveQuery, IQuery>();
	
	private EntityFactory factory = new EntityFactoryImpl();
	
	private final DataFactory dataFactory = Factory.createDataFactory(null);
	
	private boolean STRING = false;
	
	private boolean INTEGER = false;
	
	private boolean DOUBLE = false;
	
	private boolean BOOLEAN = false;
	
	public void register(String ontologyURI, Set<Rule> kb) throws ExternalToolException {
		// the ontologyURI is at the moment ignored, because at the moment
		// program only supports one instance per vm
		if (kb == null) {
			throw new ExternalToolException("The knowlebe base must not be null");
		}

		// translating all the rules
		for (Rule r : kb) {
			// the rule is a fact
			if (r.isFact()) { 
				if (p.addFact(literal2Atom(r.getHead()))) {
					factsChanged = true;
				}
			}
			// the rule is an ordinary rule
			else { 
				boolean multiple = false;
				List<ILiteral> body = new ArrayList<ILiteral>(r.getBody()
						.size());
				// check whether the molecule consists of three variables
				for (FMolecule m : r.getBody()) {
					if (m.getLeftTerm() instanceof Variable && 
							m.getPredTerm() instanceof Variable && 
							m.getRightTerm() instanceof Variable) {
						multiple = true;
					}
				}
				
				// converting the body of the rule
				
				// if the molecule is only composed of variables, then we 
				// add the rule not only with PRED_HAS_VALUE in the body, but 
				// do also add rules with the datatype predicates in the body
				if (multiple) {		
					for (FMolecule m : r.getBody()) {
						if (m.getLeftTerm() instanceof Variable && 
								m.getPredTerm() instanceof Variable && 
								m.getRightTerm() instanceof Variable) {
							STRING = true;
							body.add(literal2Literal(m));
							STRING = false;
						}
						else {
							body.add(literal2Literal(m));
						}
					}
					if (p.addRule(BASIC.createRule(BASIC
							.createHead(literal2Literal(r.getHead())), BASIC
							.createBody(body)))) {
						rulesChanged = true;
					}
					body.clear();
					for (FMolecule m : r.getBody()) {
						if (m.getLeftTerm() instanceof Variable && 
								m.getPredTerm() instanceof Variable && 
								m.getRightTerm() instanceof Variable) {
							INTEGER = true;
							body.add(literal2Literal(m));
							INTEGER = false;
						}
						else {
							body.add(literal2Literal(m));
						}
					}
					if (p.addRule(BASIC.createRule(BASIC
							.createHead(literal2Literal(r.getHead())), BASIC
							.createBody(body)))) {
						rulesChanged = true;
					}
					body.clear();
					for (FMolecule m : r.getBody()) {
						if (m.getLeftTerm() instanceof Variable && 
								m.getPredTerm() instanceof Variable && 
								m.getRightTerm() instanceof Variable) {
							DOUBLE = true;
							body.add(literal2Literal(m));
							DOUBLE = false;
						}
						else {
							body.add(literal2Literal(m));
						}
					}
					if (p.addRule(BASIC.createRule(BASIC
							.createHead(literal2Literal(r.getHead())), BASIC
							.createBody(body)))) {
						rulesChanged = true;
					}
					body.clear();
					for (FMolecule m : r.getBody()) {
						if (m.getLeftTerm() instanceof Variable && 
								m.getPredTerm() instanceof Variable && 
								m.getRightTerm() instanceof Variable) {
							BOOLEAN = true;
							body.add(literal2Literal(m));
							BOOLEAN = false;
						}
						else {
							body.add(literal2Literal(m));
						}
					}
					if (p.addRule(BASIC.createRule(BASIC
							.createHead(literal2Literal(r.getHead())), BASIC
							.createBody(body)))) {
						rulesChanged = true;
					}
					body.clear();
				}
				
				// standard converting to PRED_HAS_VALUE
				for (FMolecule m : r.getBody()) {
					body.add(literal2Literal(m));
				}
				if (p.addRule(BASIC.createRule(BASIC
						.createHead(literal2Literal(r.getHead())), BASIC
						.createBody(body)))) {
					rulesChanged = true;
				}
			}
		}
//		Map<IPredicate, IRelation> map = p.getFacts();
//		Set<Entry<IPredicate, IRelation>> entries = map.entrySet();
//		for (Entry<IPredicate, IRelation> entry : entries) {
//			System.out.println("Predicate: " + entry.getKey());
//			IRelation relation = entry.getValue();
//			System.out.println("  " + relation.size());
//			Iterator it = relation.iterator();
//			while (it.hasNext())
//				System.out.println(it.next().toString());
//		}
//		Set<IRule> rulSet = p.getRules();
//		for (IRule iR : rulSet)
//			System.out.println("Rule: " + iR.toString());
	}
	
	public void deregister(String ontologyURI) {
		// the ontologyURI is at the moment ignored, because at the moment
		// program only supports one instance per vm
		rulesChanged = true;
		factsChanged = true;
		p.resetProgram();
	}

	public Set<Map<Variable, Term>> evaluate(ConjunctiveQuery q, String ontologyURI) 
			throws ExternalToolException {
//		System.out.println(q.toString());
		// the ontologyURI is at the moment ignored, because at the moment
		// program only supports one instance per vm
		if (q == null) {
			throw new ExternalToolException("The query must not be null");
		}

		// constructing the query
		List<ILiteral> body = new ArrayList<ILiteral>(q.getLiterals().size());
		
		// converting the literals of the query
		for (Literal l : q.getLiterals()) {
			body.add(literal2Literal(l));
		}
		
		// creating the query
		IQuery query;
		// we got a conjunctive query -> replace it
		if (body.size() > 1) {
			IQuery conjQ = conjunctiveQueries.get(q);
			if (conjQ == null) { // this query was never replaced before
				// getting all variables
				final Set<IVariable> vars = new HashSet<IVariable>();
				for (final ILiteral l : body) {
					vars.addAll(l.getTuple().getAllVariables());
				}
				// creating the new predicate and literal
				final ILiteral conjL = BASIC.createLiteral(true, 
						BASIC.createPredicate("_replacement_" + q.hashCode(), 
								vars.size()), 
						BASIC.createTuple(new ArrayList<ITerm>(vars)));
				// creating and adding the new rule
				p.addRule(BASIC.createRule(BASIC.createHead(conjL), 
						BASIC.createBody(body)));
				// creating and adding the query
				conjQ = BASIC.createQuery(conjL);
				conjunctiveQueries.put(q, conjQ);
			}
			query = conjQ;
		} 
		// this is a normal query
		else { 
//			System.out.println(body.toString());
			query = BASIC.createQuery(body);
		}

		
		// update the executor, if there has been something changed
		// if there are new rules -> translate them all
		if (rulesChanged) { 
			e = new Executor(p, new ExpressionEvaluator());
		}
		// if there are new facts or rules
		if (factsChanged || rulesChanged) { 
			// -> compute the fixed point
			e.execute();
		}
		rulesChanged = false;
		factsChanged = false;

		// constructing the result set
		Set<ITuple> result = e.computeSubstitution(query);
		
		Set<Map<Variable, Term>> res = new HashSet<Map<Variable, Term>>();
		
		List<IVariable> qVars = query.getQueryVariables();
		for (ITuple t : result) {
			Map<Variable, Term> varBinding = new HashMap<Variable, Term>();
			for (IVariable v : qVars) {
				// convert the var to an F-Logic one
				Variable var = (Variable) irisTermConverter(v);

				// searching for the index of the term to extract from the tuple
				int idx = qVars.indexOf(v);
//				int[] idx = searchQueryForVar(query, v);
//				// if the variable couldn't be found
//				if (idx.length < 2) { 
//					throw new IllegalArgumentException("Couldn't find the variable " +
//							"(" + v + ") in query (" + q + ").");
//				}
				varBinding.put(var,irisTermConverter(getTermForTuple(t, idx)));
			}	
			res.add(varBinding);
		}

		// BEHAVIOR IMITATIED FROM THE KAON FACADE
		// if there are no variables in the query, fill it with as many empty
		// map objects as the result size
		if (query.getQueryVariables().isEmpty()) {
			for (int i = 0, max = result.size(); i < max; i++) {
				res.add(new HashMap<Variable, Term>());
			}
		}

		return res;
	}

	/**
	 * Converts an F-Logic molecule to an iris atom.
	 * 
	 * @param mol the F-Logic molecule to convert
	 * @return the iris atom
	 * @throws ExternalToolException if the literal is {@code null}
	 */
	private IAtom literal2Atom(FMolecule mol) throws ExternalToolException {
		if (mol == null) {
			throw new ExternalToolException("The FMolecule must not be null");
		}

		List<ITerm> terms = new ArrayList<ITerm>(2);
		// converting the terms of the literal
		terms.add(wsmoTermConverter(mol.getLeftTerm()));
		terms.add(wsmoTermConverter(mol.getRightTerm()));
		
		// return an ordinary atom
		if (STRING) {
			terms.add(1, wsmoTermConverter(mol.getPredTerm()));
			STRING = false;
			return BASIC.createAtom(BASIC.createPredicate(PRED_HAS_STRING_VALUE, terms.size()), 
				BASIC.createTuple(terms));
		}
		else if (INTEGER) {
			terms.add(1, wsmoTermConverter(mol.getPredTerm()));
			INTEGER = false;
			return BASIC.createAtom(BASIC.createPredicate(PRED_HAS_INT_VALUE, terms.size()), 
				BASIC.createTuple(terms));
		}
		else if (BOOLEAN) {
			terms.add(1, wsmoTermConverter(mol.getPredTerm()));
			BOOLEAN = false;
			return BASIC.createAtom(BASIC.createPredicate(PRED_HAS_BOOL_VALUE, terms.size()), 
				BASIC.createTuple(terms));
		}
		else if (DOUBLE) {
			terms.add(1, wsmoTermConverter(mol.getPredTerm()));
			DOUBLE = false;
			return BASIC.createAtom(BASIC.createPredicate(PRED_HAS_DOUBLE_VALUE, terms.size()), 
				BASIC.createTuple(terms));
		}
		else {
			terms.add(1, wsmoTermConverter(mol.getPredTerm()));
			return BASIC.createAtom(BASIC.createPredicate(PRED_HAS_VALUE, terms.size()), 
				BASIC.createTuple(terms));
		}
//		else {
//			return BASIC.createAtom(BASIC.createPredicate(mol.getPredTerm().toString(), terms.size()), 
//					BASIC.createTuple(terms));
//		}
	}

	/**
	 * Converts an F-Logic Molecule to an iris literal.
	 * 
	 * @param mol the F-Logic molecule to convert
	 * @return the iris literal
	 * @throws ExternalToolException if the literal is {@code null} 
	 */
	private ILiteral literal2Literal(FMolecule mol) throws ExternalToolException {
		if (mol == null) {
			throw new ExternalToolException("The literal must not be null");
		}

		return BASIC.createLiteral(true, literal2Atom(mol));
	}
	
	/**
	 * Converts a iris term to an F-Logic term
	 * 
	 * @param t the iris term
	 * @return the converted F-Logic term
	 * @throws ExternalToolException if the term is {@code null}
	 */
	private Term irisTermConverter(ITerm t) throws ExternalToolException {
		if (t == null) {
			throw new ExternalToolException("The term must not be null");
		}
		/*
		 * subinterfaces of IStringTerm have to be handeled before the
		 * IStringTerm block
		 */
		if (t instanceof IIri) {
			return factory.createIdentifier(((IIri) t).getValue());
		} else if (t instanceof IVariable) {
			return factory.createVariable((String) ((IVariable) t).getValue());		
		} else if(t instanceof StringTerm) {
			return dataFactory.createWsmlString((String) t.getValue());
		} else if(t instanceof IntegerTerm) {
			return dataFactory.createWsmlInteger(((IntegerTerm) t).toString());
		}
		else {
			return factory.createIdentifier((String) t.getValue());
		}
	}
	
	/**
	 * Searches a query for the position of a given variable.
	 * <p>
	 * The returned index describes the path through the query to the given
	 * variable. The first index is the literal and the second the term where
	 * this variable is located. If the term is a constructed term, there migth
	 * be further indexes describing the path through the constructed terms. An
	 * empty array indicates that the variable couldn't be found.
	 * </p>
	 * <p>
	 * An index always starts counting from 0
	 * </p>
	 * <p>
	 * E.g. a returned index of {@code [2, 4, 3]} tells, that the variable is in
	 * the third literal. There it is in the fifth term, which is a constructed
	 * one, and there it is the fourth argument.
	 * <p>
	 * 
	 * @param q the query where to search
	 * @param v the variable for which to look for
	 * @return the index array as described above
	 * @throws ExternalToolException if the query is {@code null}
	 * @throws ExternalToolException if the variable is {@code null}
	 */
	private int[] searchQueryForVar(IQuery q, IVariable v) throws ExternalToolException {
		if (q == null) {
			throw new ExternalToolException("The query must not be null");
		}
		if (v == null) {
			throw new ExternalToolException("Variable must not be null");
		}

		int pos = 0;
		for (ILiteral l : q.getQueryLiterals()) {
			int tPos = 0;
			for (ITerm t : l.getTuple().getTerms()) {
				if (t instanceof IConstructedTerm) {
					int[] res = searchConstructForVar(
						 (IConstructedTerm) t, v);
					if (res.length > 0) {
						int[] ret = new int[res.length + 2];
						ret[0] = pos;
						ret[1] = tPos;
						System.arraycopy(res, 0, ret, 2, res.length);
						return ret;
					}
				} else if (t.equals(v)) {
					return new int[] { pos, tPos };
				}
				tPos++;
			}
			pos++;
		}

		return new int[] {};
	}
	
	/**
	 * Searches a constructed term for the position of a given variable.
	 * <p>
	 * For a explanation how the index is constructed, look at the
	 * {@link #searchQueryForVar(IQuery, IVariable)} documentation.
	 * </p>
	 * 
	 * @param c the constructed ther where to search through
	 * @param v the variable for which to look for
	 * @return the index describing where to find the variable
	 * @throws ExternalToolException if the constructed term is {@code null}
	 * @throws ExternalToolException if the variable is {@code null}
	 * @see #searchQueryForVar(IQuery, IVariable)
	 */
	private int[] searchConstructForVar(IConstructedTerm c,
			IVariable v) throws ExternalToolException {
		if (c == null) {
			throw new ExternalToolException(
					"The constructed term must not be null");
		}
		if (v == null) {
			throw new ExternalToolException("Variable must not be null");
		}

		int pos = 0;
		for (ITerm t : c.getParameters()) {
			if (t instanceof IConstructedTerm) {
				int[] res = searchConstructForVar((IConstructedTerm) t, v);
				if (res.length > 0) {
					int[] ret = new int[res.length + 1];
					ret[0] = pos;
					System.arraycopy(res, 0, ret, 1, res.length);
					return ret;
				}
			} else if (t.equals(v)) {
				return new int[] { pos };
			}
			pos++;
		}

		return new int[] {};
	}
	
	/**
	 * Retrieves the term of a tuple at a given index.
	 * <p>
	 * For a explanation how the index is constructed, look at the
	 * {@link #searchQueryForVar(IQuery, IVariable)} documentation.
	 * </p>
	 * 
	 * @param t the tuple from where to extract the term
	 * @param i the index where to find the term
	 * @return the extracted term
	 * @throws ExternalToolException if the tuple is {@code null}
	 * @throws ExternalToolException if the index is {@code null}
	 * @see #searchQueryForVar(IQuery, IVariable)
	 */
	private ITerm getTermForTuple(ITuple t, int i) throws ExternalToolException {
//		System.out.println("Term: " + t.toString() + " - " + i[0] + " " + i[1]);
//		for (ITerm tt : t.getTerms())
//			System.out.println("  -  " + tt.toString());
		if (t == null) {
			throw new ExternalToolException("The tuple must not be null");
		}
//		if (i == null) {
//			throw new ExternalToolException("The index must not be null");
//		}

		ITerm term = t.getTerm(i);
		if (term instanceof IConstructedTerm) {
			return getTermFromConstruct((IConstructedTerm) term, i, 2);
		}
		return term;
	}

	private ITerm getTermFromConstruct(IConstructedTerm c, int i, int cur) {
		return null;
	}
	
	/**
	 * Retrieves the term of a constructed term at a given index.
	 * <p>
	 * For a explanation how the index is constructed, look at the
	 * {@link #searchQueryForVar(IQuery, IVariable)} documentation.
	 * </p>
	 * 
	 * @param t the constructed term from where to extract the term
	 * @param i the index where to find the term
	 * @param cur the current possition in the index which should be 
	 * 		handeled now.
	 * @return the extracted term
	 * @throws ExternalToolException if the tuple is {@code null}
	 * @throws ExternalToolException if the index is {@code null}
	 * @see #searchQueryForVar(IQuery, IVariable)
	 */
	@SuppressWarnings("unused")
	private ITerm getTermFromConstruct(IConstructedTerm c, int[] i, int cur) 
			throws ExternalToolException {
		if (c == null) {
			throw new ExternalToolException(
					"The constructed term must not be null");
		}
		if (i == null) {
			throw new ExternalToolException("The index must not be null");
		}

		ITerm t = c.getParameter(i[cur]);

		if (t instanceof IConstructedTerm) {
			if (i.length == cur + 1) {
				throw new IllegalArgumentException(
						"We got a constructed term, but no further indexes.");
			}
			return getTermFromConstruct((IConstructedTerm) t, i, cur + 1);
		}

		assert i.length == cur + 1 : "We got a non-constructed term, but further inedes";

		return t;
	}
	
	/**
	 * Converts a wsmo4j literal to an iris literal.
	 * 
	 * @param l the wsmo4j literal to convert
	 * @return the iris literal
	 * @throws ExternalToolException if the literal is {@code null}
	 */
	private ILiteral literal2Literal(Literal l) throws ExternalToolException {
		if (l == null) {
			throw new ExternalToolException("The literal must not be null");
		}
		return BASIC.createLiteral(l.isPositive(), literal2Atom(l));
	}

	/**
	 * Converts a wsmo4j literal to an iris atom. Watch out, the sighn (whether
	 * it is positive, or not) will be ignored.
	 * 
	 * @param l the wsmo4j literal to convert
	 * @return the iris atom
	 * @throws ExternalToolException if the literal is {@code null}
	 */
	private IAtom literal2Atom(Literal l) throws ExternalToolException {
		if (l == null) {
			throw new ExternalToolException("The literal must not be null");
		}

		List<ITerm> terms = new ArrayList<ITerm>(l.getTerms().length);
		// converting the terms of the literal
		for (org.omwg.logicalexpression.terms.Term t : l.getTerms()) {
			terms.add(wsmoTermConverter(t));
		}

		String sym = l.getPredicateUri();
		// checking whether the predicate is a builtin
		if (sym.equals(Constants.EQUAL) || 
				sym.equals(Constants.NUMERIC_EQUAL) || 
				sym.equals(Constants.STRING_EQUAL) || 
				sym.equals(Constants.STRONG_EQUAL)) {
			return BUILTIN.createEqual(terms.get(0), terms.get(1));

		} else if (sym.equals(Constants.INEQUAL) || 
				sym.equals(Constants.NUMERIC_INEQUAL) || 
				sym.equals(Constants.STRING_INEQUAL)) {
			return BUILTIN.createUnequal(terms.get(0), terms.get(1));

		} else if (sym.equals(Constants.LESS_THAN)) {
			return BUILTIN.createLess(terms.get(0), terms.get(1));

		} else if (sym.equals(Constants.LESS_EQUAL)) {
			return BUILTIN.createLessEqual(terms.get(0), terms.get(1));

		} else if (sym.equals(Constants.GREATER_THAN)) {
			return BUILTIN.createGreater(terms.get(0), terms.get(1));

		} else if (sym.equals(Constants.GREATER_EQUAL)) {
			return BUILTIN.createGreaterEqual(terms.get(0), terms.get(1));

		} else if (sym.equals(Constants.NUMERIC_ADD)) {
			return BUILTIN.createAddBuiltin(terms.get(0), terms.get(1), terms.get(2));

		} else if (sym.equals(Constants.NUMERIC_SUB)) {
			return BUILTIN.createSubtractBuiltin(terms.get(0), terms.get(1), terms.get(2));

		} else if (sym.equals(Constants.NUMERIC_MUL)) {
			return BUILTIN.createMultiplyBuiltin(terms.get(0), terms.get(1), terms.get(2));

		} else if (sym.equals(Constants.NUMERIC_DIV)) {
			return BUILTIN.createDivideBuiltin(terms.get(0), terms.get(1), terms.get(2));
		}
		// return an ordinary atom
		return BASIC.createAtom(BASIC.createPredicate(sym, 
				terms.size()), BASIC.createTuple(terms));
	}

	/**
	 * Converts a wsmo4j term to an iris term
	 * 
	 * @param t the wsmo4j term
	 * @return the converted iris term
	 * @throws ExternalToolException if the term is {@code null}
	 * @throws DatalogException if the term-type couldn't be converted
	 */
	
	private ITerm wsmoTermConverter(org.omwg.logicalexpression.terms.Term t) 
			throws ExternalToolException, DatalogException {
		if (t == null) {
			throw new ExternalToolException("The term must not be null");
		}
		if (t instanceof BuiltInConstructedTerm) {
//		} else if (t instanceof ConstructedTerm) {
//			ConstructedTerm ct = (ConstructedTerm) t;
//			List<ITerm> terms = new ArrayList<ITerm>(ct.getArity());
//			for (Term term : (List<Term>) ct.listParameters()) {
//				terms.add(wsmoTermConverter(term));
//			}
//			return TERM.createConstruct(ct.getFunctionSymbol().toString(),
//					terms);
		} else if (t instanceof DataValue) {
			return dataValueConverter((DataValue) t);
		} else if (t instanceof IRI) {
			return CONCRETE.createIri(t.toString());
		} else if (t instanceof org.omwg.ontology.Variable) {
			return TERM.createVariable(((org.omwg.ontology.Variable) t).getName());
//		} else if (t instanceof Identifier) {
//			// i doupt we got something analogous in iris -> exception
//		} else if (t instanceof NumberedAnonymousID) {
//			// i doupt we got something analogous in iris -> exception
//		} else if (t instanceof UnnumberedAnonymousID) {
//			// i doupt we got something analogous in iris -> exception
		}
		throw new DatalogException("Can't convert a term of type "
				+ t.getClass().getName());
	}
	
	/**
	 * Converts a wsmo4j DataValue to an iris ITerm.
	 * 
	 * @param v
	 *            the wsmo4j value to convert
	 * @return the correspoinding ITerm implementation
	 * @throws NullPointerException
	 *             if the value is {@code null}
	 * @throws IllegalArgumentException
	 *             if the term-type couln't be converted
	 * @throws IllegalArgumentException
	 *             if the value was a duration and the duration string couldn't
	 *             be parsed
	 */
	ITerm dataValueConverter(final DataValue v) {
		if (v == null) {
			throw new NullPointerException("The data value must not be null");
		}
		final String t = v.getType().getIRI().toString();
		if (t.equals(WsmlDataType.WSML_BOOLEAN)) {
			BOOLEAN = true;
			return CONCRETE.createBoolean(Boolean.valueOf(v.getValue()
					.toString()));
		} else if (t.equals(WsmlDataType.WSML_DOUBLE)) {
			DOUBLE = true;
			return CONCRETE.createDouble(Double.parseDouble(v.getValue()
					.toString()));
		} else if (t.equals(WsmlDataType.WSML_INTEGER)) {
			INTEGER = true;
			return CONCRETE.createInteger(Integer.parseInt(v.toString()));
		} else if (t.equals(WsmlDataType.WSML_STRING)) {
			STRING = true;
			return TERM.createString(v.toString());
		}
		throw new IllegalArgumentException("Can't convert a value of type " + t);
	}
	
}
/*
 * $log: $
 * 
 */