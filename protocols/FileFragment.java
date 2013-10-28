package protocols;

import java.io.Serializable;

/**
 * File is transferred between clients in fragments. The 
 * FileFragment class allows the file to be broken and transferred
 * in parts.
 */
public class FileFragment extends FileTransferMessage 
						  implements Serializable
{
	/**A byte array representing file fragment*/
	private byte[] fileData;
	
	/**Valid bytes available for reading*/
	private short bytesAvailable;
	
	/**Is this fragment the last fragment*/
	//This removes the requirement to check size of file
	private boolean lastFragment;
	
	/**Get the file fragment data*/
	public byte[] getFileData()
	{
		return fileData;
	}
	
	/**Set the file fragment to transfer*/
	public void setFileData( byte[] value )
	{
		fileData = value;
	}
	
    /**
     * @return Returns the bytesAvailable.
     */
    public short getBytesAvailable()
    {
        return bytesAvailable;
    }
    
    /**
     * @param bytesAvailable The bytesAvailable to set.
     */
    public void setBytesAvailable( short bytesAvailable )
    {
        this.bytesAvailable = bytesAvailable;
    }
	
    /**Is this fragment the last file fragment*/
	public boolean getLastFragmentFlag()
	{
		return lastFragment;
	}
	
	/**Set if this fragment is the last file fragment*/
	public void setLastFragmentFlag( boolean value )
	{
		lastFragment = value;
	}
}
