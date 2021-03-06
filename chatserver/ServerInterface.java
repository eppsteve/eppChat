package chatserver;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

import protocols.*;

//MAIN INTERFACE CLASS
public class ServerInterface extends JFrame
{
	protected JButton bExit;
	protected JButton bKickOut;
	protected JTextArea taMessages;

	DefaultListModel clientList;
	protected JList lstClients;
	
	protected JToolBar tbServer;
	protected JButton btbKickOut;
	protected JButton btbExit;
	protected JButton btbConfigure;
	protected JButton btbAbout;
	
	protected JMenuBar menuBar;
	protected JMenu hlpMenu;
	protected JMenu fileMenu;
	protected JMenuItem exitFMenu;
	protected JMenuItem aboutHlpMenu;
	protected JMenuItem configureFMenu;


	protected JScrollPane scrlClients;
	
	protected ChatServer server;
	
	ServerInterface( ChatServer server )
	{
		super( "EppChat Server" );
		
		this.server = server;
		
		setUpMenu();
		setUpToolBar();
		setUpMainInterface();
		
		setVisible( true );
	}
	
	protected void setUpMenu()
	{
		exitFMenu = new JMenuItem("ShutDown",KeyEvent.VK_S);
		aboutHlpMenu = new JMenuItem("About",KeyEvent.VK_A);
		configureFMenu = new JMenuItem("Configure Server",KeyEvent.VK_C);

		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(configureFMenu);
		fileMenu.add(exitFMenu);

		hlpMenu = new JMenu("Help");
		hlpMenu.setMnemonic(KeyEvent.VK_H);
		hlpMenu.add(aboutHlpMenu);

		menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(hlpMenu);
		setJMenuBar(menuBar);
	}
	
	protected void setUpToolBar()
	{
		tbServer = new JToolBar();
		btbKickOut = new JButton(new ImageIcon("icons/link.gif") );
		btbExit = new JButton(new ImageIcon("icons/logoff.gif") );
		btbConfigure = new JButton( new ImageIcon("icons/log.gif") );
		btbAbout = new JButton( new ImageIcon("icons/about.gif") );

		btbExit.setToolTipText("Exit");
		btbConfigure.setToolTipText("Configure");
		btbAbout.setToolTipText("About");
		btbKickOut.setToolTipText("KickOut");

		tbServer.add( btbConfigure );
		tbServer.add( btbKickOut );
		tbServer.add( btbExit );
		
		tbServer.setFloatable(false);
		tbServer.setRollover(true);

		tbServer.add( btbAbout );
	}

	protected void setUpListeners()
	{
		btbExit.addActionListener( new ExitButton() );
		btbConfigure.addActionListener( new ConfigureFMenu() );
		btbAbout.addActionListener( new AboutHlpMenu() );
		btbKickOut.addActionListener( new KickOutButton() );

		exitFMenu.addActionListener( new ExitButton() );
		configureFMenu.addActionListener( new ConfigureFMenu() );
		aboutHlpMenu.addActionListener( new AboutHlpMenu() );

		bExit.addActionListener( new ExitButton() );
		lstClients.addListSelectionListener( new clientSelected() );
		bKickOut.addActionListener( new KickOutButton() );

		addWindowListener( new OnExit() );
	}
	
	protected void setUpProperties()
	{
		setSize(570,465);
		setResizable(false);

		taMessages.setLineWrap(true);
		taMessages.setEditable(false);

		lstClients.setFixedCellWidth(100);
		lstClients.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

		bKickOut.setEnabled(false);

		scrlClients = new JScrollPane( lstClients );
		scrlClients.setColumnHeaderView( new JLabel("    Online Users" ) );
		
		setUpListeners();
	}
	
	protected void setUpMainInterface()
	{
		bExit = new JButton( "Shut Down" );
		bKickOut = new JButton( "Kick Out" );			
		
		taMessages = new JTextArea( 20,40 );			

		clientList = new DefaultListModel();
		lstClients = new JList( clientList );			

		setUpProperties();

		Box displayBox = Box.createHorizontalBox();
		displayBox.add(  new JScrollPane( taMessages ) );
		displayBox.add( scrlClients );

		JPanel buttonBox = new JPanel();
		buttonBox.setLayout( new FlowLayout( FlowLayout.CENTER ) );
		buttonBox.add( bKickOut );
		buttonBox.add( bExit );

		Container cp = 	this.getContentPane();
		cp.setLayout( new BorderLayout() );
		cp.add( tbServer,BorderLayout.NORTH );
		cp.add( displayBox,BorderLayout.CENTER );
		cp.add( buttonBox,BorderLayout.SOUTH );
		
	}

	class OnExit  extends WindowAdapter implements WindowListener
	{
		public void windowClosing( WindowEvent we )
		{
			server.shutDown();
		}
	}

	class ExitButton implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			server.shutDown();
		}
	}

	class KickOutButton implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			//Get index of the client that is to be kicked out
			ChatServer.ChatHandler client = (ChatServer.ChatHandler)server.handlers.elementAt( lstClients.getSelectedIndex() );
			try
			{
				//Notify the client
				client.out.writeObject( new KickedOutNotice() );
			}
			catch( IOException w )
			{}

			//Stop the thread handling the client
			client.stop();
			bKickOut.setEnabled( false );
			server.onlineUsers--;
		}
	}

	class clientSelected implements ListSelectionListener
	{
		public void valueChanged( ListSelectionEvent e )
		{
			bKickOut.setEnabled(true);
		}
	}

	class AboutHlpMenu implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			JOptionPane.showMessageDialog( server.window, "EppChat Server" 
			+"\nVersion 1.0 Gold \n\nSteve Alogaris\nJohn Drosakis\nLoukas Pyrgioglis\n(C) 2008.All rights reserved.",
			"About EppChat", JOptionPane.INFORMATION_MESSAGE );
		}
	}
	

	class ConfigureFMenu implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			ConfigureServerInfo serverDialog = new ConfigureServerInfo();
		}
	}

	//CREATES AND DISPLAYS THE CONFIGURE DIALOG
	class ConfigureServerInfo extends JDialog
	{
		JButton bSave = new JButton("Save");
		JButton bCancel = new JButton("Cancel");
		JLabel lbPort = new JLabel("Port Number : ");
		JLabel lbLimit = new JLabel("Client Limit :    ");
		JTextField tfLimit = new JTextField(6);
		JTextField tfPortNo = new JTextField(6);
		
		ConfigureServerInfo()
		{
			super(server.window,"Configure Server",true);

			Box buttonBox = Box.createHorizontalBox();
			buttonBox.add( Box.createHorizontalStrut(15) );
			buttonBox.add(bSave);
			buttonBox.add(bCancel);
			
			Container jcp = this.getContentPane();
			jcp.setLayout( new FlowLayout(FlowLayout.LEFT) );
			jcp.add(lbPort);
			jcp.add(tfPortNo);
			jcp.add( lbLimit );
			jcp.add( tfLimit );
			jcp.add( buttonBox );

			bSave.addActionListener( new SaveButton() );
			bCancel.addActionListener( new CancelButton() );
			
			server.getConfiguration();
			
			tfPortNo.setText( Integer.toString( server.serverPort ) );
			tfLimit.setText( Integer.toString( server.serverLimit ) );
			
			this.setSize(180,115);

			Point position = server.window.getLocation();
			position.x = position.x + (server.window.getWidth()/2) - (this.getWidth()/2);
			position.y = position.y + (server.window.getHeight()/2) - (this.getHeight()/2);
			this.setLocation( position );

			this.setVisible(true);
			this.setResizable( false );			
		}

		class SaveButton implements ActionListener
		{
			public void actionPerformed( ActionEvent e )
			{
				server.setConfiguration( Integer.parseInt(tfPortNo.getText()) , Integer.parseInt(tfLimit.getText()) );
				//setVisible(false);
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
