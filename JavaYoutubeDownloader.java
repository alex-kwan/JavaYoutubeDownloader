import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.codec.net.URLCodec;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

public class JavaYoutubeDownloader {

 public String getVideoID( String urlString ){
	 int start = urlString.indexOf("?v=") + 3;
	 int end = urlString.indexOf("&", start);
	 if ( end == -1 ){
		 end = urlString.length();
	 }
	 
	 return urlString.substring(start, end);
 }
 
 public String getExtension(int format) {
  return "mp4";
 }

 public void download(String videoId, int format, String encoding, String userAgent, File outputdir, String extension) throws Throwable {
  Utils.log.fine("Retrieving " + videoId);
  List<NameValuePair> qparams = new ArrayList<NameValuePair>();
  qparams.add(new BasicNameValuePair("video_id", videoId));
  qparams.add(new BasicNameValuePair("fmt", "" + format));
  URI uri = getUri("get_video_info", qparams);

  CookieStore cookieStore = new BasicCookieStore();
  HttpContext localContext = new BasicHttpContext();
  localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

  HttpClient httpclient = new DefaultHttpClient();
  HttpGet httpget = new HttpGet(uri);
  httpget.setHeader("User-Agent", userAgent);

  Utils.log.finer("Executing " + uri);
  HttpResponse response = httpclient.execute(httpget, localContext);
  HttpEntity entity = response.getEntity();
  if (entity != null && response.getStatusLine().getStatusCode() == 200) {
   InputStream instream = entity.getContent();
   String videoInfo = getStringFromInputStream(encoding, instream);
   if (videoInfo != null && videoInfo.length() > 0) {
    List<NameValuePair> infoMap = new ArrayList<NameValuePair>();
    URLEncodedUtils.parse(infoMap, new Scanner(videoInfo), encoding);
    String downloadUrl = null;
    String filename = videoId;
    int bestQuality = -1;
    for (NameValuePair pair : infoMap) {
     String key = pair.getName();
     String val = pair.getValue();
     Utils.log.finest(key + "=" + val);
     if (key.equals("title")) {
      filename = val;
     } else if (key.equals("url_encoded_fmt_stream_map")) {
      String[] formats = Utils.commaPattern.split(val);
      
      String fmtString = null;
      for (String fmt : formats) {
    	  int itagLocation = fmt.indexOf("itag=");
    	  if ( itagLocation == -1 ) continue;
    	  itagLocation += 5;
    	  int tempQuality = Integer.parseInt(fmt.substring(itagLocation, fmt.indexOf("&", itagLocation)));
    	  if ( bestQuality < tempQuality ){
    		  bestQuality = tempQuality;
    		  fmtString = fmt;
    	  }
      }
       //we are going to automatically download the best quality youtube
    	   int begin = fmtString.indexOf("url=");
    	   int sig = fmtString.indexOf("sig=");
           if (begin != -1) {
               int end = fmtString.indexOf("&", begin + 4);
               int end2 = fmtString.indexOf("&", sig + 4);
               if (end == -1) {
                  end = fmtString.length();
               }
               String tempURL = fmtString.substring(begin+ 4, end );
               String signatureURL = "&signature="+fmtString.substring(sig + 4, end2);
               downloadUrl = new String(URLCodec.decodeUrl((tempURL + signatureURL).getBytes()));
               break;
           }
     	}
    }
    
    if ( downloadUrl == null ){
    	Utils.log.fine("Content is protected");
    }
    filename = cleanFilename(filename);
    if (filename.length() == 0) {
     filename = videoId;
    } else {
     filename += "_" + videoId;
    }
    filename += "." + extension;
    File outputfile = new File(outputdir, filename);
    if (downloadUrl != null) {
     downloadWithHttpClient(userAgent, downloadUrl, outputfile);
    }
   }
  }
 }

 public void downloadWithHttpClient(String userAgent, String downloadUrl, File outputfile) throws Throwable {
 
  HttpGet httpget2 = new HttpGet(downloadUrl);
  Utils.log.finer("Executing " + httpget2.getURI());
  HttpClient httpclient2 = new DefaultHttpClient();
  
  HttpResponse response2 = httpclient2.execute(httpget2);
  HttpEntity entity2 = response2.getEntity();
 
  if (entity2 != null && response2.getStatusLine().getStatusCode() == 200) {
   long length = entity2.getContentLength();
   InputStream instream2 = entity2.getContent();
   Utils.log.finer("Writing " + length + " bytes to " + outputfile);
   if (outputfile.exists()) {
    outputfile.delete();
   }
   FileOutputStream outstream = new FileOutputStream(outputfile);
   try {
    byte[] buffer = new byte[2048];
    int count = -1;
    while ((count = instream2.read(buffer)) != -1) {
     outstream.write(buffer, 0, count);
    }
    outstream.flush();
   } finally {
    outstream.close();
   }
  }
 }

 private String cleanFilename(String filename) {
  for (char c : Utils.ILLEGAL_FILENAME_CHARACTERS) {
   filename = filename.replace(c, '_');
  }
  return filename;
 }

 private static URI getUri(String path, List<NameValuePair> qparams) throws URISyntaxException {
  URI uri = URIUtils.createURI(Utils.scheme, Utils.host, -1, "/" + path, URLEncodedUtils.format(qparams, "UTF-8"), null);
  return uri;
 }

 private String getStringFromInputStream(String encoding, InputStream instream) throws UnsupportedEncodingException, IOException {
  Writer writer = new StringWriter();

  char[] buffer = new char[1024];
  try {
   Reader reader = new BufferedReader(new InputStreamReader(instream, encoding));
   int n;
   while ((n = reader.read(buffer)) != -1) {
    writer.write(buffer, 0, n);
   }
  } finally {
   instream.close();
  }
  String result = writer.toString();
  return result;
 }
}

 