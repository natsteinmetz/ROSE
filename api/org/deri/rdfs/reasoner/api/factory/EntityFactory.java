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
package org.deri.rdfs.reasoner.api.factory;

import java.util.List;

import org.deri.rdfs.reasoner.api.terms.FMolecule;
import org.deri.rdfs.reasoner.api.terms.Rule;
import org.deri.rdfs.reasoner.exception.DatalogException;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Variable;
import org.wsmo.common.Identifier;

/**
 * Factory interface, used to create F-Logic entities (Rules, FMolecules, 
 * Terms, ...)
 * 
 * <pre>
 *  Created on May 17th, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/api/org/deri/rdfs/reasoner/api/factory/EntityFactory.java,v $,
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:22 $
 */
public interface EntityFactory {

	/** Creates an F-Logic fact (rule without body). 
	 * 
     * @param head The FMolecule that is the head of this rule.
     * @return The newly created Rule object.
     */
    Rule createRule(FMolecule head);
    
    /** Creates an F-Logic rule of the form: HEAD <- BODY.
	 * 
     * @param head The FMolecule that is the head of this rule.
     * @param body A list of FMolecules that build the body of the rule.
     * @return The newly created Rule object.
     */
    Rule createRule(FMolecule head, List<FMolecule> body);
    
    /** Creates an F-Logic molecule of the form a[b ->> c].
	 * 
     * @param leftTerm The left, subject, term of the FMolecule.
     * @param predTerm The predicate term of the FMolecule.
     * @param rightTerm The right, object, term of the FMolecule.
     * @return The newly created FMolecule object.
     */
    FMolecule createFMolecule(Term leftTerm, Term predTerm, Term rightTerm)
    		throws DatalogException;
    
    /** Creates an F-Logic identifier
	 * 
     * @param name The name of the Identifier, e.g. "http://www.example.com/ex"
     * @return The newly created Identifier object.
     */
    Identifier createIdentifier(String name);
    
    /** Creates an F-Logic variable with a given name.
	 * 
     * @param name The name of the Variable, e.g. '?x'.
     * @return The newly created Variable object.
     */
    Variable createVariable(String name);
	
}
/*
 * $Log: EntityFactory.java,v $
 * Revision 1.1  2007-05-24 07:02:22  nathalie
 * created rdfs reasoner
 *
 * 
 */
