<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Les utilisateurs d'OSM</title>
        <link rel="stylesheet" href="os/leaflet-0.7.1/leaflet.css"/>
        <link rel="stylesheet" href="design/index.css"/>
    </head>
    <body>
        <h1>Qui débute ?</h1>
        <div>
          <p>Présente les pseudos qui ont franchi 10 changesets</p>
          <p>
            Voir le site du projet à 
            <a href="https://github.com/Iratetou/usosm">
              https://github.com/Iratetou/usosm
            </a>
          </p>
        </div>
        <div>
          Pour obtenir un lien vers le flux donnant les nouveaux contributeurs
          sur la région voulue, cliquez sur cette région sur la carte.
          <div id="map"></div>
          <div>
            <div>
              <div id="infonominatim"></div>
              <div>
                Code pays : 
                <span id="countrycode"></span>
                <input type="radio" name="region" value="country" id="countryradio">
              </div>
              <div>
                Région : 
                <span id="state"></span>
                <input type="radio" name="region" value="region" checked id="regionradio">
              </div>
              <div>
                <b>Voilà le lien pour le flux correspondant :<br><span id="lienflux">[Cliquez sur la carte SVP.]</span></b>
              </div>
            </div>
            <a href="feeds/">Flux pour obtenir tous les débutants</a>.
          </div>
        </div>
        <div>
          OK OK OK mais je veux 
          <a href="tout.jsp">voir TOUS LES RESULTATS DIRECTEMENT</a>
          .
        </div>
        <script src="os/jquery-2.0.3.min.js"></script>
        <script src="os/leaflet-0.7.1/leaflet.js"></script>
        <script src="design/index.js"></script>
    </body>
</html>
