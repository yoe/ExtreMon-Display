/*
 * ExtreMon Project
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

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;

import org.apache.batik.parser.PathHandler;
import org.apache.batik.parser.PathParser;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class SVGUtils
{
	public static final String	SVG_ID			="id";
	public static final String	SVG_NS			="http://www.w3.org/2000/svg";
	public static final String	SVG_TRANSFORM	="transform";
	public static final String	SVG_USE			="use";
	public static final String	X3MON_ID		="id";
	public static final String	X3MON_MAP		="map";
	public static final String	XLINK_HREF		="href";
	public static final String	X3MON_TEMPLATE	="template";
	public static final String	X3MON_TIMESTAMP	="timestamp";
	public static final String	X3MON_NS		="http://extremon.org/ns/extremon";
	public static final String	X3MON_USAGE		="usage";
	public static final String	XLINK_NS_URL	="http://www.w3.org/1999/xlink";


	private SVGUtils()
	{
	}

	public static String getAttr(Node node,String attrName)
	{
		final NamedNodeMap attrNodeMap=node.getAttributes();
		if(attrNodeMap==null)
		{
			return null;
		}
		final Node valueNode=attrNodeMap.getNamedItem(attrName);
		if(valueNode==null)
		{
			return null;
		}
		return valueNode.getNodeValue();
	}

	public static String getAttrNS(Node node,String nameSpace,String attrName)
	{
		final NamedNodeMap attrNode=node.getAttributes();
		if(attrNode==null)
		{
			return null;
		}
		final Node valueNode=attrNode.getNamedItemNS(nameSpace,attrName);
		if(valueNode==null)
		{
			return null;
		}
		return valueNode.getNodeValue();
	}

	public static String getX3MonAttr(Node node,String attrName)
	{
		return getAttrNS(node,X3MON_NS,attrName);
	}

	public static String getX3MonId(Node node)
	{
		return getX3MonAttr(node,X3MON_ID);
	}

	public static String getX3MonMap(Node node)
	{
		return getX3MonAttr(node,X3MON_MAP);
	}

	public static boolean isX3MonUsage(Node node,String usage)
	{
		final String x3MonUsage=getX3MonAttr(node,X3MON_USAGE);
		if(x3MonUsage==null)
		{
			return false;
		}
		return x3MonUsage.contains(usage);
	}

	public static boolean isX3MonTemplate(Node node)
	{
		return isX3MonUsage(node,X3MON_TEMPLATE);
	}

	public static boolean isX3MonTimestamp(Node node)
	{
		return isX3MonUsage(node,X3MON_TIMESTAMP);
	}

	public static void cloneAttribute(Node source,Node destination,String attrName)
	{
		final Node attrNode=source.getAttributes().getNamedItem(attrName);
		if(attrNode!=null)
		{
			destination.getAttributes().setNamedItem(attrNode.cloneNode(true));
		}
	}

	public static void cloneAttributeNS(Node source,Node destination,String nameSpace,String attrName)
	{
		final Node attrNode=source.getAttributes().getNamedItemNS(nameSpace,attrName);
		if(attrNode!=null)
		{
			destination.getAttributes().setNamedItemNS(attrNode.cloneNode(true));
		}
	}

	public static void removeAttribute(Node node,String attrName)
	{
		final NamedNodeMap attrNode=node.getAttributes();
		if(attrNode==null)
		{
			return;
		}
		attrNode.removeNamedItem(attrName);
	}

	public static void removeAttributeNS(Node node,String nameSpace,String attrName)
	{
		final NamedNodeMap attrNode=node.getAttributes();
		if(attrNode==null)
		{
			return;
		}
		attrNode.removeNamedItemNS(nameSpace,attrName);
	}

	public static Point2D getCenterOfPath(Node node)
	{
		final String d=node.getAttributes().getNamedItem("d").getNodeValue();
		final CenterSeekingPathHandler csph=new CenterSeekingPathHandler();
		final PathParser parser=new PathParser();
		parser.setPathHandler(csph);
		parser.parse(d);
		return new Point2D.Double(csph.centerX,csph.centerY);
	}

	public static String join(Iterable<? extends Object> pColl,String separator)
	{
		Iterator<? extends Object> oIter;
		if(pColl==null||(!(oIter=pColl.iterator()).hasNext()))
		{
			return "";
		}
		final StringBuilder oBuilder=new StringBuilder(String.valueOf(oIter.next()));
		while(oIter.hasNext())
		{
			oBuilder.append(separator).append(oIter.next());
		}
		return oBuilder.toString();
	}

	public static String njoin(List<String> list,int count,String separator)
	{
		final StringBuilder builder=new StringBuilder();

		int clampedCount=count;
		if(clampedCount>list.size())
			clampedCount=list.size();

		for(int i=0;i<clampedCount;i+=1)
		{
			final String slice=list.get(i);
			if(slice!=null)
			{
				builder.append('.');
				builder.append(slice);
			}
		}
		
		if(builder.length()>0)
			return builder.toString().substring(1);
		else
			return "";
	}

	public static final class CenterSeekingPathHandler implements PathHandler
	{
		private double	lastX;
		private double	lastY;
		private double	minX;
		private double	minY;
		private double	maxX;
		private double	maxY;
		private double	centerX;
		private double	centerY;

		private CenterSeekingPathHandler()
		{
		}

		private void addAbsCoordinates(double x,double y)
		{
			if(x<this.minX)
				this.minX=x;
			if(x>this.maxX)
				this.maxX=x;
			if(y<this.minY)
				this.minY=y;
			if(y>this.maxY)
				this.maxY=y;
			this.lastX=x;
			this.lastY=y;
		}

		private void addRelCoordinates(double x,double y)
		{
			this.addAbsCoordinates(this.lastX+x,this.lastY+y);
		}

		@Override
		public void startPath()
		{
			this.lastX=0;
			this.lastY=0;
			this.minX=this.minY=Double.MAX_VALUE;
			this.maxX=this.maxY=Double.MIN_VALUE;
		}

		@Override
		public void closePath()
		{
		}

		@Override
		public void endPath()
		{
			this.centerX=this.minX+((this.maxX-this.minX)/2.0);
			this.centerY=this.minY+((this.maxY-this.minY)/2.0);
		}

		@Override
		public void arcAbs(final float arg0,final float arg1,final float arg2,final boolean arg3,final boolean arg4,final float x,final float y)
		{
			this.addAbsCoordinates(x,y);
		}

		@Override
		public void arcRel(float arg0,float arg1,float arg2,boolean arg3,boolean arg4,float x,float y)
		{

			this.addRelCoordinates(x,y);
		}

		@Override
		public void curvetoCubicAbs(float arg0,float arg1,float arg2,float arg3,float x,float y)
		{
			this.addAbsCoordinates(x,y);
		}

		@Override
		public void curvetoCubicRel(float arg0,float arg1,float arg2,float arg3,float x,float y)
		{
			this.addRelCoordinates(x,y);

		}

		@Override
		public void curvetoCubicSmoothAbs(float arg0,float arg1,float x,float y)
		{
			this.addAbsCoordinates(x,y);

		}

		@Override
		public void curvetoCubicSmoothRel(float arg0,float arg1,float x,float y)
		{
			this.addRelCoordinates(x,y);

		}

		@Override
		public void curvetoQuadraticAbs(float arg0,float arg1,float x,float y)
		{
			this.addAbsCoordinates(x,y);

		}

		@Override
		public void curvetoQuadraticRel(float arg0,float arg1,float x,float y)
		{
			this.addRelCoordinates(x,y);

		}

		@Override
		public void curvetoQuadraticSmoothAbs(float x,float y)
		{
			this.addAbsCoordinates(x,y);

		}

		@Override
		public void curvetoQuadraticSmoothRel(float x,float y)
		{
			this.addRelCoordinates(x,y);

		}

		@Override
		public void linetoAbs(float x,float y)
		{
			this.addAbsCoordinates(x,y);

		}

		@Override
		public void linetoHorizontalAbs(float x)
		{
			this.addAbsCoordinates(x,this.lastY);
		}

		@Override
		public void linetoHorizontalRel(float x)
		{
			this.addRelCoordinates(x,this.lastY);

		}

		@Override
		public void linetoRel(float x,float y)
		{
			this.addRelCoordinates(x,y);

		}

		@Override
		public void linetoVerticalAbs(float y)
		{
			this.addAbsCoordinates(this.lastX,y);

		}

		@Override
		public void linetoVerticalRel(float y)
		{
			this.addRelCoordinates(this.lastX,y);

		}

		@Override
		public void movetoAbs(float x,float y)
		{
			this.addAbsCoordinates(x,y);

		}

		@Override
		public void movetoRel(float x,float y)
		{
			this.addRelCoordinates(x,y);

		}
	}
}
