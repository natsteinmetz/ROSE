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

import java.util.List;
import java.util.Vector;

import org.openrdf.model.Statement;
import org.openrdf.rio.ParseErrorListener;


/**
 * Implementation of a listener for errors of the rdf parser.
 *
 * <pre>
 *  Created on April 6, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/io/RDFParseErrorListener.java,v $
 * </pre>
 *
 * @see org.openrdf.rio#ParseErrorListener
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.2 $ $Date: 2007-08-23 09:13:44 $
 */
public class RDFParseErrorListener implements ParseErrorListener{
    
    private List<RDFParserWarning> warnings = new Vector<RDFParserWarning>();
    
    private List<RDFParserError> errors = new Vector<RDFParserError>();
    
    public RDFParseErrorListener(List<RDFParserWarning> warnings, List<RDFParserError> errors) {
        this.warnings = warnings;
        this.errors = errors;
    }

    /**
     * All warnings that occur during the parsing or the transformation 
     * to a WSMO object model are collected in a List of warnings.
     * 
     * @param msg warning message
     * @param lineNo a line number related to the warning, or -1 if not available or applicable
     * @param colNo a column number related to the warning, or -1 if not available or applicable
     * @see org.openrdf.rio.ParseErrorListener#warning(java.lang.String, int, int)
     * @see RDFParseErrorListener#warning(String msg, int lineNo, int colNo, Statement statement)
     */
    public void warning(String msg, int lineNo, int colNo) {
        warning(msg, lineNo, colNo, null);
    }
    
    /**
     * All warnings that occur during the parsing or the transformation 
     * to a WSMO object model are collected in a List of warnings. All statements that 
     * are concerned by one type of warning are put into a Set of triples belonging 
     * to this warning.
     * 
     * @param msg warning message
     * @param lineNo a line number related to the warning, or -1 if not available or applicable
     * @param colNo a column number related to the warning, or -1 if not available or applicable
     * @param statement the RDF triple that is concerned by this warning
     */
    public void warning(String msg, int lineNo, int colNo, Statement statement) {
        RDFParserWarning w = new RDFParserWarning(msg, lineNo, colNo);
        if (!warnings.contains(w)) {
            warnings.add(w);
        }
        if (statement != null) {
            w = (RDFParserWarning) warnings.get(warnings.indexOf(w));
            w.addToTriples(statement);
        }
    }
    
    /**
     * All errors that occur during the parsing or the transformation 
     * to a WSMO object model are collected in a List of erros.
     * 
     * @param msg error message
     * @param lineNo a line number related to the error, or -1 if not available or applicable
     * @param colNo a column number related to the error, or -1 if not available or applicable
     * @see org.openrdf.rio.ParseErrorListener#error(java.lang.String, int, int)
     */
    public void error(String msg, int lineNo, int colNo) {
        RDFParserError e = new RDFParserError(msg, lineNo, colNo);
        if (!errors.contains(e)) {
        	errors.add(e);
        }
    }

    /**
     * 
     * @param msg fatal error message
     * @param lineNo a line number related to the fatalError, or -1 if not available or applicable
     * @param colNo a column number related to the fatalError, or -1 if not available or applicable
     * @see org.openrdf.rio.ParseErrorListener#fatalError(java.lang.String, int, int)
     */
    public void fatalError(String msg, int lineNo, int colNo) {
    	RDFParserError e = new RDFParserError(msg, lineNo, colNo);
        if (!errors.contains(e)) {
        	errors.add(e);
        }
    }
    
}
/*
 * $Log: RDFParseErrorListener.java,v $
 * Revision 1.2  2007-08-23 09:13:44  nathalie
 * *** empty log message ***
 *
 * Revision 1.1  2007/05/24 07:02:20  nathalie
 * created rdfs reasoner
 *
 * 
 */
