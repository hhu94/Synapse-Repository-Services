package org.sagebionetworks.table.query.model;

import java.util.List;

/**
 * 
 * DefaultSpecification ::= DEFAULT
 * 
 */
public class DefaultSpecification extends SQLElement {

	@Override
	public void toSql(StringBuilder builder, ToSqlParameters parameters) {
		builder.append("DEFAULT");
	}

	@Override
	<T extends Element> void addElements(List<T> elements, Class<T> type) {
		// this is a leaf.
	}

}
