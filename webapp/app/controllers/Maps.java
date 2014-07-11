package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.models.Address;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.Security.RoleSecured;
import db.DataAccess;
import db.InjectContext;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
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
    @RoleSecured.RoleAuthenticated()
    public static Promise<Result> getMap(int zoom, int x, int y) {
        String mapServer = DataProvider.getSettingProvider().getStringOrDefault("maps_tile_server", TILE_URL);
        final Promise<Result> resultPromise = WS.url(String.format(mapServer, zoom, x, y)).get().map(
                new Function<WSResponse, Result>() {
                    public Result apply(WSResponse response) {
                        return ok(response.getBodyAsStream()).as("image/jpeg");
                    }
                }
        );
        return resultPromise;
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
            final Promise<F.Tuple<Double, Double>> resultPromise = WS.url(ADDRESS_RESOLVER)
                    .setQueryParameter("street", address.getNumber() + " " + address.getStreet())
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
            return resultPromise;
        } else throw new DataAccessException("Could not find address by ID");
    }

    /**
     * Method: GET
     * Renders a testmap
     *
     * @return A test map
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result showMap() {
        return ok(simplemap.render(new MapDetails(51.1891253d, 4.2355338d, 13, "Some marker")));
    }
}