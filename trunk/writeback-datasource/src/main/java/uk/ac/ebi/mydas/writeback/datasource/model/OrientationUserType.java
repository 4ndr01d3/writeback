package uk.ac.ebi.mydas.writeback.datasource.model;


import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

public class OrientationUserType implements UserType {

	private static final int[] SQL_TYPES = {Types.VARCHAR};

	public int[] sqlTypes() { return SQL_TYPES; }
	public Class<Orientation> returnedClass() { return Orientation.class; }
	public boolean equals(Object x, Object y) { return x == y; }
	public Object deepCopy(Object value) { return value; }
	public boolean isMutable() { return false; }

	public Object nullSafeGet(ResultSet resultSet,
			String[] names,
			Object owner)
	throws HibernateException, SQLException {

		String name = resultSet.getString(names[0]);
		return resultSet.wasNull() ? null : Orientation.getInstance(name);
	}

	public void nullSafeSet(PreparedStatement statement,
			Object value,
			int index)
	throws HibernateException, SQLException {

		if (value == null) {
			statement.setNull(index, Types.VARCHAR);
		} else {
			statement.setString(index, value.toString());
		}
	}
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return null;
	}
	public Serializable disassemble(Object value) throws HibernateException {
		return null;
	}
	public int hashCode(Object x) throws HibernateException {
		return 0;
	}
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return null;
	}
}
