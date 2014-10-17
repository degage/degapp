/*
 * Created by Benjamin on 23/03/2014.
 */

 /* TODO: wordt niet langer gebruikt */

// Javascript route
var route = myJsRoutes.controllers.InfoSessions.showSessionsPage;
var today = new Date();
// Make sure the page starts with the correct information
var beginFilter = "from=" + dateToString(today) + ",until=" + dateToString(today, 100);

function dateToString(date, offset_y) {
    var offset_y = offset_y  | 0;
    return (date.getFullYear() + offset_y) + '-' + (date.getMonth()+1) + '-' + date.getDate() +
        ' ' + date.getHours() + ':' + date.getMinutes();
}

$(document).ready(function() {
    // First datetimepicker
    $('#datetimepickerfrom').datetimepicker({
        weekStart: 1,
        language: 'nl',
        todayBtn: 0,
        autoclose: 1,
        todayHighlight: 1,
        startView: 2,
        forceParse: 0,
        showMeridian: 1,
        linkFormat: "yyyy-mm-dd HH:ii",
        startdate: ""
    });
    $('#datetimepickeruntil').datetimepicker({
        weekStart: 1,
        language: 'nl',
        todayBtn: 0,
        autoclose: 1,
        todayHighlight: 1,
        startView: 2,
        forceParse: 0,
        showMeridian: 1,
        linkFormat: "yyyy-mm-dd HH:ii",
        startdate: ""
    });
    $("#input_from").datetimeinput();
    $("#input_until").datetimeinput();
    $('#hidden_from').val(dateToString(today));
    $('#hidden_until').val(dateToString(today, 100));

    $("#input_from").on('change', function() {
        var date = new Date($('#input_from').val());
        if(isNaN(date))
            $('#hidden_from').val(dateToString(today));
        else
            $('#hidden_from').val($('#input_from').val());
    });

    $("#input_until").on('change', function() {
        var date = new Date($('#input_until').val());
        if(isNaN(date)) {
            $('#hidden_until').val(dateToString(today, 100));
        }
        else
            $('#hidden_until').val($('#input_until').val());
    });
});

$('#reset').on('click', function() {
    $("#input_from").datetimeinput("resetDatetimeinput");
    $("#input_until").datetimeinput("resetDatetimeinput");
    $('#hidden_from').val(dateToString(today));
    $('#hidden_until').val(dateToString(today, 100));
    $('#searchButton').click();
});