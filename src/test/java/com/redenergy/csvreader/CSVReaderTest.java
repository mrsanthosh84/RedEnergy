/**
 * 
 */
package com.redenergy.csvreader;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

/**
 * @author manickas
 *
 */
public class CSVReaderTest {
	private Reader csvReader;

	@Before
	public void setUp() throws Exception {
		csvReader = new CSVReader();
		csvReader.setFile(getFile());
	}

	@Test(expected = Exception.class)
	public void shouldThrowExceptionWhenInputFileIsNull() throws Exception {
		csvReader.setFile(null);
		csvReader.readLines();
	}

	@Test
	public void shouldReadFromCsvFile() throws Exception {
		assertNotNull(csvReader.readLines());
		assertEquals(csvReader.readLines().size(), 17);
	}
	
	protected File getFile() throws FileNotFoundException {
        return ResourceUtils.getFile("classpath:SimpleNem12.csv");
    }

}
