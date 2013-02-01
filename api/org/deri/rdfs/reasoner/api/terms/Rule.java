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
package org.deri.rdfs.reasoner.api.terms;

import java.util.List;

/**
 * Represents an f-logic rule, of the form: HEAD <- BODY where HEAD 
 * is a single literal and BODY is a list of literals which are 
 * combined conjunctively.
 * 
 * A rule with only a head literal and an empty body is called a fact. 
 * A rule with only body literals and an empty head is called a constraint.
 * 
 * <pre>
 *  Created on April 27th, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/api/org/deri/rdfs/reasoner/api/terms/Rule.java,v $,
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:22 $
 */
public interface Rule {

	/**
     * @return Returns the body.
     */
    public List<FMolecule> getBody();

    /**
     * @return Returns the head.
     */
    public FMolecule getHead();
   
    /**
     * @return true if the rule is a fact, i.e. a rule with an empty body, false otherwise.
     */
    public boolean isFact();
    
    /**
     * @return true if the rule is a constraint, i.e. a rule with an empty head, false otherwise.
     */
    public boolean isConstraint();  
	
}
