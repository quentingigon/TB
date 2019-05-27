$(document).ready(function () {
    var evtSource = new EventSource("http://localhost:9000/eventsource", {withCredentials: true});
    var macAdress = getCookie("mac");
    var resolution = getCookie("resolution");

    evtSource.addEventListener('message', function(e) {
        var type = e.data.substr(0, e.data.indexOf("?"));
        var data = e.data.substr(e.data.indexOf("?") + 1, e.data.indexOf("|") - type.length - 1);
        var macs = e.data.substr(e.data.indexOf("|") + 1).split(",");

        if (macs.includes(macAdress)) {
            if (type === "url") {
                displayFlux(data);
            }
            else if (type === "image") {
                if (resolution === "1080") {
                    displayImage(data, 1900, 1080);
                }
                else if (resolution === "720") {
                    displayImage(data, 1280, 720);
                }
            }
            else if (type === "text") {
                displayFooterText(data);
            }
            else if (type === "video") {
                displayVideo(data);
            }
        }
    });
});