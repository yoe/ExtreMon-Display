package be.apsu.extremon.dynamics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import be.apsu.extremon.svgutils.SVGUtils;

public class Subscription
{
	private Map<String,Set<String>>	metaLabels;
	private int						shortestPath;
	private List<Set<String>>		groups;			

	public Subscription()
	{
		this.metaLabels=new HashMap<String,Set<String>>();
		this.shortestPath=Integer.MAX_VALUE;
		this.groups=new ArrayList<Set<String>>();
	}
	
	public void registerMeta(final String labelEndsWith, final String metaLabel)
	{
		Set<String> metaLabels=this.metaLabels.get(labelEndsWith);
		if(metaLabels==null)
		{
			metaLabels=new HashSet<String>();
			this.metaLabels.put(labelEndsWith,metaLabels);
		}
		metaLabels.add(metaLabel);
	}

	public void addLabel(final String label)
	{
		if(label.isEmpty() || label.startsWith("local."))
			return;
		
		reallyAddLabel(label);	
		
		for(Map.Entry<String,Set<String>> metaLabels : this.metaLabels.entrySet())
		{
			if(label.endsWith(metaLabels.getKey()))
			{
				for(String metaLabel:metaLabels.getValue())
				{
					reallyAddLabel(label + "." + metaLabel);
				}
			}
		}
	}

	public void addLabels(final Collection<String> labels)
	{
		for(String label:labels)
			addLabel(label);
	}

	public String getSubscription()
	{
		final List<String> regexParts=new ArrayList<String>();

		int position=0;
		for(Set<String> groupItems:groups)
		{
			final StringBuilder builder=new StringBuilder();

			if(position<shortestPath)
			{
				builder.append('/');
				if(groupItems.size()>1)
					builder.append('(');
				builder.append(SVGUtils.join(groupItems,"|"));
				if(groupItems.size()>1)
					builder.append(')');
			}
			else
			{
				builder.append("(/");
				if(groupItems.size()>1)
					builder.append('(');
				builder.append(SVGUtils.join(groupItems,"|"));
				if(groupItems.size()>1)
					builder.append(')');
				builder.append("|$)");
			}
			regexParts.add(builder.toString());
			position+=1;
		}

		return SVGUtils.join(regexParts,"");
	}

	private void reallyAddLabel(String label)
	{
		final String[] labelParts=label.split("\\.");
		for(int i=0;i<labelParts.length;i+=1)
		{
			if(i>groups.size()-1)
				groups.add(new HashSet<String>());
			groups.get(i).add(labelParts[i]);
		}

		if(labelParts.length<this.shortestPath)
			this.shortestPath=labelParts.length;
	}
}
