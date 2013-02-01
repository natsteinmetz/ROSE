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
package test.rdfs.reasoner.io;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.deri.rdfs.reasoner.io.RDFParser;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.sesame.sail.StatementIterator;

import junit.framework.TestCase;

/**
 * Test for the RDF parsing.
 *
 * <pre>
 *  Created on April 25, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/test/test/rdfs/reasoner/io/RDFSParserTest.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.2 $ $Date: 2007-08-24 14:04:31 $
 */
public class RDFSParserTest extends TestCase {

	private RDFParser parser = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RDFParser.RDF_SYNTAX, RDFParser.RDF_XML_SYNTAX);
		parser = new RDFParser(properties);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		System.gc();
	}
    
	public void test() throws Exception {
		InputStreamReader reader = new InputStreamReader(ClassLoader
                .getSystemResourceAsStream("test/rdfs/reasoner/io/test.rdfs"));
		Map<String, Graph> result = parser.parse(reader, "");
		Entry<String, Graph> entry = result.entrySet().iterator().next();
		Graph graph = entry.getValue();
		StatementIterator it = graph.getStatements();
		int size = 0;
		while (it.hasNext()) {
			Statement s = (Statement) it.next();
			s.toString();
//			System.out.println(s.toString());
			size++;
		}
		assertEquals(size, 21);
	}
}
