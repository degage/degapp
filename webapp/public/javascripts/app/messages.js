/**
 * Created by HannesM on 18/04/14.
 */
$(document).ready(function() {
    $('li[id^="tab"]').on('click', function() {
        var id = $(this).attr("id");
        if(id == "tab_received") {
            route = myJsRoutes.controllers.Messages.showReceivedMessagesPage;
        } else { // tab_sent
            route = myJsRoutes.controllers.Messages.showSentMessagesPage;
        }
        $('#searchButton').click();
    });
});