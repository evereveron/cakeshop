(function() {
	var extended = {
		name: 'doc-frame',
		title: 'API Documentation',
		size: 'large',

		hideLink: true,
		hideRefresh: true,

		template: _.template(
			'<div class="form-group">' +
			'	<iframe src="api.html" style="width: 100%; height: 550px;" scrolling="yes" marginwidth="0" marginheight="0" frameborder="0" vspace="0" hspace="0"></iframe>' +
			'</div>'),

		render: function() {
			Dashboard.grounds.append(this.shell.tpl);

			$('#widget-' + this.shell.id)
				.css({
					'height': '600px',
					'margin-bottom': '10px',
					'overflow': 'auto'
				})
				.html( this.template({}) );

			$('#widget-' + this.shell.id + ' button').click(this._handler);
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
})();