package com.sistemaits.optima.fluxomajic3.test;

import java.util.List;




import java.util.logging.Logger;

import org.geotools.filter.capability.FunctionNameImpl;
import org.geotools.util.Converters;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class SnapFunction implements Function{

	// La function si chiama "snap", ritorna un Point e prende in input un parametro che si chiama "shape" ed è di tipo "Gemoetry"
	public static FunctionName NAME = new FunctionNameImpl("snap", Point.class, FunctionNameImpl.parameter("shape", Geometry.class));
	private static Logger log = Logger.getLogger(SnapFunction.class.getName());
	
	private List<Expression> parameters;
	private Literal fallback;
	
	public SnapFunction(List<Expression> parameters, Literal fallback) {
		Logger.getLogger("org.geoserver.wms").severe("COSTRUTTORE!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		
		this.parameters = parameters;
		this.fallback = fallback;
		
		Logger.getLogger("org.geoserver.wms").severe(parameters.get(0).toString());
	}

	@Override
	public Object evaluate(Object arg0) {
		return evaluate(arg0, Point.class);
	}

	@Override
	public <T> T evaluate(Object object, Class<T> context) {
		log.severe("Uelapeppa!");
		log.warning("WARNINGddddddd");
		log.info("INFO");
		log.fine("FINE");
		log.finer("FINE");
		LineString geom = parameters.get(0).evaluate(object, LineString.class);
		log.severe(geom.toText());
		
		return Converters.convert(geom.getStartPoint(), context);
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

}
