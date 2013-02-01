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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.rdfs.reasoner.api.Reasoner;
import org.deri.rdfs.reasoner.exception.ExternalToolException;
import org.deri.rdfs.reasoner.exception.NonStandardRDFSUseException;
import org.deri.rdfs.reasoner.factory.RDFSReasonerFactoryImpl;
import org.deri.rdfs.reasoner.io.RDFParser;
import org.omwg.logicalexpression.*;
import org.omwg.logicalexpression.terms.*;
import org.omwg.ontology.*;
import org.openrdf.model.Graph;
import org.wsmo.common.IRI;
import org.wsmo.factory.*;
import org.wsmo.wsml.*;

/**
 * Web interface for the RDFS Reasoner. Loads the given ontology into the IRIS 
 * reasoner and performs the given query. The result is then displayed on 
 * the Web Page.
 *
 * <pre>
 *  Created on May 18, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/web-tool/src/org/deri/rdfs/reasoner/RDFSReasonerServlet.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.3 $ $Date: 2007-08-24 16:25:36 $
 */
public class RDFSReasonerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private PrintWriter out;

    // PushBackBuffer default for Parser
    private final static int bufferSize = 1123123;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // If it is a get request forward to doPost()
        doPost(request, response);
    }

    /**
     * invoked by button click in rdfs-reasoner.html
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        out = response.getWriter();

        String rdfsOntology = "";
        String wsmlQuery = "";
        
        //needed to get rid of old object in weak hashmap
        System.gc();

        boolean inFrame = request.getParameter("inframe") != null;

        try {
            // rdfs file input from url
            if (request.getParameter("url") != null
                    && request.getParameter("url").indexOf("[url]") == -1) {
                URL url = new URL(request.getParameter("url"));
                InputStream in = url.openStream();
                int k;
                byte buff[] = new byte[bufferSize];
                OutputStream xOutputStream = new ByteArrayOutputStream(
                        bufferSize);
                while ((k = in.read(buff)) != -1) {
                    xOutputStream.write(buff, 0, k);
                }
                rdfsOntology = xOutputStream.toString();

            }
            // wsml file input from textarea
            else if (request.getParameter("rdfsOntology") != null){
                rdfsOntology = request.getParameter("rdfsOntology");
            }

            if (request.getParameter("wsmlQuery") != null){
                wsmlQuery = request.getParameter("wsmlQuery");
            }

            out.println("<!DOCTYPE html PUBLIC '-W3CDTD HTML 4.01 TransitionalEN'>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>DERI RDFS Reasoning result</title>");
            out.println("  <link rel='shortcut icon' href='favicon.ico'/>");
            out.println("  <link rel='stylesheet' type='text/css' href='wsml.css'/>");
            out.println("</head>");
            out.println("<body><div class=\"box\">");

            if (rdfsOntology.length() == 0) {
                error("No Ontology found, enter ontology ");
            } else if (wsmlQuery.length() == 0) {
                error("No Query found, enter Query ");
            } else {
            	rdfsOntology = rdfsOntology.trim();
            	if (request.getParameter("entailment") != null) {
            		try{
                        doReasoning(wsmlQuery, rdfsOntology, inFrame, request.getParameter("entailment"));
                    }catch (Exception e){
                        error("Error:",e);
                    }
                }
            	else {
            		try{
                        doReasoning(wsmlQuery, rdfsOntology, inFrame, "rdfs");
                    }catch (Exception e){
                        error("Error:",e);
                    }
            	}
            }
        } catch (MalformedURLException e) {
            error("Input URL malformed: " + request.getParameter("url"));
        } catch (Exception e) {
            e.printStackTrace();
            error(e.getMessage());
        }
        out.println("</div></body>");
        out.println("</html>");
    }

    private void error(String text) {
        out.println("<div class=\"error\">" + text.replace("\n","<br/>") + "</div><br/><br/><br/>");
    }

    private void error(String text, Throwable e) {
        error(text+ " "+e.getMessage());
        if (e instanceof ParserException){
            return;
        }
        out.println("<div class=\"trace\">");
        String indent="";
        while(e !=null){
            StackTraceElement[] t = e.getStackTrace();
            out.println(e);
            for (int i=0; i<t.length; i++){
                out.println(indent+" &nbsp; "+t[i]+"<br/>");
            }
            e = e.getCause();
            indent += " &nbsp; &nbsp; ";
        }
        out.println("</div>");
    }

    private void doReasoning(String wsmlQuery, String rdfsOntology, 
    		boolean inFrame, String entailment) {

        // setup factories
        WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);        
        LogicalExpressionFactory leFactory = Factory.createLogicalExpressionFactory(null);
        
        // create parser
        Map<String, String> properties = new HashMap<String, String>();
        if (rdfsOntology.startsWith("<?xml version='1.0' encoding='UTF-8'?> ")) {
        	properties.put(RDFParser.RDF_SYNTAX, RDFParser.RDF_XML_SYNTAX);
        }
        else {
        	properties.put(RDFParser.RDF_SYNTAX, RDFParser.N_TRIPLES_SYNTAX);
        }
        RDFParser parser = new RDFParser(properties);

        // parse rdfs ontology 
        Graph ontology = null;
        try {
        	Map<String, Graph> parsedResult = parser.parse(new StringReader(rdfsOntology), "");
        	
        	// get ontology
        	Entry<String, Graph> entry = parsedResult.entrySet().iterator().next();
        	ontology = entry.getValue();
        	if (!(ontology instanceof Graph)){
                error("This reasoner can only process RDFS ontologies at present");
                return;
            }
        } catch (Exception e) {
            String errorline = "";
            error("Could not parse Ontology: " + errorline, e);
            return;
        }
        
        // get namespaces and default namespace
        Map<String, String> namespaces = parser.getNamespaces();
		String defaultNS = parser.getDefaultNS();
		
        out.println("<h1>Query Result Page</h1>");
        if (!inFrame){
            out.println("<h3>Your Query</h3>");
            out.println("<p>" + wsmlQuery + "</p>");
        }
        
        // create dummy wsml ontology to add namespaces
        // these namespaces are used for creating correct WSML queries
        Ontology wsmlOntology = wsmoFactory.createOntology(
        		wsmoFactory.createIRI(defaultNS + "dummy"));
        wsmlOntology.setDefaultNamespace(wsmoFactory.createIRI(defaultNS));
        for (Entry<String, String> entryNS : namespaces.entrySet()) {
			wsmlOntology.addNamespace(wsmoFactory.createNamespace(entryNS.getKey(), 
					wsmoFactory.createIRI(entryNS.getValue())));
		}
        
        // create query
        LogicalExpression query = null;
        try {
            query = leFactory.createLogicalExpression(wsmlQuery, wsmlOntology);
        } catch (ParserException e) {
            error("Error parsing Query:" + markErrorPos(
            		wsmlQuery, e.getErrorPos()), e);
            return;
        }

        if (query != null && ontology != null) {
            if (!inFrame){
                out.println("<h3>Query answer:</h3>");
            }
            
            // get the reasoner
            Reasoner reasoner = null;
            if (entailment.equals("rdf")) {
            	reasoner = RDFSReasonerFactoryImpl.getFactory().createRDFReasoner();
            } 
            else if (entailment.equals("rdfs")) {
            	reasoner = RDFSReasonerFactoryImpl.getFactory().createRDFSReasoner();
            }
            else if (entailment.equals("erdfs")) {
            	reasoner = RDFSReasonerFactoryImpl.getFactory().createERDFSReasoner();
            }
            else if (entailment.equals("simple")) {
            	reasoner = RDFSReasonerFactoryImpl.getFactory().createSimpleReasoner();
            }

            Set<Map<Variable,Term>> result = null;
            // Register ontology and execute query
            try {
                reasoner.registerOntology(ontology, defaultNS);
                result = reasoner.executeQuery(ontology, query);
            } catch (ExternalToolException e) {
                error(e.getMessage());
                return;
            } catch (NonStandardRDFSUseException e) {
            	error(e.getMessage());
                return;
			}

            if (result.size() == 0){
                out.println("<pre>The query returned no variable bindings.</pre>");
            }
            else {
                print(result, ontology, Integer.MAX_VALUE);
            }
            reasoner.deRegisterOntology(ontology, defaultNS);
        }
        else if (query == null) {
        	error("Please indicate a query!");
        }
        else if (ontology == null) {
        	error("Please indicate an ontology!");
        }
    }
    
//    private String resolve(Term iri, Ontology o){
//        VisitorSerializeWSMLTerms v = new VisitorSerializeWSMLTerms(o);
//        iri.accept(v);
//        return v.getSerializedObject().toString();
//    }
    
    private String markErrorPos(String error, int pos){
        if (pos<0)
            return error;
        StringBuffer ret = new StringBuffer("<span style='color:black'>"+
                error.substring(0,pos-1));
        ret.append("<b><i>");
        int i=pos-1;
        for (; i<error.length() && error.charAt(i)!=' '; i++){
            ret.append(error.charAt(i));
        }
        ret.append("</i></b>");
        ret.append(error.substring(i)+"</span>");
        return ret.toString();
    }
    
    private void print(Set<Map<Variable, Term>> result, Graph ontology, int maxResult){
        // print out the results:
        if (result.size() == 0){
            out.println("The query returned no variable bindings.");
            return;
        }
        out.print("<table class=\"result\"><thead><tr>");
        for (Variable var : result.iterator().next().keySet()) {
            out.println("<th>" + var + "</th>");
        }
        out.println("</tr></thead><tbody>");
        int i = 0;
        for (Map<Variable,Term> vBinding : result) {
            out.println("<tr>");
            if(i < maxResult){
                for (Variable var : vBinding.keySet()) {
                	Term value = vBinding.get(var);
                	if (value instanceof IRI) {
                		out.println("<td>" + ((IRI) value).getLocalName() + "</td>");
                	}
                	else {
                		out.println("<td>" + vBinding.get(var).toString() + "</td>");
                	}
                }
            }else if (i==maxResult){
                out.println("<td colspan=\"" + vBinding.keySet().size() + "\">" +
                        "[...] (further results repressed)</td>");
            }
            out.println("</tr>");
            i++;
        }
        out.println("</tbody></table>");
    }
}
/*
 * $log: $
 * 
 */

