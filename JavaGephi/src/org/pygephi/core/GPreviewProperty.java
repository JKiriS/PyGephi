package org.pygephi.core;

import java.awt.Color;
import java.awt.Font;

import org.gephi.preview.types.DependantColor;
import org.gephi.preview.types.DependantOriginalColor;

public class GPreviewProperty {
	//Constants global
    /**
     * General <code>Boolean</code> property which indicates wheter the graph is directed
     */
    public static final String DIRECTED = "directed";
    /**
     * General <code>Color</code> property of the background color
     */
    public static final String BACKGROUND_COLOR = "background-color";
    /**
     * General <code>Float</code> property which indicates the ratio of the visible graph
     * used in preview. For instance if 0.5 only 50% of nodes items are built.
     */
    public static final String VISIBILITY_RATIO = "visibility-ratio";
    /**
     * General <code>Float</code> property in percentage (0-100) describing the 
     * margin size. For instance if the value is 4 the size of the margin is 4% of
     * the total graph width.
     */
    public static final String MARGIN = "margin";
    //Constants nodes
    /**
     * Node <code>Float</code> property defining the node border size.
     */
    public static final String NODE_BORDER_WIDTH = "node.border.width";
    /**
     * Node <code>DependantColor</code> property which defines the border color. A
     * dependant color value is either the node's color or a custom color.
     */
    public static final String NODE_BORDER_COLOR = "node.border.color";
    /**
     * Node <code>Float</code> property between 0-100 which defines the opacity.
     * 100 means opaque.
     */
    public static final String NODE_OPACITY = "node.opacity";
    //Constants edges
    /**
     * Edge <code>Boolean</code> property defining whether to show edges.
     */
    public static final String SHOW_EDGES = "edge.show";
    /**
     * Edge <code>Float</code> property for the edge's thickness
     */
    public static final String EDGE_THICKNESS = "edge.thickness";
    /**
     * Edge <code>Boolean</code> property whether to draw curved edges. A
     * <code>false</code> value means straight edges.
     */
    public static final String EDGE_CURVED = "edge.curved";
    /**
     * Edge <code>EdgeColor</code> property defining the edge color. It could be
     * the source's color, the target's color, a mixed color, the edge's original
     * color or a custom color.
     */
    public static final String EDGE_COLOR = "edge.color";
    /**
     * Edge <code>Float</code> property between 0-100 which defines the opacity.
     * 100 means opaque.
     */
    public static final String EDGE_OPACITY = "edge.opacity";
    /**
     * Edge <code>Boolean</code> property defining whether edge's weight should be
     * rescaled between fixed bounds.
     */
    public static final String EDGE_RESCALE_WEIGHT = "edge.rescale-weight";
    /**
     * Edge <code>Float</code> property defining an extra distance between the node
     * and the edge.
     */
    public static final String EDGE_RADIUS = "edge.radius";
    //Constants arrows
    /**
     * Arrow <code>Float</code> property defining the arrow size.
     */
    public static final String ARROW_SIZE = "arrow.size";
    //Constants node labels
    /**
     * Node Label <code>Boolean</code> property defining whether to show node labels.
     */
    public static final String SHOW_NODE_LABELS = "node.label.show";
    /**
     * Node Label <code>Font</code> property defining node label's font.
     */
    public static final String NODE_LABEL_FONT = "node.label.font";
    /**
     * Node Label <code>Boolean></code> property defining whether to use node's size
     * in label size calculation.
     */
    public static final String NODE_LABEL_PROPORTIONAL_SIZE = "node.label.proportinalSize";
    /**
     * Node Label <code>DependantOriginalColor</code> property defining the color label.
     * The color could either be the node's color, the label original color if it has any
     * or a custom color.
     */
    public static final String NODE_LABEL_COLOR = "node.label.color";
    /**
     * Node Label <code>Boolean</code> property defining whether the label is shortened.
     */
    public static final String NODE_LABEL_SHORTEN = "node.label.shorten";
    /**
     * Node Label <code>Integer</code> property defining the maximum number of
     * characters.
     */
    public static final String NODE_LABEL_MAX_CHAR = "node.label.max-char";
    /**
     * Node Label Outline <code>Float</code> property defining the outline size. 
     */
    public static final String NODE_LABEL_OUTLINE_SIZE = "node.label.outline.size";
    /**
     * Node Label Outline <code>Float</code> property between 0-100 which defines the opacity.
     * 100 means opaque.
     */
    public static final String NODE_LABEL_OUTLINE_OPACITY = "node.label.outline.opacity";
    /**
     * Node Label Outline <code>DependantColor</code> property defining the outline color.
     * The color can be the node's color or a custom color.
     */
    public static final String NODE_LABEL_OUTLINE_COLOR = "node.label.outline.color";
    public static final String NODE_LABEL_SHOW_BOX = "node.label.box";
    public static final String NODE_LABEL_BOX_COLOR = "node.label.box.color";
    public static final String NODE_LABEL_BOX_OPACITY = "node.label.box.opacity";
    //Constants edge labels
    /**
     * Edge Label <code>Boolean</code> property defining whether to show edge labels.
     */
    public static final String SHOW_EDGE_LABELS = "edge.label.show";
    /**
     * Edge Label <code>Font</code> property defining edge label's font.
     */
    public static final String EDGE_LABEL_FONT = "edge.label.font";
    /**
     * Edge Label <code>DependantOriginalColor</code> property defining the color label.
     * The color could either be the edge's color, the label original color if it has any
     * or a custom color.
     */
    public static final String EDGE_LABEL_COLOR = "edge.label.color";
    /**
     * Edge Label <code>Boolean</code> property defining whether the label is shortened.
     */
    public static final String EDGE_LABEL_SHORTEN = "edge.label.shorten";
    /**
     * Edge Label <code>Integer</code> property defining the maximum number of
     * characters.
     */
    public static final String EDGE_LABEL_MAX_CHAR = "edge.label.max-char";
    /**
     * Edge Label Outline <code>Float</code> property defining the outline size. 
     */
    public static final String EDGE_LABEL_OUTLINE_SIZE = "edge.label.outline.size";
    /**
     * Edge Label Outline <code>Float</code> property between 0-100 which defines the opacity.
     * 100 means opaque.
     */
    public static final String EDGE_LABEL_OUTLINE_OPACITY = "edge.label.outline.opacity";
    /**
     * Edge Label Outline <code>DependantColor</code> property defining the outline color.
     * The color can be the edge's color or a custom color.
     */
    public static final String EDGE_LABEL_OUTLINE_COLOR = "edge.label.outline.color";
    //Constants UI helps
    /**
     * General <code>Boolean</code> property set as <code>true</code> when the user
     * is panning the canvas. Helps to conditionally hide elements while moving to
     * speed things up.
     */
    public static final String MOVING = "canvas.moving";

    public static Boolean newBool(boolean b){
    	if(b)
    		return Boolean.TRUE;
    	return Boolean.FALSE;
    }
    
    public static Boolean defaultBool(){
    	return Boolean.TRUE;
    }
    
    public static Color newColor(String s){
    	return Color.getColor(s);
    }
    
    public static Color newColor(float r,float g, float b){
    	return new Color(r, g, b);
    }
    
    public static Color defaultColor(){
    	return Color.BLACK;
    }
    
    public static DependantColor newDependantColor(Color c){
    	return new DependantColor(c);
    }
    
    public static Font newFont(String type, int style, int size){
    	return new Font(type, style, size);
    }
    
    public static Font defaultFont(){
    	return new Font("ËÎÌו", Font.PLAIN, 8);
    }
    
    public static DependantOriginalColor newDependantOriginalColor(Color c){
    	return new DependantOriginalColor(c);
    }
    
}
