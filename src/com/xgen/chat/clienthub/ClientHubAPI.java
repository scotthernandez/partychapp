package com.xgen.chat.clienthub;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
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
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.repackaged.com.google.common.collect.Maps;
import com.google.appengine.repackaged.com.google.common.io.CharStreams;

public class ClientHubAPI
{
    private static final Logger logger = 
        Logger.getLogger(ClientHubAPI.class.getName());


    private static HttpResponse secureRequest(HttpUriRequest request) throws ClientHubAPIException
    {
    	if (Configuration.PASSWORD == null){
    		throw new ClientHubAPIException("No Password. Cannot make secure request.");
    	}
    	
        List<String> authpref = new ArrayList<String>();
        authpref.add(AuthPolicy.DIGEST);

        HttpClient client = new DefaultHttpClient(new GAEConnectionManager());
        client.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, authpref);

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(Configuration.USERNAME, Configuration.PASSWORD);
        CredentialsProvider provider = ((AbstractHttpClient)client).getCredentialsProvider();
        provider.setCredentials(new AuthScope(Configuration.HOST, Configuration.PORT, Configuration.REALM, "digest"), credentials);

        try{
       	  HttpResponse response = client.execute(request);
          return response;
        }catch (Exception e){
        	throw new ClientHubAPIException("Could not execute secure request because of " + e.toString());
        }
        
    }
    
    public static Map<String, ClientHubContact> mapFromEntity(HttpEntity entity) throws ClientHubAPIException{

        Map<String, ClientHubContact>returnList = Maps.newHashMap();
    	try{
	    	InputStream stream = entity.getContent();
	        InputStreamReader reader = new InputStreamReader(stream);
	        String respString = CharStreams.toString(reader); 	//I think this could be done in a more efficient way.
	        stream.close();
	        
	        //If client, response is an array of json objects: [{...},{...},...]
			if (respString.charAt(0) == '['){
	        	
	        	JSONArray array = new JSONArray(respString);
	        	for (int i = 0; i < array.length(); i++){
	        		JSONObject o = array.getJSONObject(i);
	        		ClientHubContact c = new ClientHubContact(o);
	        		returnList.put(c.getEmail(), c);
	        	}
	            return returnList;
	        	
	        //If not client or other error: {error:true, message:"..."}   
	        }else if (respString.charAt(0) == '{'){
	
	        	JSONObject object = new JSONObject(respString);
	        	if (object.has("error")){
	        		logger.warning("ClientHub returned error: " + object.optString("message", "Internal message: no error message found from ClientHub."));
	        	}else{
	            	logger.severe("Unrecognized response from clienthub.  It was a JSON object, but not an error one: \n" + object.toString());
	        	}
	        	
	        }else{
	        	logger.severe("Unrecognized response from clienthub.  It was neither a JSON array or object: \n" + respString);
	        }
			
    	}catch (IOException io){
    		logger.log(Level.WARNING, "ClientHubAPI encountered: " + io.toString());
    		throw new ClientHubAPIException("Encountered IOException");
    	}catch (JSONException json){
    		logger.log(Level.SEVERE, "JSON exception in ClientHubAPI not expected: " + json.toString());
    		throw new ClientHubAPIException("Encountered JSONException");
    	}
    	
    	return null;
        
    }
    
    public static Map<String, ClientHubContact> getClientContacts(String client) throws ClientHubAPIException{
    	
    	try{
	        URI getUri = URIUtils.createURI(Configuration.SCHEME, Configuration.HOST, -1, Configuration.CONTACTS_PATH + client, null, null);
	        HttpGet get = new HttpGet(getUri);
	        
	        HttpEntity entity = secureRequest(get).getEntity();
	        return mapFromEntity(entity);
	        
	        
    	}catch (URISyntaxException uri){
    		return null;
    	}
    }
    
    public static ClientHubContact getClientContact(String client, String email) throws ClientHubAPIException{
    	NameValuePair emailParam = new BasicNameValuePair("email", email);
    	List<NameValuePair> params = Lists.newArrayList();
    	params.add(emailParam);
    	String query = URLEncodedUtils.format(params, "UTF-8");
    	
    	try{
    		
	        URI getUri = URIUtils.createURI(Configuration.SCHEME, Configuration.HOST, -1, Configuration.CONTACTS_PATH + client, query, null);
	        HttpGet get = new HttpGet(getUri);
	        HttpEntity entity = secureRequest(get).getEntity();
	        Map<String, ClientHubContact> map = mapFromEntity(entity);
	        return map == null ? null : (!map.isEmpty() ? map.get(email) : null);
	        
    	}catch(URISyntaxException e){
    		return null;
    	}
        
    }
    
    //Note: testing url is /clienthub/api/echo
    public static void postLogJSON(String client, JSONArray array) throws ClientHubAPIException{
    	try{
	        URI uri = URIUtils.createURI(Configuration.SCHEME, Configuration.HOST, -1, Configuration.UPLOAD_PATH + client, null, null);
	        HttpPost post = new HttpPost(uri);
	        
	        logger.info("Information I'll be sending: " + array.toString());
	        post.setEntity(new StringEntity(array.toString()));
	        
	        HttpEntity entity = secureRequest(post).getEntity();
	        //HttpEntity entity = new StringEntity("{error:false, message:'stump'}");
	        
	    	InputStream stream = entity.getContent();
	        InputStreamReader reader = new InputStreamReader(stream);
	        String jsonString = CharStreams.toString(reader); 	
	        
	        if (((Character)jsonString.charAt(0)).compareTo('{') == 0){
	
	        	JSONObject object = new JSONObject(jsonString);
	        	if (object.has("error")){
	        		if (object.getBoolean("error")){
	        			logger.severe("ClientHub returned error: " + object.optString("message", "Internal message: no error message found from ClientHub."));
	        		}else{
	        			return;
	        		}
	        	}
	        	
	        }else{
	        	logger.severe("Unrecognized response from clienthub: " + jsonString);
	        }
	        
    	}catch (Exception e){
    		logger.log(Level.SEVERE, "Unexpected exception: " + e.toString());
    	}

        throw new ClientHubAPIException();
    }
    
}

//Some test data

//entity = new StringEntity("[{\"email\":\"primary@10gen.com\"," +
// "\"name\":\"John Doe\"," +
// "\"jira_username\": \"john_doe\"," + 
// "\"is_xgen\":true," +
// "\"is_jira\":false," +
// "\"is_sfdc\":false," +
// "\"role\":engineer," +
// "\"is_primary\":true }," +
//"{\"email\":\"xgen@10gen.com\"," +
// "\"name\":\"Juan del Pueblo\"," +
// "\"jira_username\": \"juanito_poblano\"," + 
// "\"is_xgen\":true," +
//"\"is_jira\":false," +
//"\"is_sfdc\":false," +
//"\"role\":engineer," +
// "\"is_primary\":false }," +
//"{\"email\":\"someone@company.com\"," +
// "\"name\":\"Karyn Park\"," +
// "\"jira_username\": \"some_contact_1\"," + 
// "\"is_xgen\":false," +
//"\"is_jira\":true," +
//"\"is_sfdc\":false," +
//"\"role\":client," +
// "\"is_primary\":false }," +
//"{\"email\":\"another@company.com\"," +
// "\"name\":\"Gary\"," +
// "\"jira_username\": \"some_contact_2\"," + 
// "\"is_xgen\":false," +
//"\"is_jira\":true," +
//"\"is_sfdc\":true," +
//"\"role\":client," +
// "\"is_primary\":false }]");