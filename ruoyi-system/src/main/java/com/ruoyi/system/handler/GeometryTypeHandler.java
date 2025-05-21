package com.ruoyi.system.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.postgresql.util.PGobject;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static org.locationtech.jts.geom.Coordinate.NULL_ORDINATE;

/**
 * @author: xiaodemos
 * @date: 2025-04-04 14:27
 * @description: Geometry类型处理器
 */

@MappedJdbcTypes(value = JdbcType.JAVA_OBJECT, includeNullJdbcType = true)
@MappedTypes({Geometry.class})
public class GeometryTypeHandler extends BaseTypeHandler<Geometry> {

    private final WKBReader wkbReader = new WKBReader();
    private final WKTReader wktReader = new WKTReader();

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Geometry geometry, JdbcType jdbcType) throws SQLException {


        // 将 Geometry 对象转换为 PGobject
        PGobject pgObject = new PGobject();
        pgObject.setType("geometry");
        pgObject.setValue(toPGobject(geometry)); // 使用你提供的转换方法将 Geometry 转换为 WKT 格式

        // 设置为 PGobject 类型，明确指定为 "geometry"
        preparedStatement.setObject(i, pgObject, java.sql.Types.OTHER);

        // preparedStatement.setObject(i, toPGobject(geometry));
        System.out.println("Setting geometry: " + pgObject.getValue());


    }

    private static Geometry toGeometry(String geometryString) {
        try {
            // 如果是 WKT 格式，解析为 Geometry
            return new WKTReader().read(geometryString.split(";")[1]); // 处理掉 SRID 部分
        } catch (ParseException e) {
            System.out.println("几何字段解析失败： " + e.getMessage());
            return null;
        }
    }

    private String toPGobject(Geometry geometry) {
        if (geometry.getSRID() == 0) {
            geometry.setSRID(4326);
        }
        int dimension = dimension(geometry);
        String wkt = new WKTWriter(dimension).write(geometry);
        return "SRID=" + geometry.getSRID() + ";" + wkt;
    }

    public static int dimension(Geometry geo) {
        if (geo.getCoordinates() != null && geo.getCoordinates().length > 0) {
            return Objects.equals(NULL_ORDINATE, geo.getCoordinates()[0].getZ()) ? 2 : 3;
        } else {
            System.out.println("无法判断 geometry 是否包含 Z 值，将保存为 3 维");
            return 3;
        }
    }

    @Override
    public Geometry getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        if (resultSet.getObject(columnName) == null) {
            return null;
        }
        String string = ((PGobject) resultSet.getObject(columnName)).getValue();
        return toGeometry(string);
    }

    @Override
    public Geometry getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        if (resultSet.getObject(columnIndex) == null) {
            return null;
        }
        String string = ((PGobject) resultSet.getObject(columnIndex)).getValue();
        return toGeometry(string);
    }

    @Override
    public Geometry getNullableResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        if (callableStatement.getObject(columnIndex) == null) {
            return null;
        }
        String string = ((PGobject) callableStatement.getObject(columnIndex)).getValue();
        return toGeometry(string);
    }
}
