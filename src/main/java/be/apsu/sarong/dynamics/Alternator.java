package be.apsu.sarong.dynamics;

import java.util.Collection;
import java.util.HashSet;

public class Alternator implements Runnable
{
	private HashSet<Alteration> alterations;

	public Alternator()
	{
		alterations=new HashSet<Alteration>();
	}

	public Alternator addAll(Collection<Alteration> moreAlterations)
	{
		alterations.addAll(moreAlterations);
		return this;
	}
	
	public Alternator add(Alteration anotherAlteration)
	{
		alterations.add(anotherAlteration);
		return this;
	}

	@Override
	public void run()
	{
		for(Alteration alteration : alterations)
			alteration.alter();
	}
}
