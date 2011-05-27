package be.apsu.sarong.common;

import be.apsu.sarong.panel.SarongPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Push
{
	private Pattern					pattern;
	private String[]				captureNames;
	private Map<String,List<Event>>	events;

	public Push(String patternStr)
	{
		pattern	=Pattern.compile(patternStr);
		events	=new HashMap<String,List<Event>>();
	}

	public void setCaptures(String capturesStr)
	{
		captureNames=capturesStr.split(",");
	}

	public Push addEvent(String type, Event event)
	{
		List eventList=events.get(type);
		if(eventList==null)
		{
			eventList=new ArrayList<Event>();
			events.put(type,eventList);
		}
		eventList.add(event);
		return this;
	}

	public void handleEvent(SarongPanel panel, String monid, String type, Map vars)
	{
		List<Event> applicableEvents=events.get(type);
		if(applicableEvents==null)
			return;
		
		Map matchvars=matchMonId(monid);
		if(matchvars==null)
			return;
		
		vars.putAll(matchvars);
		
		for(Iterator<Event> eiter=applicableEvents.iterator();eiter.hasNext();)
		{
			Event	event=eiter.next();
					event.handle(panel,monid,vars);
		}
	}

	private Map matchMonId(String monid)
	{
		Map	result=null;
		Matcher matcher=pattern.matcher(monid);
		if(matcher.matches())
		{
			result=new HashMap(matcher.groupCount());
			result.put("monid", monid);
			for(int i=0;i<matcher.groupCount();i++)
				result.put(captureNames[i],matcher.group(i+1));
		}
		return result;
	}
}
