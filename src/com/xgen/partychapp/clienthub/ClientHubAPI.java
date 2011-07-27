package com.xgen.partychapp.clienthub;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

import net.rtep.nosockHttpClient.GAEConnectionManager;

import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.auth.params.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.params.*;
import org.apache.http.client.utils.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.repackaged.com.google.common.io.CharStreams;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.logging.LogDAO;
import com.imjasonh.partychapp.logging.LogEntry;
import com.imjasonh.partychapp.logging.LogJSONUtil;

public class ClientHubAPI
{
    private static final Logger logger = 
        Logger.getLogger(ClientHubAPI.class.getName());

    static final String SCHEME = "https";
    static final String HOST = "dev.10gen.com";
    static final int PORT = 443;
    static final String REALM = "clienthub";

    static final String USERNAME = "partychapp";
    static final String PASSWORD = "VG7VoQDFV64YJIhUUMlIwnjrUScBF9GG";

    private static HttpResponse secureRequest(HttpUriRequest request) throws Exception
    {
        List<String> authpref = new ArrayList<String>();
        authpref.add(AuthPolicy.DIGEST);

        HttpClient client = new DefaultHttpClient(new GAEConnectionManager());
        client.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, authpref);

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(USERNAME, PASSWORD);
        CredentialsProvider provider = ((AbstractHttpClient)client).getCredentialsProvider();
        provider.setCredentials(new AuthScope(HOST, PORT, REALM, "digest"), credentials);

        HttpResponse response = client.execute(request);
        
        return response;
    }
    
    public static void runTest() throws Exception{
        
        System.out.println(getClientContactList("monster.co"));
        System.out.println(getClientContact("monster.com", "scott@10gen.com"));
        System.out.println(getClientContact("monster.co", "scott@10gen.com"));
    	
        System.out.println("postTest: " + postTest());
    }
    
    public static List<ClientHubContact> listFromEntity(HttpEntity entity) throws Exception{
    	
    	InputStream stream = entity.getContent();
        InputStreamReader reader = new InputStreamReader(stream);
        String jsonString = CharStreams.toString(reader); 	//I think this could be done in a more efficient way.
        stream.close();
        
        List<ClientHubContact>returnList = Lists.newArrayList();
        if (jsonString.charAt(0) == '['){
        	
        	JSONArray array = new JSONArray(jsonString);
        	for (int i = 0; i < array.length(); i++){
        		JSONObject o = array.getJSONObject(i);
        		returnList.add(new ClientHubContact(o));
        	}
            return returnList;
        	
        }else if (jsonString.charAt(0) == '{'){

        	JSONObject object = new JSONObject(jsonString);
        	if (object.has("error")){
        		logger.warning("ClientHub returned error: " + object.optString("message", "Internal message: no error message found from ClientHub."));
        	}else{
            	logger.severe("Unrecognized response from clienthub.  It was a JSON object, but not an error one: \n" + object.toString());
        	}
        	return null;
        	
        }else{
        	logger.severe("Unrecognized response from clienthub.  It was neither a JSON array or object.");
        	return null;
        }
        
    }
    
    public static List<ClientHubContact> getClientContactList(String client) throws Exception{
        URI getUri = URIUtils.createURI(SCHEME, HOST, -1, "/clienthub/api/contacts/" + client, null, null);
        HttpGet get = new HttpGet(getUri);
        
        HttpEntity entity = secureRequest(get).getEntity();
        
        return listFromEntity(entity);
    }
    
    public static ClientHubContact getClientContact(String client, String email) throws Exception{
    	NameValuePair emailParam = new BasicNameValuePair("email", email);
    	List<NameValuePair> params = Lists.newArrayList();
    	params.add(emailParam);
    	String query = URLEncodedUtils.format(params, "UTF-8");
        URI getUri = URIUtils.createURI(SCHEME, HOST, -1, "/clienthub/api/contacts/" + client, query, null);
        HttpGet get = new HttpGet(getUri);
        
        HttpEntity entity = secureRequest(get).getEntity();

        List<ClientHubContact> list = listFromEntity(entity);
        return list != null && list.size() > 0 ? list.get(0) : null;
        
    }
    
    //FIXME: Change uri from test to actual API call /clienthub/api/upload/chatlog/[client_name]
    public static boolean postLogJSON(String client, JSONArray array) throws Exception{
        URI uri = URIUtils.createURI(SCHEME, HOST, -1, "/clienthub/api/echo", null, null);
        HttpPost post = new HttpPost(uri);
        
        post.setEntity(new StringEntity("{error:false, message:'stump'}"));
        //FIXME: post.setEntity(new StringEntity(array.toString()));
        
        HttpEntity entity = secureRequest(post).getEntity();
        
    	InputStream stream = entity.getContent();
        InputStreamReader reader = new InputStreamReader(stream);
        String jsonString = CharStreams.toString(reader); 	
        
        if (jsonString.charAt(0) == '{'){

        	JSONObject object = new JSONObject(jsonString);
        	if (object.has("error")){
        		if (object.getBoolean("error")){
        			logger.warning("ClientHub returned error: " + object.optString("message", "Internal message: no error message found from ClientHub."));
        		}else{
        			return true;
        		}
        	}
        	
        }else{
        	logger.severe("Unrecognized response from clienthub.");
        }
        
    	return false;
    }
    
    public static boolean postTest(){
		
		//TODO if(msg.channel.isHubLinked()){
		Channel channel = Datastore.instance().getChannelByName("asdf");
		
		Calendar start = new GregorianCalendar();
		start.set(1990, 1, 1);
		
			List<LogEntry> log = LogDAO.getLogByDates(channel.getName(), start.getTime(), new Date());
			JSONArray json = LogJSONUtil.entriesMillisecondDate(log);
			try{
				if(ClientHubAPI.postLogJSON(channel.getName(), json)){
					logger.info("Sent logs from " + channel.getLogSectionStart() 
							    + " to " + channel.getLogSectionStart() 
							    + " to ClientHub client " + channel.getName() + "successfully.");
					return true;
				}else{
					logger.warning("Failed to send logs to ClientHub client " + channel.getName());
				}
			}catch(Exception e){
				logger.severe(e.toString());
				e.printStackTrace();
			}
			return false;
		
    }
}