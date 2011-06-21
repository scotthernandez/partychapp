package info.persistent.pushbot.util;

import com.google.appengine.repackaged.com.google.common.base.Hash;
import com.google.appengine.repackaged.com.google.common.io.ByteStreams;
import com.google.common.collect.Lists;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.rss.Guid;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import org.jdom.Element;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Feeds {
  public static final Logger logger = Logger.getLogger(Feeds.class.getName());

  public static final String HUB_RELATION = "hub";  
  public static final String SELF_RELATION = "self";

  public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
  public static final String ATOM_LINK = "link";
  public static final String ATOM_REL_ATTRIBUTE = "rel";
  public static final String ATOM_HREF_ATTRIBUTE = "href";
  
  public static SyndFeed parseFeed(InputStream inputStream) {
    SyndFeedInput input = new SyndFeedInput();
    // Methods like {@link #getEntryId} rely on having access to the wire data
    input.setPreserveWireFeed(true);
    
    byte[] inputBytes;
    try {
      inputBytes = ByteStreams.toByteArray(inputStream);
    } catch (IOException err) {
      logger.log(Level.WARNING, "Feed read error 1", err);
      return null;
   }    
    
    try {
      try {
        XmlReader xmlReader = new XmlReader(new ByteArrayInputStream(inputBytes));
        return input.build(xmlReader);
      } catch (IOException err) {
        logger.log(Level.WARNING, "Feed read error 2", err);
        logger.log(Level.WARNING, "Feed contents: " + new String(inputBytes, Charset.forName("UTF-8")));
        return null;
      }
    } catch (IllegalArgumentException err) {
      logger.log(Level.WARNING, "Feed parse error 1", err);
      logger.log(Level.WARNING, "Feed contents: " + new String(inputBytes, Charset.forName("UTF-8")));
      return null;
    } catch (FeedException err) {
      logger.log(Level.WARNING, "Feed parse error 2", err);
      logger.log(Level.WARNING, "Feed contents: " + new String(inputBytes, Charset.forName("UTF-8")));
      return null;
    }  
  }

  @SuppressWarnings("unchecked")
  public
  static List<URL> getLinkUrl(SyndFeed feed, String relation) {
    List<URL> results = Lists.newArrayList();
    // Atom feeds can have links accessed directly.
    for (SyndLink link : ((List<SyndLink>) feed.getLinks())) {
      if (link.getRel().equals(relation)) {
        try {
          results.add(new URL(link.getHref()));
        } catch (MalformedURLException err) {
          logger.log(Level.INFO, "Malformed " + relation + " URL", err);
        }
      }
    }
    
    // If we have an Atom 1.0 <link> in an RSS feed, it's in the foreign markup
    // list.
    List<Element> elements = (List<Element>) feed.getForeignMarkup();
    for (Element element : elements) {
      if (element.getNamespaceURI().equals(ATOM_NAMESPACE) &&
          element.getName().equals(ATOM_LINK) &&
          relation.equals(element.getAttributeValue(ATOM_REL_ATTRIBUTE))) {
        String href = element.getAttributeValue(ATOM_HREF_ATTRIBUTE);
        if (href != null && !href.isEmpty()) {
          try {
            results.add(new URL(href));
          } catch (MalformedURLException err) {
            logger.log(Level.INFO, "Malformed " + relation + " URL", err);
          }
        }
      }
    }
  
    return results;
  }
  
  /**
   * Gets a (hopefully) unique and short identifier for this entry. 
   */
  public static String getEntryId(SyndEntry entry) {
    // Look for an Atom ID
    if (entry.getWireEntry() instanceof Entry) {
      Entry atomEntry = (Entry) entry.getWireEntry();
      if (atomEntry.getId() != null && !atomEntry.getId().isEmpty()) {
        return hash(atomEntry.getId());
      }
    }
    
    // Or an RSS GUID
    if (entry.getWireEntry() instanceof Item) {
      Item rssItem = (Item) entry.getWireEntry();
      if (rssItem.getGuid() != null) {
        Guid guid = rssItem.getGuid();
        if (guid.getValue() != null && !guid.getValue().isEmpty()) {
          return hash(guid.getValue());
        }
      }
    }
    
    // Fall back on the item link
    if (entry.getLink() != null && !entry.getLink().isEmpty()) {
      return hash(entry.getLink());
    }
    
    // Then the title
    if (entry.getTitle() != null && !entry.getTitle().isEmpty()) {
      return hash(entry.getTitle());
    }
    
    
    // Or the entry itself
    return hash(entry.toString());
  }
  
  /**
   * Gets a hash code for the input string.
   */
  private static String hash(String s) {
    try {
      return Long.toHexString(Hash.hash64(s.getBytes("UTF-8")));
    } catch (UnsupportedEncodingException err) {
      // UTF-8 is unlikely to be unsupported
      throw new RuntimeException(err);
    }
  }
  
  private Feeds() {
    // Not instantiable.
  }

}
