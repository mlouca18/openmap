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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/LayerControlButtonPanel.java,v $
// $RCSfile: LayerControlButtonPanel.java,v $
// $Revision: 1.1 $
// $Date: 2003/03/19 20:36:50 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Properties;
import javax.swing.*;

import com.bbn.openmap.Layer;
import com.bbn.openmap.LightMapHandlerChild;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.propertyEditor.*;

/**
 * A OMComponentPanel that provides controls to manupulate layer order
 * in the LayersPanel, and to provide add layer and delete layer
 * buttons.  This panel can be embedded into the LayersPanel, or it
 * can be positioned somewhere else in the application.  The
 * LayersPanel can be set to create one of these itself, in which case
 * it will be embedded.  If you don't want an embedded version, create
 * one and add it to the MapHandler, it will hook up to the
 * LayersPanel when it finds it.  The LayerPanes, LayersPanel and
 * LayerControlButtonPanel communicate with each other using
 * PropertyChangeEvents.  The LayerPanes notify the LayersPanelwhen
 * one of them is selected, and that event gets passed to this panel.
 * When a button on this panel is pressed, it fires a
 * PropertyChangeEvent with the layer and command to take to all its
 * PropertyChangeListeners. <P>
 *
 * The LayerControlButtonPanel takes these properties: <pre>
 * # Direction buttons are laid out,  vertical or horizontal (vertical is default).
 * orientation=vertical
 * # Flag on whether to insert buttons onto LayersPanel (true by default).
 * embedded=true
 * # Configuration setting when embedding into LayersPanel (WEST,
 * # NORTH, EAST, SOUTH, NORTH_SOUTH) NORTH_SOUTH puts up button above
 * # list, down button below list.
 * configuration=WEST
 * # Flag to put button that lets the user delete layers (true by default).
 * delete=true
 * # Flag to put button that lets the user add layers, if the
 * # LayersAddPanel is discovered in the MapHandler (true by default)
 * add=true
 * </pre>
 */
public class LayerControlButtonPanel extends OMComponentPanel 
    implements ActionListener, PropertyChangeListener {

    // Images
    protected static transient URL urlup;
    protected static transient ImageIcon upgif;
    protected static transient URL urlupc;
    protected static transient ImageIcon upclickedgif;
    protected static transient URL urltop;
    protected static transient ImageIcon topgif;
    protected static transient URL urltopc;
    protected static transient ImageIcon topclickedgif;
    protected static transient URL urldown;
    protected static transient ImageIcon downgif;
    protected static transient URL urldownc;
    protected static transient ImageIcon downclickedgif;
    protected static transient URL urlbottom;
    protected static transient ImageIcon bottomgif;
    protected static transient URL urlbottomc;
    protected static transient ImageIcon bottomclickedgif;
    protected static transient URL urldelete;
    protected static transient ImageIcon deletegif;
    protected static transient URL urldeletec;
    protected static transient ImageIcon deleteclickedgif;
    protected static transient URL urladd;
    protected static transient ImageIcon addgif;
    protected static transient URL urladdc;
    protected static transient ImageIcon addclickedgif;

    /**
     * Static default initializations.
     */
    static {
	urlup = LayersPanel.class.getResource("Up.gif");
	upgif = new ImageIcon(urlup, "Up");

	urlupc = LayersPanel.class.getResource("Up.gif");
	upclickedgif = new ImageIcon(urlupc, "Up (clicked)");

	urltop = LayersPanel.class.getResource("DoubleUp.gif");
	topgif = new ImageIcon(urltop, "Top");

	urltopc = LayersPanel.class.getResource("DoubleUp.gif");
	topclickedgif = new ImageIcon(urltopc, "Top (clicked)");

	urldown = LayersPanel.class.getResource("Down.gif");
	downgif = new ImageIcon(urldown, "Down");

	urldownc = LayersPanel.class.getResource("Down.gif");
	downclickedgif = new ImageIcon(urldownc, "Down (clicked)");

	urlbottom = LayersPanel.class.getResource("DoubleDown.gif");
	bottomgif = new ImageIcon(urlbottom, "Bottom");

	urlbottomc = LayersPanel.class.getResource("DoubleDown.gif");
	bottomclickedgif = new ImageIcon(urlbottomc, "Bottom (clicked)");

	urldelete = LayersPanel.class.getResource("DeleteLayer.gif");
	deletegif = new ImageIcon(urldelete, "Delete");

	urldeletec = LayersPanel.class.getResource("DeleteLayer.gif");
	deleteclickedgif = new ImageIcon(urldeletec, "Delete (clicked)");

	urladd = LayersPanel.class.getResource("AddLayer.gif");
	addgif = new ImageIcon(urladd, "Add");

	urladdc = LayersPanel.class.getResource("AddLayer.gif");
	addclickedgif = new ImageIcon(urladdc, "Add (clicked)");
    }
 
    protected JButton add = null;
    protected JButton delete = null;
    protected JButton top = null;
    protected JButton up = null;
    protected JButton down = null;
    protected JButton bottom = null;

    protected LayerAddPanel layerAddPanel;

    public final static String OrientationProperty = "orientation";
    public final static String ConfigurationProperty = "configuration";
    public final static String EmbeddedProperty = "embedded";
    public final static String DeleteLayersProperty = "delete";
    public final static String AddLayersProperty = "add";

    public final static String HORIZONTAL_CONFIG = OrientationPropertyEditor.HORIZONTAL;
    public final static String VERTICAL_CONFIG = OrientationPropertyEditor.VERTICAL;

    public final static String WEST_CONFIG = "WEST";
    public final static String EAST_CONFIG = "EAST";
    public final static String NORTH_CONFIG = "NORTH";
    public final static String SOUTH_CONFIG = "SOUTH";
    public final static String NORTH_SOUTH_CONFIG = "NORTH_SOUTH";
    public final static String DefaultConfiguration = WEST_CONFIG;

    protected int orientation = BoxLayout.Y_AXIS; // BoxLayout.X_AXIS
    protected String configuration = DefaultConfiguration;
    protected boolean embedded = true;
    protected boolean deleteLayers = true;
    protected boolean addLayers = true;

    public LayerControlButtonPanel() {
	super();
	createInterface();
    }

    public LayerControlButtonPanel(LayersPanel panel) {
	this();
	setLayersPanel(panel);
    }

    public void removeLayersPanel(LayersPanel panel) {
	panel.removePropertyChangeListener(this);
	removePropertyChangeListener(panel);

	if (embedded) {
	    if (configuration.equalsIgnoreCase(NORTH_SOUTH_CONFIG)) {
		panel.remove(up);
		panel.remove(down);
	    } else {
		panel.remove(this);
	    }
	}
    }

    /**
     * If you want to extend this class and change how the buttons are
     * displayed in the LayersPanel, change this method.
     */
    public void setLayersPanel(LayersPanel panel) {
	panel.addPropertyChangeListener(this);
	addPropertyChangeListener(panel);

	if (embedded) {
	    createInterface(); // again, reset for new config values
	    setLayout(new BoxLayout(this, orientation));

	    if (panel.getLayout() instanceof BorderLayout) {
		if (configuration.equalsIgnoreCase(WEST_CONFIG)) {
		    panel.add(this, BorderLayout.WEST);
		} else if (configuration.equalsIgnoreCase(EAST_CONFIG)) {
		    panel.add(this, BorderLayout.EAST);
		} else if (configuration.equalsIgnoreCase(NORTH_CONFIG)) {
		    panel.add(this, BorderLayout.NORTH);
		} else if (configuration.equalsIgnoreCase(SOUTH_CONFIG)) {
		    panel.add(this, BorderLayout.SOUTH);
		} else if (configuration.equalsIgnoreCase(NORTH_SOUTH_CONFIG)) {
		    panel.add(up, BorderLayout.NORTH);
		    panel.add(down, BorderLayout.SOUTH);
		}
	    } else {
		panel.add(this);
	    }
	}
    }

    protected void createInterface() {
	removeAll();

  	setAlignmentX(LEFT_ALIGNMENT);
  	setAlignmentY(CENTER_ALIGNMENT);
  	setLayout(new BoxLayout(this, orientation));

	top = new JButton(topgif);
	top.setActionCommand(LayersPanel.LayerTopCmd);
	top.setPressedIcon(topclickedgif);
	top.setToolTipText("Move selected layer to top");
	top.addActionListener(this);
  	add(top);

	up = new JButton(upgif);
	up.setActionCommand(LayersPanel.LayerUpCmd);
	up.setPressedIcon(upclickedgif);
	up.setToolTipText("Move selected layer up one");
	up.addActionListener(this);
  	add(up);

	down = new JButton(downgif);
	down.setPressedIcon(downclickedgif);
	down.setActionCommand(LayersPanel.LayerDownCmd);
	down.setToolTipText("Move selected layer down one");
	down.addActionListener(this);
  	add(down);

	bottom = new JButton(bottomgif);
	bottom.setPressedIcon(bottomclickedgif);
	bottom.setActionCommand(LayersPanel.LayerBottomCmd);
	bottom.setToolTipText("Move selected layer to bottom");
	bottom.addActionListener(this);
  	add(bottom);

	if (deleteLayers) {
	    JLabel blank = new JLabel(" ");
	    add(blank);
	    
	    delete = new JButton(deletegif);
	    delete.setActionCommand(LayersPanel.LayerRemoveCmd);
	    delete.setToolTipText("Remove selected layer");
	    delete.addActionListener(this);
	    add(delete);
	}

	if (addLayers && add != null) {
	    add(add);
	}
    }

    /**
     * Set the panel that brings up an interface to dynamically add
     * layers.
     */
    public void setLayerAddPanel(LayerAddPanel lap) {
	layerAddPanel = lap;

	if (layerAddPanel != null) {
	    add = new JButton(addgif);
	    add.setActionCommand(LayersPanel.LayerAddCmd);
	    add.setToolTipText("Add a layer");
	    add.addActionListener(this);
	    if (addLayers) {
		this.add(add);
	    }
	} else if (add != null) {
	    this.remove(add);
	}

    }

    /**
     * Get the panel interface to dynamically add layers.
     */
    public LayerAddPanel getLayerAddPanel() {
	return layerAddPanel;
    }

    /**
     * Method associated with the ActionListener interface.  This
     * method listens for action events meant to change the order of
     * the layers, as fired by the layer order buttons.
     *
     * @param e ActionEvent 
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {

	String command = e.getActionCommand();

	if (Debug.debugging("layerbuttons")) {
	    Debug.output("LayersPanel.actionPerformed(): " + command);
	}

	if (command == LayersPanel.LayerTopCmd ||
	    command == LayersPanel.LayerBottomCmd ||
	    command == LayersPanel.LayerUpCmd ||
	    command == LayersPanel.LayerDownCmd ||
	    command == LayersPanel.LayerRemoveCmd) {
	    if (selected != null) {
		if (Debug.debugging("layercontrol")) {
		    Debug.output("LayerControlButtonPanel: button firing " + 
				 command + " event for " + selected.getName());
		}
		firePropertyChange(command, null, selected);
	    } else {
		if (Debug.debugging("layercontrol")) {
		    Debug.output("LayerControlButtonPanel: button firing " + 
				 command + " event with no layer selected");
		}
	    }
	} else if (command.equals(LayersPanel.LayerAddCmd)) {
	    if (layerAddPanel != null) {
		layerAddPanel.showPanel();
	    }
	}
    }

    protected Layer selected = null;

    public void propertyChange(PropertyChangeEvent pce) {
	String command = pce.getPropertyName();
	Object obj = pce.getNewValue();
	if (Debug.debugging("layercontrol")) {
	    Debug.output("LayerControlButtonPanel: receiving PropertyChangeEvent " + pce.getPropertyName());
	}

	if (command == LayersPanel.LayerSelectedCmd &&
	    obj instanceof Layer) {

	    selected = (Layer)obj;

	    if (Debug.debugging("layercontrol")) {
		Debug.output("LayerControlButtonPanel: got notification that layer is selected: " + selected.getName());
	    }
	} else if (command == LayersPanel.LayerDeselectedCmd &&
		   selected == obj) {
	    selected = null;
	}
    }

    public void findAndInit(Object someObj) {
	if (someObj instanceof LayerAddPanel) {
	    setLayerAddPanel((LayerAddPanel)someObj);
	}

	if (someObj instanceof LayersPanel) {
	    setLayersPanel((LayersPanel)someObj);
	}
    }

    public void findAndUndo(Object someObj) {
	if (someObj instanceof LayerAddPanel) {
	    if (getLayerAddPanel() == someObj) {
		setLayerAddPanel(null);
	    }
	}

	if (someObj instanceof LayersPanel) {
	    removeLayersPanel((LayersPanel)someObj);
	}
    }

    public void setProperties(String prefix, Properties props) {
	super.setProperties(prefix, props);
	prefix = PropUtils.getScopedPropertyPrefix(prefix);

	configuration = props.getProperty(prefix + ConfigurationProperty);

	if (configuration == null) {
	    configuration = DefaultConfiguration;
	}

	embedded = LayerUtils.booleanFromProperties(props, prefix + EmbeddedProperty, embedded);
	deleteLayers = LayerUtils.booleanFromProperties(props, prefix + DeleteLayersProperty, deleteLayers);
	addLayers = LayerUtils.booleanFromProperties(props, prefix + AddLayersProperty, addLayers);
	
	String orient = props.getProperty(prefix + OrientationProperty);
	if (orient != null && (orient.equalsIgnoreCase(HORIZONTAL_CONFIG) || (orient.equalsIgnoreCase("false")))) {
	    orientation = BoxLayout.X_AXIS;
	}
    }

    public Properties getProperties(Properties props) {
	props = super.getProperties(props);
	String prefix = PropUtils.getScopedPropertyPrefix(this);

	props.put(prefix + ConfigurationProperty, configuration);
	props.put(prefix + OrientationProperty, (orientation==BoxLayout.X_AXIS?HORIZONTAL_CONFIG:VERTICAL_CONFIG));
	props.put(prefix + EmbeddedProperty, new Boolean(embedded).toString());
	props.put(prefix + DeleteLayersProperty, new Boolean(deleteLayers).toString());
	props.put(prefix + AddLayersProperty, new Boolean(addLayers).toString());
	return props;
    }

    public Properties getPropertyInfo(Properties props) {
	props = super.getPropertyInfo(props);

	props.put(ConfigurationProperty, "Pre-Defined Configuration String (WEST, EAST, NORTH, SOUTH, NORTH_SOUTH).");
	props.put(OrientationProperty, "Horizontal or Vertical.");
	props.put(OrientationProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.OrientationPropertyEditor");
	props.put(EmbeddedProperty, "Insert itself into LayersPanel.");
	props.put(EmbeddedProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
	props.put(DeleteLayersProperty, "Include button to delete layers.");
	props.put(DeleteLayersProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
	props.put(AddLayersProperty, "Include button to add layers.");
	props.put(AddLayersProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

	return props;
    }

}