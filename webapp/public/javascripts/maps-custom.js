/**
 * Created by Cedric on 3/23/2014.
 */
$(document).ready(function() {
    if(document.getElementById('map')) {
        var element = $('#map');
        var lon = element.data('lon');
        var lat = element.data('lat');
        var message = element.data('message');
        var zoom = element.data('zoom') || 13;
        // create a map in the "map" div, set the view to a given place and zoom
        var map = L.map('map').setView([lat, lon], zoom);

        // add an OpenStreetMap tile layer
        var url = myJsRoutes.controllers.Maps.getMap('z', 'x', 'y').url;
        url = url.substr(0, url.indexOf("?")) + '?zoom={z}&x={x}&y={y}'; // Cut off the parameters, append template

        L.tileLayer(url, {
            attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> vrijwilligers'
        }).addTo(map);

        // add a marker in the given location, attach some popup content to it and open the popup
        if(typeof(message) != 'undefined' && message.length > 0) {
            L.marker([lat, lon]).addTo(map)
                .bindPopup(message)
                .openPopup();
        } else {
            L.marker([lat, lon]).addTo(map);
        }
    }
});