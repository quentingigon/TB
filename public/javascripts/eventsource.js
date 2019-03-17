function displayFlux(url) {
    $("#frame0").attr('src', url);

}

$(document).ready(function () {

    var evtSource = new EventSource("http://localhost:9000/eventsource", {withCredentials: true});

    evtSource.addEventListener('message', function(e) {
        console.log(e.data);
        displayFlux(e.data);
    });

});