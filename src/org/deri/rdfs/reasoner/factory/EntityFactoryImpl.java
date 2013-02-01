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

import java.util.List;

import org.deri.rdfs.reasoner.api.factory.EntityFactory;
import org.deri.rdfs.reasoner.api.terms.FMolecule;
import org.deri.rdfs.reasoner.api.terms.Rule;
import org.deri.rdfs.reasoner.terms.FMoleculeImpl;
import org.deri.rdfs.reasoner.terms.RuleImpl;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Variable;
import org.wsmo.common.Identifier;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;

/**
 * Implementation of a Factory for creating F-Logic entities.
 *
 * <pre>
 *  Created on May 17, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/factory/EntityFactoryImpl.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:21 $
 */
public class EntityFactoryImpl implements EntityFactory {

	private WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
	
	private LogicalExpressionFactory leFactory = Factory.createLogicalExpressionFactory(null);
	
	public FMolecule createFMolecule(Term leftTerm, Term predTerm,
			Term rightTerm) {
		return new FMoleculeImpl(leftTerm, predTerm, rightTerm);
	}

	public Identifier createIdentifier(String name) {
		return wsmoFactory.createIRI(name);
	}

	public Rule createRule(FMolecule head) {
		return new RuleImpl(head);
	}

	public Rule createRule(FMolecule head, List<FMolecule> body) {
		return new RuleImpl(head, body);
	}

	public Variable createVariable(String name) {
		return leFactory.createVariable(name);
	}

}
/*
 * $log: $
 * 
 */
