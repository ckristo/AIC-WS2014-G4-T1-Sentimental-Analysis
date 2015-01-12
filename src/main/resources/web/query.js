$(document).ready(function() {
    $.ajax({
        url: "http://localhost:9000/sentiment/test",
        type: "POST"
    }).then(function(data) {

       $.ajax({
        url: "http://localhost:9000/sentiment/test/query",
        type: "GET"
    }).then(function(data) {
       $('.greeting-id').append("test");
       $('.greeting-content').append(data);
    });

    });
});

