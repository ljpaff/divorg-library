package org.chinafa.multimedia.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;

/**
 * Created by e047425 on 9/04/17.
 */
public final class ConnectionPoolManager
{

    String databaseUrl = "jdbc:mysql://"+System.getProperty("database.hostname")+":3306/MyVideos107?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Madrid";
    String userName = "divorg";
    String password = "VDboMMaArueX3056";

    Vector connectionPool = new Vector();

    public ConnectionPoolManager()
    {
        initialize();
    }

    public ConnectionPoolManager(
            //String databaseName,
            String databaseUrl,
            String userName,
            String password
    )
    {
        this.databaseUrl = databaseUrl;
        this.userName = userName;
        this.password = password;
        initialize();
    }

    private void initialize()
    {
        //Here we can initialize all the information that we need
        initializeConnectionPool();
    }

    private void initializeConnectionPool()
    {
        while(!checkIfConnectionPoolIsFull())
        {
            System.out.println("Connection Pool is NOT full. Proceeding with adding new connections");
            //Adding new connection instance until the pool is full
            connectionPool.addElement(createNewConnectionForPool());
        }
        System.out.println("Connection Pool is full.");
    }

    private synchronized boolean checkIfConnectionPoolIsFull()
    {
        final int MAX_POOL_SIZE = 2;

        //Check if the pool size
        if(connectionPool.size() < MAX_POOL_SIZE)
        {
            return false;
        }

        return true;
    }

    private synchronized boolean checkIfConnectionPoolIsEmpty()
    {
        //Check if the pool size
        if(connectionPool.size() == 0)
        {
            return true;
        }

        return false;
    }

    @Override
    protected void finalize() throws Throwable {
        while(!checkIfConnectionPoolIsEmpty()) {
            Connection connection = (Connection) connectionPool.firstElement();
            if (connection.isClosed()) {
                connectionPool.removeElementAt(0);
            } else {
                connection.close();
                connectionPool.removeElementAt(0);
            }
        }
    }

    //Creating a connection
    private Connection createNewConnectionForPool()
    {
        Connection connection = null;

        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(databaseUrl, userName, password);
            System.out.println("Connection: "+connection);
        }
        catch(SQLException sqle)
        {
            System.err.println("SQLException: "+sqle);
            return null;
        }
        catch(ClassNotFoundException cnfe)
        {
            System.err.println("ClassNotFoundException: "+cnfe);
            return null;
        }

        return connection;
    }

    public synchronized Connection getConnectionFromPool()
    {
        Connection connection = null;

        //Check if there is a connection available. There are times when all the connections in the pool may be used up
        if(connectionPool.size() > 0)
        {
            connection = (Connection) connectionPool.firstElement();
            connectionPool.removeElementAt(0);
        }
        //Giving away the connection from the connection pool
        return connection;
    }

    public synchronized void returnConnectionToPool(Connection connection)
    {
        //Adding the connection from the client back to the connection pool
        connectionPool.addElement(connection);
    }

}