package be.apsu.sarong.common;

import be.apsu.sarong.panel.SarongPanel;
import java.util.HashMap;
import java.util.Map;

public class State extends Event
{
	private Map<String,String> valueByState;

	public State()
	{
		super();
		valueByState=new HashMap(6);
	}

	public State addValueForState(String state, String value)
	{
		valueByState.put(state,value);
		return this;
	}

	private String getValueForState(String state)
	{
		return valueByState.get(state);
	}

	@Override
	void handle(SarongPanel panel, String monid, Map<String,String> vars)
	{
		String stateStr	=vars.get("state");
		String value=getValueForState(stateStr);
		if(value==null)
			return;
		vars.put("value",value);
		super.handle(panel,monid,vars);
	}
}
