package protocols;

import java.io.Serializable;

public class FileTransferRequest extends FileTransferMessage 
								 implements Serializable
{
    private long fileSize;
    private String fileName;
    
    /**
     * @return Returns the fileName.
     */
    public String getFileName()
    {
        return fileName;
    }
    
    /**
     * @param fileName The fileName to set.
     */
    public void setFileName( String fileName )
    {
        this.fileName = fileName;
    }
    
    /**
     * @return Returns the fileSize.
     */
    public long getFileSize()
    {
        return fileSize;
    }
    
    /**
     * @param fileSize The fileSize to set.
     */
    public void setFileSize( long fileSize )
    {
        this.fileSize = fileSize;
    }
}