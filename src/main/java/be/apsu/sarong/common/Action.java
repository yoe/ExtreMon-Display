package be.apsu.sarong.common;

import be.apsu.sarong.panel.SarongPanel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public abstract class Action
{
	private String				onTmpl;
	private Map<String,String>	attrTargetsTmpl;

	public Action(String onTmpl)
	{
		this.onTmpl		=onTmpl;
		attrTargetsTmpl	=new HashMap<String,String>();
	}

	public void addAttrTarget(String attrName, String attrTarget)
	{
		attrTargetsTmpl.put(attrName,attrTarget);
	}

	protected abstract void performAction(SarongPanel panel, String on, Map<String,String> attrTargets);

	private String substitutions(String template,Map vars)
	{
		for(Iterator<Entry<String,String>> iter=vars.entrySet().iterator();iter.hasNext();)
		{
			Entry var=iter.next();
			try
			{
				template=template.replace("${"+var.getKey()+"}",(String)var.getValue());
			}
			catch(ClassCastException cce)
			{
				Double value=(Double)var.getValue();
				template=template.replace("${"+var.getKey()+"}",String.valueOf(value));
			}
			
		}
		return template;
	}
	
	void performActions(SarongPanel panel,Map vars)
	{
		String	on=substitutions(onTmpl,vars);
		Map		attrTargets=new HashMap(attrTargetsTmpl.size());
		for(Iterator<Entry<String,String>> iter=attrTargetsTmpl.entrySet().iterator();iter.hasNext();)
		{
			Entry<String,String> attrTarget=iter.next();
			attrTargets.put(attrTarget.getKey(),substitutions(attrTarget.getValue(),vars));
		}

		performAction(panel,on,attrTargets);
	}
}
