(function() {
	var widget = {
		name: 'block-detail',
		size: 'medium',

		initialized: false,

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped"><%= rows %></table>'),
		templateRow: _.template('<tr><td style="width: 100px;"><%= key %></td><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= value %></td></tr>'),
		templateTxnRow: _.template('<tr><td style="width: 100px;"><%= key %></td><td style="text-overflow: ellipsis; overflow: hidden;"><%= value %></td></tr>'),

		ready: function() {
			this.render();
		},

		url: '../api/block/get',

		setData: function(data) {
			this.blockNumber = data;

			this.title = 'Block #' + this.blockNumber;
		},

		init: function(data) {
			this.setData(data);

			this.shell = Tower.TEMPLATES.widget(this.title, this.size);

			this.initialized = true;
			this.ready();
		},

		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: this.url, data: { number: parseInt(_this.blockNumber, 10) } })
			).done(function(res) {
				var rows = [],
				 keys = _.sortBy(_.keys(res.data.attributes));;

				keys = utils.idAlwaysFirst(keys);

				_.each(keys, function(val, key) {
					if ( (!res.data.attributes[val]) || (res.data.attributes[val].length == 0) ) {
						return;
					}

					if (val == 'transactions') {
						var txnHtml = [];

						_.each(res.data.attributes[val], function(txn) {
							txnHtml.push('<a href="#">' + txn  + '</a>')
						});

						rows.push( _this.templateTxnRow({ key: utils.camelToRegularForm(val), value: txnHtml.join('<br/>') }) );
					} else {
						rows.push( _this.templateRow({ key: utils.camelToRegularForm(val), value: res.data.attributes[val] }) );
					}
				});

				$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );
				$('#widget-shell-' + _this.shell.id + ' .panel-title span').html(_this.title);

				$('#widget-' + _this.shell.id + ' .value').click(function(e) {
					var isEditable = !!$(this).prop('contentEditable');
					$(this).prop('contentEditable', isEditable);

					$(this).focus();
				});

				$('#widget-' + _this.shell.id + ' a').click(function(e) {
					e.preventDefault();

					Tower.screenManager.show({ widgetId: 'txn-detail', section: 'explorer', data: $(this).text(), refetch: true });
				});
			});
		},

		render: function() {
			Tower.screenManager.grounds.append(this.shell.tpl);

			this.fetch();

			$('#widget-' + this.shell.id).css({ 'height': '240px', 'margin-bottom': '10px', 'overflow-x': 'hidden', 'width': '100%' });

		}
	};


	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
