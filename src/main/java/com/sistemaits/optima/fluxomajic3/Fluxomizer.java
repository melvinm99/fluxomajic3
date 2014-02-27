package com.sistemaits.optima.fluxomajic3;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.filter.capability.FunctionNameImpl;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.Converters;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import com.vividsolutions.jtsexample.geom.PrecisionModelExample;

/**
 * This {@link Function} take as input a LineString, and returns a buffered polygon drawn on a side 
 * of the LineString
 * @author tommaso.doninelli
 *
 */
public class Fluxomizer implements Function {
	
	/**
	 * Name declaration for this function
	 */
	public static final FunctionName NAME = new FunctionNameImpl(
			"fluxomajic3",												// Function name 
			Geometry.class, 											// Return value
			FunctionNameImpl.parameter("shape", LineString.class),		// Oredered parameters 			
			FunctionNameImpl.parameter("wms_bbox", ReferencedEnvelope.class),
			FunctionNameImpl.parameter("wms_crs", CoordinateReferenceSystem.class),
			FunctionNameImpl.parameter("wms_width", Integer.class),
			FunctionNameImpl.parameter("wms_height", Integer.class),
			FunctionNameImpl.parameter("wms_scale_denominator", Integer.class));

	/*
	 * Generic params
	 */
	private static Logger log = Logger.getLogger(Fluxomizer.class.getName());
	private List<Expression> parameters;
	private Literal fallback;
	
	public Fluxomizer(List<Expression> parameters, Literal fallback) {
		this.parameters = parameters;
		this.fallback = fallback;		
	}
	
	@Override
	public Object evaluate(Object object) {
		return evaluate(object, Geometry.class);
	}

	@Override
	public <T> T evaluate(Object object, Class<T> context) {
	
		/*
		 * Extract the param from the request
		 */
		LineString shape = parameters.get(0).evaluate(object, LineString.class);
		ReferencedEnvelope bbox = parameters.get(1).evaluate(object, ReferencedEnvelope.class);
		CoordinateReferenceSystem outCrs = parameters.get(2).evaluate(object, CoordinateReferenceSystem.class);		
		int imgWidth = parameters.get(3).evaluate(object, Integer.class);
		int imgHeight = parameters.get(4).evaluate(object, Integer.class);
		int scale = parameters.get(5).evaluate(object, Integer.class);
//		String val = SimpleCache.getTest("pippo");
//		
//		if(val == null){
//			log.severe("CACHE SCADUTA!!!!!!!!!!!!");
//			val = "lalalalala";
//			SimpleCache.setTest("pippo", val);
//		}
//		
//		debug("**********************");
//		debug(bbox.toString());
//		debug(outCrs.getName().getCode());
//		debug(val);
//		debug(imgWidth);
//		debug(imgHeight);
//		debug("**********************");
		
		
		
		
		MathTransform toProjected = null;
		MathTransform toGeometric = null;
		try {
			toProjected = CRS.findMathTransform(CRS.decode("EPSG:4326"), outCrs);
			toGeometric = CRS.findMathTransform(outCrs, CRS.decode("EPSG:4326"));
		} catch (FactoryException e) {
			log.severe("Can't convert coordinates from internal to external projection - " + e.getMessage());
		}
		
		double width = 50;		
		double scalaBase = 17061;
		
		// Spessore (quasi) costante con la scala...tie'
		width = (width*scale)/scalaBase;
		
		try {

//			 FIXME Il calcolo lo faccio *sempre* in coordinate proiettate? Allora mi devo cachare le coordinate originali riproiettate!
//			 Cosi se la richiesta ha una width diversa non devo riproiettare queste coordinate
			Geometry projected = JTS.transform(shape, toProjected);
			
			BufferParameters params = new BufferParameters(64, BufferParameters.CAP_SQUARE, BufferParameters.JOIN_BEVEL, 1);
			params.setEndCapStyle(BufferParameters.CAP_SQUARE);
			params.setSingleSided(true);
//			debug("aaa" + BufferParameters.CAP_SQUARE );
			// Semplifico la geometria e faccio il buffer			
			Geometry buffered = BufferOp.bufferOp(projected, width, params);
			
//			debug("BUFFERED:" + p.getExteriorRing().toText());
//			debug("ORIGINAL:" + projected.toText());
			
			// Simplify the polygon (remove all points "below" the original shape to avoi "cornetti")
			
//			return Converters.convert(buffered, context);
			return Converters.convert(JTS.transform(buffered, toGeometric), context);
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
//		
//		try {
//			return Converters.convert(JTS.transform(shape, tranformToRequest), context);
//		} catch (MismatchedDimensionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (TransformException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return null;
	}

	/**
	 * True if c is "left" respect AB
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public boolean isLeft(Coordinate a, Coordinate b, Coordinate c){
	     return ((b.x - a.x)*(c.y - a.y) - (b.y - a.y)*(c.x - a.x)) > 0;
	}
	
	/**
	 * TEST
	 * Returns a "buffered" polygon from a linestring 
	 * @param line
	 */
	private Geometry drawSimpleShape(Geometry line) {
		
		float width = 2;
		float delta = 1;
		
		Coordinate[] coords = line.getCoordinates();
		
		debug("---------------------------------------------");
        debug("ORIGINAL:" + line.toText());
        debug(" ");
        
        Geometry shape = null;
        
        for(int i=0; i<coords.length-1; i++) {
        	LineString segment = new LineString(new CoordinateArraySequence(new Coordinate[]{coords[i], coords[i+1]}), new GeometryFactory(line.getPrecisionModel()));
        	
        	debug("segment: " + segment.toText());
        	
        	Coordinate[] rect = new Coordinate[4];
        	rect[0] = coords[i];
        	rect[1] = coords[i+1];
        	rect[2] = shiftCoords(coords[i+1], coords[i], coords[i+1], width, delta);
        	rect[3] = shiftCoords(coords[i], coords[i], coords[i+1], width, delta);
        	rect[3] = coords[i];
        			
        	
        	
        }
        debug("---------------------------------------------");
        
        return line;
	}
	
	/**
	 * Should return a point perpendicular to "which" with respect to the line c1-c2 
	 * @param which
	 * @param c1
	 * @param c2
	 * @param width
	 * @param delta
	 * @return
	 */
	protected Coordinate shiftCoords(Coordinate which, Coordinate c1, Coordinate c2, float width, double delta) {
        double xDiff = c2.x - c1.x;
        double yDiff = c2.y - c1.y;
        double len = segLength(xDiff, yDiff);
        double dx = xDiff / len;
        double dy = yDiff / len;
        double finalX = which.x + (delta + (width * 0.5)) * dy;
        double finalY = which.y + (delta + (width * 0.5)) * dx;
        return new Coordinate(finalX, finalY);
    }
	
	private double segLength(double x, double y) {
        return Math.sqrt(x * x + y * y);	// FIXME Buuu per Math.sqr
    }
	
	@Override
	public Object accept(ExpressionVisitor visitor, Object extraData) {
		return visitor.visit(this, extraData);
	}

	@Override
	public Literal getFallbackValue() {
		return fallback;
	}

	@Override
	public FunctionName getFunctionName() {
		return NAME;
	}

	@Override
	public String getName() {
		return NAME.getName();
	}

	@Override
	public List<Expression> getParameters() {
		return parameters;
	}

	private void debug(int message){log.info(Integer.toString(message));}
	private void debug(String message){log.info(message);}
}
