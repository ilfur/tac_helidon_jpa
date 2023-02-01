/*
 * Copyright (c) 2015 by Oracle. All Rights Reserved
 */
import oracle.ons.ONS;
import oracle.ons.Subscriber;
import oracle.ons.Notification;
import java.util.Date;
import java.nio.ByteBuffer;
import java.sql.DriverManager;
import java.util.Properties;
import oracle.jdbc.internal.OracleConnection;

public class FanWatcher 
{
  private static boolean debug = false;
  private static Subscriber s;

  public static void main(String args[]) {
    String subType =  "";
    if (args.length < 1) {
      System.out.println("Usage: java fanWatcher config [events ...]");
      System.out.println("Set config to  'crs' to use CRS");
      System.out.println("Set config to  'autoons' to use Auto-ONS; set user, password, and url in the environment to connect to the database");
      System.out.println("Set config to the configuration string otherwise, e.g., nodes=host:port,...");
      return;
    }
    String config = args[0];
    for (int i = 1; i < args.length; i++) {
      if (i > 1) {
         subType += "|";                   // more than one - OR them together
      }
      if (args[i].trim().matches("^[A-Za-z].*$")) {
        subType += "%\"" + args[i] + "\""; // upward compatible with earliest version
      } else {
        subType += args[i];                 // pass through unchanged
      }
    }  
    System.out.println("Subscribing to events of type: " + subType);
    fanWatcher onc_s = new FanWatcher(config, subType);
    while (onc_s.receiveEvents());
  }

  public FanWatcher(String config, String eventType) {
    if (config.equals("autoons")) { // auto-ONS
      try {
        Class.forName("oracle.jdbc.OracleDriver");
        String user = System.getenv("user");
        String password = System.getenv("password");
        String url = System.getenv("url");
        if (url == null || user == null || password == null) {
          System.out.println("Environment variables for user, password, and url must be set");
          System.exit(1);
        }
        java.util.Properties p = new java.util.Properties();
        p.put("url", url);
        p.put("user", user);
        p.put("password", password);
        OracleConnection oc = (OracleConnection)
          DriverManager.getConnection(p.getProperty("url"), p);
        Properties props = oc.getServerSessionInfo();
        config = props.getProperty("AUTH_ONS_CONFIG");
        if (config == null || config.equals("")) {
          System.out.println("Failed to get Auto-ONS configuration; maybe an older release");
          System.exit(1);
        }
        System.out.println("Auto-ONS configuration="+config);
        oc.abort();
        oc.close();
      } catch (Exception e) {
        System.out.println("Failed to connect to database");
        e.printStackTrace();
        System.exit(1);
      }
    }
    if (config.equals("crs")) {
      s = new Subscriber(eventType, ""); // subscribe to service events only
    } else {
      ONS ons = new ONS(config.trim());
      if (ons == null) {
        System.out.println("Failed to get ONS server");
        System.exit(1);
      }
      System.out.println("Opening FAN Subscriber Window ...\n\n\n");
      s = ons.createNewSubscriber(eventType, "");
    }
    if (s == null) {
      System.out.println("Failed to get subscriber");
      System.exit(1);
    }
    if (debug) {
       System.out.println("FANWatcher starting");
    }
  }

  public boolean receiveEvents() {
    if (debug) {
      System.out.println( "** In receiveEvents. Creating Notification now ...");
    }
    if (s == null) {
      System.out.println("Failed to get ONS server");
      System.exit(1);
    }
    Notification e = s.receive(true);  // blocking wait for notification receive
  
    // print event header to std out. Make debug only eventually
    if (debug) {
      System.out.println( "** HA event received -- Printing header:" );
      e.print();
    }

    // Print the header details
    printEvtHeader(e);
  
    if (debug) {
      System.out.println( "** Body length = " + e.body().length);
      System.out.println( "** Event type = " + e.type());
    }
  
    /* Test the event type to attempt to determine the event body format.
 
      Database events generated are "free-format" events -
      the event body is a string. It consists of space delimited 
      key=value pairs.
  
      The following test only looks for database events.
      Other events will be received, but their bodies will not be displayed.
    */
    if (e.type().startsWith("database") ) {
      if (debug) { System.out.println( "Printing event"); }
      evtPrint(e);
    } else {
      System.out.println("Unknown event type. Not displaying body"); 
    }

    try {
      if (e.type().equals("onc/shutdown")) {
        if (debug) {
          System.out.println("Shutdown event received.");
          System.out.println(" ONC subscriber exiting!");
        }
        s.close();
        return false; // don't continue
      } else {
        java.lang.Thread.currentThread().sleep(100);
        if (debug) {
          System.out.println("Sleep and retry.");
        }
      }
    } catch (Exception te) {
      te.printStackTrace(); 
    }
    return true;
  }

  public void printEvtHeader(Notification e) {
    System.out.println( "\n** Event Header **" );
    System.out.println("Notification Type: " + e.type());
    System.out.println("Delivery Time: " + new Date (e.deliveryTime()));
    System.out.println("Creation Time: " + new Date (e.creationTime()));
    System.out.println("Generating Node: " + e.generatingNode() );
  }

  // Print free format event
  public void evtPrint(Notification e) {
    if (debug)  {
       System.out.println("De-coding a free-format event");
       ByteBuffer ffbuf = ByteBuffer.wrap(e.body());
       showBufferData(ffbuf,"ffbufName");
    }

    if (debug) {
       System.out.println( "** About to generate Body Block **" ); 
    }
    // convert the byte array event body to a String
    System.out.println("Event payload:\n" + new String(e.body())); 
  }

  private void showBufferData(ByteBuffer buf, String name) {
    //Displays byte buffer contents
    int pos = buf.position();
    buf.position(0);
    if (buf.hasArray()) System.out.println("There is an array!");
    System.out.println("Raw Data for " + name);
    while(buf.hasRemaining()) {
      System.out.print( buf.get() + " ");
    }
    System.out.println();
    //Restore position and return
    buf.position(pos);
  }
}
