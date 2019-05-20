function displayFlux(url) {
    $("#frame0").attr('src', url);
}

// TODO autoplay not working,
//  check youtube player -> https://developers.google.com/youtube/iframe_api_reference#Loading_a_Video_Player
function displayVideo(url) {
    $("#frame0").attr('src', url + "?autoplay=true");
}

function displayFooterText(text) {
    $('#footer0').show().html("").append(text);
}

function hideFooter() {
    $('#footer0').hide();
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
                hideFooter();
                displayFlux(data);
            }
            else if (type === "image") {

            }
            else if (type === "text") {
                displayFooterText(data);
            }
            else if (type === "video") {
                hideFooter();
                displayVideo(data);
            }
        }

    });

});