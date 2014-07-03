/**
 * Created by Benjamin on 28/03/2014.
 */
/**
 * Method called when the DOM is fully loaded
 */
$(document).ready(function() {
    // Two datetimeinputs
    $("#input_from").datetimeinput();
    $("#input_until").datetimeinput();
    // Specify dates today and later
    var now = new Date();
    var later = new Date();
    later.setMinutes(later.getMinutes() + 5);
    // First datetimepicker
    $('#datetimepickerfrom').datetimepicker({
        weekStart: 1,
        language: 'nl',
        todayBtn:  0,
        autoclose: 1,
        todayHighlight: 1,
        startView: 2,
        forceParse: 0,
        showMeridian: 1,
        linkFormat: "yyyy-mm-dd HH:ii",
        pickerPosition: 'top-left',
        startDate: now // current date and time
    });
    // Second datetimepicker
    $('#datetimepickeruntil').datetimepicker({
        weekStart: 1,
        language: 'nl',
        todayBtn:  0,
        autoclose: 1,
        todayHighlight: 1,
        startView: 2,
        forceParse: 0,
        showMeridian: 1,
        linkFormat: "yyyy-mm-dd HH:ii",
        pickerPosition: 'top-left',
        startDate: later // 5 minutes later than now
    });
    // Code to link both datetimepickers with each other

    // When datetimepicker from is changed, make sure the user can't select a date before from
    // in datetimepicker until
    $('#datetimepickerfrom').datetimepicker().on('changeDate', function(evt) {
        if(evt.date != null) {
            var date = new Date(evt.date);
            var correctedDate = new Date(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(),
                date.getMinutes(), 0);
            $('#datetimepickeruntil').datetimepicker('setEndDateToNextDisabled', correctedDate);
            correctedDate.setHours(correctedDate.getHours() - 2);
            correctedDate.setMinutes(correctedDate.getMinutes() + 5);
            $('#datetimepickeruntil').datetimepicker('setStartDate', correctedDate);
        } else {
            var correctedDate = new Date();
            correctedDate.setMinutes(correctedDate.getMinutes() + 5);
            $('#datetimepickeruntil').datetimepicker('setStartDate', correctedDate);
            $('#datetimepickeruntil').datetimepicker('setEndDate', Infinity);
        }
    });
    // When datetimepicker until is changed, make sure the user can't select a date after until
    // in datetimepicker until
    $('#datetimepickeruntil').datetimepicker().on('changeDate', function(evt) {
        if(evt.date != null) {
            var date = new Date(evt.date);
            var correctedDate = new Date(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(),
                date.getMinutes(), 0);
            $('#datetimepickerfrom').datetimepicker('setStartDateToPreviousDisabled', correctedDate, new Date());
            correctedDate.setHours(correctedDate.getHours() - 2);
            correctedDate.setMinutes(correctedDate.getMinutes() - 5);
            $('#datetimepickerfrom').datetimepicker('setEndDate', correctedDate);
        } else {
            var correctedDate = new Date();
            $('#datetimepickerfrom').datetimepicker('setStartDate', correctedDate);
            $('#datetimepickerfrom').datetimepicker('setEndDate', Infinity);
        }
    });
});