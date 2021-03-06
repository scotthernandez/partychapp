/**
 * Javascript that controls the way the log is displayed in it's channel page.
 * For now, it's the primary way to see the log for a channel.
 */

goog.require('goog.ui.DatePicker');
goog.require('goog.i18n.DateTimeSymbols');
goog.require('goog.i18n.DateTimeSymbols_en_ISO');
goog.provide('partychapp.Log');


/** @const */
var setLimit = 10;

 
 /**
  * @param {string} channelName
  * @returns Object
  */
var myLog = function(channelName){
		var that = {};
		
		var channel = channelName;
		var	limit = setLimit
		var	start,
			end;
		
		/**
		 * @param {number} offset
		 * @param {number} lim
		 */
		var getEntries = function(offset, lim){
			goog.net.XhrIo.send('/logentriesjson/', function(e){
				var xhr = e.target;
				var entries = xhr.getResponseJson()['entries'];
				if (xhr.getResponseJson()['error']){
					return;
				}
				for (var i in entries){
					entries[i]['content'] = entries[i]['content'].replace(/&/g,'&amp;')                                        
													             .replace(/>/g,'&gt;')                                           
													             .replace(/</g,'&lt;')                                         
													             .replace(/"/g,'&quot;');
					entries[i]['content'] = ticketFilter(entries[i]['content']);
				}
				
				renderLog(entries, offset);
			},
			'POST',
			'channelName=' + encodeURIComponent(channel) +
			  '&limit=' + encodeURIComponent(lim+'') + 
			  '&offset=' + encodeURIComponent(offset+''));
		}
		
		/**
		 * @param {Array.<Object>} data
		 * @param {number} offset
		 */
		var renderLog = function(data, offset){
			if (data.length < 1){
				return;
			}
			soy.renderElement(
				      goog.dom.$('log-table'),
				      partychapp.templates.logTable,
				      {data: data});
			start = offset;
			end = offset + data.length - 1;
		}
		
		var showOlder = function(){
			 getEntries(end + 1, limit);
		}
		that.showOlder = showOlder;
		
		var showNewer = function(){
			 var offset = start - limit;
			 offset = offset < 0 ? 0 : offset;

			 getEntries(offset, limit);
		}
		that.showNewer = showNewer;
		
		getEntries(0, limit);
		goog.dom.$('olderlog-button').onclick = that.showOlder;
		goog.dom.$('newerlog-button').onclick = that.showNewer;
		return that;
}
goog.exportSymbol('myLog', myLog);

/**
 * 
 * @param {string} message
 * @returns {string} modified
 */
function ticketFilter(message){
	var re = /\b[A-Z]+-[0-9]+\b/g;
	var newstr = message.replace(re, function(match){
											return '<a href="http://jira.mongodb.org/browse/' + match + '">' + match + '</a>';
											});
	return newstr;
}
goog.exportSymbol('ticketFilter', ticketFilter);


function numbersDropdown(id, start, finish){
	var select = document.getElementById(id);
	var option;
	for (var i = start; i <= finish; i++){
		option = document.createElement('option');
		option.value = i >= 10 ? i : '0'+i;
		option.text = i >= 10 ? i : '0'+i;
		select.add(option);
	}
}
goog.exportSymbol("numbersDropdown", numbersDropdown);
