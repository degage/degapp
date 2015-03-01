/**
 * Created by Benjamin on 05/04/2014.
 */

function clickHandler() {
    $('li[id^="tab"] input').removeClass('searchTextField');

    if(typeof $(this).find('input') != 'undefined') {
        $(this).find('input').addClass('searchTextField');
        beginAsc = $(this).data("asc");
    }
    $('#searchButton').click();
}

$(document).ready(function() {
    $('li[id^="tab"]').on('click', clickHandler);
});
