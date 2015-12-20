$(initUserPicker);

// note: this is also used for cars and is therefore a slight misnomer

function initUserPicker() {
    $(".userpicker > input[type=text]").on("input", function() {
        var userpicker = $(this).parent();
        var search = userpicker.find("input[type=text]").val();
        if (search) {
            $.get(userpicker.data("url") + "?search=" + userpicker.find("input[type=text]").val(), function(data) {
                userpicker.find(".dropdown-menu").html(data);
                userpicker.find("input[type=text]").dropdown("toggle");
            });
        }
        userpicker.find("input[type=hidden]").val("");
        userpicker.find("div").html("");
    });

/*  Gave rise to stack overflow in bootstrap 3.3.1, probably because class 'open' is now only set after the dropdown
    gets focus.

    $(".userpicker > input[type=text]").on("focus", function() {
        if (!$(this).parent().hasClass("open")) {
            $(this).dropdown("toggle");
        }
    });
*/

    $(".userpicker").on("show.bs.dropdown", function() {
        if ($(this).find(".dropdown-menu").html() == "") {
            return false;
        }
    });

    $(".userpicker").on("hide.bs.dropdown", function() {
        if ($(this).find("input[type=text]").is(":focus") && $(this).find(".dropdown-menu").html() != "") {
            return false;
        }
    });

    $(".userpicker > .dropdown-menu").on("keypress", "li", function() {
        $(this).parent().parent().find("input[type=text]").trigger("focus");
    });

    $(".userpicker > .dropdown-menu").on("keydown", "li", function(e) {
        if (e.which == 9) {
            $(this).parent().parent().find("input[type=text]").dropdown("toggle");
        }
    });

    $(".userpicker > .dropdown-menu").on("click", "li", function() {
        var userpicker = $(this).parent().parent();
        userpicker.find("input[type=text]").val($(this).find("span").text());
        userpicker.find("input[type=hidden]").val($(this).data("uid"));
        userpicker.find("div").html($(this).data("uid"));
        // :tabbable eeds jquery-ui
        // $(":input:tabbable").eq($(":input:tabbable").index(userpicker.find("input[type=text]")) + 1).focus();
        $(":input").eq($(":input").index(userpicker.find("input[type=text]")) + 1).focus();
    });

    $(".userpicker > .dropdown-menu").on("keydown", "li:first", function(e) {
        if (e.which == 38) {
            e.preventDefault();
            e.stopPropagation();
            $(this).parent().parent().find("input[type=text]").trigger("focus");
        }
    });

    $(".userpicker > .dropdown-menu").on("keydown", "li:last", function(e) {
        if (e.which == 40) {
            e.preventDefault();
            e.stopPropagation();
            $(this).parent().parent().find("input[type=text]").trigger("focus");
        }
    });

    $(".userpicker > input[type=text]").on("keydown", function(e) {
        if (e.which == 9) {
            e.preventDefault();
            e.stopPropagation();
            $(this).blur();
            $(this).dropdown("toggle");
            if (e.shiftKey) {
                // :tabbable needs jquery-ui
                // $(":input:tabbable").eq($(":input:tabbable").index($(this)) - 1).focus();
                $(":input").eq($(":input").index($(this)) - 1).focus();
            } else {
                // :tabbable needs jquery-ui
                // $(":input:tabbable").eq($(":input:tabbable").index($(this)) + 1).focus();
                $(":input").eq($(":input").index($(this)) + 1).focus();
            }
        } else if (e.which == 38) {
            e.preventDefault();
            e.stopPropagation();
            $(this).parent().find(".dropdown-menu > li:last a").trigger("focus");
        } else if (e.which == 40) {
            e.preventDefault();
            e.stopPropagation();
            $(this).parent().find(".dropdown-menu > li:first a").trigger("focus");
        }
    });
}