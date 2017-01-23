package net.adamsanchez.seriousvote;

import net.adamsanchez.seriousvote.Data.Database;

/**
 * Created by adam_ on 01/22/17.
 */
public class Milestones {

    //query database for person
    //Check for last day of voting
    //check number of sequential votes
    //If more than or equal to the milestone give reward
    Database db;
    public Milestones(){
        db = new Database();
        db.createPlayerTable();
    }

    //TODO Create Milestones Table
    //Each milestone table will have a random  selection
    //Each milestone table will have a set selection



}
