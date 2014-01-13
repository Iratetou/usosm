var map = L.map('map').setView([45.90, 1.52], 5); // Vive la France !
L.tileLayer('http://{s}.tile.cloudmade.com/cbc8737401f34e66a7a677942960dedd/997/256/{z}/{x}/{y}.png', {
  attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://cloudmade.com">CloudMade</a>'
          //maxZoom: 18
})
        .addTo(map);
var repNominatim;
function onMapClick(e)
{
  var reqnomi;

  reqnomi =
          "http://nominatim.openstreetmap.org/reverse?format=json&lat="
          + e.latlng.lat
          + "&lon="
          + e.latlng.lng
          + "&addressdetails=1";
  $("#infonominatim").html(
          "<a href=\""
          + reqnomi
          + "\">envoi question à Nominatim</a> pour "
          + e.latlng);
  $.ajax({
    url: reqnomi,
    data: {
    },
    success: function(data)
    {
      $("#infonominatim").html("<a href=\"" + reqnomi + "\">vu</a>.")
      if (typeof data === "string")
        repNominatim = JSON.parse(data);
      else
        repNominatim = data;
      arrangeLienFlux();
    }
  });
}

map.on('click', onMapClick);

$("#countryradio").click(arrangeLienFlux);
$("#regionradio").click(arrangeLienFlux);

function arrangeLienFlux()
{
  if (typeof repNominatim !== "undefined")
  {
    var address = repNominatim.address;
    if (typeof address === "undefined")
    {
      $("#infonominatim").html(
              "<a href=\"" + reqnomi + "\">pas de géocode ici.</a>.")
      $("#countrycode").html("?");
      $("#state").html("?");
      $("#lienflux").html("(pas de lien)");
    }
    else
    {
      var country_code;
      var state;

      country_code = address.country_code;
      state = address.state;
      if (typeof country_code === "undefined")
        $("#countrycode").html("?");
      else
        $("#countrycode").html(country_code);
      if (typeof state === "undefined")
        $("#state").html("?");
      else
        $("#state").html(state);
      if ($("#regionradio").is(":checked"))
        $("#lienflux").html(
                "<a href=\"feeds/"
                + country_code
                + "/"
                + encodeURIComponent(state)
                + "\">feeds/"
                + country_code
                + "/"
                + state
                + "</a>");
        else
        $("#lienflux").html(
                "<a href=\"feeds/"
                + country_code
                + "\">feeds/"
                + country_code
                + "</a>");
    }
  }
  else
    $("#lienflux").html("[Cliquez sur la carte SVP.]");
}