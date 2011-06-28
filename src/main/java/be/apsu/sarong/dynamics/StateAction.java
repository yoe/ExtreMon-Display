package be.apsu.sarong.dynamics;

import java.util.Map;

/**
 *
 * @author frank@apsu.be
 */
public class StateAction extends Action
{
    private String[] stateValues;
    
    public StateAction(String attribute, String onTemplate, String[] stateValues)
    {
        super(attribute,"",onTemplate);
        this.stateValues=stateValues;
    }

    @Override
    public void performAction(Map variables)
    {
        String value=null;
             
        Double dValue=(Double)variables.get("value");
        String on=substitutions(getOnTemplate(),variables);
        
        int code=(int)dValue.intValue();
        
        value=stateValues[code];
        //System.out.println("WANT TO SET " + on + "=" + value + " for state " + code);
        
        if(getElement(on)!=null)
        {
            System.out.println("SETTING " + on + "=" + value + " for state " + code);
            queueSet(on,value);
        }
        else
        {
            System.out.println("FAILED " + on + "=" + value + " ; NOT FOUND");
        }
    }
}