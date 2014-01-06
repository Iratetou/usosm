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
        <!-- en cours
        <div>
          Pour obtenir un lien vers le flux donnant les nouveaux contributeurs
          sur une région donnée, cliquez sur cette région sur la carte.
          <div id="map"></div>
        </div>
        -->
        <div>
          OK OK OK mais je veux 
          <a href="tout.jsp">voir TOUS LES RESULTATS DIRECTEMENT</a>
          .
        </div>
        <script src="os/jquery-2.0.3.min.js"></script>
        <script src="os/leaflet-0.7.1/leaflet.js"></script>
        <script>
      		var map = L.map('map').setView([45.90, 1.52], 5); // Vive la France !
          L.tileLayer('http://{s}.tile.cloudmade.com/cbc8737401f34e66a7a677942960dedd/997/256/{z}/{x}/{y}.png', {
          attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://cloudmade.com">CloudMade</a>'
          //maxZoom: 18
          })
          .addTo(map);          
          function onMapClick(e) {
              alert("You clicked the map at " + e.latlng);
          }

          map.on('click', onMapClick);
        </script>
    </body>
</html>
