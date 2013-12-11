<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Les utilisateurs d'OSM</title>
    </head>
    <body>
        <h1>Qui débute ?</h1>
        <p>Présente les pseudos qui ont franchi 10 changesets</p>
        <p>
          <c:set var="places" value="${ap.placesDeChangement}"/>
          <c:forEach var="rap" items="${ap.rapports}">
          <h2><fmt:formatDate type="date" value="${rap.diff.timestamp}"/></h2>
          <dl>
            <c:forEach var="déb" items="${rap.bonsDébutants}">
              <dt>
                <a href="http://www.openstreetmap.org/user/${déb.pseudo}">${déb.pseudo}</a> ${déb.UID}
              </dt>
              <dd>${places[déb.UID]}</dd>
            </c:forEach>
          </dl>
          </c:forEach>
        </p>
    </body>
</html>
