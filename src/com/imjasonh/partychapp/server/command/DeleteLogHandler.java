package com.imjasonh.partychapp.server.command;


import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.logging.LogDAO;

public class DeleteLogHandler extends SlashCommand {

	DeleteLogHandler() {
		super("deleteLog1324");
	}

	@Override
	public String documentation() {
		//Not come up in docs
		return null;
	}

	@Override
	void doCommand(Message msg, String argument) {
		LogDAO.deleteAll();

	}

}
