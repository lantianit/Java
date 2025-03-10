import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import common.DBUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Study {
    
    private static final String URL = "";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    
    private static volatile DataSource dataSource = null;
    
    private static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (DBUtil.class) {
                if (dataSource == null) {
                    MysqlDataSource mysqlDataSource = new MysqlDataSource();//
                    mysqlDataSource.setURL(URL);
                    mysqlDataSource.setUser(USERNAME);
                    mysqlDataSource.setPassword(PASSWORD);
                    dataSource = mysqlDataSource;
                }
            }
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
    
    
    
}