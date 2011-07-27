package com.imjasonh.partychapp.server.command;

import java.util.List;

import com.google.common.base.Strings;
import com.imjasonh.partychapp.Member.Permissions;
import com.imjasonh.partychapp.filters.SharedURL;
import com.imjasonh.partychapp.filters.SharedURLDAO;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;

public class SharedHandler extends SlashCommand{

	SharedHandler() {
		super("shared");
	}

	@Override
	public String documentation() {
		return "/shared -v | [index] - List of currently shared links in the channel. See URLs instead of titles with -v." +
				" Specify an index to get the url.";
	}

	@Override
	void doCommand(Message msg, String argument) {
		//When an arg is provided, user wants the URL.
		if (!Strings.isNullOrEmpty(argument)){
			String[] pieces = argument.split("\\s+", 1);
			if(pieces[0].compareTo("-v")==0){
				showList(msg, true);
			}else{
				try{
					int i = Integer.parseInt(pieces[0]);
					String url = SharedURLDAO.getURLByIndex(msg.channel.getName(), i);
					if (url == null){
						msg.channel.sendDirect("_That index is invalid._", msg.member);
					}else{
						msg.channel.sendDirect("url: "+url, msg.member);
					}
				}catch (NumberFormatException e){
					msg.channel.sendDirect("_The argument format is incorrect._", msg.member);
				}
			}
			return;
		}

		//No args means user wants to see list of link descriptions
		showList(msg, false);
	}
	
	private void showList(Message msg, boolean verbose){
		List<SharedURL> shared = SharedURLDAO.getURLsByChannelByDate(msg.channel.getName());
		if (!shared.isEmpty()){
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < shared.size(); i++){
				Member m = msg.channel.getMemberByJID(shared.get(i).getJID());
				String line = "("+i+") ["+m.getAlias()+"] - ";
				if (!Strings.isNullOrEmpty(shared.get(i).getTitle()) && !verbose){
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
	
	@Override
	public boolean allows(Message msg) {
		return msg.member.hasPermissions(Permissions.MEMBER);
	}

}
