/**
 * Created by Benjamin on 23/03/2014.
 */
// Javascript route
var route = myJsRoutes.controllers.Reserve.showCarsPage;

function loadModal(carId) {
    myJsRoutes.controllers.Reserve.reserve(carId, $('#input_from_value' ).val(), $('#input_to_value' ).val()).ajax({
        success : function(html) {
            $("#resultModal").html(html);
            $('#detailsModal').modal('show');

        },
        error : function() {
            $("#resultModal").html("De reservatie kan niet worden uitgevoerd, probeer later opnieuw");
        }
    });
}

$(document).ready(function() {
    var now = new Date();
    var later = new Date();
    later.setHours(later.getHours() + 1);
    var dateFormat = "yyyy-mm-dd";
    var timeFormat = "hh:ii";
    var inputFrom = $('#input_from_date');
    var inputTo = $('#input_to_date');
    var fromTime = $('#input_from_time');
    var toTime = $('#input_to_time');
    var search = $('#searchButton');

    $('.datepicker.from').datetimepicker({
        language: 'nl',
        autoclose: 1,
        todayHighlight: 1,
        startView: 2,
        minView: 2,
        forceParse: 0,
        showMeridian: 1,
        format: dateFormat,
        initialDate: from,
        pickerPosition: 'bottom-left'
    });

    $('.datepicker.until').datetimepicker({
        language: 'nl',
        autoclose: 1,
        todayHighlight: 1,
        startView: 2,
        minView: 2,
        forceParse: 0,
        showMeridian: 1,
        format: dateFormat,
        initialDate: until,
        pickerPosition: 'bottom-left'
    });

    $('.timepicker').datetimepicker({
        language: 'nl',
        autoclose: 1,
        todayHighlight: 1,
        startView: 1,
        forceParse: 0,
        showMeridian: 1,
        format: timeFormat,
        pickerPosition: 'bottom-left'
    });

    $('.datetimepicker-hours .table-condensed thead').html('').append($('<tr>').append(
        $('<th>').css('width', '180px').text('Selecteer het uur')));
    $('.datetimepicker-minutes .table-condensed thead').html('').append($('<tr>').append(
        $('<th>').css('width', '180px').text('Selecteer de minuten')));

    inputFrom.datetimeinput({
        formatString: dateFormat
    });
    inputTo.datetimeinput({
        formatString: dateFormat
    });
    fromTime.datetimeinput({
        formatString: timeFormat
    });
    toTime.datetimeinput({
        formatString: timeFormat
    });

    $('#extraButton').on('click', function() {
        if(!$('#extraFiltering').hasClass('in')) {
            $('#extraEnv').append($('#filterbuttons'));
            $('#extraButton').html('minder filteropties');
        } else {
            $('#basicEnv').append($('#filterbuttons'));
            $('#extraButton').html('meer filteropties');
        }
    });

    search.on('mousedown', function() {
        $('#input_from_value').val($('#input_from_date').val() + ' ' + $('#input_from_time').val());
        $('#input_to_value').val($('#input_to_date').val() + ' ' + $('#input_to_time').val());
        $('#input_fuel').val($('#selectFuel').find('option:selected').val());
    });

    if(from === '' || until === '') {
        inputFrom.val(now.getFullYear() + '-'
            + (now.getMonth() < 10 ? '0' : '') + (now.getMonth() + 1) + '-'
            + (now.getDate() < 10 ? '0' : '') + now.getDate());
        inputTo.val(later.getFullYear() + '-'
            + (later.getMonth() < 10 ? '0' : '') + (later.getMonth() + 1) + '-'
            + (later.getDate() < 10 ? '0' : '') + later.getDate());
        fromTime.val(
                (now.getHours() < 10 ? '0' : '') + now.getHours() + ':' +
                (now.getMinutes() < 10 ? '0' : '') + now.getMinutes());
        toTime.val(
                (later.getHours() < 10 ? '0' : '') + later.getHours() + ':' +
                (later.getMinutes() < 10 ? '0' : '') + later.getMinutes());
    } else {
        inputFrom.val(from);
        inputTo.val(until);
        fromTime.val('00:00');
        toTime.val('23:55');
        search.trigger('mousedown');
        search.click();
    }

});

// Adjust the value of a checkbox to it's state (checked or unchecked)
$('input[type=checkbox]').each(function () {
    $(this).on('change', function() {
        this.value = this.checked ? 1 : 0;
    })
});