/**
 * Created by Benjamin on 15/05/2014.
 */

/**
 * [kc] Used on dashboard to fill calendar with current registrations
 */
function registrationEvent (from, until, name, detailsLink) {
       var parts1 = from.split(" ");
       var d1 = parts1[0].split("-");
       var t1 = parts1[1].split(":");
       var parts2 = until.split(" ");
       var d2 = parts2[0].split("-");
       var t2 = parts2[1].split(":");
       //events.push(
       return new CalendarEvent(name,
          new Date(d1[0],(d1[1]-1),d1[2],t1[0],t1[1]),
          new Date(d2[0],(d2[1]-1),d2[2],t2[0],t2[1]),
          detailsLink,
          "details"
        );
}

/**
 * Fill calendar when document is loaded.
 * Uses global events variable!
 */
$(document).ready(function() {
    var inputFrom = $('#input_from');
    var inputUntil = $('#input_until');
    var calendar = $('#calendar');
    var collapseID = 'button_collapse';
    calendar.calendar({
        startDateId: inputFrom,
        endDateId: inputUntil,
        events: events
    });
    $('.custom_calendar').css('margin-bottom', '0px');
    calendar.append($('<div>')
        .attr('id', collapseID)
        .attr('class', 'collapse')
        .append($('<button>')
            .attr('type', 'submit')
            .attr('class', 'btn btn-block btn-sm btn-default')
            // .css('border-radius', '0px 0px 10px 10px')
            .css('background-color', '#d4d4d4')
            .append($('<b>')
                .text('Zoek naar auto\'s')
            )
        )
    );
    $('input[id^=input]').on("change", function() {
        $('#' + collapseID).collapse('show');
    });
});

