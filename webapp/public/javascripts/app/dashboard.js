/**
 * Created by Benjamin on 15/05/2014.
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