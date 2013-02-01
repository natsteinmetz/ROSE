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
 * Implementation of an F-Logic Attribute Molecule of the 
 * form a[b->>c].
 * 
 * <pre>
 *  Created on April 27th, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/api/org/deri/rdfs/reasoner/api/terms/FMolecule.java,v $,
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:22 $
 */
public interface FMolecule {
	
	/**
	 * Method to get the left, subject, term of an FMolecule.
	 * 
	 * @return left term of the FMolecule
	 */
	public org.omwg.logicalexpression.terms.Term getLeftTerm();

	/**
	 * Method to set the left, subject, term of an FMolecule.
	 * 
	 * @param leftTerm Left term of the FMolecule
	 */
	public void setLeftTerm(org.omwg.logicalexpression.terms.Term leftTerm);
	
	/**
	 * Method to get the predicat term of an FMolecule.
	 * 
	 * @return predicate term of the FMolecule
	 */
	public org.omwg.logicalexpression.terms.Term getPredTerm();

	/**
	 * Method to set the predicate term of an FMolecule.
	 * 
	 * @param predTerm Predicate term of the FMolecule
	 */
	public void setPredTerm(org.omwg.logicalexpression.terms.Term predTerm);
	
	/**
	 * Method to get the right, object, term of an FMolecule.
	 * 
	 * @return right term of the FMolecule
	 */
	public org.omwg.logicalexpression.terms.Term getRightTerm();

	/**
	 * Method to set the right, object, term of an FMolecule.
	 * 
	 * @param rightTerm Right term of the FMolecule
	 */
	public void setRightTerm(org.omwg.logicalexpression.terms.Term rightTerm);
	
	/**
	 * Method to get all Variables from this FMolecule
	 * 
	 * @return list of variables
	 */
	public List<org.omwg.ontology.Variable> getVariables();
	
	/**
	 * Method to check whether a given FMolecule is ground (i.e. 
	 * an FMolecule without variables is ground) or not.
	 * 
	 * @return true if the FMolecule is ground, false otherwise
	 */
	public boolean isGround();

}
/*
 * $Log: FMolecule.java,v $
 * Revision 1.1  2007-05-24 07:02:22  nathalie
 * created rdfs reasoner
 *
 * 
 */