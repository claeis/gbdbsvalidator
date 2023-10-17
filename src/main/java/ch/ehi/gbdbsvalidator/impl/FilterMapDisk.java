package ch.ehi.gbdbsvalidator.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.h2.api.ErrorCode;

import ch.ehi.gbdbsvalidator.FilterMap;

public class FilterMapDisk  implements FilterMap  {
    private Connection con=null;
    private PreparedStatement insStmt=null;
    private PreparedStatement updStmt=null;
    private PreparedStatement getStmt=null;
    private boolean pendingCommits=false;
    private java.io.File dbfile=null;
    private String lastIteratorKey=null;
    private String lastIteratorValue=null;
    private long totalKeySize=0L;
    private long totalValueSize=0L;
    public FilterMapDisk(java.io.File dbfile) {
        deleteDbFile(dbfile);
        this.dbfile=dbfile;
        //String dburl="jdbc:h2:file:"+dbfile.getAbsolutePath()+";PAGE_SIZE=512;LOG=0;LOCK_MODE=0;UNDO_LOG=0;CACHE_SIZE="+java.lang.Runtime.getRuntime().maxMemory()/1024L/2L;
        String dburl="jdbc:h2:file:"+dbfile.getAbsolutePath();
        try {
            con=DriverManager.getConnection(dburl);
            con.setAutoCommit(false);
        } catch (SQLException e) {
            throw new IllegalStateException("failed to open db",e);
        }
        try {
            createTable();
            con.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("failed to create table",e);
        }
        try {
            String sql="INSERT INTO gb(NAME, DATA) VALUES (?,?);";
            insStmt=con.prepareStatement(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("failed to create insert statement",e);
        }
        try {
            String sql="UPDATE gb SET DATA=? WHERE NAME=?;";
            updStmt=con.prepareStatement(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("failed to create insert statement",e);
        }
        try {
            String sql="SELECT DATA FROM gb WHERE NAME=?;";
            getStmt=con.prepareStatement(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("failed to create select statement",e);
        }
    }
    private void createTable() throws SQLException {
        Statement stmt=con.createStatement();
        {
            String sql="CREATE TABLE gb(NAME VARCHAR(255) PRIMARY KEY, DATA CLOB);";
            stmt.execute(sql);
        }
    }
    @Override
    public void put(String key, String value) {
        try {
            insStmt.clearParameters();
            insStmt.setString(1, key);
            insStmt.setString(2, value);
            insStmt.execute();
            pendingCommits=true;
            totalKeySize+=key.length();
            totalValueSize+=value.length();
        } catch (SQLException e) {
            if(e.getErrorCode()!=ErrorCode.DUPLICATE_KEY_1) {
                throw new IllegalStateException("failed to insert data",e);
            }
        }
        try {
            updStmt.clearParameters();
            updStmt.setString(2, key);
            updStmt.setString(1, value);
            updStmt.execute();
            pendingCommits=true;
        } catch (SQLException e) {
            throw new IllegalStateException("failed to update data",e);
        }
    }

    @Override
    public String get(String key) {
        if(lastIteratorKey!=null && lastIteratorKey.equals(key)) {
            return lastIteratorValue;
        }
        try {
            doCommit();
            getStmt.clearParameters();
            getStmt.setString(1, key);
            ResultSet rs=getStmt.executeQuery();
            if(rs.next()) {
                String data=rs.getString(1);
                return data;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("failed to get data",e);
        }
        return null;
    }

    private void doCommit() {
        if(pendingCommits) {
            try {
                con.commit();
            } catch (SQLException e) {
                throw new IllegalStateException("failed to commit data",e);
            }
            pendingCommits=false;
        }
        
    }
    @Override
    public Iterator<String> iterator() {
        String sql="SELECT NAME,DATA FROM gb;";
        try {
            doCommit();
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery(sql);
            return new Iterator<String>() {
                private boolean next=false;
                private boolean nextDone=false;
                @Override
                public boolean hasNext() {
                    try {
                        if(!nextDone) {
                            next=rs.next();
                            nextDone=true;
                        }
                    } catch (SQLException e) {
                        throw new IllegalStateException("failed to test for next key",e);
                    }
                    return next;
                }

                @Override
                public String next() {
                    try {
                        if(!nextDone) {
                            next=rs.next();
                        }
                        if(!next) {
                            throw new java.util.NoSuchElementException("no next key");
                        }
                        nextDone=false;
                        lastIteratorKey=rs.getString(1);
                        lastIteratorValue=rs.getString(2);
                        return lastIteratorKey;
                    } catch (SQLException e) {
                        throw new IllegalStateException("failed to get next key",e);
                    }
                }
            };
        } catch (SQLException e) {
            throw new IllegalStateException("failed to create iterator",e);
        }
    }
    public void close() {
        if(con!=null) {
            try {
                con.close();
            } catch (SQLException e) {
                throw new IllegalStateException("failed to close db",e);
            }
            deleteDbFile(dbfile);
            con=null;
        }
    }
    private void deleteDbFile(java.io.File dbfile) {
        java.io.File file=new java.io.File(dbfile+".mv.db");
        file.delete();
    }
    public long getTotalKeySize() {
        return totalKeySize;
    }
    public long getTotalValueSize() {
        return totalValueSize;
    }
}
