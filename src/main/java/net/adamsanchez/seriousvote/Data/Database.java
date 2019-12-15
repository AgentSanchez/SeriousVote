package net.adamsanchez.seriousvote.Data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.U;
import net.adamsanchez.seriousvote.Data.PlayerRecord;

import java.sql.*;
import java.util.*;
import java.sql.Date;

/**
 * Created by adam_ on 01/22/17.
 */
public class Database {
    private String host = "localhost";
    private String port = "3306";
    private String username = "root";
    private String password = "ohokay";
    private String dbname = "SeriousVote";
    private String dbType = "mysql";
    private String table_prefix = "SV";
    private String playerTable = "players";
    private String url = "jdbc:mysql://test.com:3306/testdata?useSSL=false";
    private int minIdleConnections = 5;
    private int maxActiveConnections = 10;
    private String timezoneFix = "&useUnicode=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    HikariConfig config = new HikariConfig();
    HikariDataSource ds;




    public Database(){

        SeriousVote sv = SeriousVote.getInstance();
        this.host = sv.databaseHostname;
        this.port = sv.databasePort;
        this.dbname = sv.databaseName;
        this.table_prefix = sv.databasePrefix;
        this.username = sv.databaseUsername;
        this.password = sv.databasePassword;
        try {
            this.minIdleConnections = Integer.parseInt(sv.minIdleConnections);
            this.maxActiveConnections = Integer.parseInt(sv.maxActiveConnections);
        } catch(Exception e) {
            U.info("Incorrect values given for connection pool, reverting to default");
            U.info("Max Active: " + maxActiveConnections);
            U.info("Min Idle: " + minIdleConnections);
        }
        playerTable = table_prefix + "players";

        url = "jdbc:mysql://"+ host + ":" + port + "/" + dbname + "?useSSL=false";

        config.setJdbcUrl(url + timezoneFix);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maxActiveConnections);
        config.setMinimumIdle(minIdleConnections);
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(1770000);
        config.setPoolName("SeriousVote-SQL");

        //Instantiate Pool
        ds = new HikariDataSource(config);
        U.info("Ready for connections");
    }

    public Database(String url, String username, String password){
        config.setJdbcUrl(url + timezoneFix);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(1770000);
        config.setPoolName("SeriousVote-SQL");

        ds = new HikariDataSource(config);
        U.info("Ready for connections");
    }






    /////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////


    public void shutdown(){
        ds.close();
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }


    public PreparedStatement preparedStatement(Connection con, String string){
        PreparedStatement statement = null;
        try{
            statement = con.prepareStatement(string);

        } catch (SQLException e) {
            U.error("Error in DB Connection");
        }
        return statement;
    }

    private ResultSet genericQuery(Connection con, String query){
        ResultSet results = null;
        try {
            results = con.createStatement().executeQuery(query);
        } catch (SQLException e) {
            U.error("Error running query!", e);
        }
        return results;
    }


    public ResultSet genericSelectQuery(Connection con, String table, String field, String value){
        String initial = "SELECT * FROM %s WHERE %s='%s'";
        ResultSet results = genericQuery(con, String.format(initial,table,field,value));
        return  results;
    }

    /**
     * Gets an ordered list of records starting at the nth record (offset)
     * @param con
     * @param table
     * @param orderByField
     * @param offset
     * @return
     */
    public ResultSet orderedSelectQuery(Connection con, String table, String orderByField, int offset){
        String initial = "SELECT * FROM %s ORDER BY %s DESC LIMIT 1 OFFSET %s";
        ResultSet results = genericQuery(con, String.format(initial,table,orderByField,offset));
        return  results;
    }



    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    public PlayerRecord getPlayer(String playerIdentifier){
        ResultSet results = null;
        try(Connection con = getConnection()){
             results = genericSelectQuery(con, playerTable, "player", playerIdentifier.toString());
            if(results.first()){
                int sequentialVotes = results.getInt("voteSpree");
                Date lastVote = results.getDate("lastVote");
                int totalVote = results.getInt("totalVotes");
                return new PlayerRecord(playerIdentifier, totalVote,sequentialVotes,lastVote);
            }
        } catch (SQLException e) {
            U.error("Trouble getting information from the database");
        }
        return null;
    }

    /**
     * Retrieves the nth row from the database if it exists
     * @param rank The n'th record
     * @return
     */
    public PlayerRecord getRecordByRank(int rank){
        ResultSet results = null;
        try(Connection con = getConnection()){

            results = orderedSelectQuery(con,playerTable, "totalVotes", rank);

            if(results.first()){
                int sequentialVotes = results.getInt("voteSpree");
                Date lastVote = results.getDate("lastVote");
                int totalVote = results.getInt("totalVotes");
                String playerIdentifier = results.getString("player");
                return new PlayerRecord(playerIdentifier, totalVote,sequentialVotes,lastVote);
            }
        } catch (SQLException e) {
            U.error("Trouble getting information from the database");
        }
        return null;
    }

    public void updatePlayer(PlayerRecord player){
        playerUpdateQuery(this.playerTable, player.getPlayerIdentifier(), player.getTotalVotes(), player.getVoteSpree(), player.getLastVote());
    }

    /**
     * Sets all player votes to 0
     */
    public void resetPlayers(){
        String query = String.format("UPDATE %s SET totalVotes = 0", playerTable);

        try(Connection con = getConnection()){
            PreparedStatement statement = preparedStatement(con, query);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePlayer(String playerIdentifier){
        String query = String.format("DELETE FROM %s WHERE player='%s';", playerTable, playerIdentifier);
         try(Connection con = getConnection()){
             PreparedStatement statement = preparedStatement(con,query);
             statement.execute();
         } catch (SQLException e) {
             e.printStackTrace();
         }
    }

    public void playerUpdateQuery(String table, String playerIdentifier, int totalVotes, int voteSpree, Date lastVote){
        String initial = "REPLACE INTO %s(player, totalVotes, voteSpree, lastVote) VALUES(?,?,?,?)";

        try(Connection con = getConnection()){
            PreparedStatement statement = preparedStatement(con, String.format(initial,table));
            statement.setString(1, playerIdentifier);
            statement.setInt(2, totalVotes);
            statement.setInt(3, voteSpree);
            statement.setDate(4, lastVote);
            statement.execute();
        } catch (SQLException e) {
            U.error("Error in trying to update player vote record!");
        }
    }

    public void createPlayerTable(){
        String table = String.format("CREATE TABLE IF NOT EXISTS %s(" +
                "player			VarChar(36) PRIMARY KEY," +
                "lastVote		DATE," +
                "totalVotes		INT," +
                "voteSpree		INT" +
                ")", playerTable);

        try(Connection con = getConnection()){
            con.createStatement().executeUpdate(table);
        } catch (SQLException e) {
            U.error("Error Creating SQL TABLE-- CHECK YOUR DATA CONFIG", e);
        }

    }

    /**
     * Returns the number of players in the table
     */
    public int getCount(){
        ResultSet results = null;
        int count = 0;
        String query = String.format("SELECT COUNT(*) FROM %s;",playerTable);
        try(Connection con = getConnection()){
            results = con.createStatement().executeQuery(query);
            while(results.next()){
                count = results.getInt(1);
                U.debug("Table has " + count + " players.");
                return count;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

}
