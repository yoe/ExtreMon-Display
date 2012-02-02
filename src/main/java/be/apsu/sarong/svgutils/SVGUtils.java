package be.apsu.sarong.svgutils;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class SVGUtils
{
    public static final String SVG_ID = "id";
    public static final String SVG_NS = "http://www.w3.org/2000/svg";
    public static final String SVG_TRANSFORM = "transform";
    public static final String SVG_USE = "use";
    public static final String X3MON_ID = "id";
    public static final String XLINK_HREF = "href";
    public static final String X3MON_TEMPLATE = "template";
    public static final String X3MON_TIMESPINNER = "timespinner";
    public static final String X3MON_NS = "http://extremon.org/ns/extremon";
    public static final String X3MON_USAGE = "usage";
    public static final String XLINK_NS_URL = "http://www.w3.org/1999/xlink";
    
    public static String getAttr(Node node, String attrName)
    {
        NamedNodeMap attrNodeMap = node.getAttributes();
        if (attrNodeMap == null)
        {
            return null;
        }
        Node valueNode = attrNodeMap.getNamedItem(attrName);
        if (valueNode == null)
        {
            return null;
        }
        return valueNode.getNodeValue();
    }

    public static String getAttrNS(Node node, String nameSpace, String attrName)
    {
        NamedNodeMap attrNode = node.getAttributes();
        if (attrNode == null)
        {
            return null;
        }
        Node valueNode = attrNode.getNamedItemNS(nameSpace, attrName);
        if (valueNode == null)
        {
            return null;
        }
        return valueNode.getNodeValue();
    }

    public static String getX3MonAttr(Node node, String attrName)
    {
        return getAttrNS(node, X3MON_NS, attrName);
    }

    public static String getX3MonId(Node node)
    {
        return getX3MonAttr(node, X3MON_ID);
    }

    public static boolean isX3MonUsage(Node node, String usage)
    {
        String x3MonUsage = getX3MonAttr(node, X3MON_USAGE);
        if (x3MonUsage == null)
        {
            return false;
        }
        return x3MonUsage.contains(usage);
    }

    public static boolean isX3MonTemplate(Node node)
    {
        boolean is = isX3MonUsage(node, X3MON_TEMPLATE);
        return is;
    }
    
    public static boolean isX3MonTimeSpinner(Node node)
    {
        boolean is = isX3MonUsage(node, X3MON_TIMESPINNER);
        return is;
    }

    public static void cloneAttribute(Node source, Node destination, String attrName)
    {
        Node attrNode = source.getAttributes().getNamedItem(attrName);
        if (attrNode != null)
        {
            destination.getAttributes().setNamedItem(attrNode.cloneNode(true));
        }
    }

    public static void cloneAttributeNS(Node source, Node destination, String nameSpace, String attrName)
    {
        Node attrNode = source.getAttributes().getNamedItemNS(nameSpace, attrName);
        if (attrNode != null)
        {
            destination.getAttributes().setNamedItemNS(attrNode.cloneNode(true));
        }
    }

    public static void removeAttribute(Node node, String attrName)
    {
        NamedNodeMap attrNode = node.getAttributes();
        if (attrNode == null)
        {
            return;
        }
        attrNode.removeNamedItem(attrName);
    }

    public static void removeAttributeNS(Node node, String nameSpace, String attrName)
    {
        NamedNodeMap attrNode = node.getAttributes();
        if (attrNode == null)
        {
            return;
        }
        attrNode.removeNamedItemNS(nameSpace, attrName);
    }
    
    public static String join(Iterable< ? extends Object> pColl, String separator)
    {
        Iterator< ? extends Object> oIter;
        if (pColl == null || (!(oIter = pColl.iterator()).hasNext()))
        {
            return "";
        }
        StringBuilder oBuilder = new StringBuilder(String.valueOf(oIter.next()));
        while (oIter.hasNext())
        {
            oBuilder.append(separator).append(oIter.next());
        }
        return oBuilder.toString();
    }

    public static String njoin(List<String> list, int count, String separator)
    {
        StringBuilder builder = new StringBuilder();

        if (count > list.size())
        {
            count = list.size();
        }

        for (int i = 0; i < count; i++)
        {
            String slice = list.get(i);
            if (slice != null)
            {
                builder.append('.');
                builder.append(slice);
            }
        }

        return builder.toString().substring(1);
    }
}
