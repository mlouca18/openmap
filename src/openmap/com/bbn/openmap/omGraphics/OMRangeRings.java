// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMRangeRings.java,v $
// $RCSfile: OMRangeRings.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics;

import java.awt.*;
import java.io.Serializable;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.proj.*;

/**
 * An object that manages a series of range circles.  It is really an
 * OMCircle that manages a set of inner circles and an OMPOint.  The
 * location of these inner circles depend on two new variables, the
 * interval and intervalUnits.  If the intervalUnits are null, then
 * the interval represents the number of inner circles, not including
 * the outer ring and the innermost point, that are spaced evenly
 * between them.  If the intervalUnits are not null, then the interval
 * represents the number of intervalUnits where inner circles are
 * placed.  For example, if the intervalUnits is Length.MILE, and the
 * interval is 5, then inner circles will be placed every 5 miles.  If
 * the intervalUnits is null, then there will be 5 inner circles drawn
 * between the center point and the outer ring.
 *
 * @see OMCircle 
 */
public class OMRangeRings extends OMCircle implements Serializable {
    /** The inner ring of circles. */
    protected transient OMCircle[] subCircles = null;
    /** The labels for the circles. */
    protected transient OMText[] labels = null;

    /** By default, there are 3 inner rings. */
    public final static int DEFAULT_INTERVAL = 3;
    /** The number of rings, or the unit interval, depending on
     *  whether intervalUnits is null or not. */
    protected int interval = DEFAULT_INTERVAL;
    /** The unit object specifying the interval meaning. */
    protected Length intervalUnits = null;
    /** The DrawingAttributes object used to reflect the outer circle
     *  properties to the inner circles. */
    protected DrawingAttributes drawingAttributes = new DrawingAttributes();
    /** The center point of the range rings. */
    protected OMPoint centerPoint;
    /** The default format. */
    public final static java.text.DecimalFormat DEFAULT_FORMAT = new java.text.DecimalFormat();
    /** Formatting for the labels with units. */
    protected java.text.NumberFormat form = DEFAULT_FORMAT;

    protected boolean drawLabels = true;

    /**
     * Creates an OMRangeRings with a Lat-lon center and a lat-lon axis.
     * Rendertype is RENDERTYPE_LATLON.
     *
     * @param latPoint latitude of center point, decimal degrees
     * @param lonPoint longitude of center point, decimal degrees
     * @param radius distance in decimal degrees (converted to radians
     * internally).  
     */
    public OMRangeRings(float latPoint, float lonPoint, float radius) {
	this(new LatLonPoint(latPoint, lonPoint), radius, 
	     Length.DECIMAL_DEGREE, -1);
    }

    /**
     * Create an OMRangeRings with a lat/lon center and a physical
     * distance radius.  Rendertype is RENDERTYPE_LATLON.
     *
     * @param latPoint latitude of center of circle in decimal degrees
     * @param lonPoint longitude of center of circle in decimal degrees
     * @param radius distance
     * @param units com.bbn.openmap.proj.Length object.
     */
    public OMRangeRings(float latPoint, float lonPoint, 
			float radius, Length units) {
	this(new LatLonPoint(latPoint, lonPoint), radius, units, -1);
    }

    /**
     * Create an OMRangeRings with a lat/lon center and a physical
     * distance radius.  Rendertype is RENDERTYPE_LATLON.
     *
     * @param latPoint latitude of center of circle in decimal degrees
     * @param lonPoint longitude of center of circle in decimal degrees
     * @param radius distance
     * @param units com.bbn.openmap.proj.Length object specifying units.
     * @param nverts number of vertices for the poly-circle (if &lt; 3, value
     * is generated internally)
     */
    public OMRangeRings(float latPoint, float lonPoint, 
			float radius, Length units, int nverts) {
	this(new LatLonPoint(latPoint, lonPoint), radius, units, nverts);
    }

    /**
     * Create an OMRangeRings with a lat/lon center and a physical
     * distance radius.  Rendertype is RENDERTYPE_LATLON.
     *
     * @param center LatLon center of circle
     * @param radius distance
     * @param units com.bbn.openmap.proj.Length object specifying
     * units for distance.
     * @param nverts number of vertices for the poly-circle(if &lt; 3,
     * value is generated internally) 
     */
    public OMRangeRings(LatLonPoint center, float radius, 
			Length units, int nverts) {
	super(center, radius, units, nverts);
	centerPoint = createCenterPoint();
	form.setMaximumFractionDigits(2);
    }

    protected OMPoint createCenterPoint() {
	return new OMPoint((float)center.getLatitude(), 
			   (float)center.getLongitude());
    }

    /**
     * Set the interval.  If the interval units are null, then this
     * interval represents the number of circles within the external,
     * defined circle.  If the interval units are not null, then this
     * interval represents the unit intervals where range rings are
     * placed.
     */
    public void setInterval(int interval) {
	this.interval = interval;
    }

    /** Convenience method to set both at one time. */
    public void setInterval(int interval, Length units) {
	setInterval(interval);
	setIntervalUnits(units);
    }

    /**
     * Get the interval number.
     */
    public int getInterval() {
	return interval;
    }

    /**
     * Set the interval units.  If this is null, then the interval
     * value will represent the number of rings drawn within the
     * defined outer ring.  If this is not null, then it represents
     * the units of the interval where the range rings are drawn.
     */
    public void setIntervalUnits(Length units) {
	intervalUnits = units;
    }

    /**
     * Get the interval units.
     */
    public Length getIntervalUnits() {
	return intervalUnits;
    }

    /**
     * Flag for whether the rings should be labeled.
     */
    public void setDrawLabels(boolean dl) {
	drawLabels = dl;
    }

    public boolean getDrawLabels() {
	return drawLabels;
    }

    /** 
     * Set the format for the number labels.  If null, the defaule
     * will be used.  This only applies to the labels with units. 
     */
    public void setFormat(java.text.NumberFormat nf) {
	if (nf != null) {
	    form = nf;
	} else {
	    form = DEFAULT_FORMAT;
	}
    }

    /**
     * Get the format used for the labeling of unit rings.
     */
    public java.text.NumberFormat getFormat() {
	return form;
    }

    /**
     * Set the radius. This is meaningful only if the render type is
     * RENDERTYPE_LATLON.  Note that while the radius is specified as
     * decimal degrees, it only means the distance along the ground
     * that that number of degrees represents at the equator, *NOT* a
     * radius of a number of degrees around a certain location.  There
     * is a difference.
     *
     * @param radius float radius in decimal degrees 
     */
    public void setRadius(float radius) {
        setRadius(radius, Length.DECIMAL_DEGREE);
    }

    /**
     * Set the radius with units.
     * This is meaningful only if the render type is
     * RENDERTYPE_LATLON.
     *
     * @param radius float radius
     * @param units Length specifying unit type.
     */
    public void setRadius(float radius, Length units) {
        this.radius = units.toRadians(radius);
	setNeedToRegenerate(true);
    }

    /** 
     * Take the interval and intervalUnits, and then create the
     * proper inner circles. 
     */
    public OMCircle[] createCircles() {
	OMCircle[] circles;
	OMText[] t;
	int i;
	float rad;
	String value;	
	if (intervalUnits == null) {
	    circles = new OMCircle[interval];
	    t = new OMText[interval];

	    for (i = 0; i < interval; i++) {
		rad = (float)((i + 1) * radius/(interval + 1));
		circles[i] = new OMCircle(center, rad, Length.RADIAN, -1);
		value = ((i+1) + "/"+ (interval+1));
		t[i] = new OMText(center.getLatitude() + Length.DECIMAL_DEGREE.fromRadians(rad), center.getLongitude(), value, OMText.JUSTIFY_CENTER);
	   } 
	} else {
	    float realDistanceInterval = intervalUnits.toRadians(interval);
	    int number = (int)(radius/realDistanceInterval);
	    circles = new OMCircle[number];
	    t = new OMText[number + 1];
	    for (i = 0; i < number; i++) {
		rad = (float)((i+1)*realDistanceInterval);
		circles[i] = new OMCircle(center, rad, Length.RADIAN, -1);
		value = (form.format((double)intervalUnits.fromRadians(rad)) + " " + intervalUnits.getAbbr());
		t[i] = new OMText(center.getLatitude() + Length.DECIMAL_DEGREE.fromRadians(rad), center.getLongitude(), value, OMText.JUSTIFY_CENTER);
	    }
	    value = (form.format((double)intervalUnits.fromRadians(radius)) + " " + intervalUnits.getAbbr());
	    t[i] = new OMText(center.getLatitude() + Length.DECIMAL_DEGREE.fromRadians(radius), center.getLongitude(), value, OMText.JUSTIFY_CENTER);
	}
	labels = t;
	return circles;
    }

    /**
     * Prepare the circles for rendering.
     *
     * @param proj Projection
     * @return true if generate was successful
     */
    public boolean generate(Projection proj) {
	if (getNeedToRegenerate() == true) {
	    subCircles = createCircles();
	}

	centerPoint = createCenterPoint();
	centerPoint.generate(proj);

	setRenderType(RENDERTYPE_LATLON); // Can't be anything else.
	int i;	
	if (subCircles != null) {
	    for (i = 0; i < subCircles.length; i++) {
		subCircles[i].generate(proj);
		labels[i].generate(proj);
	    }

	    // do the one for the outer ring if there are units.
	    if (labels.length > i) {
		labels[i].generate(proj);
	    }
	}

	return super.generate(proj);
    }

    /**
     * Paint the circles.
     *
     * @param g Graphics context to render into
     */
    public void render(Graphics g) {
	super.render(g);
	drawingAttributes.setFrom(this);

	if (subCircles != null) {
	    // Draw from the larger to the smaller, so the lines of
	    // the smaller circles will appear on top of the bigger
	    // ones.
	    for (int i = subCircles.length - 1; i >= 0; i--) {
		drawingAttributes.setTo(subCircles[i]);
		drawingAttributes.setTo(labels[i]);
		labels[i].setLinePaint(drawingAttributes.getLinePaint());
		subCircles[i].render(g);
		if (drawLabels) {
		    labels[i].render(g);
		}
	    }
	    // do the one for the outer ring if there are units.
	    if (labels.length > subCircles.length && drawLabels) {
		labels[subCircles.length].setLinePaint(drawingAttributes.getLinePaint());
		labels[subCircles.length].render(g);
	    }
	}
	if (centerPoint != null) {
	    drawingAttributes.setTo(centerPoint);
	    centerPoint.render(g);
	}
    }

    /**
     * Return the shortest distance from the circle to an XY-point.
     *
     * @param x X coordinate of the point.
     * @param y Y coordinate fo the point.
     * @return float distance from circle to the point
     */
    public float distance(int x, int y) {
	float dist = normalizeDistanceForLineWidth(super.distance(x, y));

	// Not sure whether the inner circles should be queried for
	// distance measurements.
	float tmpDist;
//  	if (dist != 0 && subCircles != null) {
//  	    for (int i = 0; i < subCircles.length; i++) {
//  		tmpDist = subCircles[i].distance(x, y);
//  		if (tmpDist == 0) return tmpDist;
		
//  		if (tmpDist < dist) {
//  		    dist = tmpDist;
//  		}
//  	    }
//  	}
	tmpDist = centerPoint.distance(x, y);
	if (tmpDist < dist) {
	    dist = tmpDist;
	}

	return dist;
    }
}