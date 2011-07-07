/**
 * Javascript that controls the way the log is displayed in it's channel page.
 * For now, it's the primary way to see the log for a channel.
 */

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
				renderLog(xhr.getResponseJson()['entries'], offset);
			},
			'POST',
			'channelName=' + encodeURIComponent(channel) +
			  '&limit=' + lim + 
			  '&offset=' + offset);
		}
		
		/**
		 * @param {Array.<Object>} data
		 * @param {number} offset
		 */
		var renderLog = function(data, offset){
			if (data.length <= 1){
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
			 getEntries(end, limit);
		}
		that.showOlder = showOlder;
		
		var showNewer = function(){
			 var offset = start - limit + 1;
			 
			 if (offset < 0){
				 return;
			 }

			 getEntries(offset, limit);
		}
		that.showNewer = showNewer;
		
		getEntries(0, limit);
		goog.dom.$('olderlog-button').onclick = that.showOlder;
		goog.dom.$('newerlog-button').onclick = that.showNewer;
		return that;
}
goog.exportSymbol('myLog', myLog);

