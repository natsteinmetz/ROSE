package org.deri.rdfs.reasoner.terms;

import java.util.ArrayList;
import java.util.List;

import org.deri.rdfs.reasoner.api.terms.FMolecule;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Variable;


/**
 * Implementation of an F-Logic Attribute Molecule of the 
 * form a[b->>c].
 * 
 *  <pre>
 *  Created on April 27th, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/terms/FMoleculeImpl.java,v $,
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:21 $
 */
public class FMoleculeImpl implements FMolecule{

    private Term leftTerm = null;
    
    private Term predTerm = null;
    
    private Term rightTerm = null;

    public FMoleculeImpl(Term leftTerm, Term predTerm, Term rightTerm) {
    	this.leftTerm = leftTerm;
    	this.predTerm = predTerm;
    	this.rightTerm = rightTerm;
    }

    public Term getLeftTerm() {
        return leftTerm;
    }

    public void setLeftTerm(Term leftTerm) {
        this.leftTerm = leftTerm;
    }
    
    public Term getPredTerm() {
        return predTerm;
    }

    public void setPredTerm(Term predTerm) {
        this.predTerm = predTerm;
    }
    
    public Term getRightTerm() {
        return rightTerm;
    }

    public void setRightTerm(Term rightTerm) {
        this.rightTerm = rightTerm;
    }

    public List<Variable> getVariables() {
        List<Variable> result = new ArrayList<Variable>();
        if (leftTerm instanceof Variable) {
        	result.add((Variable) leftTerm);
        } 
        if (predTerm instanceof Variable) {
        	result.add((Variable) predTerm);
        }
        if (rightTerm instanceof Variable) {
        	result.add((Variable) rightTerm);
        }
        return result;
    }

    public boolean isGround() {
    	if (getVariables().size() == 0) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    public String toString() {
        return leftTerm.toString() + "[" + predTerm.toString() + 
        		" ->> " + rightTerm.toString() + "]";
    }

    /**
     * <p>
     * The <code>equals</code> method implements an equivalence relation
     * on non-null object references. FMolecules are equal if their 
     * terms are equal.
     * </p>
     * <p>
     * It is generally necessary to override the <code>hashCode</code> method 
     * whenever this method is overridden.
     * </p>
     * @param o the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     * @see java.lang.Object#equals(java.lang.Object)
     * @see java.lang.Object#hashCode()
     */
    public boolean equals(Object obj) {
        if (obj instanceof FMoleculeImpl) {
        	FMoleculeImpl f = (FMoleculeImpl) obj;
        	return f.leftTerm.toString().equals(this.leftTerm.toString()) && 
        		f.predTerm.toString().equals(this.predTerm.toString()) && 
        		f.rightTerm.toString().equals(this.rightTerm.toString());
        }
        return false;
    }

    /**
     * <p>
     * If two objects are equal according to the <code>equals(Object)</code> method, then calling
     * the <code>hashCode</code> method on each of the two objects must produce the same integer
     * result. However, it is not required that if two objects are unequal according to
     * the <code>equals(Object)</code> method, then calling the <code>hashCode</code> method on each of the two
     * objects must produce distinct integer results.
     * </p>
     * <p>
     * This method should be overriden, when the <code>equals(Object)</code> method is overriden.
     * </p>
     * @return A hash code value for this Object.
     * @see java.lang.Object#hashCode()
     * @see java.lang.Object#equals(Object)
     */
    public int hashCode() {
    	int hash = 7;
        hash = 31 * hash + (null == leftTerm ? 0 : leftTerm.hashCode());
        hash = 31 * hash + (null == predTerm ? 0 : predTerm.hashCode());
        hash = 31 * hash + (null == rightTerm ? 0 : rightTerm.hashCode());
        return hash;
    }

}
