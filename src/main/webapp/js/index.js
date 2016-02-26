var demo = {
	on: false,

	add: function() {
		if (!this.on) {
			return 0;
		}

		return Math.ceil(Math.random() * 10);
	}
}

var Tower = {
	ready: false,
	stomp: null,
	current: null,
	status: {},

	screenManager: screenManager,

	// Tower Control becomes ready only after the first status is received from the server
	isReady: function() {
		Tower.ready = true;

		// let everyone listening in know
		$(document).trigger('WidgetInternalEvent', [ 'tower-control|ready|true' ]);

		return true;
	},


	init: function() {
		// Adding event for sleep / wake
		$(document).on('visibilitychange', function(e) {
			$(document).trigger('WidgetInternalEvent', [ 'tower-control|sleep|' + document.hidden]);
		});

		// Adding event for hash changes
		$(window).on('hashchange', this.processHash);


		// event handler registration for clipboard fuckery
		$('#_clipboard_button').on('click', utils.copyToClipboard);

		this.processHash();
		this.socketInit();
	},


	processHash: function() {
		// http://localhost:8080/ethereum-enterprise/index.html#section=explorer&widgetId=txn-detail&data=0xd6398cb5cb5bac9d191de62665c1e7e4ef8cd9fe1e9ff94eec181a7b4046345c
		// http://localhost:8080/ethereum-enterprise/index.html#section=explorer&widgetId=block-detail&data=2
		if (window.location.hash) {
			var params = {}, hash = window.location.hash.substring(1, window.location.hash.length);

			_.each(hash.split('&'), function(pair) {
				pair = pair.split('=');
				params[pair[0]] = decodeURIComponent(pair[1]);
			});

			var werk = function() {
				if (params.section) {
					$('.rad-sidebar #' + params.section).click();
				}

				if (params.widgetId) {
					Tower.screenManager.show({ widgetId: params.widgetId, section: params.section ? params.section : Tower.current, data: JSON.parse(params.data), refetch: true });
				}
			};

			// do when ready
			if (!Tower.ready) {
				$(document).on('WidgetInternalEvent', function(ev, action) {
					if (action.indexOf('tower-control|ready|') === 0) {
						werk();
					}
				});
			} else {
				werk();
			}
		}
	},


	socketInit: function() {
		this.stomp = Stomp.over(new SockJS('/ethereum-enterprise/ws'));
		this.stomp.debug = null;
		this.stomp.connect({}, function(frame) {
			// Connection successful

			// Startup & Update perma-widgets
			Tower.section['default']();
		}, function(err) {
			// Connection error
			Tower.stomp = false;

			// Startup & Update perma-widgets
			Tower.section['default']();
		});
	},


	section: {
		'default': function() {
			var STATUS_END_POINT = '/node/status',
			 statusUpdate = function(status) {
				if (status.status === 'running') {
					$('#default-node-status').html( $('<span>', { html: 'Running' }) );

					$('#default-node-status').parent().find('.fa')
					 .removeClass('fa-pause rad-txt-danger')
					 .addClass('fa-play rad-txt-success');
				} else {
					$('#default-node-status').html( $('<span>', { html: utils.capitalize(status.status) }) );

					$('#default-node-status').parent().find('.fa')
					 .removeClass('fa-play rad-txt-success')
					 .addClass('fa-pause rad-txt-danger');
				}

				utils.prettyUpdate(Tower.status.peerCount, status.peerCount + demo.add(), $('#default-peers'));
				utils.prettyUpdate(Tower.status.latestBlock, status.latestBlock + demo.add(), $('#default-blocks'));
				utils.prettyUpdate(Tower.status.pendingTxn, status.pendingTxn + demo.add(), $('#default-txn'));

				Tower.status = status;

				// Tower Control becomes ready only after the first status is received from the server
				(!Tower.ready && Tower.isReady());
			};


			if (!utils.subscribe('/topic' + STATUS_END_POINT, statusUpdate)) {
				Tower.debug('FALLBACK');

				var def = function() {
					$.when(
						utils.load({ url: 'api' + STATUS_END_POINT })
					).done(function(response) {
						var status = response.data.attributes;

						statusUpdate(status);
					});
				};

				def();
				setInterval(def, 5000);
			}
		},

		'console': function() {
			var widgets = [
				{ widgetId: 'node-info' },
				{ widgetId: 'node-control' },
				{ widgetId: 'node-settings' }
			];

			Tower.screenManager.showSection('console', widgets);
		},

		'peers': function() {
			var widgets = [
				{ widgetId: 'peers-add' },
				{ widgetId: 'peers-list' },
				{ widgetId: 'peers-neighborhood', data: Tower.status.nodeIP }
			];

			Tower.screenManager.showSection('peers', widgets);
		},

		'api': function() {
			Tower.screenManager.showSection('api', []);
		},

		'contracts': function() {
			var widgets = [
				{ widgetId: 'contract-list' }
			];

			Tower.screenManager.showSection('contracts', widgets);
		},

		'explorer': function() {
			var widgets = [
				{ widgetId: 'block-detail', data: Tower.status.latestBlock },
				{ widgetId: 'block-list', data: Tower.status.latestBlock },
				{ widgetId: 'block-view' }
			];

			Tower.screenManager.showSection('explorer', widgets);
		}
	},


	debug: function(message) {
		var _ref;
		return typeof window !== "undefined" && window !== null ? (_ref = window.console) != null ? _ref.log(message) : void 0 : void 0;
    }
};



$(function() {
	$(window).on('scroll', function(e) {
		if ($(window).scrollTop() > 50) {
			$('body').addClass('sticky');
		} else {
			$('body').removeClass('sticky');
		}
	});

	$(document).on('click', function(e) {
		var $item = $('.rad-dropmenu-item');

		if ($item.hasClass('active')) {
			$item.removeClass('active');
		}
	});



	// Menu (burger) handler
	$('.rad-toggle-btn').on('click', function() {
		$('.rad-logo-container').toggleClass('rad-nav-min');
		$('.rad-sidebar').toggleClass('rad-nav-min');
		$('.rad-body-wrapper').toggleClass('rad-nav-min');

		setTimeout(function() {

		}, 200);
	});

	// Theme handler / switcher
	$('li.rad-dropdown > a.rad-menu-item').on('click', function(e) {
		e.preventDefault();
		e.stopPropagation();

		$('.rad-dropmenu-item').removeClass('active');
		$(this).next('.rad-dropmenu-item').toggleClass('active');
	});


	$(document).on('click', function(e) {
		var el = $(e.target);

		if ( el.parent().parent().hasClass('rad-panel-action') ) {

			// Widget collapse / expand handler
			if ( el.hasClass('fa-chevron-down') ) {
				var $ele = el.parents('.panel-heading');

				$ele.siblings('.panel-footer').toggleClass('rad-collapse');
				$ele.siblings('.panel-body').toggleClass('rad-collapse', function() {
					setTimeout(function() {

					}, 200);
				});

			// Widget close handler
			} else if ( el.hasClass('fa-close') ) {
				var $ele = el.parents('.panel');
				$ele.addClass('panel-close');

				setTimeout(function() {
					$ele.parent().css({ 'display': 'none'});
				}, 210);

			// Widget refresh handler
			} else if ( el.hasClass('fa-rotate-right') ) {
				var wid = el.parents('.panel').parent().attr('id').replace('widget-shell-', ''),
				 $ele = el.parents('.panel-heading').siblings('.panel-body');

				$ele.append('<div class="overlay"><div class="overlay-content"><i class="fa fa-refresh fa-2x fa-spin"></i></div></div>');

				setTimeout(function() {
					$ele.find('.overlay').remove();

					(Tower.screenManager.idMap[wid].fetch && Tower.screenManager.idMap[wid].fetch());
				}, 2000);
			} else if ( el.hasClass('fa-link') ) {
				var wid = el.parents('.panel').parent().attr('id').replace('widget-shell-', ''),
				 params = {
					section: Tower.screenManager.idMap[wid].section,
					widgetId: Tower.screenManager.idMap[wid].name
				 },
				 link = document.location.protocol + '//' + document.location.host + document.location.pathname + '#';

				if (Tower.screenManager.idMap[wid].data) {
					params.data = JSON.stringify(Tower.screenManager.idMap[wid].data);
				}
console.log('[create]', params)
				link += $.param(params);

				// Notification tooltip
				$(el).tooltip({ placement: 'top' }).tooltip('show');

				setTimeout(function() {
					$(el).tooltip('destroy');
				}, 1000);

				$('#_clipboard').val(link);
				$('#_clipboard_button').click();
			}
		}
	});


	// Settings collapse / expand handler
	$('.rad-chk-pin input[type=checkbox]').change(function(e) {
		$('body').toggleClass('flat-theme');
		$('#rad-color-opts').toggleClass('hide');
	});

	// Theme checkbox handlers
	$('.rad-color-swatch input[type=radio]').change(function(e) {
		if ($('.rad-chk-pin input[type=checkbox]').is(':checked')){
			$('body').removeClass().addClass('flat-theme').addClass(this.value);
			$('.rad-color-swatch label').removeClass('rad-option-selected');
			$(this).parent().addClass('rad-option-selected');
			$(window).scrollTop(0);
		} else {
			return false;
		}
	});


	// Navigation menu handler
	$('.rad-sidebar li').click(function(e) {
		if ($(this).attr('id') === 'sandbox') {
			return;
		}

		e.preventDefault();

		Tower.current = $(this).attr('id');

		$('.rad-sidebar li').removeClass('active');
		$(this).addClass('active');

		Tower.section[Tower.current]();

		$('.rad-page-title').html( $('<span>', { html: $(this).find('.rad-sidebar-item').html() }) );
	});




	// ---------- INIT -----------
	Tower.init();

	// add dispatcher listener
	// $(document).on('WidgetInternalEvent', function(ev, action) {
	// 	Tower.debug(ev, action);
	// });

	// Setting 'Console' as first section
	$('.rad-sidebar li').first().click();
});
