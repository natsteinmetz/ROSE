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

import org.wsml.reasoner.ConjunctiveQuery;

/**
 * Represents an exception that is caused by an external tool during
 * the registration / deregistration of an ontology or during the 
 * execution of a query.
 * 
 * <pre>
 *  Created on April 6, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/exception/ExternalToolException.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:23 $
 */
public class ExternalToolException extends Exception {
    
    private static final long serialVersionUID = 5436234289071988044L;

    public ExternalToolException(String message) {
        super(message);
    }
    
    public ExternalToolException(String message, Throwable t) {
        super(message, t);
    }

    /**
     * Creates an ExternalToolException caused by a problem during 
     * the execution of a query.
     */
    public ExternalToolException(ConjunctiveQuery q) {
        super("Failed to translate query: " + q.toString());
    }

    /**
     * Creates an ExternalToolException caused by a problem during 
     * the execution of a query.
     */
    public ExternalToolException(ConjunctiveQuery q, Throwable t) {
        super("Failed to translate query: " + q.toString(), t);
    }
    
}
/*
 * $log: $
 * 
 */
