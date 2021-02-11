/*
Copyright (c) 2021, Stu Coates
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.feeddirectory.podcastindex;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Map;

public class PodcastIndexClient {

  private String key;
  private String secret;
  private String userAgent;

  public static final String PI_API_URL = "https://api.podcastindex.org";

  /**
   * Instantiate an instance of the client
   *
   * @param key       The API key issued by podcastindex.org
   * @param secret    The API secret issued by podcastindex.org
   * @param userAgent What you wish to be identified as to podcastindex.org
   */
  public PodcastIndexClient(String key, String secret, String userAgent) {
    this.key = key;
    this.secret = secret;
    this.userAgent = userAgent;
  }

  private String authHeader(long epoch) throws UnsupportedEncodingException, NoSuchAlgorithmException {
    String auth = key + secret + epoch;

    MessageDigest crypt = MessageDigest.getInstance("SHA-1");
    crypt.reset();
    crypt.update(auth.getBytes("UTF-8"));

    return byteToHex(crypt.digest());
  }

  /**
   * Make a call to the podcastindex.org API
   *
   * @param apiEndpoint The API that should be called, e.g. "/api/1.0/podcasts/byfeedurl"
   * @param parameters  A map of the parameters that should be passed to the API
   * @return The response from the API. You should check that a 200 response code has been returned
   * @throws IOException
   * @throws UnsupportedEncodingException
   * @throws NoSuchAlgorithmException
   */
  public PodcastIndexResponse callAPI(String apiEndpoint, Map<String, String> parameters) throws IOException, UnsupportedEncodingException, NoSuchAlgorithmException {

    long start = System.currentTimeMillis();

    if (apiEndpoint.startsWith("/")) {
      apiEndpoint = PI_API_URL + apiEndpoint;
    }

    StringBuilder p = new StringBuilder(apiEndpoint);
    if (parameters != null) {
      int i = 0;
      for (String k : parameters.keySet()) {
        if (i++ == 0)
          p.append('?');
        else
          p.append('&');
        p.append(k).append('=').append(URLEncoder.encode(parameters.get(k), "UTF-8"));
      }
    }

    URL u = new URL(p.toString());
    HttpsURLConnection conn = (HttpsURLConnection) u.openConnection();
    conn.setRequestMethod("GET");

    long epoch = System.currentTimeMillis() / 1000;
    conn.setRequestProperty("User-Agent", userAgent);
    conn.setRequestProperty("X-Auth-Date", Long.toString(epoch));
    conn.setRequestProperty("X-Auth-Key", key);
    conn.setRequestProperty("Authorization", authHeader(epoch));

    conn.setDoOutput(false);
    conn.setDoInput(true);

    int code = conn.getResponseCode();

    StringBuilder content = new StringBuilder();
    if (code == 200) {
      try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
        String l = br.readLine();
        while (l != null) {
          content.append(l).append("\n");
          l = br.readLine();
        }
      }
    } else {
      try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
        String l = br.readLine();
        while (l != null) {
          content.append(l).append("\n");
          l = br.readLine();
        }
      }
    }

    long end = System.currentTimeMillis();

    // Check performance
    // System.out.println(apiEndpoint + " " + (end - start) + "ms");

    return new PodcastIndexResponse(code, content.toString());
  }

  private String byteToHex(byte[] binary) {
    try (Formatter formatter = new Formatter()) {
      for (byte b : binary) {
        formatter.format("%02x", b);
      }
      return formatter.toString();
    }
  }

}
