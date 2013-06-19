package com.blueferdi.diamondbox.json;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ferdi
 */
public class JsonRetain
{

    public static void main(String[] args)
    {
        String[] jsons =
        {
            "[{\"name\":\"xf\", \"age\":13, \"location\":\"hz\"}, {\"name\":\"fx\", \"age\":31, \"location\":\"bj\"}, {\"name\":\"zs\", \"age\":29 }]",
            "[{\"name\":\"xf\", \"job\":\"manager\", \"member\":130}, {\"name\":\"fx\", \"job\":\"sales\", \"gender\":\"male\"}]"
        };
        
        System.out.println(JsonRetain.retain(jsons, "name"));
        
    }

    public static String retain(String[] jsons, String retainKey)
    {
        Map<String, JSONObject> map = new HashMap<String, JSONObject>();

        String key = "";

        for (int i = 0; i < jsons.length; i++)
        {
            JSONArray array = JSONObject.parseArray(jsons[i]);

            for (int j = 0; j < array.size(); j++)
            {
                JSONObject object = array.getJSONObject(j);
                key = object.containsKey(retainKey) ? object.getString(retainKey) : "";

                if (map.containsKey(key))
                {
                    map.get(key).putAll(object);
                }
                else
                {
                    map.put(key, object);
                }
            }
        }

        return JSONObject.toJSONString(map.values());

    }
}
