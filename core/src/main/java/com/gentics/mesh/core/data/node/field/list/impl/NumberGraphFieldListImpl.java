package com.gentics.mesh.core.data.node.field.list.impl;

import com.gentics.mesh.core.data.node.field.basic.NumberGraphField;
import com.gentics.mesh.core.data.node.field.impl.basic.NumberGraphFieldImpl;
import com.gentics.mesh.core.data.node.field.list.AbstractBasicGraphFieldList;
import com.gentics.mesh.core.data.node.field.list.NumberGraphFieldList;
import com.gentics.mesh.core.rest.node.field.list.impl.NumberFieldListImpl;
import com.gentics.mesh.handler.InternalActionContext;

public class NumberGraphFieldListImpl extends AbstractBasicGraphFieldList<NumberGraphField, NumberFieldListImpl>implements NumberGraphFieldList {

	@Override
	public NumberGraphField createNumber(String number) {
		NumberGraphField field = createField();
		field.setNumber(number);
		return field;
	}

	@Override
	public NumberGraphField getNumber(int index) {
		return getField(index);
	}

	@Override
	protected NumberGraphField createField(String key) {
		return new NumberGraphFieldImpl(key, getImpl());
	}

	@Override
	public Class<? extends NumberGraphField> getListType() {
		return NumberGraphFieldImpl.class;
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
	}

	@Override
	public NumberFieldListImpl transformToRest(InternalActionContext ac, String fieldKey) {
		NumberFieldListImpl restModel = new NumberFieldListImpl();
		for (NumberGraphField item : getList()) {
			restModel.add(item.getNumber());
		}
		return restModel;
	}

}
