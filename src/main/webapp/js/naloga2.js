$(document).ready(function() {
    $("[id*='selectAll']").click(function() {
        $('.checkBox').find('input:checkbox').prop('checked', $(this).prop('checked'));
    });
});