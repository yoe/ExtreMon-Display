package be.apsu.sarong.client;

public class X3Measure
{
	public String label;
	public String value;
	
	public X3Measure(String label, String value)
	{
		this.label = label;
		this.value = value;	
	}

	@Override
	public boolean equals(Object _that)
	{
		if(!(_that instanceof X3Measure))
			return false;
		X3Measure that=(X3Measure)_that;
		return that.label.equals(this.label);
	}

	@Override
	public int hashCode()
	{
		return this.label.hashCode();
	}
}
