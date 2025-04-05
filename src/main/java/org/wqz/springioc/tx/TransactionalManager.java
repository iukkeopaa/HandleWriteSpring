package org.wqz.springioc.tx;

import java.sql.*;

/**
 * @Description:
 * @Author: wjh
 * @Date: 2025/4/5 下午4:50
 */
public class TransactionalManager {

    public  static ThreadLocal<Connection>  threadLocal =new ThreadLocal<Connection>();



    static {

        Connection connection=getConnection();
        threadLocal.set(connection);
    }
    private static Connection getConnection(){

        Connection connection =null;
        PreparedStatement preparedStatement =null;

        ResultSet resultSet=null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

             connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/db1");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return connection;
    }
}
