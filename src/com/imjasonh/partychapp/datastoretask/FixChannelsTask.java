package com.imjasonh.partychapp.datastoretask;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.WebRequest;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;


public class FixChannelsTask extends DatastoreTask {
  private static final Logger logger =
      Logger.getLogger(FixChannelsTask.class.getName());
  
  @Override
  public void handle(WebRequest url, TestableQueue q) {
    List<String> keys = keys(url);
    for (String key : keys) {
      Channel c = Datastore.instance().getChannelByName(key);
      c.put();
    }
    logger.warning(
        "Handled " + keys.size() + " keys. ");
  }

  @Override
  public Iterator<String> getKeyIterator(String lastKeyHandled) {
    return Datastore.instance().getAllEntityKeys(Channel.class, lastKeyHandled);
  }
}
