package net.adamsanchez.seriousvote;

import javax.xml.crypto.Data;
import javax.xml.transform.Result;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * Created by adam_ on 01/22/17.
 */
public class Database {
    private String host = "localhost";
    private String port = "3306";
    private String username = "root";
    private String password = "password";
    private String dbType = "mySQL";
    private Connection db;
    private String table_prefix = "SV";
    private String playerTable = "players";




    public Database(){
        reconnect();
        terminateConnection();
        playerTable = table_prefix + "players";
    }





    /////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    public void reconnect(){
        db = getConnection();
    }

    public void terminateConnection(){
        try {
            db.close();
        } catch (SQLException e) {
            U.error("DB could not be closed...Maybe it's still in use?");
        }
    }

    public Connection getConnection(){
        Connection connection = null;
        U.info("Attempting to connect to the database...");
        try {
            connection = DriverManager.getConnection("jdbc:" + dbType + "://" + host + ":" + port + "/", username, password);
        } catch (SQLException e) {
            U.error("Failed to establish connection to the database");
        }
        return connection;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public Statement statement(){
        Statement statement = null;
        try{
            statement= db.createStatement();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }

    private ResultSet genericQuery(String query){
        ResultSet results = null;
        try {
            results = statement().executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public ResultSet genericSelectQuery(String table, String field, String value){
        String initial = "SELECT FROM %s WHERE %s='%s'";
        ResultSet results = genericQuery(String.format(initial,table,field,value));
        return  results;
    }
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    public void getPlayer(UUID uuid){
        genericSelectQuery(playerTable, "players", uuid.toString());


    }
}
