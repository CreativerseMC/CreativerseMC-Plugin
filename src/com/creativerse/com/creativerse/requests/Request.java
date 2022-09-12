package com.creativerse.requests;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class Request {

    public static String upload(String API_KEY, File file) throws Exception{
        URL url = new URL("https://api.nft.storage/upload");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST");
        http.addRequestProperty("Authorization", "Bearer " + API_KEY);
        http.setDoOutput(true);

        byte[] b = new byte[(int) file.length()];

        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(b);

        http.connect();
        try(OutputStream out = http.getOutputStream()) {
            out.write(b);
        }

        InputStream input = http.getInputStream();
        return new String(input.readAllBytes());
    }

    public static byte[] getFile(String IPFS_NODE, String cid) throws Exception {
        URL url = new URL(IPFS_NODE + "/api/v0/cat?arg=" + cid);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.connect();

        InputStream input = http.getInputStream();
        byte[] b = input.readAllBytes();

        return b;
    }
}
