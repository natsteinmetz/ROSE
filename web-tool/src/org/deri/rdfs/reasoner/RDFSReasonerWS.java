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
package org.deri.rdfs.reasoner;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.deri.rdfs.reasoner.api.Reasoner;
import org.deri.rdfs.reasoner.exception.ExternalToolException;
import org.deri.rdfs.reasoner.exception.NonStandardRDFSUseException;
import org.deri.rdfs.reasoner.factory.RDFSReasonerFactoryImpl;
import org.deri.rdfs.reasoner.io.RDFParser;
import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Variable;
import org.openrdf.model.Graph;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.wsml.ParserException;


/**
 * Provides the Wrapper Class handling the Web Service Requests.
 *
 * <pre>
 *  Created on May 18, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/web-tool/src/org/deri/rdfs/reasoner/RDFSReasonerWS.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @author Holger Lausen, DERI Innsbruck
 * @version $Revision: 1.3 $ $Date: 2007-08-24 16:25:36 $
 */
public class RDFSReasonerWS{
    
	public VariableBinding[][] getSimpleEntailmentQueryAnswerWithRDFXMLSyntax(String wsmlQuery, String rdfsOntology) {
        // create RDFParser
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RDFParser.RDF_SYNTAX, RDFParser.RDF_XML_SYNTAX);
		RDFParser parser = new RDFParser(properties);
        
		// get the RDFS reasoner
        Reasoner reasoner = RDFSReasonerFactoryImpl.getFactory().createSimpleReasoner();
		
        return getQueryAnswers(wsmlQuery, rdfsOntology, parser, reasoner);
    }
    
    public VariableBinding[][] getSimpleEntailmentQueryAnswerWithNTripleSyntax(String wsmlQuery, String rdfsOntology) {
        // create RDFParser
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RDFParser.RDF_SYNTAX, RDFParser.N_TRIPLES_SYNTAX);
		RDFParser parser = new RDFParser(properties);
        
		// get the RDFS reasoner
        Reasoner reasoner = RDFSReasonerFactoryImpl.getFactory().createSimpleReasoner();
		
        return getQueryAnswers(wsmlQuery, rdfsOntology, parser, reasoner);
    }
	
    public VariableBinding[][] getRDFEntailmentQueryAnswerWithRDFXMLSyntax(String wsmlQuery, String rdfsOntology) {
        // create RDFParser
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RDFParser.RDF_SYNTAX, RDFParser.RDF_XML_SYNTAX);
		RDFParser parser = new RDFParser(properties);
        
		// get the RDFS reasoner
        Reasoner reasoner = RDFSReasonerFactoryImpl.getFactory().createRDFReasoner();
		
        return getQueryAnswers(wsmlQuery, rdfsOntology, parser, reasoner);
    }
    
    public VariableBinding[][] getRDFEntailmentQueryAnswerWithNTripleSyntax(String wsmlQuery, String rdfsOntology) {
        // create RDFParser
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RDFParser.RDF_SYNTAX, RDFParser.N_TRIPLES_SYNTAX);
		RDFParser parser = new RDFParser(properties);
        
		// get the RDFS reasoner
        Reasoner reasoner = RDFSReasonerFactoryImpl.getFactory().createRDFReasoner();
		
        return getQueryAnswers(wsmlQuery, rdfsOntology, parser, reasoner);
    }
    
    public VariableBinding[][] getRDFSEntailmentQueryAnswerWithRDFXMLSyntax(String wsmlQuery, String rdfsOntology) {
    	// create RDFParser
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RDFParser.RDF_SYNTAX, RDFParser.RDF_XML_SYNTAX);
		RDFParser parser = new RDFParser(properties);
        
		// get the RDFS reasoner
        Reasoner reasoner = RDFSReasonerFactoryImpl.getFactory().createRDFSReasoner();
        
        return getQueryAnswers(wsmlQuery, rdfsOntology, parser, reasoner);
    }
    
    public VariableBinding[][] getRDFSEntailmentQueryAnswerWithNTripleSyntax(String wsmlQuery, String rdfsOntology) {
    	// create RDFParser
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RDFParser.RDF_SYNTAX, RDFParser.N_TRIPLES_SYNTAX);
		RDFParser parser = new RDFParser(properties);
		
		// get the RDFS reasoner
        Reasoner reasoner = RDFSReasonerFactoryImpl.getFactory().createRDFSReasoner();
        
        return getQueryAnswers(wsmlQuery, rdfsOntology, parser, reasoner);
    }

    public VariableBinding[][] getERDFSEntailmentQueryAnswerWithRDFXMLSyntax(String wsmlQuery, String rdfsOntology) {
        // create RDFParser
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RDFParser.RDF_SYNTAX, RDFParser.RDF_XML_SYNTAX);
		RDFParser parser = new RDFParser(properties);
        
		// get the ERDFS reasoner
        Reasoner reasoner = RDFSReasonerFactoryImpl.getFactory().createERDFSReasoner();
		
        return getQueryAnswers(wsmlQuery, rdfsOntology, parser, reasoner);
    }
    
    public VariableBinding[][] getERDFSEntailmentQueryAnswerWithNTripleSyntax(String wsmlQuery, String rdfsOntology) {
        // create RDFParser
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RDFParser.RDF_SYNTAX, RDFParser.N_TRIPLES_SYNTAX);
		RDFParser parser = new RDFParser(properties);
        
		// get the ERDFS reasoner
        Reasoner reasoner = RDFSReasonerFactoryImpl.getFactory().createERDFSReasoner();
		
        return getQueryAnswers(wsmlQuery, rdfsOntology, parser, reasoner);
    }
    
    private VariableBinding[][] getQueryAnswers(String wsmlQuery, 
    		String rdfsOntology, RDFParser parser, Reasoner reasoner) {
    	WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
        LogicalExpressionFactory leFactory = Factory.createLogicalExpressionFactory(null);
        
    	Map<String, Graph> parsedResult = null;
        Graph ontology = null;
        try {
            parsedResult = parser.parse(new StringReader(rdfsOntology), "");
            // get ontology
        	Entry<String, Graph> entry = parsedResult.entrySet().iterator().next();
        	ontology = entry.getValue();
        } catch (Exception e) {
            throw new RuntimeException ("Error Parsing Ontology: "+e.getMessage());
        }
            
        if (!(ontology instanceof Graph)){
            throw new RuntimeException("This reasoner can only process RDFS ontologies " +
            		"at present");
        }
        
        // get namespaces and default namespace
        Map<String, String> namespaces = parser.getNamespaces();
		String defaultNS = parser.getDefaultNS();
        
		// create dummy wsml ontology to add namespaces
        // these namespaces are used for creating correct WSML queries
        Ontology wsmlOntology = wsmoFactory.createOntology(
        		wsmoFactory.createIRI(defaultNS + "dummy"));
        wsmlOntology.setDefaultNamespace(wsmoFactory.createIRI(defaultNS));
        for (Entry<String, String> entryNS : namespaces.entrySet()) {
			wsmlOntology.addNamespace(wsmoFactory.createNamespace(entryNS.getKey(), 
					wsmoFactory.createIRI(entryNS.getValue())));
		}
		
        LogicalExpression query = null;
        try {
            query = leFactory.createLogicalExpression(wsmlQuery, wsmlOntology);
        } catch (ParserException e) {
            throw new RuntimeException ("Error Parsing Query: "+ e.getMessage());
        }
        
        // Register ontology
        try {
            reasoner.registerOntology(ontology, defaultNS);
        } catch (ExternalToolException e) {
        	throw new RuntimeException ("Error registering ontology at reasoner: "+ e.getMessage());
        } catch (NonStandardRDFSUseException e) {
        	throw new RuntimeException ("Error registering ontology at reasoner: "+ e.getMessage());
		}

        Set<Map<Variable, Term>> result;
		try {
			result = reasoner.executeQuery(ontology, query);
		} catch (ExternalToolException e) {
			throw new RuntimeException ("Error executing query at reasoner: "+ e.getMessage());
		}
        
        if (result.size() == 0){
            return null;
        }
        
        Iterator<Map<Variable,Term>> i = result.iterator();
        VariableBinding[][] vb = null;
        int key = 0;
        while(i.hasNext()){
            Map<Variable,Term> map = i.next();
            Iterator<Variable> keys = map.keySet().iterator();
            if (vb == null){
                vb = new VariableBinding[result.size()][map.size()];
            }
            int n=0;
            //vb[key] = new VariableBinding();
            while (keys.hasNext()){
                Variable v = keys.next();
                vb[key][n] = new VariableBinding();
                vb[key][n].key = v.toString();
                vb[key][n].value = map.get(v).toString();
                n++;
            }
            key++;
        }
        return vb;
    }
    
}
/*
 * $log: $
 * 
 */
