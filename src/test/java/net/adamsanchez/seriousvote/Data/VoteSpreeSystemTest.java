package net.adamsanchez.seriousvote.Data;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Adam Sanchez on 4/15/2018.
 */
public class VoteSpreeSystemTest {
    @Test
    public void getRemainingDays() throws Exception {
        assertEquals(1, VoteSpreeSystem.getRemainingDays(6));
        assertEquals(1, VoteSpreeSystem.getRemainingDays(27));
        assertEquals(3, VoteSpreeSystem.getRemainingDays(4));
        assertEquals(6, VoteSpreeSystem.getRemainingDays(1));
        assertEquals(7, VoteSpreeSystem.getRemainingDays(0));
        assertEquals(7, VoteSpreeSystem.getRemainingDays(7));
        assertEquals(3, VoteSpreeSystem.getRemainingDays(18));
        assertEquals(3, VoteSpreeSystem.getRemainingDays(67));
        assertEquals(6, VoteSpreeSystem.getRemainingDays(365));
        assertEquals(1, VoteSpreeSystem.getRemainingDays(364));
        assertEquals(7, VoteSpreeSystem.getRemainingDays(427));
        assertEquals(3, VoteSpreeSystem.getRemainingDays(865));
        assertEquals(3, VoteSpreeSystem.getRemainingDays(67));
        assertEquals(1, VoteSpreeSystem.getRemainingDays(29));
    }

}