function displayFlux(url) {
    $("#frame0").attr('src', url);
}

$(document).ready(function () {

    var evtSource = new EventSource("http://localhost:9000/eventsource", {withCredentials: true});

    var macAdress = "test";

    evtSource.addEventListener('message', function(e) {
        var url = e.data.substr(0, e.data.indexOf("|"));
        var macs = e.data.substr(e.data.indexOf("|") + 1).split(",");

        if (macs.includes(macAdress)) {
            displayFlux(url);
        }
    });

});