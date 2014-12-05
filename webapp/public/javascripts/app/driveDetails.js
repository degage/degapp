/**
 * Created by Benjamin on 05/04/2014.
 */
function setAdjustEnvironment() {
    $('#fromadjust').removeClass('hidden');
    $('#fromdetails').addClass('hidden');
    $('#untiladjust').removeClass('hidden');
    $('#untildetails').addClass('hidden');
    $('#submit').removeClass('hidden');
    $('#adjust').text('Annuleer');
    $('#annulateReservation').addClass('hidden');
}

function hideAdjustEnvironment() {
    $('#fromdetails').removeClass('hidden');
    $('#fromadjust').addClass('hidden');
    $('#untildetails').removeClass('hidden');
    $('#untiladjust').addClass('hidden');
    $('#submit').addClass('hidden');
    $('#adjust').text('Reservatie inkorten');
    $('#annulateReservation').removeClass('hidden');
}

$(document).ready(function() {
    $('#adjust').on('click', function() {
        if(!($('#fromadjust').hasClass('hidden'))) {
            hideAdjustEnvironment();
        } else {
            setAdjustEnvironment();
        }
    });

    $('#adjustInfo').on('click', function() {
       $('#adjustEnv').removeClass('hidden');
    })

    $('#annulateAdjustInfo').on('click', function() {
        $('#adjustEnv').addClass('hidden');
    })

    if($('#datetimepickerfrom') === undefined)
        return;

    $('#datetimepickerfrom').datetimepicker({
        weekStart: 1,
        language: 'nl',
        todayBtn: 0,
        autoclose: 1,
        todayHighlight: 1,
        startView: 2,
        forceParse: 0,
        showMeridian: 1,
        linkFormat: "yyyy-mm-dd HH:ii"
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
        linkFormat: "yyyy-mm-dd HH:ii"
    });
    // TODO: can these be avoided?
    //$("#input_from").datetimeinput();
    //$("#input_until").datetimeinput();

    // Disable all dates excepted those within the reservation dates
    $('#datetimepickerfrom').datetimepicker('setStartDate', correctedFrom);
    $('#datetimepickeruntil').datetimepicker('setStartDate', adjustedFrom);

    $('#datetimepickeruntil').datetimepicker('setEndDate', correctedUntil);
    $('#datetimepickerfrom').datetimepicker('setEndDate', adjustedUntil);

    // When datetimepicker from is changed, make sure the user can't select a date before from
    // in datetimepicker until
    $('#datetimepickerfrom').datetimepicker().on('changeDate', function(evt) {
        if(evt.date != null) {
            var date = new Date(evt.date);
            var correctedDate = new Date(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(),
                date.getMinutes(), 0);
            correctedDate.setHours(correctedDate.getHours() - 2);
            correctedDate.setMinutes(correctedDate.getMinutes() + 5);
            $('#datetimepickeruntil').datetimepicker('setStartDate', correctedDate);
        } else {
            $("#input_from").val('@reservation.getFrom.toString("yyyy-MM-dd HH:mm")');
            $('#datetimepickeruntil').datetimepicker('setStartDate', adjustedFrom);
        }
    });
    // When datetimepicker until is changed, make sure the user can't select a date after until
    // in datetimepicker until
    $('#datetimepickeruntil').datetimepicker().on('changeDate', function(evt) {
        if(evt.date != null) {
            var date = new Date(evt.date);
            var correctedDate = new Date(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(),
                date.getMinutes(), 0);
            correctedDate.setHours(correctedDate.getHours() - 2);
            correctedDate.setMinutes(correctedDate.getMinutes() - 5);
            $('#datetimepickerfrom').datetimepicker('setEndDate', correctedDate);
        } else {
            $("#input_until").val('@reservation.getTo.toString("yyyy-MM-dd HH:mm")');
            $('#datetimepickerfrom').datetimepicker('setEndDate', adjustedUntil);
        }
    });
});
