package chatclient;

import java.io.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;


import protocols.*;

public class ClientInterface extends JFrame
{
	private ChatClient client;

	//Have to create a new object each time so that ObjectStream will read it
	//private protocols.Message msg = new protocols.Message();

	static JList lstClients;
	
	//Holds references to all message windows
	protected Vector messageWindows;
	
	//Provides mapping of tab number to clientId
	protected Vector tabToClient;

	protected JButton bSend;
	protected JTextField tfMessage;

	protected JScrollPane scrlClients;
	

	protected JMenuBar menuBar;
	protected JMenu hlpMenu;
	protected JMenu tabMenu;
	protected JMenu fileMenu;
	protected JMenu chatMenu;
	protected JMenuItem sendChatMenu;
	protected JMenuItem nickChatMenu;
	protected JMenuItem exitFMenu;
	protected JMenuItem aboutHlpMenu;
	protected JMenuItem closeTabMenu;
	protected JMenuItem connectFMenu;
	protected JMenuItem configureFMenu;
	protected JMenuItem	disconnectFMenu;
	protected JMenuItem closeAllTabsMenu;

	protected JTabbedPane tbChatWindows;
	

	ClientInterface( ChatClient client )
	{
		super( "Epp Chat" );
		//this.setIconImage( new ImageIcon("Icons/dukeWaveRed.gif") );
		this.client = client;

		setUpMenu();
		setUpMainInterface();
		
		setVisible(true);
		tfMessage.requestFocus();
	}

	protected void setUpMenu()
	{
		sendChatMenu = new JMenuItem("Send a file");
		nickChatMenu = new JMenuItem("Change nickname");
		exitFMenu = new JMenuItem("Exit",KeyEvent.VK_X);
		connectFMenu = new JMenuItem("Connect",KeyEvent.VK_C);
		aboutHlpMenu = new JMenuItem("About",KeyEvent.VK_A);
		closeTabMenu = new JMenuItem( "Close Tab",KeyEvent.VK_C );
		configureFMenu = new JMenuItem("Configure");
		disconnectFMenu = new JMenuItem("Disconnect",KeyEvent.VK_D);
		closeAllTabsMenu = new JMenuItem( "Close All Tabs",KeyEvent.VK_A );


		
		connectFMenu.setIcon( new ImageIcon("icons/lan.gif") );
		configureFMenu.setIcon( new ImageIcon("icons/log.gif") );
		aboutHlpMenu.setIcon( new ImageIcon("icons/about.gif") );
				
		
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(connectFMenu);
		fileMenu.add(disconnectFMenu);
		fileMenu.add(configureFMenu);
		fileMenu.addSeparator();
		fileMenu.add(exitFMenu);

		chatMenu = new JMenu( "Chat" );
		chatMenu.setMnemonic( KeyEvent.VK_C );
		chatMenu.add( nickChatMenu );
		chatMenu.add( sendChatMenu );
		
		tabMenu = new JMenu( "Tabs" );
		tabMenu.setMnemonic( KeyEvent.VK_T );
		tabMenu.add( closeTabMenu );
		tabMenu.add( closeAllTabsMenu );
		
		hlpMenu = new JMenu("Help");
		hlpMenu.setMnemonic(KeyEvent.VK_H);
		hlpMenu.add(aboutHlpMenu);
		
		menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(chatMenu);
		menuBar.add(tabMenu);
		menuBar.add(hlpMenu);
		setJMenuBar(menuBar);
	}
	
	protected void setUpListeners()
	{
		sendChatMenu.addActionListener( new SendChatMenu() );
		nickChatMenu.addActionListener( new NickChatMenu() );
		configureFMenu.addActionListener( new ConfigureFMenu() );
		exitFMenu.addActionListener( new ExitButton() );
		aboutHlpMenu.addActionListener( new AboutHlpMenu() );
		connectFMenu.addActionListener( new ConnectFMenu() );
		disconnectFMenu.addActionListener( new DisconnectFMenu() );
		closeTabMenu.addActionListener( new CloseTabButton() );
		closeAllTabsMenu.addActionListener( new CloseAllTabsMenu() );

		bSend.addActionListener( new SendButton() );
				UIManager.put("bSend.background", Color.BLACK);

		tfMessage.addActionListener( new SendMessage() );
		//tfMessage.addFocusListener( new FocusMessageField() );

		lstClients.addMouseListener( new SelectClient() );
		addWindowListener( new OnExit() );
	}
	
	protected void setUpProperties()
	{
		setSize(475,280);
		setResizable(false);

		bSend.setEnabled( true );
		bSend.setToolTipText( "Send Your Message" );
		
		tfMessage.setEnabled( false );
		tfMessage.setToolTipText( "Enter Your Message Here" );
		
		sendChatMenu.setEnabled(false);
		nickChatMenu.setEnabled(false);		
		disconnectFMenu.setEnabled( false );
		tabMenu.setEnabled( false );
		closeTabMenu.setEnabled( false );
		closeAllTabsMenu.setEnabled( false );

		lstClients.setFixedCellWidth(101);
		lstClients.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		lstClients.setToolTipText( "Double Click To Chat Privately" );

		scrlClients = new JScrollPane( lstClients );
		scrlClients.setColumnHeaderView( new JLabel("Online Users" ) );
		
		tbChatWindows.setToolTipText( "Conversation Windows. Choose Recipient" );
		
		setUpListeners();
	}
		
	protected void setUpMainInterface()
	{
 		bSend = new JButton("       Send       ");

		tfMessage = new JTextField(32);		
		lstClients = new JList();
		
		setUpTabs();
		setUpProperties();
		
		Box displayBox = Box.createHorizontalBox();
		displayBox.add( tbChatWindows );
		displayBox.add( Box.createHorizontalStrut(3) );
		displayBox.add( scrlClients );

		Box commandBox = Box.createHorizontalBox();
		commandBox.add( tfMessage );
		commandBox.add( Box.createHorizontalStrut(3) );
		commandBox.add( bSend );

		Container cp = this.getContentPane();
		cp.setLayout( new FlowLayout(FlowLayout.LEFT) );
		cp.add( displayBox );
		cp.add( commandBox );
	}
	
	protected void setUpTabs()
	{
		messageWindows = new Vector(5,2);
		tabToClient = new Vector(5,2);
		tbChatWindows = new JTabbedPane( JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT );
		
		//Set up the main room tab. This tab cannot be closed
		messageWindows.addElement( new JTextArea(10,30) );
		((JTextArea)messageWindows.elementAt(0)).setLineWrap(true);
		((JTextArea)messageWindows.elementAt(0)).setEditable(false);		
		
		tbChatWindows.addTab( "Main",new JScrollPane( (JTextArea)
							messageWindows.elementAt(0),
							JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
							JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		
		tbChatWindows.setIconAt( 0,new ImageIcon( "icons/usmenu.gif" ) );
		//tbChatWindows.setForegroundAt( 0, Color.BLUE );
	}
	
	//Open a tab to chat with specified friend
	void openNewTab( int friendId )
	{
		//Check if a conversation tab is already open for the friend
		//If yes then set focus to that tab
		int index = tabToClient.indexOf( new Integer( friendId ) );
		if( index != -1 )
		{
			tbChatWindows.setSelectedIndex( index );
			return;
		}
		
		//Open a new conversation tab. Add a new message window to
		//list, map the client to tab, set properties and add tab
		messageWindows.addElement( new JTextArea(10,30) );
		tabToClient.addElement( new Integer(friendId) );
		((JTextArea)messageWindows.lastElement()).setLineWrap(true);
		((JTextArea)messageWindows.lastElement()).setEditable(false);		
		lstClients.setSelectedIndex( friendId );
		tbChatWindows.addTab( (String)lstClients.getSelectedValue(),(new JScrollPane( (JTextArea)messageWindows.lastElement() ) ) );
		tbChatWindows.setIconAt( tabToClient.size(),new ImageIcon( "icons/aol.gif" ) );

		tabMenu.setEnabled( true );
		closeTabMenu.setEnabled( true );
		closeAllTabsMenu.setEnabled( true );
	}

	private void showMessage( int tabSelected, String message )
	{
		((JTextArea)messageWindows.elementAt(tabSelected)).append( message );
	}
		
	void showMessage( Message userMsg )
	{
		int tabIndex = 0;

		//If message is public show in main room tab else sort the
		//the message to a tab using mappin in tabToClient
		if( ((Message)userMsg).audience == true )
		{
			((JTextArea)messageWindows.elementAt(tabIndex)).append( ((Message)userMsg).message + "\n");
		}
		else
		{
			tabIndex = tabToClient.indexOf( new Integer(((Message)userMsg).senderId) );
			//if( tabIndex == -1 )
				//JOptionPane.showMessageDialog( client.window, "Index Not Found", "Index Error", JOptionPane.INFORMATION_MESSAGE );				
			((JTextArea)messageWindows.elementAt(tabIndex+1)).append( ((Message)userMsg).message + "\n");
		}
		//JScrollBar hBar = scrlMessages.getVerticalScrollBar();
		//hBar.setValue( hBar.getMaximum() );
	}
	
	protected void sendMessage()
	{
		String str = tfMessage.getText();
		int tabSelected = tbChatWindows.getSelectedIndex();

		if( str.length() != 0 )
		{
			try
			{
				if( tabSelected == 0 )
				{
					client.sendPublicMessage( str );
				}
				else
				{
					Integer clientIndex = (Integer)tabToClient.elementAt( tabSelected-1 );
					client.sendPrivateMessage( clientIndex.intValue(),str );
					((JTextArea)messageWindows.elementAt( tabSelected )).append(client.Name + " says > " + str + "\n");
				}
			}
			catch( IOException io_ex )
			{
				showMessage( tabSelected,"\n\nCannot Send Message...\n\n" );
			}
			tfMessage.setText("");
		}
	}
	
	//Close all conversation tabs except the main chat room
	protected void closeAllTabs()
	{
		int index = 1;
		while( index != tbChatWindows.getTabCount() )
		{
			tbChatWindows.removeTabAt( index );
			messageWindows.removeElementAt( index );
		}
		tabToClient.clear();

		tabMenu.setEnabled( false );
		closeTabMenu.setEnabled( false );
		closeAllTabsMenu.setEnabled( false );
	}
	
	//Show effects of disconnection on the interface
	synchronized void notifyDisconnect()
	{
		connectFMenu.setEnabled( true );
		disconnectFMenu.setEnabled( false );
		tfMessage.setEnabled( false );
		bSend.setEnabled( false );
		closeAllTabs();
		//lstClients.updateUI();
		lstClients.setEnabled( false );
	}

	//If this client was in a conversation with the user then close
	//tab and inform the user that his friend has left
	void notifyUserLeft( String clientName )
	{
		int friendId = client.clientList.indexOf( clientName );
		int index = tabToClient.indexOf( new Integer( friendId ) );
	
		//If no conversation tab for the specified user
		if( index == -1 )
			return;
		
		JOptionPane.showMessageDialog( client.window,clientName+" Has Logged Out",
										"User Left",JOptionPane.INFORMATION_MESSAGE );
										
		tbChatWindows.removeTabAt( index+1 );
		messageWindows.removeElementAt( index+1 );
		tabToClient.removeElementAt( index );

		//If only the main room tab is left disable the tab menu
		if( tbChatWindows.getTabCount() == 1 )
		{
			tabMenu.setEnabled( false );
			closeTabMenu.setEnabled( false );
			closeAllTabsMenu.setEnabled( false );
		}					
	}
	
	 /**
     * Finds the nick for id in the current list of users
     * @param userId 
     * 		Unique id of the user assigned by server
     * @return 
     * 		The nickname corresponding to the id
     */
    String getFriendName( int userId )
    {
        return (String)lstClients.getModel().getElementAt( userId );
    }
	
	class OnExit extends WindowAdapter implements WindowListener
	{
		public void windowClosing( WindowEvent we )
		{
			client.shutDown();
		}

		//Make textField get the focus whenever frame is activated.
		public void windowActivated(WindowEvent e)
		{
	        tfMessage.requestFocus();
    	}
	}

	class CloseTabButton implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			int index = tbChatWindows.getSelectedIndex();
			if( index !=0 )
			{
				tbChatWindows.removeTabAt( index );
				messageWindows.removeElementAt( index );
				tabToClient.removeElementAt( index-1 );
				
				if( tbChatWindows.getTabCount() == 1 )
				{
					tabMenu.setEnabled( false );
					closeTabMenu.setEnabled( false );
					closeAllTabsMenu.setEnabled( false );
				}					
			}
		}
	}
	
	class CloseAllTabsMenu implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			closeAllTabs();
		}
	}
	
	class ExitButton implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			client.shutDown();
		}
	}

	class SendButton implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			sendMessage();
			tfMessage.requestFocus();
		}
	}

	class SendMessage implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			sendMessage();
		}
	}

	class SelectClient extends MouseAdapter implements MouseListener
	{
	     public void mouseClicked(MouseEvent e)
	     {
     		if( !lstClients.isSelectionEmpty() )
     		{
	     		if ( e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 )
    	 		{
					openNewTab( lstClients.getSelectedIndex() );
					client.sendChatRequest( lstClients.getSelectedIndex() );
      			}
      		}
		}
	}

	class SendChatMenu implements ActionListener
	{
				
			private JFileChooser fileToSendChooser;

	        SendChatMenu()
        {
            fileToSendChooser = new JFileChooser();
            //fileToSendChooser.setDialogType( JFileChooser.OPEN_DIALOG );
            fileToSendChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
            fileToSendChooser.setMultiSelectionEnabled( false );
        }

        public void actionPerformed( ActionEvent e )
        {
            int tabSelected = tbChatWindows.getSelectedIndex();
            if ( tabSelected == 0 )
            {
                JOptionPane.showMessageDialog( ClientInterface.this,"File can only be transferred in private chat","File Transfer Error",JOptionPane.ERROR_MESSAGE );
                return;
            }
            int userAction = fileToSendChooser.showDialog( ClientInterface.this,"Send" );

            if ( userAction == JFileChooser.APPROVE_OPTION )
            {
                File selectedFile = fileToSendChooser.getSelectedFile();

                Integer clientIndex = (Integer) tabToClient.elementAt( tabSelected - 1 );
                //System.out.println( selectedFile.getName() );
                client.sendFile( clientIndex.intValue(),selectedFile );
            }
        }
		
	}
	protected ClientInfo clientInfo = new ClientInfo();
	class NickChatMenu implements ActionListener
	{
		public void actionPerformed( ActionEvent e)
		{
			do
			{
				client.Name = JOptionPane.showInputDialog( client.window,"Enter new nickname ?");
			}
			while( (client.Name==null || client.Name.length()==0) );
			
			client.window.setTitle( "Epp Chat <--> " + client.Name );
			
			client.changeNickName(client.Name);
			
		}
		
	}

	class ConfigureFMenu implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			ConfigureServerInfo serverDialog = new ConfigureServerInfo();
		}
	}
	
	class AboutHlpMenu implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			JOptionPane.showMessageDialog( client.window, "Epp Chat" 
			+"\nVersion 1.0 Gold \n Steve Alogaris \nJohn Drosakis \nLoukas Pyrgioglis\n(C) 2008. All rights reserved.",
			"About Epp Chat", JOptionPane.INFORMATION_MESSAGE );
		}
	}
		


	class ConnectFMenu implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			do
			{
				client.Name = JOptionPane.showInputDialog( client.window,"Enter Login Name ?");
			}
			while( (client.Name==null || client.Name.length()==0) );
			
			if( client.connectToServer() )
			{
				client.window.setTitle( "Epp Chat <--> " + client.Name );

				if( lstClients.getModel() != client.clientList )
				{
					lstClients.setModel( client.clientList );
				}
			
				connectFMenu.setEnabled( false );
				disconnectFMenu.setEnabled( true );			
				tfMessage.setEnabled( true );
				bSend.setEnabled( true );
				nickChatMenu.setEnabled( true );
				sendChatMenu.setEnabled( true );
				lstClients.setEnabled( true );
				((JTextArea)messageWindows.elementAt(0)).setText("");
				tfMessage.requestFocus();
			}
		}
	}

	
	class DisconnectFMenu implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			client.disconnectFromServer( true );
			notifyDisconnect();
		}
	}
	/*
	class FocusMessageField extends FocusAdapter implements FocusListener
	{
		public void focusLost( FocusEvent fe )
		{
			if(!( fe.getOppositeComponent() instanceof JMenuItem ))
			{
				tfMessage.requestFocus();
			}
		}
	}
	*/
	//DISPLAYS AND SETS CONFIGURATION OPTIONS
	class ConfigureServerInfo extends JDialog
	{
		JButton bSave = new JButton("Save");
		JButton bCancel = new JButton("Cancel");
		JLabel lbServer = new JLabel("Server Name : ");
		JTextField tfServerName = new JTextField(10);
		JLabel lbPort = new JLabel("Port No :            ");
		JTextField tfPortNo = new JTextField(6);
		
		ConfigureServerInfo()
		{
			super(client.window,"Configure Connection",true);

			Box buttonBox = Box.createHorizontalBox();
			buttonBox.add( Box.createHorizontalStrut(50) );
			buttonBox.add(bSave);
			//buttonBox.add( Box.createHorizontalStrut(10) );
			buttonBox.add(bCancel);
			
			Container jcp = this.getContentPane();
			jcp.setLayout( new FlowLayout(FlowLayout.LEFT) );
			jcp.add(lbServer);
			jcp.add(tfServerName);
			jcp.add(lbPort);
			jcp.add(tfPortNo);
			//jcp.add( commandBox );
			jcp.add( buttonBox );

			bSave.addActionListener( new SaveButton() );
			bCancel.addActionListener( new CancelButton() );
			
			client.getConfiguration();
			
			tfServerName.setText( client.serverAddress );
			tfPortNo.setText( Integer.toString( client.serverSocketNumber ) );
			
			this.setSize(230,120);
					
			//Position the dialog in the center of the interface
			Point position = client.window.getLocation();
			position.x = position.x + (client.window.getWidth()/2) - (this.getWidth()/2);
			position.y = position.y + (client.window.getHeight()/2) - (this.getHeight()/2);
			this.setLocation( position );

			this.setVisible(true);
			this.setResizable( false );			
		}
		

		class SaveButton implements ActionListener
		{
			public void actionPerformed( ActionEvent e )
			{
				client.setConfiguration( tfServerName.getText(), Integer.parseInt(tfPortNo.getText()) );
				dispose();
			}
		}
		
		class CancelButton implements ActionListener
		{
			public void actionPerformed( ActionEvent e )
			{
				dispose();
			}
		}
		
	}
}
