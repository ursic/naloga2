function set_select_all_listener() {
    $("[id*='selectAll']").click(function() {
        $('.checkBox').find('input:checkbox').prop('checked', $(this).prop('checked'));
    });    
}

function set_upload_form_listener() {
    $('#browseButton').click(function() {
        $("[id*='fileInput']").click().change(function() {
            var that = this;
            $('.fileText').text($(that).val());
        });
    });
}

function set_button_listeners() {
    $('.uploadButton, .storeButton, .emptyButton').click(function() {
        $('.errorText').hide();
        $('.loader').show();
    });
}

function set_check_row_listener() {
    $("[class*='employeeTableRow']").click(function() {
        $(this).find('input').prop('checked', function(i, val ) {
            return !val;});
    });
    $('input:checkbox').click(function(e) {
        e.stopPropagation();
    });
}

$(document).ready(function() {
    set_select_all_listener();
    set_upload_form_listener();
    set_button_listeners();
    set_check_row_listener();
});