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
package org.deri.rdfs.reasoner.factory;

import java.util.Map;

import org.deri.rdfs.reasoner.api.Reasoner;
import org.deri.rdfs.reasoner.api.factory.RDFSReasonerFactory;
import org.deri.rdfs.reasoner.impl.ERDFSReasonerImpl;
import org.deri.rdfs.reasoner.impl.IOWLReasonerImpl;
import org.deri.rdfs.reasoner.impl.RDFReasonerImpl;
import org.deri.rdfs.reasoner.impl.RDFSReasonerImpl;
import org.deri.rdfs.reasoner.impl.SimpleReasonerImpl;

/**
 * A default implementation of a factory that constructs RDFS reasoners.
 * 
 * <pre>
 *  Created on April 6th, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/factory/RDFSReasonerFactoryImpl.java,v $,
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:21 $
 */
public class RDFSReasonerFactoryImpl implements RDFSReasonerFactory{

	private final static RDFSReasonerFactoryImpl aFactory = new RDFSReasonerFactoryImpl();
	
	/**
     * @return an instance of the default implementation of the
     * 			RDFSReasonerFactory interface.
     */
    public static RDFSReasonerFactory getFactory() {
    	return aFactory;
    }
	
    public Reasoner createSimpleReasoner(Map<String, Object> params) {
		if (params == null) {
			return new SimpleReasonerImpl(BuiltInReasoner.IRIS);
		}
		else {
            BuiltInReasoner builtin = params
                    .containsKey(PARAM_BUILT_IN_REASONER) ? (BuiltInReasoner) params
                    .get(PARAM_BUILT_IN_REASONER)
                    : BuiltInReasoner.IRIS;
            return new SimpleReasonerImpl(builtin);
        }
	}
    
    public Reasoner createSimpleReasoner() {
    	return createSimpleReasoner(null);
    }
    
    public Reasoner createRDFReasoner(Map<String, Object> params) {
		if (params == null) {
			return new RDFReasonerImpl(BuiltInReasoner.IRIS);
		}
		else {
            BuiltInReasoner builtin = params
                    .containsKey(PARAM_BUILT_IN_REASONER) ? (BuiltInReasoner) params
                    .get(PARAM_BUILT_IN_REASONER)
                    : BuiltInReasoner.IRIS;
            return new RDFReasonerImpl(builtin);
        }
	}
    
    public Reasoner createRDFReasoner() {
    	return createRDFReasoner(null);
    }
    
    public Reasoner createRDFSReasoner(Map<String, Object> params) {
		if (params == null) {
			return new RDFSReasonerImpl(BuiltInReasoner.IRIS);
		}
		else {
            BuiltInReasoner builtin = params
                    .containsKey(PARAM_BUILT_IN_REASONER) ? (BuiltInReasoner) params
                    .get(PARAM_BUILT_IN_REASONER)
                    : BuiltInReasoner.IRIS;
            return new RDFSReasonerImpl(builtin);
        }
	}
    
    public Reasoner createRDFSReasoner() {
    	return createRDFSReasoner(null);
    }
    
	public Reasoner createERDFSReasoner(Map<String, Object> params) {
		if (params == null) {
			return new ERDFSReasonerImpl(BuiltInReasoner.IRIS);
		}
		else {
            BuiltInReasoner builtin = params
                    .containsKey(PARAM_BUILT_IN_REASONER) ? (BuiltInReasoner) params
                    .get(PARAM_BUILT_IN_REASONER)
                    : BuiltInReasoner.IRIS;
            return new ERDFSReasonerImpl(builtin);
        }
	}

	public Reasoner createERDFSReasoner() {
    	return createERDFSReasoner(null);
    }
	
	public Reasoner createIOWLReasoner(Map<String, Object> params) {
		if (params == null) {
			return new IOWLReasonerImpl(BuiltInReasoner.IRIS);
		}
		else {
            BuiltInReasoner builtin = params
                    .containsKey(PARAM_BUILT_IN_REASONER) ? (BuiltInReasoner) params
                    .get(PARAM_BUILT_IN_REASONER)
                    : BuiltInReasoner.IRIS;
            return new IOWLReasonerImpl(builtin);
        }
	}
   
	public Reasoner createIOWLReasoner() {
    	return createIOWLReasoner(null);
    }

}
/*
 * $Log: RDFSReasonerFactoryImpl.java,v $
 * Revision 1.1  2007-05-24 07:02:21  nathalie
 * created rdfs reasoner
 *
 * 
 */
