package rpc_abstract;

import java.util.concurrent.Callable;

public abstract class SendMessage implements Callable<Void> {
	
	public abstract void send();

}
