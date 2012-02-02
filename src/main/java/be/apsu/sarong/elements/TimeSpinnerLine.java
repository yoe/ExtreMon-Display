package be.apsu.sarong.elements;

import org.w3c.dom.Element;

import be.apsu.sarong.dynamics.Alteration;

public class TimeSpinnerLine 
{
	private Element element;
	private double	centerX,centerY;
	
	public TimeSpinnerLine(Element element)
	{
		super();
		this.element = element;
		double y1=Double.parseDouble(element.getAttribute("y1"));
		double y2=Double.parseDouble(element.getAttribute("y2"));
		
		centerX=Double.parseDouble(element.getAttribute("x1"));
		centerY=y1+((y2-y1)/2.0);
	}
	
	public Alteration setSpinPosition(int percent)
	{
		StringBuffer buffer=new StringBuffer("rotate(");
					 buffer.append(percent * 3.6);
					 buffer.append(' ');
					 buffer.append(centerX);
					 buffer.append(' ');
					 buffer.append(centerY);
					 buffer.append(')');
					 
		return new Alteration(element,"transform",buffer.toString());
					 
	}
}
