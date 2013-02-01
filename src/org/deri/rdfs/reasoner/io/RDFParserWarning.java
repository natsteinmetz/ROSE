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
package org.deri.rdfs.reasoner.io;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Statement;

/**
 * Implementation of an RDF Parser Warning.
 *
 * <pre>
 *  Created on April 6, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/io/RDFParserWarning.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:20 $
 */
public class RDFParserWarning extends RDFParserMessage{
    
    private Set<Statement> triples = null;
    
    public RDFParserWarning(String warningMessage, int lineNo, int colNo) {
        super(warningMessage, lineNo, colNo);
        if (triples == null) {
            triples = new HashSet<Statement>();
        }
    }
    
    /**
     * All triples that were not recognized, and thus not transformed 
     * and added to the WSMO object model, are put into a Set. 
     * 
     * @param statement statement to be added to the triples set
     */
    public void addToTriples(Statement statement) {
        triples.add(statement);
    }
    
    /**
     * This method returns a Set containing all triples 
     * that were not recognized, and thus not transformed 
     * and added to the WSMO object model. 
     * 
     * @return Set of RDF statements concerned by this warning
     */
    public Set getTriples() {
        return triples;
    }

    /**
     * <p>
     * The <code>equals</code> method implements an equivalence relation
     * on non-null object references. RDFParserWarnings are equal if their 
     * warning message and their line and column numbers are equal.
     * </p>
     * <p>
     * It is generally necessary to override the <code>hashCode</code> method whenever this method
     * is overridden.
     * </p>
     * @param o the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     * @see java.lang.Object#equals(java.lang.Object)
     * @see java.lang.Object#hashCode()
     */
    public boolean equals(Object obj) {
        if (obj instanceof RDFParserWarning) {
            RDFParserWarning w = (RDFParserWarning) obj;
            return ((w.getMessage().equals(this.getMessage())
                     && (w.getLine() == this.getLine()) && 
                     (w.getColumn() == this.getColumn())));
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
        return super.hashCode();
    }
   
}
/*
 * $Log: RDFParserWarning.java,v $
 * Revision 1.1  2007-05-24 07:02:20  nathalie
 * created rdfs reasoner
 *
 * 
 */
