package utool.plugin.roundrobin.communication;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import utool.plugin.activity.AbstractIncomingCommandHandler;

import android.util.Log;

/**
 * This class is the actual parser of xml. It creates a FeedHandler and passes 
 * the xml message to it. Accessed by creating a new SaxFeedParser and 
 * calling parser.parse(message);
 * @author waltzm
 * @version 10/20/2012
 *
 */
public class SaxFeedParser{

	/**
	 * The incoming command handler to pass mssages to
	 */
	private AbstractIncomingCommandHandler handler;
	
	/**
	 * Constructor for creating the parser.
	 * Parser must be instantiated in order to call parse
	 * @param handler the incoming command handler to pass messages to
	 */
	public SaxFeedParser(AbstractIncomingCommandHandler handler)
	{
		this.handler = handler;
	}

	/**
	 * Parses the given xml message and if formatted correctly it will call
	 * the correct IncomngCommandHandler method
	 * @param msg xml String to parse
	 */
	public void parse(String msg) 
	{
		//create the factory
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			//create the parser
			SAXParser parser = factory.newSAXParser();
			//create the feedhandler
			FeedHandler feed = new FeedHandler(handler);
			//parse the msg using handler
			parser.parse(new InputSource(new StringReader(msg)), feed);
		} 
		catch (NullPointerException e) 
		{
			Log.e("Sax Feed Parser","Error: "+e.getMessage());
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			Log.e("Sax Feed Parser","Error: "+e.getMessage());
			e.printStackTrace();
		} catch (SAXException e) {
			Log.e("Sax Feed Parser","Error: "+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("Sax Feed Parser","Error: "+e.getMessage());
			e.printStackTrace();
		} 			

	}

}