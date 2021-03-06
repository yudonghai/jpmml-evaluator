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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Iterables;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;

public class ModelEvaluatorBuilder implements EvaluatorBuilder, Serializable {

	private PMML pmml = null;

	private Model model = null;

	private ModelEvaluatorFactory modelEvaluatorFactory = null;

	private ValueFactoryFactory valueFactoryFactory = null;

	private java.util.function.Function<FieldName, FieldName> inputMapper = null;

	private java.util.function.Function<FieldName, FieldName> resultMapper = null;


	protected ModelEvaluatorBuilder(){
	}

	/**
	 * <p>
	 * Selects the first scorable model.
	 * </p>
	 *
	 * @throws MissingElementException If the PMML does not contain any scorable models.
	 */
	public ModelEvaluatorBuilder(PMML pmml){
		this(pmml, (String)null);
	}

	/**
	 * <p>
	 * Selects the named model.
	 * </p>
	 *
	 * @throws MissingElementException If the PMML does not contain a model with the specified model name.
	 *
	 * @see Model#getModelName()
	 */
	public ModelEvaluatorBuilder(PMML pmml, String modelName){
		setPMML(Objects.requireNonNull(pmml));
		setModel(PMMLUtil.findModel(pmml, modelName));
	}

	public ModelEvaluatorBuilder(PMML pmml, Model model){
		setPMML(Objects.requireNonNull(pmml));
		setModel(Objects.requireNonNull(model));
	}

	@Override
	public ModelEvaluatorBuilder clone(){

		try {
			return (ModelEvaluatorBuilder)super.clone();
		} catch(CloneNotSupportedException cnse){
			throw new InternalError(cnse);
		}
	}

	@Override
	public ModelEvaluator<?> build(){
		PMML pmml = getPMML();
		Model model = getModel();

		if((pmml == null) || (model == null)){
			throw new IllegalStateException();
		}

		ModelEvaluatorFactory modelEvaluatorFactory = getModelEvaluatorFactory();
		if(modelEvaluatorFactory == null){
			modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
		}

		ValueFactoryFactory valueFactoryFactory = getValueFactoryFactory();
		if(valueFactoryFactory == null){
			valueFactoryFactory = ValueFactoryFactory.newInstance();
		}

		Configuration configuration = new Configuration(modelEvaluatorFactory, valueFactoryFactory);

		ModelEvaluator<?> modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml, model);
		modelEvaluator.configure(configuration);

		checkSchema(modelEvaluator);

		java.util.function.Function<FieldName, FieldName> inputMapper = getInputMapper();
		java.util.function.Function<FieldName, FieldName> resultMapper = getResultMapper();

		if(inputMapper != null){
			Iterable<InputField> inputFields = modelEvaluator.getInputFields();

			if(modelEvaluator instanceof HasGroupFields){
				HasGroupFields hasGroupFields = (HasGroupFields)modelEvaluator;

				inputFields = Iterables.concat(inputFields, hasGroupFields.getGroupFields());
			}

			for(InputField inputField : inputFields){
				inputField.setName(inputMapper.apply(inputField.getName()));
			}
		} // End if

		if(resultMapper != null){
			Iterable<ResultField> resultFields = Iterables.concat(modelEvaluator.getTargetFields(), modelEvaluator.getOutputFields());

			for(ResultField resultField : resultFields){
				resultField.setName(resultMapper.apply(resultField.getName()));
			}
		}

		return modelEvaluator;
	}

	protected void checkSchema(ModelEvaluator<?> modelEvaluator){
		Model model = modelEvaluator.getModel();

		MiningSchema miningSchema = model.getMiningSchema();

		List<InputField> inputFields = modelEvaluator.getInputFields();
		List<InputField> groupFields = Collections.emptyList();

		if(modelEvaluator instanceof HasGroupFields){
			HasGroupFields hasGroupFields = (HasGroupFields)modelEvaluator;

			groupFields = hasGroupFields.getGroupFields();
		} // End if

		if((inputFields.size() + groupFields.size()) > 1000){
			throw new InvalidElementException("Model has too many input fields", miningSchema);
		}

		List<TargetField> targetFields = modelEvaluator.getTargetFields();
		List<OutputField> outputFields = modelEvaluator.getOutputFields();

		if((targetFields.size() + outputFields.size()) < 1){
			throw new InvalidElementException("Model does not have any target or output fields", miningSchema);
		}
	}

	public PMML getPMML(){
		return this.pmml;
	}

	protected ModelEvaluatorBuilder setPMML(PMML pmml){
		this.pmml = pmml;

		return this;
	}

	public Model getModel(){
		return this.model;
	}

	protected ModelEvaluatorBuilder setModel(Model model){
		this.model = model;

		return this;
	}

	public ModelEvaluatorFactory getModelEvaluatorFactory(){
		return this.modelEvaluatorFactory;
	}

	public ModelEvaluatorBuilder setModelEvaluatorFactory(ModelEvaluatorFactory modelEvaluatorFactory){
		this.modelEvaluatorFactory = modelEvaluatorFactory;

		return this;
	}

	public ValueFactoryFactory getValueFactoryFactory(){
		return this.valueFactoryFactory;
	}

	public ModelEvaluatorBuilder setValueFactoryFactory(ValueFactoryFactory valueFactoryFactory){
		this.valueFactoryFactory = valueFactoryFactory;

		return this;
	}

	public java.util.function.Function<FieldName, FieldName> getInputMapper(){
		return this.inputMapper;
	}

	public ModelEvaluatorBuilder setInputMapper(java.util.function.Function<FieldName, FieldName> inputMapper){
		this.inputMapper = inputMapper;

		return this;
	}

	public java.util.function.Function<FieldName, FieldName> getResultMapper(){
		return this.resultMapper;
	}

	public ModelEvaluatorBuilder setResultMapper(java.util.function.Function<FieldName, FieldName> resultMapper){
		this.resultMapper = resultMapper;

		return this;
	}
}