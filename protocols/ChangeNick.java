package protocols;

import java.io.*;

public class ChangeNick implements Serializable
{
	public String newName;	
	public String oldName;
	public int userID;
}