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
package org.deri.rdfs.reasoner.terms;

import java.util.LinkedList;
import java.util.List;

import org.deri.rdfs.reasoner.api.terms.FMolecule;
import org.deri.rdfs.reasoner.api.terms.Rule;

/**
 * Represents an f-logic rule, of the form:  HEAD <- BODY where HEAD 
 * is a single literal and BODY is a list of literals which are 
 * combined conjunctively.
 * 
 * A rule with only a head literal and an empty body is called a fact. 
 * A rule with only body literals and an empty head is called a constraint.
 * 
 * <pre>
 *  Created on April 27th, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/terms/RuleImpl.java,v $,
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:21 $
 */
public class RuleImpl implements Rule{
    
    private List<FMolecule> body = null;
    
    private FMolecule head = null;
    
    /**
     * Creates a rule with the given head and body.
     * 
     * @param body
     * @param head
     */
    public RuleImpl(FMolecule head, List<FMolecule> body) {
        super();
        this.body = body;
        this.head = head;
    } 
    
    /**
     * Creates a fact, i.e. a rule with an empty body.
     * @param head
     */
    public RuleImpl(FMolecule head){
        this(head, new LinkedList<FMolecule>());
    }
    
    /**
     * Creates a constraint, i.e. a rule with an empty head.
     * @param body
     */
    public RuleImpl(List<FMolecule> body) {
    	this(null, body);
    }
    
    /**
     * @return Returns the body.
     */
    public List<FMolecule> getBody() {
        return body;
    }

    /**
     * @return Returns the head.
     */
    public FMolecule getHead() {
        return head;
    }
   
    public boolean isFact(){
        if (body == null || body.size() == 0){
            return true;
        }
        return false;
    }
    
    public boolean isConstraint(){
        return (head == null);
    }  

    public String toString(){
        String result = "";
        
        if (!isConstraint()) {
        	// fact or general rule
            result = this.getHead().toString();
        }     
        if (!isFact()){
            result += " :- ";
            
            int i = 1;
            List<FMolecule> body = this.getBody();
            for (FMolecule literal : body ){
                result += literal.toString();
                if (i < body.size()){
                    result += ", ";
                }
                i++;
            }
        }
        result += ".";
        return result;
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
        if (this == obj)
            return true;
        if ((obj == null) || (obj.getClass() != this.getClass()))
            return false;
        RuleImpl r = (RuleImpl) obj;
        return (head == r.head || (head != null && head.equals(r.head))) 
        		&& (body == r.body || (body != null && body.equals(r.body)));
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
    	hash = 31 * hash + (null == head ? 0 : head.hashCode());
    	hash = 31 * hash + (null == body ? 0 : body.hashCode());
    	return hash;
    }
    
}
