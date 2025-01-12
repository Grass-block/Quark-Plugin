package org.tbstcraft.quark.data.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

interface ParamDispatcher {
    Map<Class<?>, LambdaResolver<?>> RESOLVERS = new HashMap<>();

    static void init() {
        dispatch(byte.class, PreparedStatement::setByte);
        dispatch(Byte.class, PreparedStatement::setByte);
        dispatch(short.class, PreparedStatement::setShort);
        dispatch(Short.class, PreparedStatement::setShort);
        dispatch(int.class, PreparedStatement::setInt);
        dispatch(Integer.class, PreparedStatement::setInt);
        dispatch(long.class, PreparedStatement::setLong);
        dispatch(Long.class, PreparedStatement::setLong);
        dispatch(float.class, PreparedStatement::setFloat);
        dispatch(Float.class, PreparedStatement::setFloat);
        dispatch(double.class, PreparedStatement::setDouble);
        dispatch(Double.class, PreparedStatement::setDouble);
        dispatch(boolean.class, PreparedStatement::setBoolean);
        dispatch(Boolean.class, PreparedStatement::setBoolean);

        dispatch(byte[].class, PreparedStatement::setBytes);
        dispatch(Date.class, (ps, p, v) -> ps.setDate(p, new java.sql.Date(v.getTime())));
        dispatch(String.class, PreparedStatement::setString);
        dispatch(Object.class, PreparedStatement::setObject);
    }

    static <T> LambdaResolver<T> select(Object obj) {
        var type = RESOLVERS.get(obj.getClass());

        return ((LambdaResolver<T>) Objects.requireNonNullElse(type, RESOLVERS.get(Object.class)));
    }

    static void set(PreparedStatement ps, int position, Object value) throws SQLException {
        select(value).exec(ps, position, value);
    }

    static <T> void dispatch(Class<T> type, LambdaResolver<T> value) {
        RESOLVERS.put(type, value);
    }

    interface LambdaResolver<T> {
        void exec(PreparedStatement ps, int index, T value) throws SQLException;
    }
}
