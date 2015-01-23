	
	var service_endpoint_url = "http://localhost:9000/twitter_sentiment_service";

	var sessions = {};

	var $input_username;
	var $input_token;
	var $input_from;
	var $input_to;
	var $input_register_alert_placeholder;
	var $input_query_alert_placeholder;
	var $input_classifier_model;
	var $input_training_config;
	
	var $query_form;
	var $query_results;
	var $query_chart;
	
	var $register_loader;
	var $query_loader;

	$(document).ready(function() {
		$input_username = $('#input_username');
		$input_token = $('#input_token');
		$input_from = $('#input_from');
		$input_to = $('#input_to');
		$input_classifier_model = $('#input_classifier_model');
		$input_training_config = $('#input_training_config');
		$input_register_alert_placeholder = $('#input_register_alert_placeholder');
		$input_query_alert_placeholder = $('#input_query_alert_placeholder');
		
		$register_loader = $('#register-loader');
		$query_loader = $('#query-loader');
		
		$input_from.datepicker();
		$input_to.datepicker();
		
		$query_form = $('#query-form');
		$query_results = $('#query-results');
		$query_chart = $('#query-chart');
		
		$.datepicker.setDefaults({
			dateFormat: 'yy-mm-dd',
			beforeShow: function(input, inst) {
				var offset = $(input).offset();
				var height = $(input).height();
				window.setTimeout(function () {
					inst.dpDiv.css({ top: (offset.top + height + 15) + 'px', left: (offset.left) + 'px' });
				}, 1);
			}
		});
	});

	/**
	 * Object encapsulating Twitter Sentiment service interaction.
	 */
	var TwitterSentimentService = {
		/**
		 * Registers a user for the Twitter sentiment service.
		 * @param {string} username the user's name to register
		 * @param {function} successCallback callback called after registration was successful (parameters: data)
		 * @param {function} errorCallback callback called in case of an error / unsuccessful registration (parameters: statusCode, errorMessage)
		 * @param {function} completeCallback callback called (in any case) after registration attempt is completed (parameters: none)
		 */
		register : function (username, successCallback, errorCallback, completeCallback) {
			$.ajax(service_endpoint_url+"/register", {
				method: 'POST',
				data: username,
				success : function(data) {
					if (successCallback !== undefined) {
						successCallback(data);
					}
				},
				error : function(jqXHR) {
					if (errorCallback !== undefined) {
						errorCallback(jqXHR.status, jqXHR.responseText);
					}
				},
				complete: function() {
					if (completeCallback !== undefined) {
						completeCallback();
					}
				}
			});
		},

		/**
		 * Queries the Twitter sentiment service.
		 * @param {string} token
		 * @param {string} from from date (format: yyyy-MM-dd)
		 * @param {string} to to date (format: yyyy-MM-dd)
		 * @param {string} classifierModel the classifier model parameter
		 * @param {string} trainingConfig the training config parameter
		 * @param {function} successCallback callback called after query operation was successful (parameters: data)
		 * @param {function} errorCallback callback called in case of an error / unsuccessful query operation (parameters: statusCode, errorMessage)
		 * @param {function} completeCallback callback called (in any case) after query attempt is completed (parameters: none)
		 */
		query : function (token, from, to, classifierModel, trainingConfig, successCallback, errorCallback, completeCallback) {
			$.ajax(service_endpoint_url+"/query", {
				method: 'GET',
				data: {
					'token' : token,
					'from' : from,
					'to' : to,
					'classifierModel' : classifierModel,
					'trainingConfig' : trainingConfig
				},
				success : function(data) {
					if (successCallback !== undefined) {
						successCallback(data);
					}
				},
				error : function(jqXHR) {
					if (errorCallback !== undefined) {
						errorCallback(jqXHR.status, jqXHR.responseText);
					}
				},
				complete: function() {
					if (completeCallback !== undefined) {
						completeCallback();
					}
				}
			});
		}
	};
	
	/**
	 * Shows an alert message for a form element.
	 * @param {jQuery} $input the form element (as jQuery object)
	 * @param {String} msg the message
	 * @param {String} type the type of the alert message (defaults to danger)
	 */
	function showFormElementAlertMsg($input, msg, type) {
		if (type === undefined) {
			type = "danger";
		}
		var $alert = $input.closest('.form-group').find('.alert');
		$alert.html(msg);
		$alert.addClass("alert-"+type);
		$alert.show();
	}
	
	/**
	 * Removes an alert message for a form element.
	 * @param {jQuery} $input
	 */
	function resetFormElementAlert($input) {
		var $alert = $input.closest('.form-group').find('.alert');
		$alert.html("");
		$alert.removeClass().addClass("alert");
		$alert.hide();
	}

	/**
	 * On-click callback for register button.
	 */
	function _register() {
		var username = $input_username.val();
		
		resetFormElementAlert($input_username);
		
		// check arguments
		if (!username.trim()) {
			showFormElementAlertMsg($input_username, "Please specify your Twitter username");
			return;
		} else if (username.length < 2 || username.indexOf("@") !== 0) {
			showFormElementAlertMsg($input_username, "Please specify a valid Twitter username");
			return;
		}
		
		$register_loader.show();

		var successCallback = function(data) {
			$input_token.val(data.token);

			$register_loader.hide();
			
			showFormElementAlertMsg($input_register_alert_placeholder, "<p>Successfully registered user <span class='success-msg-username'>"+data.username+"</span> &ndash; your session token:<br/> <span class='success-msg-token'>"+data.token+"</span></p>", 'success');
			
			$query_form.show();
		};
		
		var errorCallback = function(statusCode, errorMsg) {
			$register_loader.hide();
			
			showFormElementAlertMsg($input_username, "Couldn't register user:<br/>"+errorMsg);
		};

		TwitterSentimentService.register(username, successCallback, errorCallback);
	}

	/**
	 * On-click callback for query button.
	 */
	function _query() {
		var token = $input_token.val();
		var from  = $input_from.val();
		var to    = $input_to.val();
		var classifierModel = $input_classifier_model.val();
		var trainingConfig = $input_training_config.val();

		resetFormElementAlert($input_from);
		resetFormElementAlert($input_to);

		// check arguments
		if (!token.trim()) {
			showFormElementAlertMsg($input_query_alert_placeholder, "Session token missing - please register");
			return;
		}
		if (!from) {
			showFormElementAlertMsg($input_from, "Please specify a from date");
			return;
		}
		if (!to) {
			showFormElementAlertMsg($input_to, "Please specify a to date");
			return;
		}
		
		$query_loader.show();

		var successCallback = function(data) {
			$query_loader.hide();
			
			showFormElementAlertMsg($input_query_alert_placeholder, "Query performed successfully -- see your results below:", 'success');
			
			//TODO
			console.dir(data.aggregated_sentiment);
			
			generateQueryChart(data.aggregated_sentiment);
			populateQueryResults(data.tweets);
		};
		
		var errorCallback = function(statusCode, errorMsg) {
			$query_loader.hide();
			
			showFormElementAlertMsg($input_query_alert_placeholder, "Couldn't perform query operation:<br/>"+errorMsg);
		};

		TwitterSentimentService.query(token, from, to, classifierModel, trainingConfig, successCallback, errorCallback);
	}

	function populateQueryResults(data) {
		$query_results.empty();
		
		for (var i in data) {
			$query_results.append($('<div/>', {
				class : 'query-result sentiment-'+data[i].sentiment+' panel panel-default'
			}).append($('<div/>', {
				class : 'panel-body',
				text : data[i].text
			})).append($('<div/>', {
				class : 'panel-footer',
				html : '<span class="label label-'+data[i].sentiment+'">'+data[i].sentiment+'</span>'
			})));
		}
	}
	
	function generateQueryChart(data) {
		var series_data = [];
		for (var i in data) {
			series_data.push([i, data[i]]);
		}
		
		console.dir(series_data);
		
		$query_chart.highcharts({
			chart: {
				plotBackgroundColor: null,
				plotBorderWidth: null,
				plotShadow: false
			},
			title: {
				text: 'Aggregated sentiment'
			},
			tooltip: {
				pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
			},
			plotOptions: {
				pie: {
					allowPointSelect: true,
					cursor: 'pointer',
					dataLabels: {
						enabled: true,
						format: '<b>{point.name}</b>: {point.percentage:.1f} %',
						style: {
							color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
						}
					}
				}
			},
			series: [{
				type: 'pie',
				name: 'Aggregated sentiment',
				data: series_data
			}]
		});
	}
