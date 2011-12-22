package be.apsu.sarong.dynamics;

import java.util.Map;

public class MeasureCacheElement
{
	private Measure 			measure;
	private Map<String,String>	variables;
	
	public MeasureCacheElement(Measure measure, Map<String, String> variables)
	{
		super();
		this.measure = measure;
		this.variables = variables;
	}
	
	public Measure getMeasure()
	{
		return measure;
	}
	public void setMeasure(Measure measure)
	{
		this.measure = measure;
	}
	public Map<String, String> getVariables()
	{
		return variables;
	}
	public void setVariables(Map<String, String> variables)
	{
		this.variables = variables;
	}	
}
