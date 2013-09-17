package com.blueferdi.diamondbox.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Aug 20, 2013 3:37:00 PM
 * @version 0.0.1
*/
public class MongoRSClient 
{
    public static void main(String[] args) throws UnknownHostException, InterruptedException    
    {
        List<ServerAddress> addrs = new ArrayList<ServerAddress>();
        
        addrs.add(new ServerAddress("127.0.0.1:27017"));
        addrs.add(new ServerAddress("127.0.0.1:27018"));
        
        Mongo mongo = new Mongo(addrs);
        
        mongo.setReadPreference(ReadPreference.secondaryPreferred());
        
        
        
        int count = 0;
        
        while(true)
        {
            try
            {
                DB db = mongo.getDB("rs_test");
                System.out.println(db.getCollection("rs_test").save(new BasicDBObject("test","test" + count)).getN());
            
                System.out.println(db.getCollection("rs_test").findOne(new BasicDBObject("test","test" + count)).get("test"));
                count++;
                Thread.sleep(10000);
            }
            catch (Exception ex)
            {
                System.out.println(ex.getMessage());
            }
        }    
        
    }

}
