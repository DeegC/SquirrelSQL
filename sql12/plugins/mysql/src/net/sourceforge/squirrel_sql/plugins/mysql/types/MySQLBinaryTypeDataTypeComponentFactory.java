package net.sourceforge.squirrel_sql.plugins.mysql.types;

import net.sourceforge.squirrel_sql.fw.datasetviewer.ColumnDisplayDefinition;
import net.sourceforge.squirrel_sql.fw.datasetviewer.cellcomponent.DataTypeShort;
import net.sourceforge.squirrel_sql.fw.datasetviewer.cellcomponent.IDataTypeComponent;
import net.sourceforge.squirrel_sql.fw.datasetviewer.cellcomponent.IDataTypeComponentFactory;
import net.sourceforge.squirrel_sql.fw.dialects.DialectType;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * A factory that creates DataTypeShort for rendering columns of MySQL TINYINT UNSIGNED.
 *
 */
public class MySQLBinaryTypeDataTypeComponentFactory implements
		IDataTypeComponentFactory {

	/**
	 * @see net.sourceforge.squirrel_sql.fw.datasetviewer.cellcomponent.IDataTypeComponentFactory#constructDataTypeComponent()
	 */
	@Override
	public IDataTypeComponent constructDataTypeComponent() {
		return new DataTypeBinaryWithUUID(null, new ColumnDisplayDefinition(30, "uuid"));
	}

	/**
	 * @see net.sourceforge.squirrel_sql.fw.datasetviewer.cellcomponent.IDataTypeComponentFactory#getDialectType()
	 */
	@Override
	public DialectType getDialectType() {
		return DialectType.MYSQL5;
	}


	@Override
	public boolean matches(DialectType dialectType, int sqlType,
			String sqlTypeName) {
		return new EqualsBuilder().append(getDialectType(), dialectType)
				.append(-2, sqlType)
				.append("BINARY", sqlTypeName).isEquals();
	}

}
