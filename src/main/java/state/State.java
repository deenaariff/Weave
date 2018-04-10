package state;

import ledger.Ledger;

public abstract class State {
	
	public Ledger ledger;

    public abstract int run();
	
}
