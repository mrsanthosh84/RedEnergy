/**
 * 
 */
package com.redenergy.csvreader;

import java.io.File;
import java.util.List;

/**
 * @author manickas
 *
 */
public interface Reader {

	/**
	 * Set the file to read.
	 * 
	 * @param file
	 */
	void setFile(File file);

	/**
	 * Read all lines from the file
	 * 
	 * @return
	 * @throws Exception
	 */
	List<String[]> readLines() throws Exception;

}
