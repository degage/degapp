/**
 * Created by HannesM on 2/05/14.
 */

function deletePrivilege(e) {
    var currentRow = $(e).closest('tr');
    var inputs = currentRow.find("input");
    for(var i = 0; i < inputs.length; i++) {
        if(inputs[i].classList.contains("privilege_user_id")) {
            if(inputs[i].classList.contains("privilege_not_in_db")) { // Not in database yet -> hard delete
                currentRow.remove();
            } else { // In database -> soft delete so we can post this
                // Tricky way to find this class, but doesn't seem to work in another way
                inputs[i].value = -1 * inputs[i].value;
                currentRow.hide();
            }
            break;
        }
    }
}

function newPrivilege(e) {
    var currentRow = $(e).closest('tr');
    currentRow.before(newPriviligeRow());
    initUserPicker(); // userpicker.js has to be included!
}

function newPriviligeRow() {
    return '' +
        '<tr>' +
        '<td>' +
            '<div class="userpicker col-md-12" data-url="' + userpickerRoute + '">' + // TODO: route to userpicker is hardcoded, find fix?
                '<input type="text" data-toggle="dropdown" class="form-control input-md" />' +
                '<input type="hidden" class="privilege_user_id privilege_not_in_db" />' +
                '<ul class="dropdown-menu" role="menu"></ul>' +
            '</div>' +
        '</td>' +
        '<td>' +
            '<div class="btn-group btn-group-xs">' +
                '<button type="button" class="btn btn-danger" onclick="deletePrivilege(this)">Verwijderen</button>' +
            '</div>' +
        '</td>' +
        '</tr>';
}

function updatePrivileged(carId) {
    var values = "";


    var ids = document.getElementsByClassName("privilege_user_id");

    for(var i = 0; i < ids.length; i++) {
        var value = ids[i].value;
        values = addValue(values, value);
    }

    myJsRoutes.controllers.Cars.updatePriviliged(carId, values).ajax({
        success : function() {
            location.reload();
            scroll(0,0);
        },
        error : function() { // Errormessage will come in flashes
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