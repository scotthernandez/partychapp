goog.require('goog.dom');
goog.require('goog.dom.classes');
goog.require('goog.net.XhrIo');
goog.require('goog.string');

goog.require('partychapp.templates');
goog.require('partychapp.Log');
goog.require('partychapp.ScoreTable');

// These three shouldn't be required, but a couple dependencies are missing
// inside closure-library, so we need to force these files to get pulled in
// to avoid warnings.
goog.require('goog.debug.ErrorHandler');
goog.require('goog.events.EventHandler');
goog.require('goog.Uri');

function showCreateForm() {
  goog.dom.classes.add(goog.dom.$('create-button-container'), 'hidden');
  goog.dom.classes.remove(goog.dom.$('channel-settings-table'), 'hidden');
}
goog.exportSymbol('showCreateForm', showCreateForm);

function submitCreateRoom() {
  var roomName = goog.dom.$('room-name').value;
  var inviteOnly = goog.dom.$('inviteonly-true').checked;
  var invitees = goog.dom.$('invitees').value;

  if (goog.string.isEmptySafe(roomName)) {
    alert('Please enter a room name.');
    return false;
  }

  goog.net.XhrIo.send(
      '/channel/create',
      function(e) {
        var resultNode = goog.dom.$('create-result');
        goog.dom.classes.remove(resultNode, 'hidden');
        var xhr = e.target;
        resultNode.innerHTML = xhr.getResponseText();
      },
      'POST',
      'name=' + encodeURIComponent(roomName) +
          '&inviteonly=' + inviteOnly +
          '&invitees=' + encodeURIComponent(invitees));

  return false;
}
goog.exportSymbol('submitCreateRoom', submitCreateRoom);

/**
 * @param {string} channelName
 */
function acceptInvitation(channelName) {
  window.location.href =
      '/channel/invitation/accept?name=' + encodeURIComponent(channelName);
}
goog.exportSymbol('acceptInvitation', acceptInvitation);

/**
 * @param {string} channelName
 */
function declineInvitation(channelName) {
  window.location.href =
      '/channel/invitation/decline?name=' + encodeURIComponent(channelName);
}
goog.exportSymbol('declineInvitation', declineInvitation);

/**
 * @param {string} channelName
 */
function requestInvitation(channelName) {
  window.location.href =
      '/channel/invitation/request?name=' + encodeURIComponent(channelName);
}
goog.exportSymbol('requestInvitation', requestInvitation);

/**
 * @param {string} channelName
 */
function getInvitation(channelName) {
  window.location.href =
      '/channel/invitation/get?name=' + encodeURIComponent(channelName);
}
goog.exportSymbol('getInvitation', getInvitation);

function displayChannels(userInfo, targetNode) {
  targetNode.setAttribute('style', 'display: block');
  if (userInfo.error) {
    targetNode.innerHTML = "ERROR: " + userInfo.error;
    return;
  }

  var channelListNode = goog.dom.$dom('ul', 'channel-list');

  var channels = userInfo['channels'];
  for (var i = 0, channel; channel = channels[i]; i++) {
    var linkNode = goog.dom.$dom(
        'a',
        {'href': '/channel/' + channel['name']},
        channel['name']);
    var descriptionNode = goog.dom.$dom(
        'span',
        'description',
        ' as ',
        goog.dom.$dom('b', {}, channel['alias']),
        channel['memberCount'] > 1
            ? ' with ' + (channel['memberCount'] - 1) +
                (channel['memberCount'] == 2 ? ' other' : ' others')
            : '');
    var channelNode = goog.dom.$dom('li', {}, linkNode, descriptionNode);
    channelListNode.appendChild(channelNode);
  }

  targetNode.appendChild(channelListNode);
}
goog.exportSymbol('displayChannels', displayChannels);

function printEmail(opt_anchorText) {
  var a = [112, 97, 114, 116, 121, 99, 104, 97, 112, 112, 64, 103, 111, 111,
      103, 108, 101, 103, 114, 111, 117, 112, 115, 46, 99, 111, 109];
  var b = [];
  for (var i = 0; i < a.length; i++) {
    b.push(String.fromCharCode(a[i]));
  }
  b = b.join('');
  document.write('<' + 'a href="mailto:' + b + '">' +
                 (opt_anchorText || b) +
                 '<' + '/a>');
}
goog.exportSymbol('printEmail', printEmail);

var tieCollapseButton = function(headerId, targetId){
	var collapsed;
	var header = document.getElementById(headerId);
	var button = document.createElement('div');
	var target = document.getElementById(targetId);
	
	var change = function(){
		if(collapsed){
			collapsed = false;
			button.innerHTML = '[-]';
			target.style.display = 'inherit';
		}else{
			collapsed = true;
			button.innerHTML = '[+]';
			target.style.display = 'none';
		}
	}

	//Init button div
	button.onclick = change;
	button.style.display = 'inline';
	header.insertBefore(button);
	
	//Init state
	button.innerHTML = '[+]';
	collapsed = true;
	target.style.display = 'none';
}
goog.exportSymbol('tieCollapseButton', tieCollapseButton);


var kickOnClick = function(c, j, b, r){
	var channel = c;
	var jid = j;
	var button = b;
	
	var kick = function(){
		goog.net.XhrIo.send('/channel/kick', function(e){
				var xhr = e.target;
				if(xhr.getResponseText() == 'success'){
					r.parentNode.removeChild(r);
				}else{
					alert(xhr.getResponseText());
				}
			},
			'POST',
			'name=' + encodeURIComponent(channel) +
			'&member=' + encodeURIComponent(jid));
	}
	button.onclick = kick;
}
goog.exportSymbol('kickOnClick', kickOnClick);

var adminOnClick = function(c, j, d, current){
	var channel = c;
	var jid = j;
	var permissions = ['member', 'mod', 'admin'];
	var dropdown = d;

	/**
	 * @param {number} toSet
	 */
	var change = function(){
		var toSet = d.selectedIndex;
		goog.net.XhrIo.send('/channel/changePermissions', function(e){
				var xhr = e.target;
				if(xhr.getResponseText() != 'success'){
					alert(xhr.getResponseText());
				}
				window.location.reload(true);
			},
			'POST',
			'name=' + encodeURIComponent(channel) +
			'&toModify=' + encodeURIComponent(jid) +
			'&permissions=' + encodeURIComponent(permissions[toSet]));
	}
	for (var i = 0; i < permissions.length; i++){
		if (current.toLowerCase() == permissions[i].toLowerCase()){
			d.selectedIndex = i;
		}
	}
	d.onchange = change;
}
goog.exportSymbol('adminOnClick', adminOnClick);