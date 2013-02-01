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
package test.rdfs.reasoner;

import test.rdfs.reasoner.entailment.ERDFSEntailmentTest;
import test.rdfs.reasoner.entailment.RDFEntailmentTest;
import test.rdfs.reasoner.entailment.RDFSEntailmentTest;
import test.rdfs.reasoner.io.RDFSParserTest;
import test.rdfs.reasoner.transformation.ERDFSRulesTest;
import test.rdfs.reasoner.transformation.RDFRulesTest;
import test.rdfs.reasoner.transformation.RDFSRulesTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Collection of all RDFS reasoner tests.
 *
 * <pre>
 *  Created on May 17, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/test/test/rdfs/reasoner/ReasonerTests.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.2 $ $Date: 2007-08-24 14:04:31 $
 */
public class ReasonerTests {

	public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
	
	 public static Test suite() {
	        TestSuite suite = new TestSuite("Test suite for the RDFS reasoner framework");
	        // $JUnit-BEGIN$
	        suite.addTestSuite(RDFSParserTest.class);
	        suite.addTestSuite(RDFRulesTest.class);
	        suite.addTestSuite(RDFSRulesTest.class);
	        suite.addTestSuite(ERDFSRulesTest.class);
	        suite.addTestSuite(RDFEntailmentTest.class);
	        suite.addTestSuite(RDFSEntailmentTest.class);
	        suite.addTestSuite(ERDFSEntailmentTest.class);
	        // $JUnit-END$
	        return suite;
	    }
	
}
/*
 * $log: $
 * 
 */