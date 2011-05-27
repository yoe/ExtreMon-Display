package be.apsu.sarong.common;

import be.apsu.sarong.panel.SarongPanel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class Event
{
	private List<Action> actions;

	public Event()
	{
		actions=new ArrayList(2);
	}

	public Iterator<Action> actionsIter()				{return actions.iterator();}
	public boolean			hasActions()				{return !actions.isEmpty();}
	public Event			addAction(Action action)	{actions.add(action);return this;}

	void handle(SarongPanel panel, String monid, Map<String,String> vars)
	{
		for(Iterator<Action> aIter=actionsIter();aIter.hasNext();)
		{
			Action	action=aIter.next();
					action.performActions(panel,vars);
		}
	}
}
