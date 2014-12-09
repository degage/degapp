/**
 * Created by HannesM on 28/04/14.
 */

function deleteAvailability(e) {
    var currentRow = $(e).closest('tr');
    var inputs = currentRow.find("input");
    for(var i = 0; i < inputs.length; i++) {
        if(inputs[i].classList.contains("availability_id")) {
            // Tricky way to find this class, but doesn't seem to work in another way
            if(inputs[i].value == 0) { // Just created, not in database yet -> hard delete
                currentRow.remove();
            } else { // Already in database -> make hidden and flip id to negative
                inputs[i].value = -1 * inputs[i].value;
                currentRow.hide();
            }
            break;
        }
    }
}

function newAvailability(e) {
    var currentRow = $(e).closest('tr');
    currentRow.before(newRow());
    installDateTimePickers()
}

// TODO: clone a HTML-element?
function newRow() {
    return '' +
        '<tr>' +
        '<td class="hidden">' +
            '<input class="availability_id hidden" value="0">' +
        '</td>' +
        '<td>' +
            '<form class="form-inline">' +
            '<select class="beginDayOfWeek form-control">' +
                '<option value="0" >Zondag</option>' +
                '<option value="1" >Maandag</option>' +
                '<option value="2" >Dinsdag</option>' +
                '<option value="3" >Woensdag</option>' +
                '<option value="4" >Donderdag</option>' +
                '<option value="5" >Vrijdag</option>' +
                '<option value="6" >Zaterdag</option>' +
            '</select>' +
            '</form>' +
        '</td>' +
        '<td>' +
            '<div class="datetimepicker-input input-group date" data-date-format="HH:ii" ' +
             ' data-min-view="0" data-start-view="1">' +
                '<input class="beginTime form-control input-md" size="5" type="text" ' +
                ' data-max-view="0" data-start-view="0"  value="00:00" readonly>' +
                '<span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>' +
            '</div>' +
        '</td>' +
        '<td>' +
            '<form class="form-inline">' +
            '<select class="endDayOfWeek form-control">' +
                '<option value="0" >Zondag</option>' +
                '<option value="1" >Maandag</option>' +
                '<option value="2" >Dinsdag</option>' +
                '<option value="3" >Woensdag</option>' +
                '<option value="4" >Donderdag</option>' +
                '<option value="5" >Vrijdag</option>' +
                '<option value="6" >Zaterdag</option>' +
            '</select>' +
            '</form>' +
        '</td>' +
        '<td>' +
            '<div class="datetimepicker-input input-group date" data-date-format="HH:ii" ' +
             ' data-min-view="0" data-start-view="1" >' +
                '<input class="endTime form-control input-md" size="5" type="text" ' +
                '   value="01:00" readonly>' +
                '<span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>' +
            '</div>' +
        '</td>' +
        '<td>' +
            '<div class="btn-group btn-group-xs">' +
                '<button type="button" class="btn btn-danger" onclick="deleteAvailability(this)">Verwijderen</button>' +
            '</div>' +
        '</td>' +
        '</tr>';
}

function updateCarAvailabilities(carId) {
    var values = "";

    var ids = document.getElementsByClassName("availability_id");
    var beginDays = document.getElementsByClassName("beginDayOfWeek");
    var beginTimes = document.getElementsByClassName("beginTime");
    var endDays = document.getElementsByClassName("endDayOfWeek");
    var endTimes = document.getElementsByClassName("endTime");

    if(ids.length != beginDays.length || ids.length != beginTimes.length || ids.length != endDays.length || ids.length != endTimes.length) {
        alert("Er is iets misgegaan bij de verwerking van de input...");
    }

    for(var i = 0; i < ids.length; i++) {
        var value = ids[i].value + "," + beginDays[i].value + "," + beginTimes[i].value + "," + endDays[i].value + "," + endTimes[i].value;
        values = addValue(values, value);
    }

     myJsRoutes.controllers.Availabilities.updateAvailabilities(carId, values).ajax({
         success : function() {
             location.reload();
             scroll(0,0);
         },
         error : function() { // Error message will come in flashes
             location.reload();
             scroll(0,0);
         }
     });
}

function addValue(values, value) {
    if(values === "") {
        return values + value;
    } else {
        return values + ";" + value;
    }
}