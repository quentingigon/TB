function displayFlux(url) {
    $("#footer0").css("display", "none");
    $("#frame0").attr('src', url);
    $("#footer0").css("display", "inline-block");

}

function displayFooterText(text) {
    $("#footer0").css("display", "inline-block");
    $("#footer0").html(text);
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
    // TODO get eventSource url from server ?
    var evtSource = new EventSource("http://localhost:9000/eventsource", {withCredentials: true});

    var macAdress = getCookie("mac");

    evtSource.addEventListener('message', function(e) {
        var type = e.data.substr(0, e.data.indexOf("?"));
        var data = e.data.substr(e.data.indexOf("?") + 1, e.data.indexOf("|") - type.length - 1);
        var macs = e.data.substr(e.data.indexOf("|") + 1).split(",");

        if (macs.includes(macAdress)) {
            if (type === "url") {
                displayFlux(data);
            }
            else if (type === "image") {

            }
            else if (type === "text") {
                displayFooterText(data);
            }
            else if (type === "video") {

            }
        }

    });

});