package protocols;

import java.io.Serializable;

public class FileTransferControl extends FileTransferMessage 
								 implements Serializable 
{
	/**Request to transfer the file was accepted by recipient*/
	public final static short ACCEPTED = 200;
	/**File transfer is cancelled by the sending client*/
	public final static short CANCELLED_SENDER = 400;
	/**File transfer is cancelled by the receiving client*/
	public final static short CANCELLED_RECIEVER = 401;
	/**File transfer is cancelled by the server*/
	public final static short CANCELLED_SERVER = 500;
	/** File transfer has succesfully comleted */
	public final static short TRANSFER_COMPLETE = 999;

	/**Control code used to control the current file transfer*/
	private short controlCode;

	
	/**Get the control code recieved*/
	public short getControlCode()
	{
		return controlCode;
	}
	
	/**Set the control code to be sent*/
	public void setControlCode( short value )
	{
		controlCode = value;
	}
}
