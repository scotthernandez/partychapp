package com.imjasonh.partychapp.server.command;


import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.Permissions;
import com.imjasonh.partychapp.logging.LogDAO;
import com.imjasonh.partychapp.logging.LogEntry;

public class LogHandler implements CommandHandler {

	@Override
	public void doCommand(Message msg) {
		LogDAO.put(new LogEntry(msg));
	}

	@Override
	public boolean matches(Message msg) {
		return !msg.channel.isLoggingDisabled();
	}

	@Override
	public String documentation() {
		// Return null so it doesn't come up in documentation.
		return null;
	}

	@Override
	public boolean allows(Message msg) {
		return msg.member.hasPermissions(Permissions.MEMBER);
	}
}
