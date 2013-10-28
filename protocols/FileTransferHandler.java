
package chatclient;

import java.io.*;

import javax.swing.*;

import protocols.*;

import java.util.Random;


class FileTransferHandler
{
    //Available Modes that the class can be in
    /** The object is not engaged in any file transfer */
    static final int IDLE = 0;
    /** The object is currently recieving a file from another client */
    static final int RECEIVING = 1;
    /** The object is sending a file to another client */
    static final int SENDING = 2;
    /** The object is aborting the current file transfer */
    static final int ABORTING = 5;

    //The stage of file transfer
    /** Transfer mechanism has been initialized */
    static final int INITIALIZED = 0;
    /** Request for file transfer is sent. Waiting for acceptance */
    static final int REQUEST_SENT = 2;
    /** Request to accept file transfer recieved. Waiting for user response */
    static final int REQUEST_RECIEVED = 3;

    /** Transfer request is accepted. Get Ready to transfer file */
    static final int TRANSFER_ACCEPTED = 10;
    /** Transfer is cancelled by user or server. Abort transfer */
    static final int TRANSFER_CANCELLED = 11;
    /** Transfer has been succesfully completed */
    static final int TRANSFER_COMPLETE = 12;

    private final short BUFFER_SIZE = 1024;

    //Current Mode of the object. Initially set to IDLE
    protected int currentMode = IDLE;
    //Current state of transfer. Valid only if currentMode is not IDLE
    protected int currentStage;

    //Current message that is to be processed
    protected boolean newMessage = false;
    protected FileTransferMessage transferMessage;

    protected int fileOffset = 0;
    protected byte[] buffer;
    
    private FileInputStream inFile;
    private FileOutputStream outFile;

    protected int localClient;
    protected int remoteClient;

    protected ChatClient netConn;
    protected TransferThread transferThread;

    /**
     * Creates a new instance of <code>FileTransferHandler</code> for
     * specified sender,reciever
     * 
     * @param local
     *            The id of the local client
     * @param remote
     *            The id of the remote client
     */
    FileTransferHandler( int local, int remote, ChatClient clientObject )
    {
        localClient = local;
        remoteClient = remote;
        netConn = clientObject;
        
        //buffer = new byte[BUFFER_SIZE];

        transferThread = new TransferThread();
    }

    /**
     * @return Returns the currentMode.
     */
    int getCurrentMode()
    {
        return currentMode;
    }

    /**
     * Starts sending file to intended recipient. Sets the <i>transfer mode </i>
     * and <i>transfer state </i>
     * 
     * @param fileToSend
     *            The <code>File</code> to be sent to the user
     * @see java.io.File
     */
    protected void sendFile( File fileToSend )
    {
        if ( currentMode == IDLE )
        {
            try
            {
                inFile = new FileInputStream( fileToSend );

                //Create request to send to remote client to initiate file
                // transfer
                //newMessage = false;
                transferMessage = new FileTransferRequest();
                transferMessage.senderId = localClient;
                transferMessage.recieverId = remoteClient;
                transferMessage.setFileId( (new Random( 1000 )).nextInt() );
                ((FileTransferRequest) transferMessage).setFileName( fileToSend.getName() );
                ((FileTransferRequest) transferMessage).setFileSize( inFile.getChannel().size() );
                newMessage = true;

                currentMode = SENDING;
                currentStage = INITIALIZED;
                transferThread.setParams( ((FileTransferRequest) transferMessage).getFileName(),(int)((FileTransferRequest) transferMessage).getFileSize() );
                transferThread.start();
            }
            catch ( FileNotFoundException ex )
            {
                // TODO Auto-generated catch block
                //ex.printStackTrace();
            }
            catch ( IOException io_ex )
            {
                // TODO Auto-generated catch block
                //ex.printStackTrace();
            }
        }
    }

    /**
     * Sets up <code>FileTransferHandler</code> to start recieving a file.
     * Call when a request is recieved from another client
     * 
     * @param transferRequest
     *            The <code>FileTransferRequest</code> recieved from the
     *            sender
     */
    protected void receiveFile( File fileToRecieve, long fileSize, boolean accepted )
    {
        if ( currentMode == IDLE )
        {
            try
            {
                //newMessage = false;
                transferMessage = new FileTransferControl();
                transferMessage.setFileId( (new Random( 1000 )).nextInt() );
                transferMessage.senderId = localClient;
                transferMessage.recieverId = remoteClient;

                if ( accepted )
                {
                    ((FileTransferControl) transferMessage).setControlCode( FileTransferControl.ACCEPTED );
                    outFile = new FileOutputStream( fileToRecieve );
                }
                else
                {
                    ((FileTransferControl) transferMessage).setControlCode( FileTransferControl.CANCELLED_RECIEVER );
                }

                newMessage = true;
                currentMode = RECEIVING;
                currentStage = INITIALIZED;
                transferThread.setParams( fileToRecieve.getName(),(int)fileSize );
                transferThread.start();
            }
            catch ( FileNotFoundException ex )
            {
                // TODO Auto-generated catch block
                //ex.printStackTrace();
            }
        }
    }

    /**
     * Abort the current transfer immediately
     * 
     * @param who
     *            Who requested the transfer to be aborted. True = local client,
     *            False = remote client
     */
    protected void abortTransfer( boolean who )
    {
        if ( currentMode != IDLE )
        {
            newMessage = false;
            if ( who == true )
            {
                //newMessage = false;
                FileTransferControl reply = new FileTransferControl();
                reply.setFileId( (new Random( 1000 )).nextInt() );
                reply.senderId = localClient;
                reply.recieverId = remoteClient;
                newMessage = true;

                if ( currentMode == SENDING )
                    reply.setControlCode( FileTransferControl.CANCELLED_SENDER );
                else
                    reply.setControlCode( FileTransferControl.CANCELLED_RECIEVER );
            }
            currentMode = ABORTING;
        }
    }

    protected void setTransferMessage( FileTransferMessage msg )
    {
        //wait for old message to be completely processed
        while ( newMessage != false )
        {
            try
            {
                Thread.sleep( 2 );
            }
            catch ( InterruptedException ex )
            {
                // TODO Auto-generated catch block
                //ex.printStackTrace();
            }
        }
        transferMessage = msg;
        newMessage = true;
    }

    class TransferThread extends Thread
    {
        private String fileName;
        private int fileSize;
        
        /**
         * Set the parameters of file transfer
         * @param fileName
         * 		Name of file being transferred
         * @param fileSize
         * 		Size of file being transferred, 0 if not known
         */
        void setParams( String fileName,int fileSize )
        {
         this.fileName = fileName;
         this.fileSize = fileSize;
        }
        
        public void run()
        {
            ProgressMonitor transferProgressBar = null;
            
            this.setPriority( Thread.MIN_PRIORITY );
            while ( (currentStage != TRANSFER_CANCELLED) )
            {
                try
                {
                    Thread.sleep( 5 );
                }
                catch ( InterruptedException ex1 )
                {
                    // TODO Auto-generated catch block
                    //ex1.printStackTrace();
                }

                if ( (currentMode != IDLE) && (newMessage) )
                {
                    switch ( currentMode )
                    {
                        case SENDING:
                        {
                            switch ( currentStage )
                            {
                                case INITIALIZED:
                                    netConn.sendObject( transferMessage );
                                    newMessage = false;
                                    currentStage = REQUEST_SENT;
                                    
                                    //Assumes file size is known
                                    transferProgressBar = new ProgressMonitor( netConn.window,"FileTransfer : "+fileName,"0% Completed",0,fileSize);
                                    transferProgressBar.setProgress(0);
                                    break;

                                case REQUEST_SENT:
                                    short controlCode = ((FileTransferControl) transferMessage).getControlCode();
                                    //newMessage = false;
                                    if ( controlCode == FileTransferControl.ACCEPTED )
                                    {
                                        currentStage = TRANSFER_ACCEPTED;
                                    }
                                    else if ( controlCode == FileTransferControl.CANCELLED_RECIEVER )
                                    {
                                        try
                                        {
                                            inFile.close();
                                        }
                                        catch ( IOException ex2 )
                                        {
                                            // TODO Auto-generated catch block
                                            //ex2.printStackTrace();
                                        }
                                        
                                        //assumes correct data flow
                                        transferProgressBar.close();
                                        
                                        JOptionPane.showMessageDialog( netConn.window,"FileTransfer Cancelled",
                                                                       "File Transfer Update",JOptionPane.WARNING_MESSAGE );
                                        currentStage = TRANSFER_CANCELLED;
                                        currentMode = IDLE;
                                    }
                                    break;

                                case TRANSFER_ACCEPTED:
                                {
                                    int bytesRead = -1;
                                    newMessage = true;
                                    try
                                    {
                                        if( transferProgressBar.isCanceled() )
                                        {
                                            //Ignore for now
                                        }
                                        //inFile.skip( fileOffset );
                                        //buffer = new byte[ inFile.available() ];
                                        buffer = new byte[ BUFFER_SIZE ];
                                        bytesRead = inFile.read( buffer );
                                        fileOffset += bytesRead;
                                        
                                        transferProgressBar.setProgress( fileOffset );
                        				transferProgressBar.setNote((short)(100.0 * (fileOffset) / fileSize)+"% Completed");
                                        
                                        transferMessage = new FileFragment();
                                        //FIle id is not implemented so multiple file transfers are not possible
                                        ((FileFragment) transferMessage).setFileId( 0/*(new Random( 1000 )).nextInt()*/ );
                                        ((FileFragment) transferMessage).setFileData( buffer );
                                        ((FileFragment) transferMessage).setBytesAvailable( (short)bytesRead );
                                        transferMessage.senderId = localClient;
                                        transferMessage.recieverId = remoteClient;
                                        
                                        if( inFile.available() != 0 )
                                            ((FileFragment) transferMessage).setLastFragmentFlag( false );
                                        else
                                            ((FileFragment) transferMessage).setLastFragmentFlag( true );

                                        //System.out.println( " <---> Available : "+inFile.available() );
                                        netConn.sendObject( transferMessage );
                                    }
                                    catch ( IOException ex )
                                    {
                                        // TODO: handle exception
                                        //currentStage = ABORTING;
                                        System.out.println( "IOException" );
                                    }
                                    
                                    if( (bytesRead == -1) || (bytesRead < BUFFER_SIZE) )
                                    {
                                        try
                                        {
                                            inFile.close();
                                            transferProgressBar.close();
                                        }
                                        catch ( IOException ex2 )
                                        {
                                            // TODO Auto-generated catch block
                                            //ex2.printStackTrace();
                                        }
                                        currentStage = TRANSFER_COMPLETE;
                                        currentMode = IDLE;
                                        newMessage = false;
                                        //System.out.println( "File Transfer Complete" );
                                        JOptionPane.showMessageDialog( netConn.window,"File Transfer Completed Successfully",
                                                                       "File Transfer Update",JOptionPane.INFORMATION_MESSAGE );
                                    }
                                }
                                break;

                                //case TRANSFER_CANCELLED:
                                //break;

                                //case TRANSFER_COMPLETE:
                                //break;

                                default:
                                    break;
                            }

                            break;
                        }

                        case RECEIVING:
                        {
                            switch ( currentStage )
                            {
                                case INITIALIZED:
                                    //System.out.println( "Receiver : Transfer
                                    // Request Received" );
                                    netConn.sendObject( transferMessage );
                                    
                                    //If the user accepted get ready for transfer else send the cancel message and go idle
                                    if ( ((FileTransferControl) transferMessage).getControlCode() == FileTransferControl.ACCEPTED )
                                    {
                                        currentStage = TRANSFER_ACCEPTED;
                                        transferProgressBar = new ProgressMonitor( netConn.window,"FileTransfer : "+fileName,"0% Completed",0,fileSize);
                                        transferProgressBar.setProgress(0);
                                    }
                                    else
                                        currentMode = IDLE;
                                    newMessage = false;
                                    break;

                                case TRANSFER_ACCEPTED:
                                    try
                                    {
                                        if( transferProgressBar.isCanceled() )
                                        {
                                            //Ignore for now
                                        }
                                        outFile.write( ((FileFragment) transferMessage).getFileData(),0,
                                                       ((FileFragment) transferMessage).getBytesAvailable() );
                                        outFile.flush();
                                        newMessage = false;
                                        fileOffset = (int)outFile.getChannel().size();
                                        transferProgressBar.setProgress( fileOffset );
                        				transferProgressBar.setNote((short)(100.0 * (fileOffset) / fileSize)+"% Completed");
                                    }
                                    catch ( IOException ex )
                                    {
                                        // TODO Auto-generated catch block
                                        //ex.printStackTrace();
                                    }
                                    if ( ((FileFragment) transferMessage).getLastFragmentFlag() )
                                    {
                                        try
                                        {
                                            outFile.close();
                                            transferProgressBar.close();
                                        }
                                        catch ( IOException ex2 )
                                        {
                                            // TODO Auto-generated catch block
                                            //ex2.printStackTrace();
                                        }
                                        currentStage = TRANSFER_COMPLETE;
                                        currentMode = IDLE;
                                        System.out.println( "File Transfer Complete" );
                                        JOptionPane.showMessageDialog( netConn.window,"File Transfer Completed Successfully",
                                                                       "File Transfer Update",JOptionPane.INFORMATION_MESSAGE );
                                    }
                                    break;
                            }
                            break;
                        }

                        case ABORTING:
                            if ( newMessage )
                                netConn.sendObject( transferMessage );
                            try
                            {
                                inFile.close();
                                outFile.close();
                            }
                            catch ( IOException ex )
                            {
                                // TODO Auto-generated catch block
                                //ex.printStackTrace();
                            }
                            currentStage = TRANSFER_CANCELLED;
                            currentMode = IDLE;
                            JOptionPane.showMessageDialog( netConn.window,"FileTransfer Cancelled","File Transfer Update",JOptionPane.WARNING_MESSAGE );
                            break;

                        default:
                            break;
                    }
                }
            }
        }
    }
}