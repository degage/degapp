package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.homepage.information;

public class Information extends Controller {

    // does not need injected context
    public static Result index() {
        return ok(information.render());
    }

}