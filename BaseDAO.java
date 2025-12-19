package com.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class BaseDAO {
    
    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
    
    protected int executeUpdate(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            return stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("❌ Error executing update: " + e.getMessage());
            return 0;
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    protected <T> List<T> executeQuery(String sql, Function<ResultSet, T> mapper, Object... params) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<T> results = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                T result = mapper.apply(rs);
                if (result != null) {
                    results.add(result);
                }
            }
            
            return results;
            
        } catch (SQLException e) {
            System.err.println("❌ Error executing query: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    protected <T> T executeQuerySingle(String sql, Function<ResultSet, T> mapper, Object... params) {
        List<T> results = executeQuery(sql, mapper, params);
        return results.isEmpty() ? null : results.get(0);
    }
    
    protected void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("❌ Error closing database resources: " + e.getMessage());
        }
    }
}