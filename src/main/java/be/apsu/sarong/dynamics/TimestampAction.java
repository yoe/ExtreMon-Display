package be.apsu.sarong.dynamics;

import java.util.Map;

/**
 *
 * @author frank@apsu.be
 */
public class TimestampAction extends Action
{
    public TimestampAction()
    {
        super(null,null,null);
    }

    @Override
    public void performAction(Map<String,String> variables, double nValue, String sValue)
    {
    	if(sValue!=null)
    		return;
    	getPanel().setLastRemoteTimestamp((long)(nValue*1000));
    }
}
