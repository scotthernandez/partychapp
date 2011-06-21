package com.imjasonh.partychapp.datastoretask;

import com.google.common.collect.Iterators;

import com.imjasonh.partychapp.WebRequest;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatastoreTaskMaster extends DatastoreTask {
  private static final Logger logger =
        Logger.getLogger(DatastoreTaskMaster.class.getName());
  
  public static final int kNumPerBatch = 10;
  
  @Override
  public void handle(WebRequest req, TestableQueue q) {
    long startTime = System.currentTimeMillis();
    
    // get a JDO extent. this only works with LiveDatastore, so cast away.
    // grab N items, get their keys and make a JSON array of their names
    // add a task with that array as an arg
    Action act;
    try {
      act = Action.valueOf(req.getParameter("act"));
    } catch (IllegalArgumentException err) {
      logger.warning("Could not find action " + req.getParameter("act"));
      return;
    } catch (NullPointerException err) {
      logger.warning("Missing action parameter");
      return;
    }

    // This is only passed if we break and resume.
    String lastKeyHandled = req.getParameter("lastKeyHandled");

    String maxParam = req.getParameter("max");
    int max = maxParam != null ? Integer.parseInt(maxParam) : -1;
    
    Iterator<String> keys = act.datastoreTask.getKeyIterator(lastKeyHandled);
          
    int count = 0;
    boolean suppressContinuation = false;
    TestableQueue.Options opts = new TestableQueue.Options("/tasks/" + act.name());
    while (keys.hasNext()) {
      if ((max > 0) && ((count+1) > max)) {
        suppressContinuation = true;
        break;
      }

      lastKeyHandled = keys.next();
      ++count;
      
      opts.param("key", lastKeyHandled);
      if ((count % kNumPerBatch) == 0) {
        q.add(opts);

        opts = new TestableQueue.Options("/tasks/" + act.name());
      }
      
      // cut ourselves off after 20 seconds. we don't want app engine to kill us.
      if ((System.currentTimeMillis() - startTime) > 20*1000) {
        break;
      }
    }
    if ((count % kNumPerBatch) != 0) {
      q.add(opts);
      opts = new TestableQueue.Options("/tasks/" + act.name());
    }
    if (count < max) {
      suppressContinuation = true;
    }
    if (count != 0) {
      if (!suppressContinuation) {
        logger.log(Level.INFO,
                "created sub-tasks for " + count + " objects. creating " +
                "replacement task for remaining objects. lastKeyHandled = " +
                lastKeyHandled);
        // just add a replacement task, and only end when we try and there's nothing else remaining.
        TestableQueue.Options replacement = new TestableQueue.Options("/tasks/" + Action.MASTER_TASK.name());
        q.add(replacement.param("act", act.name())
                         .param("lastKeyHandled", lastKeyHandled));          
      } else {
        logger.log(Level.INFO,
                "created sub-tasks for " + count + " objects. suppressing replacement. " +
                "lastKeyHandled = " + lastKeyHandled);
      }
    } else {
      logger.log(Level.INFO,
              "all done! lastKeyHandled = " + lastKeyHandled);
    }
  }

  /**
   * The master task doesn't need to iterate over any keys.
   */
  @Override
  public Iterator<String> getKeyIterator(String lastKeyHandled) {
    return Iterators.emptyIterator();
  }
}
