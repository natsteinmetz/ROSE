<% 
  boolean inFrame=false;
  if (request.getParameter("inframe")!=null){ 
    inFrame=true;
  }
  String eval=request.getParameter("eval");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
  <title>DERI RDFS Reasoner</title>
  <link rel="shortcut icon" href="favicon.ico"/>
  <link rel='stylesheet' type='text/css' href='wsml.css'/>
  <meta name="author" content="Nathalie Steinmetz" />
  <meta name="description" content="The major objective of the Digital Enterprise 
  Research Institute (DERI) is to bring current Web technology to its full 
  potential by combining and improving recent trends around the Web."/>
  
  <script type="text/javascript">

// decrypt helper function
function decryptCharcode(n,start,end,offset) {
	n = n + offset;
	if (offset > 0 && n > end)	{
		n = start + (n - end - 1);
	} else if (offset < 0 && n < start)	{
		n = end - (start - n - 1);
	}
	return String.fromCharCode(n);
}

// decrypt string
function decryptString(enc,offset) {
	var dec = "";
	var len = enc.length;
	for(var i=0; i < len; i++)	{
		var n = enc.charCodeAt(i);
		if (n >= 0x2B && n <= 0x3A)	{
			dec += decryptCharcode(n,0x2B,0x3A,offset);	// 0-9 . , - + / :
		} else if (n >= 0x40 && n <= 0x5A)	{
			dec += decryptCharcode(n,0x40,0x5A,offset);	// A-Z @
		} else if (n >= 0x61 && n <= 0x7A)	{
			dec += decryptCharcode(n,0x61,0x7A,offset);	// a-z
		} else {
			dec += enc.charAt(i);
		}
	}
	return dec;
}

// decrypt spam-protected emails
function linkTo_UnCryptMailto(s)	{
	location.href = decryptString(s,1);
}

</script>
  
</head>
<body>
<div class="box">
<%if (!inFrame){%>
<h1>RDFS Reasoner</h1>
  <p>Enter an RDFS graph either by pasting it into the text area below 
  or enter its URL. The reasoner takes an RDFS file in either RDF/XML or 
  NTriple syntax as input.
  </p>
  <p>You can choose whether you want to use the simple entailment regime, the 
  RDF entailment regime, the RDFS entailment regime or the extensional RDFS 
  (eRDFS) entailment regime by changing the value of the radio button below the 
  text area. By default the RDFS entailment regime is used.
  </p>
  <p>
  As queries only WSML conjunctive queries are allowed. Such a query is build by 
  conjunctions of WSML attribute value molecules. Variables start with a '?' 
  and 'hasValue' is a mandatory WSML keyword.
  </p>
  <p>
  If a default namespace is indicated in the XML document (either a namespace 
  with an empty prefix or a namespace defined as follows: xmlns="myNS"), this 
  is taken as default namespace in the queries. If a namespace other than the 
  default namespace is part of the query, it's 
  prefix can be used as follows: rdf#localname. Please note that the namespace 
  prefixes that may be used in the queries must correspond to the ones used in 
  the XML document. A full URI needs to written as follows in a query: 
  '_"http://test.example.org/test#localname" '.
  </p>
  <p>
  Query examples: 
  <ul>
    <li>?paper[hasAuthor <b>hasValue</b> ?author] and ?paper[submittedTo 
  <b>hasValue</b> ISWC] </li>
    <li>?x[rdf#type <b>hasValue</b> ?y]</li>
  </ul>
  Pressing the &lt;Submit Query&gt; button will then display the variable bindings 
  for the query you entered.
  </p>
<%}%>
<form action="rdfs_reasoner" 
<%
  if(inFrame) out.println("target=\"answer\"");
  else out.println("target=\"_self\"");
%>
method="post">
  <table border="0">
    <tr>
          <td>Graph URL:</td>
          <td><input type="text" size="80" name="url" value="[url]"></td>
     </tr>
    <tr>
      <td style="vertical-align: top;">RDFS Graph:</td>
      <td style="vertical-align: top;">
      <textarea  class="c#:nogutter:nocontrols"
      style="font:normal 10pt Arial;" name="rdfsOntology" cols="100" 
      rows="20"><?xml version='1.0' encoding='UTF-8'?> 
<!DOCTYPE rdf:RDF [
     <!ENTITY rdf 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'>
     <!ENTITY myNS 'http://test.example.org/test#'>
     <!ENTITY rdfs 'http://www.w3.org/2000/01/rdf-schema#'>
]> 

<rdf:RDF xmlns:myNS="&myNS;"
     xmlns:rdf="&rdf;"
     xmlns:rdfs="&rdfs;">

<rdf:Description rdf:about="&myNS;Person">
    <rdfs:subClassOf rdf:resource="&myNS;Animal"/>
</rdf:Description>

<rdf:Description rdf:about="&myNS;Man">
    <rdfs:subClassOf rdf:resource="&myNS;Person"/>
</rdf:Description>

<rdf:Description rdf:about="&myNS;Woman">
    <rdfs:subClassOf rdf:resource="&myNS;Person"/>
</rdf:Description>

<rdf:Description rdf:about="&myNS;Child">
    <rdfs:subClassOf rdf:resource="&myNS;Person"/>
</rdf:Description>

<rdf:Description rdf:about="&myNS;hasParent">
	<rdfs:subPropertyOf rdf:resource="&myNS;hasRelative"/>
    <rdfs:domain rdf:resource="&myNS;Child"/>
    <rdfs:range rdf:resource="&myNS;Person"/>
</rdf:Description>

<rdf:Description rdf:about="&myNS;hasMother">
    <rdfs:subPropertyOf rdf:resource="&myNS;hasParent"/>
    <rdfs:domain rdf:resource="&myNS;Child"/>
    <rdfs:range rdf:resource="&myNS;Woman"/>
</rdf:Description> 

<rdf:Description rdf:about="&myNS;hasFather">
	<rdfs:subPropertyOf rdf:resource="&myNS;hasParent"/>
    <rdfs:domain rdf:resource="&myNS;Child"/>
    <rdfs:range rdf:resource="&myNS;Man"/>
</rdf:Description>

<rdf:Description rdf:about="&myNS;hasName">
    <rdfs:domain rdf:resource="&myNS;Person"/>
    <rdfs:range rdf:resource="&rdfs;Literal"/>
</rdf:Description>

<myNS:Man rdf:about="&myNS;john">
	<myNS:hasMother rdf:resource="&myNS;anna"/>
	<myNS:hasName>John</myNS:hasName>
</myNS:Man>

<myNS:Woman rdf:about="&myNS;anna">
	<myNS:hasName>Anna</myNS:hasName>
</myNS:Woman>

</rdf:RDF>
        </textarea>
        </td>
      </tr>
      <tr><td colspan="2"></td></tr>
      <tr>
         <td>Conjunctive Query:</td>
         <td><input type="text" size="80" name="wsmlQuery" 
         value="?child[hasMother hasValue ?mother] and ?child[rdf#type hasValue ?type]"/>
         </td>
      </tr>
      <tr>
        <td></td>
        <td></td>
      </tr>
      <tr>
        <td></td>
 	    <td>
 	      Entailment Regimes: &nbsp;	&nbsp;	&nbsp;	&nbsp; 
 	      <input type = "radio" name = "entailment" value = "simple">Simple
 	      &nbsp;	&nbsp;	&nbsp;	&nbsp;
          <input type = "radio" name = "entailment" value = "rdf">RDF
          &nbsp;	&nbsp;	&nbsp;	&nbsp;
          <input checked = "checked" type = "radio" name = "entailment" value="rdfs">RDFS
          &nbsp;	&nbsp;	&nbsp;	&nbsp;
          <input type = "radio" name = "entailment" value="erdfs">eRDFS
        </td>
      </tr>
      <tr>
        <td></td>
        <td></td>
      </tr>
      <tr>
        <td></td>
        <td>
          <input class="button" type="submit" value="Submit Query">
          &nbsp;	&nbsp;	&nbsp;	&nbsp;
          <input class="button" type="reset" value="Reset">
        </td>
      </tr>
  </table>
<%if (inFrame){%>
<input type="hidden" name="inframe" value="true"/>
  <% if (eval!=null){ %>
    <input type="hidden" name="eval" value="<%=eval %>"/>
  <%}%>
<%}%>
</form>


<%if (!inFrame) {%>
  <p>&nbsp;</p>
  <div style="font-size:smaller">
  <p>This RDFS Reasoner implementation is based on the work described in 
     <a href="http://www.inf.unibz.it/~jdebruijn/publications-type/Bruijn-Heymans-LogiFoun-07.html">
     Logical foundations of (e)RDF(S): Complexity and reasoning.</a>.
  </p>
  <p>The RDFS Reasoner currently supports the simple, the RDF, the RDFS and the eRDFS entailment 
  regimes. It has the following limitations:</p>
  <ul>
  <li> only simple datatypes are supported (int, double, string).</li>
  </ul>
  <p> The reasoner is using <a href="http://iris-reasoner.org/">IRIS</a> 
  as underlying reasoner engine.
  </p>
  <p>
  The release rdfs-reasoner v0.1 (support for simple, RDF and RDFS Entailment Regimes) can be downloaded at 
  <a href="http://tools.deri.org/rdfs-reasoner/releases/v0.1/rdfs-reasoner-v0.1.zip">rdfs-reasoner v0.1</a>. 
  </p>
  <p>
  The release rdfs-reasoner v0.2 (support for simple, RDF, RDFS and eRDFS entailment regimes) can 
  be downloaded at 
  <a href="http://tools.deri.org/rdfs-reasoner/releases/v0.2/rdfs-reasoner-v0.2.zip">rdfs-reasoner v0.2</a>.
  </p>
  <p>
  The source code contains an example that shows how to use the RDFS Reasoner (e.g. parse an 
  RDFS file, create a reasoner, register the graph at the reasoner, execute queries,...).
  </p>
    
  <p>This service is also accessible as <a href="services/rdfs_reasoner?wsdl">Web Service</a>. <br/></p>
 
  <p>The current interface might be changed without notice, so
  please drop us a <a href="javascript:linkTo_UnCryptMailto('lzhksn9mzsgzkhd-rsdhmldsyZcdqh-nqf');">
  line</a> if you use it.</p>
<%}%>
  </div>
 
<p><small><a href="history.html">Version History</a> | <a href="frame.jsp">Display Using Frames</a> | <a target="_top" href="index.jsp">No Frames</a> </small> </p>
<p><small>$Date: 2007-11-14 09:16:36 $</small>
 
</script> 
</body>
</html>
