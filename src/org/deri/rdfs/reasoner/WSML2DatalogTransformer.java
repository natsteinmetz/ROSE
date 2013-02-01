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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deri.rdfs.reasoner.exception.DatalogException;
import org.omwg.logicalexpression.Atom;
import org.omwg.logicalexpression.AttributeConstraintMolecule;
import org.omwg.logicalexpression.AttributeInferenceMolecule;
import org.omwg.logicalexpression.AttributeValueMolecule;
import org.omwg.logicalexpression.CompoundMolecule;
import org.omwg.logicalexpression.Conjunction;
import org.omwg.logicalexpression.Constraint;
import org.omwg.logicalexpression.Disjunction;
import org.omwg.logicalexpression.Equivalence;
import org.omwg.logicalexpression.ExistentialQuantification;
import org.omwg.logicalexpression.Implication;
import org.omwg.logicalexpression.InverseImplication;
import org.omwg.logicalexpression.LogicProgrammingRule;
import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.MembershipMolecule;
import org.omwg.logicalexpression.Negation;
import org.omwg.logicalexpression.NegationAsFailure;
import org.omwg.logicalexpression.SubConceptMolecule;
import org.omwg.logicalexpression.UniversalQuantification;
import org.omwg.logicalexpression.Visitor;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.DataValue;
import org.omwg.ontology.Variable;
import org.omwg.ontology.WsmlDataType;
import org.wsml.reasoner.Literal;
import org.wsml.reasoner.Rule;


/**
 * Transformation from WSML logical expressions to rules.
 *
 * <pre>
 *  Created on April 27, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/WSML2DatalogTransformer.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:23 $
 */
public class WSML2DatalogTransformer implements Visitor{
	
	// Predicates that are used to represent the F-Molecules in datalog.
	public final static String PRED_HAS_VALUE = "f-logic-has-value";
	
	public final static String PRED_HAS_STRING_VALUE = "has-string-value";
	
	public final static String PRED_HAS_INT_VALUE = "has-int-value";
	
	public final static String PRED_HAS_BOOL_VALUE = "has-bool-value";
	
	public final static String PRED_HAS_DOUBLE_VALUE = "has-double-value";

	private List<Literal> datalogBody;

    private Literal datalogHead = null;

    private boolean inHeadOfRule;

    private boolean inBodyOfRule;
    
    private Set<org.deri.rdfs.reasoner.api.terms.Rule> rules;
	
    /**
     * Generates a WSML2Datalog converter.
     */
    public WSML2DatalogTransformer() {
    	datalogBody = new LinkedList<Literal>();
    	inHeadOfRule = false;
    	inBodyOfRule = false;
    }
	
    public Rule transformLogExpr(LogicalExpression expr, Set<org.deri.rdfs.reasoner.api.terms.Rule> rules) {
    	this.rules = rules;
    	Rule rule = null;
    	expr.accept(this);
        rule = (Rule) getSerializedRule();
        if (rule == null) {
            throw new IllegalArgumentException("WSML rule can not be " +
            		"translated to datalog: " + expr.toString());
        }
    	return rule;
    }
    
    /**
     * Constructs a datalog representation of the given WSML rule.
     * The representation is a datalog program.
     * 
     * The construction introduces new predicates for which a defining
     * datalog rules will not be generated here (in order to avoid multiple
     * generation of the same rules). Instead they will be generated only
     * once in WSML2Datalog.transform().
     * 
     * @return the Datalog program that represents the WSML rule or null if
     *         the visited rule does not conform to the syntax requirements
     *         stated in class WSML2Datalog and thus can not be translated
     *         to datalog.
     */
    private Rule getSerializedRule() {
    	Rule rule = null;
    	if (datalogBody.size() != 0) {
    		rule = new Rule(datalogHead, datalogBody);
    	} else if (datalogHead != null) {
    		rule = new Rule(datalogHead);
    	}
    	return rule;
    }

    private void storeLiteral(Literal l) {
    	if (inBodyOfRule) {
    		datalogBody.add(l);
    	} else if (inHeadOfRule) {
    		if (datalogHead == null) {
    			datalogHead = l;
    		} else {
    			throw new DatalogException(
    			"Multiple atoms in the head of a rule are not allowed in " +
    			"simple WSML rules!");
    		}
    	} else {
    		// We do not have an implication but only a simple fact.
    		datalogHead = l;
    	}
    }

    @SuppressWarnings("unchecked")
	public void visitAtom(Atom expr) {
    	String predUri = expr.getIdentifier().toString();
        Literal l;
        if (expr.getArity() > 0) {
        	l = new Literal(true, predUri, expr.listParameters());
        }
        else {
        	l = new Literal(true, predUri, new ArrayList<Term>());
        }
        storeLiteral(l);
	}
    
	public void visitAttributeValueMolecule(AttributeValueMolecule expr) {
		Literal l = null;
		String datatype = null;
		if (expr.getRightParameter() instanceof Variable) {
			for (org.deri.rdfs.reasoner.api.terms.Rule r : rules) {
				if (r.getHead().getPredTerm().equals(expr.getAttribute())) {
					if (r.getHead().getRightTerm() instanceof DataValue) {
						datatype = ((DataValue) r.getHead().getRightTerm())
							.getType().toString();
					}
				}
			}
		}
		if (datatype != null) {
			if (datatype.equals(WsmlDataType.WSML_STRING)) {
				l = new Literal(true, PRED_HAS_STRING_VALUE, expr.getLeftParameter(), 
						expr.getAttribute(), expr.getRightParameter());
			}
			if (datatype.equals(WsmlDataType.WSML_INTEGER)) {
				l = new Literal(true, PRED_HAS_INT_VALUE, expr.getLeftParameter(), 
						expr.getAttribute(), expr.getRightParameter());
			}
			if (datatype.equals(WsmlDataType.WSML_DOUBLE)) {
				l = new Literal(true, PRED_HAS_DOUBLE_VALUE, expr.getLeftParameter(), 
						expr.getAttribute(), expr.getRightParameter());
			}
			if (datatype.equals(WsmlDataType.WSML_BOOLEAN)) {
				l = new Literal(true, PRED_HAS_BOOL_VALUE, expr.getLeftParameter(), 
						expr.getAttribute(), expr.getRightParameter());
			}
		} 
		else if (expr.getRightParameter() instanceof DataValue) {
			DataValue value = (DataValue) expr.getRightParameter();
			if (value.getType().toString().equals(WsmlDataType.WSML_STRING)) {
				l = new Literal(true, PRED_HAS_STRING_VALUE, expr.getLeftParameter(), 
						expr.getAttribute(), expr.getRightParameter());
			}
			if (value.getType().toString().equals(WsmlDataType.WSML_INTEGER)) {
				l = new Literal(true, PRED_HAS_INT_VALUE, expr.getLeftParameter(), 
						expr.getAttribute(), expr.getRightParameter());
			}
			if (value.getType().toString().equals(WsmlDataType.WSML_DOUBLE)) {
				l = new Literal(true, PRED_HAS_DOUBLE_VALUE, expr.getLeftParameter(), 
						expr.getAttribute(), expr.getRightParameter());
			}
			if (value.getType().toString().equals(WsmlDataType.WSML_BOOLEAN)) {
				l = new Literal(true, PRED_HAS_BOOL_VALUE, expr.getLeftParameter(), 
						expr.getAttribute(), expr.getRightParameter());
			}
		} 
		else {
			l = new Literal(true, PRED_HAS_VALUE, expr.getLeftParameter(), 
					expr.getAttribute(), expr.getRightParameter());
		}		
    	storeLiteral(l);
	}

	public void visitConjunction(Conjunction expr) {
		if ((!(expr.getLeftOperand() instanceof Conjunction) && 
    			!(expr.getLeftOperand() instanceof AttributeValueMolecule) &&
    			!(expr.getLeftOperand() instanceof Atom))
    			&& (!(expr.getRightOperand() instanceof Conjunction) & 
    					!(expr.getRightOperand() instanceof AttributeValueMolecule) && 
    					!(expr.getRightOperand() instanceof Atom))) {
    		throw new DatalogException("Query could not be transformed to datalog");
    	}
		expr.getLeftOperand().accept(this);
		expr.getRightOperand().accept(this);
	}

	public void visitLogicProgrammingRule(LogicProgrammingRule expr) {
    	if (!(expr.getLeftOperand() instanceof AttributeValueMolecule) &&
    			!(expr.getLeftOperand() instanceof Atom)) {
    		throw new DatalogException("Query could not be transformed to datalog");
    	}
    	if (!(expr.getRightOperand() instanceof Conjunction) && 
    			!(expr.getRightOperand() instanceof AttributeValueMolecule) &&
    			!(expr.getRightOperand() instanceof Atom)) {
    		throw new DatalogException("Query could not be transformed to datalog");
    	}
    	inHeadOfRule = true;
    	expr.getLeftOperand().accept(this);
    	inHeadOfRule = false;
    	inBodyOfRule = true;
    	expr.getRightOperand().accept(this);
	}

	public void visitAttributeContraintMolecule(AttributeConstraintMolecule expr) {
		throw new DatalogException("Query could not be transformed to datalog");
	}

	public void visitAttributeInferenceMolecule(AttributeInferenceMolecule expr) {
		throw new DatalogException("Query could not be transformed to datalog");
	}
	
	public void visitCompoundMolecule(CompoundMolecule expr) {
		throw new DatalogException("Query could not be transformed to datalog");
	}
	
	public void visitConstraint(Constraint expr) {
		throw new DatalogException("Query could not be transformed to datalog");
	}

	public void visitDisjunction(Disjunction expr) {
		throw new DatalogException("Query could not be transformed to datalog");
	}

	public void visitEquivalence(Equivalence expr) {
		throw new DatalogException("Query could not be transformed to datalog");
	}

	public void visitExistentialQuantification(ExistentialQuantification expr) {
		throw new DatalogException("Query could not be transformed to datalog");
	}

	public void visitImplication(Implication expr) {
		throw new DatalogException("Query could not be transformed to datalog");
	}

	public void visitInverseImplication(InverseImplication expr) {
		throw new DatalogException("Query could not be transformed to datalog");
	}
	
	public void visitMemberShipMolecule(MembershipMolecule expr) {
		throw new DatalogException("Query could not be transformed to datalog");
	}

	public void visitNegation(Negation expr) {
		throw new DatalogException("Query could not be transformed to datalog");
	}

	public void visitNegationAsFailure(NegationAsFailure expr) {
		throw new DatalogException("Query could not be transformed to datalog");
	}

	public void visitSubConceptMolecule(SubConceptMolecule expr) {
		throw new DatalogException("Query could not be transformed to datalog");
	}

	public void visitUniversalQuantification(UniversalQuantification expr) {
		throw new DatalogException("Query could not be transformed to datalog");
	}
}
/*
 * $log: $
 * 
 */