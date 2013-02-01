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
package org.deri.rdfs.reasoner.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.openrdf.model.Graph;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.rio.ParseException;
import org.openrdf.rio.Parser;
import org.openrdf.rio.StatementHandlerException;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.openrdf.rio.rdfxml.RdfXmlParser;
import org.openrdf.rio.turtle.TurtleParser;

/**
 * A default implementation for the use of the rio RDF parser.
 * 
 * <pre>
 *  Created on April 6th, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/io/RDFParser.java,v $,
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.3 $ $Date: 2007-08-23 09:13:44 $
 */
public class RDFParser {

	// This map collects the rdf file's namespaces.
    private Map<String, String> namespaces = null;
    
    private String defaultNS = "";
    
    private String defaultNSPrefix = "";
    
	// The RdfXmlParser receives a StatementHandler, a ParseErrorListener
    // and a NamespaceListener.
    private Parser parser = null;
    
    private RDFStatementHandler handler = null;
    
    private RDFParseErrorListener errorListener = null;
    
    private RDFNamespaceListener namespaceListener = null;

    // The RDFParseErrorListener fills  lists with warnings and errors
    private List<RDFParserWarning> warnings = new Vector<RDFParserWarning>();
    
    private List<RDFParserError> errors = new Vector<RDFParserError>();
    
    private Graph graph = null;
    
    // The following strings describe different RDF syntaxes that can be parsed
    
    public static final String RDF_SYNTAX = "RDF syntax";
    
    public static final String RDF_XML_SYNTAX = "RDF/XML Syntax";
    
    public static final String N_TRIPLES_SYNTAX = "N-Triples Syntax";
    
    public static final String TURTLE_SYNTAX = "Turtle Syntax";
    
    public RDFParser(Map<String, String> properties) {
		super();
		
		// setup parser
		Object o = properties.get(RDF_SYNTAX);
        if (o == null || o.equals(RDF_XML_SYNTAX) || !(o.equals(N_TRIPLES_SYNTAX) || 
        		o.equals(TURTLE_SYNTAX))) {
        	// by default, the parser parses rdf/xml syntax
        	parser = new RdfXmlParser();
        }
        else if (o == N_TRIPLES_SYNTAX) {
        	parser = new NTriplesParser();
        }
        else if (o == TURTLE_SYNTAX) {
        	parser = new TurtleParser();
        }	
		setup();
	}

	private void setup() {
		
    	// by default, an in-memory RdfRepository is used as storage backend
        graph = new GraphImpl();
        namespaces = new HashMap<String, String>();
        handler = new RDFStatementHandler(graph);
        errorListener = new RDFParseErrorListener(warnings, errors);
        namespaceListener = new RDFNamespaceListener(namespaces);
	    parser.setVerifyData(true);
	    parser.setStopAtFirstError(false);
	    parser.setParseErrorListener(errorListener);
	    parser.setNamespaceListener(namespaceListener);  
	    parser.setStatementHandler(handler);
    }
	
	/**
	 * This method parses an RDFS ontology and returns a map containing the 
	 * default namespace of the ontology and the resulting RDF graph.
	 * @param reader InputStreamReader	
	 * @param uri Default namespace
	 * @return Map<String, Graph> map containing the default namespace of the 
	 * 		   ontology and the RDF graph resulting from parsing
	 * @throws IOException
	 * @throws ParseException
	 * @throws StatementHandlerException
	 */
	public Map<String, Graph> parse(Reader reader, String uri) 
			throws IOException, ParseException, StatementHandlerException {
		parser.parse(reader, uri);
		
		//TODO log errors
//		//print out parse errors
//		for (RDFParserError e : errors) {
//			System.out.println("Error: " + e.getMessage());
//		}
//		for (RDFParserWarning e : warnings) {
//			System.out.println("Warning: " + e.getMessage());
//		}
		
		// set the default namespace
        namespaceListener.setDefaultNS(uri);
        namespaces = namespaceListener.getNamespaces();
        defaultNS = namespaceListener.getDefaultNS();
        defaultNSPrefix = namespaceListener.getDefaultNSPrefix();
        
        Map<String, Graph> result = new HashMap<String, Graph>();
        result.put(defaultNS, graph);
		return result;
	}
	
	public Map<String, Graph> parse(InputStream input, String uri) 
			throws IOException, ParseException, StatementHandlerException {
		return this.parse(new InputStreamReader(input), uri);
	}
	
	public Map<String, String> getNamespaces() {
		return namespaces;
	}
	
	public String getDefaultNS() {
		return defaultNS;
	}
	
	public String getDefaultNSPrefix() {
		return defaultNSPrefix;
	}
	
}
/*
 * $Log: RDFParser.java,v $
 * Revision 1.3  2007-08-23 09:13:44  nathalie
 * *** empty log message ***
 *
 * Revision 1.2  2007-08-08 16:23:56  nathalie
 * some documentation/maintenance addings
 *
 * Revision 1.1  2007/05/24 07:02:20  nathalie
 * created rdfs reasoner
 *
 * 
 */
