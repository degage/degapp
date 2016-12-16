/* Referrer.java
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

package data;

import play.api.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the state of a referrer. Used as an url argument when a page can be reached
 * from different pages
 */
public class Referrer {

    private static Map<String,Referrer> map = new HashMap<>();

    public static Referrer register(String caption, Call call, String key) {
        if (map.containsKey(key)) {
            throw new IllegalArgumentException("Referrer with key '" + key + "' already exists");
        } else {
            Referrer r = new Referrer(caption, call, key);
            map.put(key,r);
            return r;
        }
    }

    private Call call;

    private String caption;

    private String key;

    public Referrer(String caption, Call call, String key) {
        this.call = call;
        this.caption = caption;
        this.key = key;
    }

    public Result redirect() {
        return Controller.redirect(call);
    }

    public Html breadcrumb() {
        return views.html.snippets.breadcrumb.apply(caption,call);
    }

    public String getKey() {
        return key;
    }

    public Call getCall() {
        return call;
    }

    public static Referrer get(String key) {
        return map.get(key);
    }
}
