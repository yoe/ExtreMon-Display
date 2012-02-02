package be.apsu.sarong.client;

public class TimeValue
{
	private double	 time;
	private String	 value;
	
	public TimeValue(double time, String value)
	{
		super();
		this.time = time;
		this.value = value;
	}
	
	public double getTime()
	{
		return time;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public TimeValue setTime(long time)
	{
		this.time = time;
		return this;
	}
	
	public TimeValue setValue(String value)
	{
		this.value = value;
		return this;
	}	
}
