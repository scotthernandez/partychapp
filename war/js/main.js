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
  
  if (channels.length < 1){
	  targetNode.style.display = "none";
	  return;
  }
  
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
  var a = [99, 105, 114, 99, 117, 105, 116, 108, 101, 103, 111, 64, 103, 109, 97, 105, 108, 46, 99, 111, 109];
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


var joinOnClick = function(c, j, b){
	var channel = c;
	var jid = j;
	var button = b;
	
	var join = function(){
		goog.net.XhrIo.send('/channel/join', function(e){
				var xhr = e.target;
				if(xhr.getResponseText() == 'success'){
					window.location.reload(true);
				}else{
					alert(xhr.getResponseText());
				}
			},
			'POST',
			'name=' + encodeURIComponent(channel) +
			'&member=' + encodeURIComponent(jid));
	}
	button.onclick = join;
}
goog.exportSymbol('joinOnClick', joinOnClick);


var deleteOnClick = function(c, j, b){
	var channel = c;
	var jid = j;
	var button = b;
	
	var deletechan = function(){
		if(confirm('Are you sure you would like to delete this room?\n(this action cannot be undone)')){
			goog.net.XhrIo.send('/channel/delete', function(e){
					var xhr = e.target;
					if(xhr.getResponseText() == 'success'){
						window.location = "/index.jsp";
					}else{
						alert(xhr.getResponseText());
					}
				},
				'POST',
				'name=' + encodeURIComponent(channel) +
				'&member=' + encodeURIComponent(jid));
		}
	}
	button.onclick = deletechan;
}
goog.exportSymbol('deleteOnClick', deleteOnClick);


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

var enableUserRadioButton = function(bool){
	document.getElementById('jid-input').disabled = !bool;
}
goog.exportSymbol('enableUserRadioButton', enableUserRadioButton);

var changeJIDForm = function(email){
	var form = document.getElementById('change-jid-form');
	var div = document.getElementById('results');
	
	if (form.elements['use-email'][0].checked){
		form.elements['newJID'].value = email;
	}else if (form.elements['newJID'].value == ''){
		alert('Please insert an email address in the text box.');
	}
	
	
	goog.net.XhrIo.send('/user/edit', function(e){
		var xhr = e.target;
		var resp = xhr.getResponseText();
		var split = resp.split('Logout:');
		if(resp == 'Success'){
			window.location.reload(true);
		}else if(split.length > 1){
			//Dialog for logging out.
			window.location.replace(split[1]);
		}else{
			div.innerHTML = resp;
		}
	},
	'POST',
	'newJID=' + encodeURIComponent(form.elements['newJID'].value));
}
goog.exportSymbol('changeJIDForm', changeJIDForm);


var submitUserForm = function(){
	   var form = document.getElementById('user-edit-form');
	   var div = document.getElementById('results');
	 
	       //tie a value to another
	   document.getElementById('custom').value = document.getElementById('jid-input').value;
	
	   var getCheckedRadio = function(radio){
	         for(var i = 0; i < radio.length; i++){
	                if ( radio[i].checked == true){
	                       return radio[i];
	                }
	         }
	   }
	
	   var jid = getCheckedRadio(form.elements['jid']).value;
	   var alias = form.elements['alias'].value;
       
	   div.innerHTML = "";
	   
    goog.net.XhrIo.send('/user/edit/jid', function(e){
		var xhr = e.target;
		var resp = xhr.getResponseText();
		var split = resp.split('Logout:');
		if(split.length > 1){
			//Dialog for logging out.
			window.location.replace(split[1]);
		}else{
			div.innerHTML += "<br /> JID: <br /> " + resp;
		}
		goog.net.XhrIo.send('/user/edit/alias', function(e){
			var xhr = e.target;
			var resp = xhr.getResponseText();
			div.innerHTML += "<br /> Alias: <br />" + resp;
	   	},
	   	'POST',
	   	'alias=' + encodeURIComponent(alias));
		
   	},
   	'POST',
   	'jid=' + encodeURIComponent(jid));
   	
   	
}
goog.exportSymbol('submitUserForm', submitUserForm);

var submitMergeForm = function(){
   var form = document.getElementById('merge-form');
   var div = document.getElementById('results');
 
   var jid = form.elements['jid'].value;
	
	 goog.net.XhrIo.send('/user/merge/request-servlet', function(e){
			var xhr = e.target;
			var resp = xhr.getResponseText();
			div.innerHTML = resp;
		},
		'POST',
		'jid=' + encodeURIComponent(jid));
		
		
}
goog.exportSymbol('submitMergeForm', submitMergeForm);