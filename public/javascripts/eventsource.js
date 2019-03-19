function displayFlux(url) {
    $("#frame0").attr('src', url);

}

function getCookie(input) {
    var cookies = document.cookie.split(';');
    for (var i = 0; i < cookies.length; i++) {
        var name = cookies[i].split('=')[0].toLowerCase();
        var value = cookies[i].split('=')[1].toLowerCase();
        if (name === input) {
            return value;
        } else if (value === input) {
            return name;
        }
    }
    return "";
};

$(document).ready(function () {

    var evtSource = new EventSource("http://localhost:9000/eventsource", {withCredentials: true});

    evtSource.addEventListener('message', function(e) {
        console.log(getCookie("mac"));
        console.log(e.data);
        displayFlux(e.data);
    });

});