package be.apsu.extremon.dynamics;

import org.w3c.dom.Element;

import be.apsu.extremon.STATE;

public class Respondable
{
	private String	label;
	private Element element;
	private int		state;
	private boolean	responding;
	private String	comment;
	
	public Respondable(Element element,String label)
	{
		this.element=element;
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
	
	public Element getElement()
	{
		return element;
	}
	
	public void setElement(Element element)
	{
		this.element=element;
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
}
