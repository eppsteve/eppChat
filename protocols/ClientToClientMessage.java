package protocols;

import java.io.Serializable;

abstract class ClientToClientMessage implements Serializable
{
	public int senderId;
	public int recieverId;	
}
