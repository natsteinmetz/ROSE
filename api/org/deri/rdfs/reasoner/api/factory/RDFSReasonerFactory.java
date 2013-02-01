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

import java.util.Map;

import org.deri.rdfs.reasoner.api.Reasoner;

/**
 * An interface for  for getting different RDFS reasoners.
 * 
 * <pre>
 *  Created on April 6th, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/api/org/deri/rdfs/reasoner/api/factory/RDFSReasonerFactory.java,v $,
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:22 $
 */
public interface RDFSReasonerFactory {

	public String PARAM_BUILT_IN_REASONER = "BUILT IN REASONER";

	public enum BuiltInReasoner {IRIS};
	
	/**
     * Creates a simple RDF reasoner backed implementation. As reasoning engine 
     * the default engine IRIS is taken.
     * 
     * @return the reasoner
     */
    public Reasoner createSimpleReasoner();
	
    /**
     * Creates an RDF reasoner backed implementation. As reasoning engine 
     * the default engine IRIS is taken.
     * 
     * @return the reasoner
     */
    public Reasoner createRDFReasoner();
    
    /**
     * Creates an RDF reasoner backed implementation
     * 
     * @param params 
     * 		Configuration parameters. The following parameter is
     *      accepted at the moment:
     *      <ul>
     *      	<li>PARAM_BUILT_IN_REASONER: The internal reasoner
     *          	implementation to use, see @link BuiltInReasoner</li>
     *       </ul>
     *       If no parameter is defined, the internal reasoner IRIS is used 
     *       by default.
     * @return the reasoner
     */
    public Reasoner createRDFReasoner(Map<String, Object> params);
    
    /**
     * Creates an RDFS reasoner backed implementation. As reasoning engine 
     * the default engine IRIS is taken.
     * 
     * @return the reasoner
     */
    public Reasoner createRDFSReasoner();
    
    /**
     * Creates an RDFS reasoner backed implementation
     * 
     * @param params 
     * 		Configuration parameters. The following parameter is
     *      accepted at the moment:
     *      <ul>
     *      	<li>PARAM_BUILT_IN_REASONER: The internal reasoner
     *          	implementation to use, see @link BuiltInReasoner</li>
     *       </ul>
     *       If no parameter is defined, the internal reasoner IRIS is used 
     *       by default.
     * @return the reasoner
     */
    public Reasoner createRDFSReasoner(Map<String, Object> params);
    
    /**
     * Creates an eRDF reasoner backed implementation. As reasoning engine 
     * the default engine IRIS is taken.
     * 
     * @return the reasoner
     */
    public Reasoner createERDFSReasoner();    
    
    /**
     * Creates an eRDFS reasoner backed implementation
     * 
     * @param params 
     * 		Configuration parameters. The following parameter is
     *      accepted at the moment:
     *      <ul>
     *      	<li>PARAM_BUILT_IN_REASONER: The internal reasoner
     *          	implementation to use, see @link BuiltInReasoner</li>
     *       </ul>
     *       If no parameter is defined, the internal reasoner IRIS is used 
     *       by default.
     * @return the reasoner
     */
    public Reasoner createERDFSReasoner(Map<String, Object> params);
    
    /**
     * Creates an iOWL reasoner backed implementation. As reasoning engine 
     * the default engine IRIS is taken.
     * 
     * @return the reasoner
     */
    public Reasoner createIOWLReasoner();
    
    /**
     * Creates an iOWL reasoner backed implementation
     * 
     * @param params 
     * 		Configuration parameters. The following parameter is
     *      accepted at the moment:
     *      <ul>
     *      	<li>PARAM_BUILT_IN_REASONER: The internal reasoner
     *          	implementation to use, see @link BuiltInReasoner</li>
     *       </ul>
     *       If no parameter is defined, the internal reasoner IRIS is used 
     *       by default.
     * @return the reasoner
     */
    public Reasoner createIOWLReasoner(Map<String, Object> params);
    
}
/*
 * $Log: RDFSReasonerFactory.java,v $
 * Revision 1.1  2007-05-24 07:02:22  nathalie
 * created rdfs reasoner
 *
 * 
 */