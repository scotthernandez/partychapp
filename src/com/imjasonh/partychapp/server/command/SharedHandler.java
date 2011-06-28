package com.imjasonh.partychapp.server.command;

import java.net.URI;
import java.util.List;

import com.google.common.base.Strings;
import com.imjasonh.partychapp.Channel.SharedURL;
import com.imjasonh.partychapp.Message;

public class SharedHandler extends SlashCommand{

	SharedHandler() {
		super("shared");
	}

	@Override
	public String documentation() {
		return "/shared [index]- List of currently shared URLs in the channel.";
	}

	@Override
	void doCommand(Message msg, String argument) {
		/*If has an argument:
		*	msg.channel.getShared(arg 1)
		*   if not null:
		*		broadcast to msg.member the url
		*		return
		*
		* For each shared
		* 	add to some list "(<#>) <member> - <title>:annotation \n"
		* if not empty:
		* 	broadcast to msg.member that (String) list
		* return
		*/
		if (!Strings.isNullOrEmpty(argument)){
			String[] pieces = argument.split("\\s+", 1);
			try{
				int i = Integer.parseInt(pieces[0]);
				URI url = msg.channel.getLink(i);
				if (url == null){
					msg.channel.sendDirect("_That index is invalid._", msg.member);
				}else{
					msg.channel.sendDirect("url: "+url.toString(), msg.member);
				}
			}catch (NumberFormatException e){
				msg.channel.sendDirect("_The argument format is incorrect._", msg.member);
			}
			return;
		}
		
		List<SharedURL> shared = msg.channel.getShared();
		if (!shared.isEmpty()){
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < shared.size(); i++){
				String line = "("+i+") ["+shared.get(i).member.getAlias()+"] - ";
				if (!Strings.isNullOrEmpty(shared.get(i).title)){
					line += shared.get(i).title;
				}else{
					line += shared.get(i).uri.toString();
				}
				if (shared.get(i).annotation != null){
					line += ": _"+shared.get(i).annotation+"_";
				}
				builder.append(line+"\n");
			}
			msg.channel.sendDirect(builder.toString(), msg.member);
		}else{
			msg.channel.sendDirect("No one has shared anything in this channel. Start /share-ing!", msg.member);
		}
	}

}
