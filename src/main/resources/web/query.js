$(document).ready(function() {
    $.ajax({
        url: "http://localhost:9000/sentiment/test",
        type: "POST"
    }).then(function(data) {

       $.ajax({
        url: "http://localhost:9000/sentiment/test/query",
        type: "GET",
        timeout: 30000   // timeout 30 seconds (query might take some time)
                         //TODO: display progress indicator (spinner etc.)
    }).then(function(data) {
       $('.greeting-id').append("test");
       $('.greeting-content').append(data);
    });

    });
});

