package be.apsu.sarong.panel;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.batik.swing.svg.SVGUserAgentAdapter;

public class X3UserAgent extends SVGUserAgentAdapter
{
	private Logger logger;
	
	public X3UserAgent()
	{
		super();
		logger=Logger.getLogger(X3UserAgent.class.getName());
	}

	public void setLogger(Logger logger)
	{
		this.logger=logger;
	}

	@Override
	public void displayError(Exception ex)
	{
		logger.log(Level.SEVERE,"Batik Error",ex);
	}

	@Override
	public void displayError(String message)
	{
		logger.log(Level.SEVERE,message);
	}

	@Override
	public void displayMessage(String message)
	{
		logger.log(Level.INFO,message);
	}
}
