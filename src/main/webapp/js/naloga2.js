function set_select_all() {
    $("[id*='selectAll']").click(function() {
        if ($(this).prop('checked')) {
            $('.checkBox').find('input:checkbox').prop('checked', true);
        } else {
            $('.checkBox').find('input:checkbox').prop('checked', false);
        }
    });
}

$(document).ready(function() {
    set_select_all();
});