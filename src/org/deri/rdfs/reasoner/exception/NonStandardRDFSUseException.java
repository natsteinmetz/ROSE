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
package org.deri.rdfs.reasoner.exception;

import java.util.List;
import java.util.Vector;

/**
 * Represents an exception that occurs when there is a 
 * non-standard use of the RDFS vocabulary within the RDF graph.
 * 
 * <pre>
 *  Created on August 9, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/exception/NonStandardRDFSUseException.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-08-23 09:09:01 $
 */
public class NonStandardRDFSUseException extends Exception {

	private static final long serialVersionUID = 1L;

	private static List<String> nonStandardUsages = new Vector<String>();
	
	/**
     * @param message - explanation for the exception
     */
    public NonStandardRDFSUseException(String message) {
        super(message);
    }
    
    /**
     * Creates an NonStandardRDFSUseException caused by a problem during 
     * the execution of a query.
     * 
     * @param s - statement that causes the exception
     * @throws NonStandardRDFSUseException 
     * @throws Exception 
     */
    public NonStandardRDFSUseException(List<String> usages) 
    		throws NonStandardRDFSUseException {
    	NonStandardRDFSUseException.nonStandardUsages = usages;
    	String error = "Extensional RDFS entailment not possible: " +
    			"non-standard use of RDFS vocabulary occured " +
    			"in the following statements: ";
    	for (String s : usages) {
    		error = error + "\n" + s;
    	}
    	throw new NonStandardRDFSUseException(error);
    }
    
    /**
     * 
     * @return List containing string forms of the statements causing 
     * the non-standard usage of RDFS vocabulary
     */
    public static List<String> getNonStandardRDFSUsages() {
    	return nonStandardUsages;
    }
    
}
/*
 * $log: $
 * 
 */