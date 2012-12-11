import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class Utils {
	public static String newline = System.getProperty("line.separator");
	public static final Logger log = Logger.getLogger(JavaYoutubeDownloader.class.getCanonicalName());
	public static final Level defaultLogLevelSelf = Level.FINER;
	public static final Level defaultLogLevel = Level.WARNING;
	public static final Logger rootlog = Logger.getLogger("");
	public static final String scheme = "http";
	public static final String host = "www.youtube.com";
	public static final Pattern commaPattern = Pattern.compile(",");
	public static final char[] ILLEGAL_FILENAME_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' };
	

	 public static void setupLogging() {
	  changeFormatter(new Formatter() {
	   @Override
	   public String format(LogRecord arg0) {
	    return arg0.getMessage() + newline;
	   }
	  });
	  explicitlySetAllLogging(Level.FINER);
	 }

	 public static void changeFormatter(Formatter formatter) {
	  Handler[] handlers = rootlog.getHandlers();
	  for (Handler handler : handlers) {
	   handler.setFormatter(formatter);
	  }
	 }

	 public static void explicitlySetAllLogging(Level level) {
	  rootlog.setLevel(Level.ALL);
	  for (Handler handler : rootlog.getHandlers()) {
	   handler.setLevel(defaultLogLevelSelf);
	  }
	  log.setLevel(level);
	  rootlog.setLevel(defaultLogLevel);
	 }


}
