(function() {
    var Client = window.Client = {};

    Client.post = function(url, data) {
        if (!_.isString(data)) {
            data = JSON.stringify(data);
        }

        return $.ajax({
            url: url,
            method: "POST",
            dataType: "json",
            contentType: "application/json",
            data: data,
        });
    };

	Client.connect = function() {
		var stomp = Client.stomp = Stomp.over(new SockJS('/ethereum-enterprise/ws'));
		stomp.debug = null;
		stomp.connect({},
            function(frame) {
                console.log("Connected via STOMP!");
            },
            function(err) {
                console.log("Lost STOMP connection??", err);
                setTimeout(Client.connect, 1000); // always reconnect
            }
        );
	};

    Client.connect();
})();
