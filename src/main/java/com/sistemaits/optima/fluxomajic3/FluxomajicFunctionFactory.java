package com.sistemaits.optima.fluxomajic3;

import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.NameImpl;
import org.geotools.filter.FunctionFactory;
import org.opengis.feature.type.Name;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

import com.sistemaits.optima.fluxomajic3.test.SnapFunction;

public class FluxomajicFunctionFactory implements FunctionFactory {

	/**
	 * Come sotto
	 */
	@Override
	public Function function(String name, List<Expression> args, Literal fallback) {
		return function(new NameImpl(name), args, fallback);
	}

	/**
	 * Ritorna una function usando il nome
	 */
	@Override
	public Function function(Name name, List<Expression> args, Literal fallback) {
		if( SnapFunction.NAME.getFunctionName().equals(name)){
            return new SnapFunction( args, fallback );
        }
		else if( Fluxomizer.NAME.getFunctionName().equals(name) ){
			return new Fluxomizer(args, fallback);
		}
        return null; // we do not implement that function
	}

	/**
	 * Ritorna l'elenco delle Function disponibili
	 */
	@Override
	public List<FunctionName> getFunctionNames() {
		List<FunctionName> list = new ArrayList<FunctionName>();
		list.add(SnapFunction.NAME);
		list.add(Fluxomizer.NAME);
		return list;
	}

}
