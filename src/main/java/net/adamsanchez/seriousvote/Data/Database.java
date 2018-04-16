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
        playerTable = table_prefix + "players";

        url = "jdbc:mysql://"+ host + ":" + port + "/" + dbname + "?useSSL=false";

        config.setJdbcUrl(url + timezoneFix);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(20);
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
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(20);
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
        Connection connection = null;
        return ds.getConnection();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public Statement statement(){
        Statement statement = null;
        try{
            Connection con = getConnection();
            statement = con.createStatement();

        } catch (SQLException e) {
            U.error("Unable to connect --- ", e);
        }
        return statement;
    }

    public PreparedStatement preparedStatement(String string){
        PreparedStatement statement = null;
        try{
            statement = getConnection().prepareStatement(string);

        } catch (SQLException e) {
            U.error("Error in DB Connection");
        }
        return statement;
    }

    private ResultSet genericQuery(String query){
        ResultSet results = null;
        try {
            results = statement().executeQuery(query);
            results.getStatement().getConnection().close();
        } catch (SQLException e) {
            U.error("Error running query!", e);
        }
        return results;
    }


    public ResultSet genericSelectQuery(String table, String field, String value){
        String initial = "SELECT * FROM %s WHERE %s='%s'";
        ResultSet results = genericQuery(String.format(initial,table,field,value));
        return  results;
    }



    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    public PlayerRecord getPlayer(UUID uuid){
        ResultSet results = genericSelectQuery(playerTable, "player", uuid.toString());
        try {
            if(results.first()){
                int sequentialVotes = results.getInt("voteSpree");
                Date lastVote = results.getDate("lastVote");
                int totalVote = results.getInt("totalVotes");
                return new PlayerRecord(uuid, totalVote,sequentialVotes,lastVote);
            }
        } catch (SQLException e) {
            U.error("Trouble getting information from the database");
        }
        return null;
    }

    public void updatePlayer(PlayerRecord player){
        playerUpdateQuery(this.playerTable, player.uuid.toString(), player.totalVotes, player.voteSpree, player.lastVote);

    }

    public void playerUpdateQuery(String table, String uuid, int totalVotes, int voteSpree, Date lastVote){
        String initial = "REPLACE INTO %s(player, totalVotes, voteSpree, lastVote) VALUES(?,?,?,?)";

        try(PreparedStatement statement = preparedStatement(String.format(initial,table))){
            statement.setString(1, uuid);
            statement.setInt(2, totalVotes);
            statement.setInt(3, voteSpree);
            statement.setDate(4, lastVote);
            statement.execute();
            statement.getConnection().close();
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

        try {
            statement().executeUpdate(table);
            statement().getConnection().close();
        } catch (SQLException e) {
            U.error("Error Creating SQL TABLE-- CHECK YOUR DATA CONFIG", e);
        }

    }
}
