/* Maps.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see http://www.gnu.org/licenses/.
 */

package controllers;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.models.Address;
import com.fasterxml.jackson.databind.JsonNode;
import db.DataAccess;
import db.InjectContext;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.maps.simplemap;

import static play.libs.F.Function;
import static play.libs.F.Promise;

/**
 * Created by Cedric on 3/2/14.
 */
public class Maps extends Controller {

    private static final String TILE_URL = "http://tile.openstreetmap.org/%d/%d/%d.png";
    private static final String ADDRESS_RESOLVER = "http://nominatim.openstreetmap.org/search";

    public static class MapDetails {
        private double latitude;
        private double longtitude;
        private int zoom;
        private String message;

        public MapDetails(double latitude, double longtitude, int zoom, String message) {
            this.latitude = latitude;
            this.longtitude = longtitude;
            this.zoom = zoom;
            this.message = message;
        }

        public MapDetails(double latitude, double longtitude, int zoom) {
            this(latitude, longtitude, zoom, null);
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongtitude() {
            return longtitude;
        }

        public int getZoom() {
            return zoom;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Returns a map tile for given longtitude, latitude and zoom.
     *
     * @param zoom
     * @param x
     * @param y
     * @return An image for given tile
     */
    @AllowRoles
    @InjectContext
    // TODO: inject context probably does not work here
    public static Promise<Result> getMap(int zoom, int x, int y) {
        String mapServer = DataAccess.getInjectedContext().getSettingDAO().getSettingForNow("maps_tile_server");
        return WS.url(String.format(mapServer, zoom, x, y)).get().map(
                new Function<WSResponse, Result>() {
                    public Result apply(WSResponse response) {
                        return ok(response.getBodyAsStream()).as("image/jpeg");
                    }
                }
        );
    }

    /**
     * Resolves the longtitude and latitude for a given address ID
     *
     * @param addressId The address to resolve
     * @return A promise with the longtitude and latitude
     */
    @InjectContext
    // TODO: inject context probably does not work here
    public static Promise<F.Tuple<Double, Double>> getLatLongPromise(int addressId) {
        AddressDAO dao = DataAccess.getInjectedContext().getAddressDAO();
        Address address = dao.getAddress(addressId);
        if (address != null) {
            return WS.url(ADDRESS_RESOLVER)
                    .setQueryParameter("street", address.getNum() + " " + address.getStreet())
                    .setQueryParameter("city", address.getCity())
                    .setQueryParameter("country", "Belgium")
                            // TODO: uncomment postalcode line, it's only commented for test data purposes
                            // .setQueryParameter("postalcode", address.getZip())
                    .setQueryParameter("format", "json").get().map(
                            new Function<WSResponse, F.Tuple<Double, Double>>() {
                                public F.Tuple<Double, Double> apply(WSResponse response) {
                                    JsonNode node = response.asJson();
                                    if (node.size() > 0) {
                                        JsonNode first = node.get(0);
                                        return new F.Tuple<>(first.get("lat").asDouble(), first.get("lon").asDouble());
                                    } else return null;
                                }
                            }
                    );
        } else throw new DataAccessException("Could not find address by ID");
    }

    /**
     * Method: GET
     * Renders a testmap
     *
     * @return A test map
     */
    @AllowRoles
    @InjectContext
    public static Result showMap() {
        return ok(simplemap.render(new MapDetails(51.1891253d, 4.2355338d, 13, "Some marker")));
    }
}
