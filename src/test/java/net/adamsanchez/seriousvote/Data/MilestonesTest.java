package net.adamsanchez.seriousvote.Data;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Adam Sanchez on 4/15/2018.
 */
public class MilestonesTest {
    @Test
    public void getRemainingDays() throws Exception {
        assertEquals(1, Milestones.getRemainingDays(6));
        assertEquals(1, Milestones.getRemainingDays(27));
        assertEquals(3, Milestones.getRemainingDays(4));
        assertEquals(6, Milestones.getRemainingDays(1));
        assertEquals(7, Milestones.getRemainingDays(0));
        assertEquals(7, Milestones.getRemainingDays(7));
        assertEquals(3, Milestones.getRemainingDays(18));
        assertEquals(3, Milestones.getRemainingDays(67));
        assertEquals(6, Milestones.getRemainingDays(365));
        assertEquals(1, Milestones.getRemainingDays(364));
        assertEquals(7, Milestones.getRemainingDays(427));
        assertEquals(3, Milestones.getRemainingDays(865));
        assertEquals(3, Milestones.getRemainingDays(67));
        assertEquals(1, Milestones.getRemainingDays(29));
    }

}