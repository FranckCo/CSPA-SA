package fr.insee.cspa.sa.test;


import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;

public class DataBlockTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testDataBlock() {

		// creates a block of 1000 random numbers
		DataBlock block = new DataBlock(1000);
		block.randomize();

		// extracts a sub-datablock of 105 items (at position 1, 9, 17...)
		DataBlock subBlock = block.extract(1, 105, 8);
		DescriptiveStatistics all = new DescriptiveStatistics(subBlock);
		double max = all.getMax();
		double min = all.getMin();
		// creates extracts of 10 data (0, 10, 20...), (1, 11, 21, ...) (indexes in subBlock)
		// or (1, 81, 161...), (2, 82, 162...) (indexes in block) and computes statistics on them
		double sum1 = 0, ssq1 = 0;
		for (int i = 0; i < 10; ++i) {
			DataBlock extract = subBlock.extract(i, -1, 10);
			DescriptiveStatistics stats = new DescriptiveStatistics(extract);
			assertTrue(stats.getMax() <= max);
			assertTrue(stats.getMin() >= min);
			sum1 += stats.getSum();
			ssq1 += stats.getSumSquare();
			}
		assertTrue(Math.abs(subBlock.sum() - sum1)< 1e-12);
		assertTrue(Math.abs(subBlock.ssq() - ssq1)< 1e-12);

		// same problem in reverse order
		double sum2 = 0, ssq2 = 0;
		int n=0;
		for (int i = 0; i < 10; ++i) {
			DataBlock extract = subBlock.extract(subBlock.getLastIndex()-i, -1, -10);
			DescriptiveStatistics stats = new DescriptiveStatistics(extract);
			assertTrue(stats.getMax() <= max);
			assertTrue(stats.getMin() >= min);
			sum2 += stats.getSum();
			ssq2 += stats.getSumSquare();
			n+=extract.getLength();
			}
		assertTrue(Math.abs(subBlock.sum() - sum2)< 1e-12);
		assertTrue(Math.abs(subBlock.ssq() - ssq2)< 1e-12);
	}
}
