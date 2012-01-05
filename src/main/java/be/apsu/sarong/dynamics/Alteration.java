package be.apsu.sarong.dynamics;

import org.w3c.dom.Element;

public class Alteration
{
	private Element on;
	private String  attribute;
	private String 	value;
	
	public Alteration(Element on, String attribute, String value)
	{
		super();
		this.on = on;
		this.attribute = attribute;
		this.value = value;
	}
	
	public void alter()
	{
		if(attribute!=null)
			on.setAttribute(attribute,value);
		else
            on.setTextContent(value);
	}

	// our identity in collections depends on the element we act upon
	// so we get replaced by the last instance acting on the same element
	
	@Override
	public boolean equals(Object _that)
	{
		Alteration that=(Alteration)_that;
		return this.on.equals(that.on);
	}

	@Override
	public int hashCode()
	{
		return on.hashCode();
	}

	@Override
	public String toString()
	{
		return on.getAttribute("id");
	}
}
