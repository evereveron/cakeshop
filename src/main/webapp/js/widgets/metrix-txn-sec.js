(function() {
	var extended = {
		name: 'metrix-txn-sec',
		title: 'Metrix: TXN per Second',
		size: 'large',

		hideLink: true,

		topic: '/topic/metrix/txn-sec',


		subscribe: function() {
			utils.subscribe(this.topic, this.onData);
		},

		onData: function(data) {
			if ( (data) && (data.demo) ) {
				widget.chart.push([{ time: (new Date()).getTime() / 1000, y: Math.floor(Math.random() * 1000) + 1 }]);
			}

//			var b = {
//				time: (new Date()).getTime(),
//				y: data.datapoint
//			};
//
//			widget.chart.push([ b ]);
		},

		postRender: function() {
			$('#widget-' + widget.shell.id).html( '<div id="' + widget.name + '" class="epoch category10" style="width:100%; height: 210px;"></div>' );

			widget.chart = $('#' + widget.name).epoch({
			    type: 'time.area',
			    data: [ {
			    	label: 'TXN per SEC',
			    	values: [ { time: (new Date()).getTime() / 1000, y: 0 } ] 
			    } ],
			    axes: ['left', 'right', 'bottom']
			});

			// DEMO ANCHOR, REMOVE WHEN REAL DATA EXISTS
			setInterval(function() { widget.onData({demo: true}); }, 1000);
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
})();
