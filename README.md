# podcastindex.org Java Client
A simple client in Java for podcastindex.org

To use this client you must have an API key and secret from podcastindex.org.

There is a dependency on [json-simple](https://github.com/cliftonlabs/json-simple) - but you can easily port this to other JSON libraries if you wish.

Get started by instantiating an instance of ``org.feeddirectory.podcastindex.PodcastIndexClient``:

``PodcastIndexClient pi=new PodcastIndexClient(KEY,SECRET,USER_AGENT)``

...and then make calls to the ``callAPI`` method:

<pre>
Map<String,String> params=new HashMap<>();
params.put("url","http://mp3s.nashownotes.com/pc20rss.xml");

PodcastIndexResponse resp=pi.callAPI("/api/1.0/podcasts/byfeedurl",params);

if(resp.getResponseCode()!=200) {
  System.err.println("Ooops!");
} else { 
  System.out.println("Rejoice!");  
  System.out.println(resp.getJsonResponse().toJson());
}
</pre>


Enjoy!

[Stu](mailto:stu@feeddirectory.org)
