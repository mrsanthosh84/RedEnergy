package com.redenergy.csvreader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;

public class CSVReader implements Reader {

	private final Logger logger = LoggerFactory.getLogger(CSVReader.class);

	private File csvFile;

	public CSVReader() {
	}

	public void setFile(File file) {
		this.csvFile = file;
	}

	/**
	 * Read all lines from csv.
	 *
	 * @return all lines as String array.
	 */
	public List<String[]> readLines() throws Exception {

		if (isNull(csvFile)) {
			throw new Exception("Input csv file cant be null");
		}
		logger.info("Started reading the csv file '{}'", csvFile.getName());
		try {
			au.com.bytecode.opencsv.CSVReader reader = new au.com.bytecode.opencsv.CSVReader(new FileReader(csvFile));
			return reader.readAll();
		} catch (IOException e) {
			throw new Exception("Exception reading the csvfile ", e);
		}
	}

}
