package utool.plugin.roundrobin.communication;

/**
 * This class is for holding attribute message pairs pulled from xml
 * This class is used by the FeedHander to temporarily store information
 * of Tags and their attributes.
 * @author waltzm
 * @version 10/20/2012
 *
 */
public class Tag {

	/**
	 * Attribute
	 */
	private String attr;
	
	/**
	 * Message
	 */
	private String msg;

	/**
	 * Constructor that accepts the attribute and tag message
	 * @param attr attribute
	 * @param msg message
	 */
	public Tag(String attr, String msg)
	{
		this.attr=attr;
		this.msg=msg;
	}

	/**
	 * Default constructor
	 */
	public Tag()
	{
		attr="";
		msg="";
	}

	/**
	 * Getter for attribute
	 * @return attribute
	 */
	public String getAttr()
	{
		return attr;
	}

	/**
	 * Getter for message
	 * @return message
	 */
	public String getMsg()
	{
		return msg;
	}

	/**
	 * Overwrites the toString to return attribute: message
	 */
	public String toString()
	{
		return attr+": "+msg;
	}

	/**
	 * Setter for attribute
	 * @param attr the attribute to include
	 */
	public void setAttr(String attr) {
		this.attr=attr;		
	}
	
	/**
	 * Setter for message
	 * @param msg the message to put in
	 */
	public void setMsg(String msg) {
		this.msg=msg;		
	}


}
