/*
 * Copyright (c) 2018 Villu Ruusmann
 *
 * This file is part of JPMML-Evaluator
 *
 * JPMML-Evaluator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-Evaluator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-Evaluator.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.evaluator;

import java.lang.reflect.Field;

import org.dmg.pmml.PMMLObject;

public class MissingAttributeException extends InvalidFeatureException {

	public MissingAttributeException(String message){
		super(message);
	}

	public MissingAttributeException(String message, PMMLObject context){
		super(message, context);
	}

	public MissingAttributeException(PMMLObject object, Field field){
		super(formatMessage(XPathUtil.formatElementOrAttribute(field)), object);
	}

	static
	public String formatMessage(String xpath){
		return "Required attribute " + xpath + " is not defined";
	}
}