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

/**
 * This class gives structure to an abstract RDF Parser Message, 
 * which can be either a warning or an error.
 *
 * <pre>
 *  Created on April 6, 2007
 *  Committed by $Author: nathalie $
 *  $Source: /usr/local/cvsroot/rdfs-reasoner/src/org/deri/rdfs/reasoner/io/RDFParserMessage.java,v $
 * </pre>
 *
 * @author Nathalie Steinmetz, DERI Innsbruck
 * @version $Revision: 1.1 $ $Date: 2007-05-24 07:02:20 $
 */
public abstract class RDFParserMessage {

	private String message;
	
	private int lineNo;
    
    private int colNo;
	
    public RDFParserMessage(String message, int lineNo, int colNo) {
        this.message = message;
        this.lineNo = lineNo;
        this.colNo = colNo;
    }
    
    /**
     * Gets the message associated with this parser message.
     * 
     * @return The parser message.
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Gets the line number associated with this parser message.
     * 
     * @return A line number, or -1 if no line number is available or applicable.
     */
    public int getLine() {
        return lineNo;
    }
    
    /**
     * Gets the column number associated with this parser message.
     * 
     * @return A column number, or -1 if no column number is available or applicable.
     */
    public int getColumn() {
        return colNo;
    }
    
	/**
     * @return A textual representation of an RDFParserWarning
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (lineNo != -1) {
            return message + " - at line: " + lineNo + ", at column: " + colNo;
        }
        else {
            return message;
        }
    }
    
}
/*
 * $Log: RDFParserMessage.java,v $
 * Revision 1.1  2007-05-24 07:02:20  nathalie
 * created rdfs reasoner
 *
 *
 */