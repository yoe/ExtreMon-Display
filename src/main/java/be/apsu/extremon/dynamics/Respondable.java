package be.apsu.extremon.dynamics;

import org.w3c.dom.Element;

import be.apsu.extremon.STATE;

public class Respondable
{
	private String	label;
	private Element targetElement;
	private int		state;
	private boolean	responding;
	private String	comment;
	private String 	responderName;
	private Element	responderNameElement;
	
	public Respondable(Element targetElement,String label)
	{
		this.targetElement=targetElement;
		this.label=label;
		this.responding=false;
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public void setLabel(String label)
	{
		this.label=label;
	}
	
	public int getState()
	{
		return state;
	}
	
	public int getDisplayState()
	{
		if(responding)
			return this.state | STATE.RESPONDING_BITMASK;
		else
			return this.state;
	}
	
	public void setState(int state)
	{
		this.state=state;
	}
	
	public String getComment()
	{
		return comment;
	}
	
	public void setComment(String comment)
	{
		this.comment=comment;
	}

	public void setResponding(boolean responding)
	{
		this.responding=responding;
	}

	public boolean isResponding()
	{
		return responding;
	}	
	
	public String getResponderName()
	{
		return responderName;
	}

	public void setResponderName(String responderName)
	{
		this.responderName=responderName;
	}

	public Element getTargetElement()
	{
		return targetElement;
	}

	public void setTargetElement(Element targetElement)
	{
		this.targetElement=targetElement;
	}

	public Element getResponderNameElement()
	{
		return responderNameElement;
	}

	public void setResponderNameElement(Element responderNameElement)
	{
		this.responderNameElement=responderNameElement;
	}
}
