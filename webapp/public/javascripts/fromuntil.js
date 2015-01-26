/*
 */

/* Extends datetimepicker by automatically copying the value entered in a from-picker to an until-picker,
   unless the latter was already changed. */

$(function(){
  $('#until-container')
    .datetimepicker()
    .on('changeDate', function(ev) {
        $('#until-container').data("altered", true);
    });

  $('#from-container')
    .datetimepicker()
    .on('changeDate', function(ev){
        $untilContainer = $('#until-container');
        if(typeof $untilContainer.data("altered") == 'undefined'){
            $untilContainer.datetimepicker('setUTCDate', ev.date);
            $untilContainer.data("altered", true);
        }
    });
});


