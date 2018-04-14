import net.adamsanchez.seriousvote.Data.Database;
import net.adamsanchez.seriousvote.Data.PlayerRecord;

import java.util.UUID;

/**
 * Created by Adam Sanchez on 4/4/2018.
 */
public class Test {

    public static void main(String [] args){
        String url = "jdbc:mysql://sql.seriousservers.net:3306/COT_SeriousVote?useSSL=false";
        Database database = new Database(url, "root", "anklebaldbedwhynook");
        PlayerRecord playerRecord = database.getPlayer(UUID.fromString("40f96278-5922-480f-b079-2efd6edb009c"));
        System.out.println(playerRecord.getTotalVotes());

    }
}
