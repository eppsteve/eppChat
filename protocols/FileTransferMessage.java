package protocols;

import java.io.Serializable;

/**
 * @author Duke
 */
public abstract class FileTransferMessage extends ClientToClientMessage
        implements Serializable
{
    /**
     * Id of the file that this fragment is part of. Allows transfer of multiple
     * files.
     */
    private int fileId;

    /**
     * Get the unique id of the file being transferred
     * 
     * @return Returns the fileId.
     */
    public int getFileId()
    {
        return fileId;
    }

    /**
     * Set the Id of the file to transfer
     * 
     * @param fileId
     *            The fileId to set.
     */
    public void setFileId( int fileId )
    {
        this.fileId = fileId;
    }
}