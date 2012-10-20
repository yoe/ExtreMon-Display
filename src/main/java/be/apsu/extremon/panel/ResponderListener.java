package be.apsu.extremon.panel;

public interface ResponderListener
{
	void heartBeat(final double timeDiff);
	void responding(final String label, final boolean responding);
	void responderComment(final String label, final String comment);
}
