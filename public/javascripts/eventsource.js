function displayFlux(url) {
    $("#frame0").attr('src', url);
    // maybe display text message in footer ?
}

function getCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

$(document).ready(function () {

    var evtSource = new EventSource("http://localhost:9000/eventsource", {withCredentials: true});

    var macAdress = getCookie("mac");

    evtSource.addEventListener('message', function(e) {
        var url = e.data.substr(0, e.data.indexOf("|"));
        var macs = e.data.substr(e.data.indexOf("|") + 1).split(",");

        if (macs.includes(macAdress)) {
            displayFlux(url);
        }
    });

});