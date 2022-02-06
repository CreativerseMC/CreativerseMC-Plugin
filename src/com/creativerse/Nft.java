package com.creativerse;

import com.creativerse.Util;
import com.creativerse.requests.Request;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;

public class Nft {
    private static String logoCid = "bafkreiet7k6btmxuwwzu7spig4pci45upj2444htkhfjawll43bvmykoaq";

    public static JSONObject createJSON(String API_KEY, int id, File schem) throws Exception {
        JSONObject json = new JSONObject();

        String response = Request.upload(API_KEY, schem);
        JSONObject responseObj = new JSONObject(response);
        String cid = responseObj.getJSONObject("value").getString("cid");
        json.put("schem", "ipfs://" + cid);

        json.put("image", "ipfs://" + logoCid);
        int[] loc = Util.unpair(id);
        json.put("description", "This is plot #" + id + " located in the Creativerse as (" + loc[0] + ", " + loc[1] + ").");
        json.put("name", "Creativerse Plot #" + id);

        return json;
    }

}
