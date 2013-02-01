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

import java.util.*;
import java.util.Map.Entry;

import org.openrdf.rio.*;

/**
 * Implementation of a namespace listener for the RDF parser.
 *
 * <pre>
 *  Created on April 6, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/io/RDFNamespaceListener.java,v $
 * </pre>
 *
 * @see org.openrdf.rio#NamespaceListener
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:20 $
 */
public class RDFNamespaceListener implements NamespaceListener{

    private Map<String, String> namespaces = null;
    
    private String defaultNS = "";
    
    private String defaultNSPrefix = "";
    
    public RDFNamespaceListener(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }
    
    /**
     * The namespace listener adds all the namespaces to a HashMap.
     * The prefix is the entry's key, the uri is the value.
     *
     * @param prefix the namespace's prefix
     * @param uri the namespace's uri
     */
    public void handleNamespace(String prefix, String uri) {
        namespaces.put(prefix, uri);
    }
    
    /**
     * Get the map with all namespaces
     */
    public Map<String, String> getNamespaces() {
    	return namespaces;
    }
    
    /**
     * Get the default namespace
     */
    public String getDefaultNS() {  	
    	return defaultNS;
    }
    
    /**
     * Set the default namespace
     */
    public void setDefaultNS(String uri) {
    	if (uri == "" || uri == null) {
    		setDefaultNS();
    	}
    	else {
    		defaultNS = uri;
    	}
    }
    
    /**
     * Sets as default namespace a namespace that is unequal to the 
     * standard rdf, rdfs, etc. namespaces.
     */
    public void setDefaultNS() {
//    	String defaultNSCandidate = "http://www.example.org/ontologies/example#";
        Iterator it = namespaces.entrySet().iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.equals("")){
                defaultNS = value;
            }
            else if (key.equals("myNS")) {
            	defaultNS = value;
            }
//            //could be improved to guess better a suitable namespace if
//            //no default was in input
//            if (!key.equals("dc") && !key.equals("owl")
//                && !key.equals("xsd") && !key.equals("part-whole")
//                && !key.equals("rdf") && !key.equals("rdfs")
//                && !key.equals("wsmo") && !key.equals("foaf")
//                && !key.equals("xsi")) {
//                defaultNSCandidate = value;
//                defaultNSPrefix = key;
//            }
        }
//        if (defaultNS == ""){
//            if(defaultNSPrefix != null) {
//                namespaces.remove(defaultNSPrefix);
//            }
//            defaultNS = defaultNSCandidate; 
//        }
    }
    
    /**
     * Get the prefix of the default namespace, if any.
     */
    public String getDefaultNSPrefix() {
    	return defaultNSPrefix;
    }
    
    /**
     * Set the default namespace's prefix.
     */
    public void setDefaultNSPrefix(String prefix) {
    	defaultNSPrefix = prefix;
    }
    
}
/*
 * $Log: RDFNamespaceListener.java,v $
 * Revision 1.1  2007-05-24 07:02:20  nathalie
 * created rdfs reasoner
 *
 * 
 */