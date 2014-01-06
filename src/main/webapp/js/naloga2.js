function set_upload_form_listener() {
    $('.browseButton.bbt').click(function() {
        $("[class='fit']").click().change(function() {
            var that = this;
            $('.fileText').text($(that).val());
        });
    });
    $('.browseButton.bbb').click(function() {
        $("[class='fib']").click().change(function() {
            var that = this;
            $('.fileText').text($(that).val());
        });
    });
}

function set_button_listeners() {
    $('.uploadButton, .storeButton, .emptyButton, .langButton, .lbt').click(function() {
        $('.errorText').hide();
        $('.loader').show();
    });
}

$(document).ready(function() {
    set_upload_form_listener();
    set_button_listeners();
});
