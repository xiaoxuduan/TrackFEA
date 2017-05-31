package trackFEA;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class EquationSetTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGaussEliminate() throws Exception {
//		fail("Not yet implemented");
		double[][] a=
			{
				{0.2368, 0.2471, 0.2568, 1.2671},	
				{0.1968, 0.2071, 1.2168, 0.2271},	
				{0.1581, 1.1675, 0.1768, 0.1871},	
				{1.1161, 0.1254, 0.1397, 0.1490}	
			};
		double[][] b=
			{
					{1.8471},
					{1.7471},
					{1.6471},
					{1.5471},
			};
		double[][] x=EquationSet.gaussEliminate(a, b);
		for(int i=0; i<x.length; i++){
			System.out.println(x[i][0]);
		}
	}

}
