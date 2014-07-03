/**
 * Created by HannesM on 18/04/14.
 *
 * Tabs have to have id="tabXxx" and the name of the filter-value
 * There also needs to be a searchButton (can be hidden)
 * And also a searchField with id="hidden_input" with the name as the filterfield-name, as we would use for pagination.js
 *
 * For example:
 *
 * <ul class="nav nav-tabs" id="myTab">
 *  <li id="tab_unread" name="0" class="active"><a href="#resultsPanel" data-toggle="tab">Ongelezen</a></li>
 *  <li id="tab_read" name="1"><a href="#resultsPanel" data-toggle="tab">Gelezen</a></li>
 *  <li class="hidden"><button id="searchButton"></button></li>
 * </ul>
 * <input id='hidden_input' name="notification_read" value="1" class="searchTextField hidden">
 */
$(document).ready(function() {
    $('li[id^="tab"]').on('click', function() {
        var name = $(this).attr("name");
        $('#hidden_input').attr("value", name);

        $('#searchButton').click();
    });
});