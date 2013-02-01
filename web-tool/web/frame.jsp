<% 
  String eval=request.getParameter("eval");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
  <title>RDFS Reasoner</title>
  <link rel="shortcut icon" href="favicon.ico"/>
</head>
<frameset rows="490,*" frameborder="yes" border="1" framespacing="0" cols="*">
  <frame name="query" src="index.jsp?inframe=true<%if(eval!=null) out.print("&eval="+eval); %>">
  <frame name="answer" src="">
</frameset>
</html>
