package be.apsu.sarong.common;

import be.apsu.sarong.panel.SarongPanel;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.w3c.dom.Element;

public class OldSetAction extends Action
{
	public OldSetAction(String onTmpl)
	{
		super(onTmpl);
	}

	@Override
	protected void performAction(final SarongPanel panel, final String on, final Map<String,String> attrTargets)
	{
		panel.queueUpdate(new Runnable()
		{
            @Override
			public void run()
			{
				System.err.println("on="  + on);
				for(Iterator<Entry<String,String>> i=attrTargets.entrySet().iterator();i.hasNext();)
				{
					Entry<String,String> target=i.next();
					System.err.println("\t" + target.getKey() + " => " + target.getValue());

					Element elem=panel.getElementById(on);
					if(elem!=null)
					{
						if(target.getKey().equals("cdata"))
						{
							System.err.println("setting cdata");
							elem.setTextContent(target.getValue());
						}
						else
						{
							System.err.println("setting " + target.getKey());
							elem.setAttribute(target.getKey(),target.getValue());
						}
					}
				}
			}
		});	
	}
}
