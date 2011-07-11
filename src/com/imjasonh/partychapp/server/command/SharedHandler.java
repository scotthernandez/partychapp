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
		//When an arg is provided, user wants the URL.
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

		//No args means user wants to see list of link descriptions
		List<SharedURL> shared = msg.channel.getShared();
		if (!shared.isEmpty()){
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < shared.size(); i++){
				String line = "("+i+") ["+shared.get(i).getMember().getAlias()+"] - ";
				if (!Strings.isNullOrEmpty(shared.get(i).getTitle())){
					line += shared.get(i).getTitle();
				}else{
					line += shared.get(i).getUrl().toString();
				}
				if (!Strings.isNullOrEmpty(shared.get(i).getAnnotation())){
					line += ": _"+shared.get(i).getAnnotation()+"_";
				}
				builder.append(line+"\n");
			}
			msg.channel.sendDirect(builder.toString(), msg.member);
		}else{
			msg.channel.sendDirect("No one has shared anything in this channel. Start /share-ing!", msg.member);
		}
	}

}
