package com.imjasonh.partychapp.logging;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.server.json.JsonServlet;


@SuppressWarnings("serial")
public class DownloadLogServlet extends HttpServlet{

	  private static final Logger logger =
	      Logger.getLogger(JsonServlet.class.getName());
	  private static final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH mm");
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

			    String channelName = req.getParameter("name");
			    
			    String startStr = req.getParameter("startDate") + " " 
			    				+ req.getParameter("startHour") + " "
			    				+ req.getParameter("startMin");
			    						
			    String endStr = req.getParameter("endDate") + " " 
							  + req.getParameter("endHour") + " "
							  + req.getParameter("endMin");
			 
			    Date startDate;
			    Date endDate;
			    try {
					startDate = formatter.parse(startStr);
					endDate = formatter.parse(endStr);
			    } catch (ParseException e) {
					logger.log(Level.SEVERE, "Servlet received invalid date string: " + startStr + " or " + endStr);
					return;
				} 
			    
			    
			    
			    if (req.getParameter("Delete") != null){
			    	LogDAO.deleteLogByDates(channelName, startDate, endDate);
			    	resp.sendRedirect(Datastore.instance().getChannelByName(channelName).webUrl());
			    	return;
			    	
			    }else{ //"Download"
				    try{
						ServletOutputStream out = resp.getOutputStream();
						StringBuffer sb = generateFileBuffer(channelName, startDate, endDate);
				 
						if (sb == null){
							//If nothing to download, just return.
							//TODO: return some JSON object so the page can give feedback to user.
					    	resp.sendRedirect(Datastore.instance().getChannelByName(channelName).webUrl());
							return;
						}
						
				        resp.setContentType("text/plain");
				        resp.setHeader("Content-Disposition","attachment;filename=temp.txt");
						
						InputStream in = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
				 
						byte[] outputByte = new byte[4096];
						//copy binary content to output stream
						while(in.read(outputByte, 0, 4096) != -1)
						{
							out.write(outputByte, 0, 4096);
						}
						
						in.close();
						out.flush();
						
			    	}catch (Exception e) {
						logger.log(Level.SEVERE, "Some other exception, probably IO.");
						e.printStackTrace();
					}
			    }
    }
		 
		private static StringBuffer generateFileBuffer(String channelName, Date startStr, Date endStr)
		{
			List<LogEntry> log = LogDAO.getLogByDates(channelName, startStr, endStr);
			
			if (log.isEmpty()){
				return null;
			}
			
			StringBuffer writer = new StringBuffer();
		 
		    writer.append("Log for room " + channelName + ": \n");
		    
		    for (LogEntry entry : log){
		    	writer.append(entry.toString());
		    	writer.append("\n");
		    }
		 
			return writer;
		}
}
