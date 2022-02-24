package com.creativerse;

import com.creativerse.Util;
import com.creativerse.requests.Request;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

public class Nft {
    private static String logoCid = "bafkreiet7k6btmxuwwzu7spig4pci45upj2444htkhfjawll43bvmykoaq";

    public static JSONObject createJSON(String API_KEY, int id, File schem, File gltf, long date, int[] xz) throws Exception {
        JSONObject json = new JSONObject();

        String response = Request.upload(API_KEY, schem);
        JSONObject responseObj = new JSONObject(response);
        String cid = responseObj.getJSONObject("value").getString("cid");
        json.put("schem", "ipfs://" + cid);

        response = Request.upload(API_KEY, gltf);
        responseObj = new JSONObject(response);
        cid = responseObj.getJSONObject("value").getString("cid");
        json.put("animation_url", "ipfs://" + cid + "?filename=animation.gltf");

        JSONObject dateAttribute = new JSONObject("{" +
                "\"display_type\": \"date\"," +
                "\"trait_type\": \"Last Saved\"," +
                "\"value\":\"" + date + "\"" +
                "}");
        JSONObject xAttribute = new JSONObject("{" +
                "\"display_type\": \"number\"," +
                "\"trait_type\": \"Plot X\"," +
                "\"value\": " + xz[0] + "}");
        JSONObject zAttribute = new JSONObject("{" +
                "\"display_type\": \"number\"," +
                "\"trait_type\": \"Plot Z\"," +
                "\"value\": " + xz[1] + "}");
        Collection c = new ArrayList();
        c.add(dateAttribute);
        c.add(xAttribute);
        c.add(zAttribute);

        json.put("attributes", c);
        json.put("image", "ipfs://" + logoCid);
        int[] loc = Util.unpair(id);
        json.put("description", "This is plot #" + id + " located in the Creativerse as (" + loc[0] + ", " + loc[1] + ").");
        json.put("name", "Creativerse Plot #" + id);

        return json;
    }

}
