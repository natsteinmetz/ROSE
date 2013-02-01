ROSE
====

RDF(S) Reasoner

This RDFS Reasoner implementation is based on the work described in Logical foundations of (e)RDF(S): Complexity and reasoning 
(http://www.inf.unibz.it/~jdebruijn/publications-type/Bruijn-Heymans-LogiFoun-07.html). The work has been done under the umbrella of the Semantic Technology Institute (STI) at the University of Innsbruck, Austria. It is published under the LGPL license (http://www.gnu.org/copyleft/lesser.html).

The RDFS Reasoner currently supports the simple, the RDF, the RDFS and the eRDFS entailment regimes. It has the following limitations:
- only simple datatypes are supported (int, double, string).</li>
  
The reasoner is using IRIS (http://iris-reasoner.org) as underlying reasoner engine. 

The source code contains an example that shows how to use the RDFS Reasoner (e.g. parse an RDFS file, create a reasoner, register the graph at the reasoner, execute queries, etc.).