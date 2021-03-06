/* ExtreMon Project
 * Copyright (C) 2009-2012 Frank Marien
 * frank@apsu.be
 *  
 * This file is part of ExtreMon.
 *    
 * ExtreMon is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ExtreMon is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ExtreMon.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.apsu.extremon.svgutils;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class SVGUtils {
    public static final String SVG_ID = "id";
    public static final String SVG_NS = "http://www.w3.org/2000/svg";
    public static final String SVG_TRANSFORM = "transform";
    public static final String SVG_USE = "use";
    public static final String X3MON_ID = "id";
    public static final String X3MON_MAP = "map";
    public static final String XLINK_HREF = "href";
    public static final String X3MON_TEMPLATE = "template";
    public static final String X3MON_TIMESTAMP = "timestamp";
    public static final String X3MON_NS = "http://extremon.org/ns/extremon";
    public static final String X3MON_USAGE = "usage";
    public static final String XLINK_NS_URL = "http://www.w3.org/1999/xlink";

    private SVGUtils() {
    }

    public static String getAttr(Node node, String attrName) {
	final NamedNodeMap attrNodeMap = node.getAttributes();
	if (attrNodeMap == null) {
	    return null;
	}
	final Node valueNode = attrNodeMap.getNamedItem(attrName);
	if (valueNode == null) {
	    return null;
	}
	return valueNode.getNodeValue();
    }

    public static String getAttrNS(Node node, String nameSpace,
	    String attrName) {
	final NamedNodeMap attrNode = node.getAttributes();
	if (attrNode == null) {
	    return null;
	}
	final Node valueNode = attrNode.getNamedItemNS(nameSpace,
		attrName);
	if (valueNode == null) {
	    return null;
	}
	return valueNode.getNodeValue();
    }

    public static String getX3MonAttr(Node node, String attrName) {
	return getAttrNS(node, X3MON_NS, attrName);
    }

    public static String getX3MonId(Node node) {
	return getX3MonAttr(node, X3MON_ID);
    }

    public static String getX3MonMap(Node node) {
	return getX3MonAttr(node, X3MON_MAP);
    }

    public static boolean isX3MonUsage(Node node, String usage) {
	final String x3MonUsage = getX3MonAttr(node, X3MON_USAGE);
	if (x3MonUsage == null) {
	    return false;
	}
	return x3MonUsage.contains(usage);
    }

    public static boolean isX3MonTemplate(Node node) {
	return isX3MonUsage(node, X3MON_TEMPLATE);
    }

    public static boolean isX3MonTimestamp(Node node) {
	return isX3MonUsage(node, X3MON_TIMESTAMP);
    }

    public static void cloneAttribute(Node source, Node destination,
	    String attrName) {
	final Node attrNode = source.getAttributes().getNamedItem(
		attrName);
	if (attrNode != null) {
	    destination.getAttributes().setNamedItem(
		    attrNode.cloneNode(true));
	}
    }

    public static void cloneAttributeNS(Node source, Node destination,
	    String nameSpace, String attrName) {
	final Node attrNode = source.getAttributes().getNamedItemNS(
		nameSpace, attrName);
	if (attrNode != null) {
	    destination.getAttributes().setNamedItemNS(
		    attrNode.cloneNode(true));
	}
    }

    public static void removeAttribute(Node node, String attrName) {
	final NamedNodeMap attrNode = node.getAttributes();
	if (attrNode == null) {
	    return;
	}
	attrNode.removeNamedItem(attrName);
    }

    public static void removeAttributeNS(Node node, String nameSpace,
	    String attrName) {
	final NamedNodeMap attrNode = node.getAttributes();
	if (attrNode == null) {
	    return;
	}
	attrNode.removeNamedItemNS(nameSpace, attrName);
    }

    public static String join(Iterable<? extends Object> pColl,
	    String separator) {
	Iterator<? extends Object> oIter;
	if (pColl == null || (!(oIter = pColl.iterator()).hasNext())) {
	    return "";
	}
	final StringBuilder oBuilder = new StringBuilder(
		String.valueOf(oIter.next()));
	while (oIter.hasNext()) {
	    oBuilder.append(separator).append(oIter.next());
	}
	return oBuilder.toString();
    }

    public static String njoin(List<String> list, int count,
	    String separator) {
	final StringBuilder builder = new StringBuilder();

	int clampedCount = count;
	if (clampedCount > list.size())
	    clampedCount = list.size();

	for (int i = 0; i < clampedCount; i += 1) {
	    final String slice = list.get(i);
	    if (slice != null) {
		builder.append('.');
		builder.append(slice);
	    }
	}

	if (builder.length() > 0)
	    return builder.toString().substring(1);
	else
	    return "";
    }
}
