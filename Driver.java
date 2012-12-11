import java.io.File;


public class Driver {
	
	 private static void usage(String error) {
		  if (error != null) {
		   System.err.println("Error: " + error);
		  }
		  System.err.println("usage: JavaYoutubeDownload url destination");
		  System.exit(-1);
		 }

	 
	public static void main(String[] args){
		try {
			   if ( args.length < 2 ){
				  usage( null );
			   }
			   Utils.setupLogging();
			   JavaYoutubeDownloader downloader = new JavaYoutubeDownloader();
			   Utils.log.fine("Starting");
			   String videoId = null;
			   String outdir = ".";
			   if (args.length == 2) {
				   videoId = downloader.getVideoID( args[0]);
				   outdir = args[1];
			   }

			   int format = 18; // http://en.wikipedia.org/wiki/YouTube#Quality_and_codecs
			   String encoding = "UTF-8";
			   String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13";
			   File outputDir = new File(outdir);
			   String extension = downloader.getExtension(format);
			   downloader.download(videoId, format, encoding, userAgent, outputDir, extension);

			  } catch (Throwable t) {
			   t.printStackTrace();
			  }
			  Utils.log.fine("Finished");
	}
}
