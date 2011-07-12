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

import com.imjasonh.partychapp.server.json.JsonServlet;

public class DownloadLogServlet extends HttpServlet{
	  private static final Logger logger =
	      Logger.getLogger(JsonServlet.class.getName());
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		 
			//tell browser program going to return an application file 
		        //instead of html page
		        resp.setContentType("text/plain");
		        resp.setHeader("Content-Disposition","attachment;filename=temp.txt");
		        

			    String channelName = req.getParameter("name");
			    String startStr = req.getParameter("startDate") + " " 
			    				+ req.getParameter("startHour") + " "
			    				+ req.getParameter("startMin");
			    						
			    String endStr = req.getParameter("endDate") + " " 
							  + req.getParameter("endHour") + " "
							  + req.getParameter("endMin");
			    
			    Date startDate;
			    Date endDate;
			    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH mm");
			    try {
					startDate = formatter.parse(startStr);
					endDate = formatter.parse(endStr);
					
					ServletOutputStream out = resp.getOutputStream();
					StringBuffer sb = generateFileBuffer(channelName, startDate, endDate);
			 
					InputStream in = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
			 
					byte[] outputByte = new byte[4096];
					//copy binary contect to output stream
					while(in.read(outputByte, 0, 4096) != -1)
					{
						out.write(outputByte, 0, 4096);
					}
					in.close();
					out.flush();
					
				} catch (ParseException e) {
					logger.log(Level.SEVERE, "Servlet received invalid date string: " + startStr + " or " + endStr);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Some other exception, probably IO.");
					e.printStackTrace();
				}
		 
			}
		 
		private static StringBuffer generateFileBuffer(String channelName, Date startStr, Date endStr)
		{
			List<LogEntry> log = LogDAO.getLogByDates(channelName, startStr, endStr);
			
			StringBuffer writer = new StringBuffer();
		 
		    writer.append("Log for room " + channelName + ":");
		    
		    for (LogEntry entry : log){
		    	writer.append(entry.toString());
		    	writer.append("\n");
		    }
		 
			return writer;
		}
}
