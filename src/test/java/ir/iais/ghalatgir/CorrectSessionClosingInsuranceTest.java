/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.iais.ghalatgir;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ashkan
 */
public class CorrectSessionClosingInsuranceTest {
    
    public CorrectSessionClosingInsuranceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    // 
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of main method, of class CorrectSessionClosingEnsurance.
     */
    @Test
    public void testMain() throws Exception {
        System.out.println("Testing the Session Handler...");
        String[] args = new String[3];
        // set your files here!
        args[0] = "/media/ashkan/B/pazhm/GhalatGir/GhalatGir/src/main/java/ir/iais/ghalatgir/test";
        args[1] = "/media/ashkan/B/pazhm/GhalatGir/GhalatGir/src/main/java/ir/iais/ghalatgir/HsModifyPanel.txt";
        args[2] = "/media/ashkan/B/pazhm/GhalatGir/GhalatGir/src/main/java/ir/iais/ghalatgir/hello";
        CorrectSessionClosingEnsurance.main(args);
        // TODO review the generated test code and remove the default call to fail.
    }
    
}
