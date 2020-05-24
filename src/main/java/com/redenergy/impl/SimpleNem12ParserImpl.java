package com.redenergy.impl;

import static java.lang.String.format;
import static java.util.Objects.isNull;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redenergy.EnergyUnit;
import com.redenergy.MeterRead;
import com.redenergy.MeterVolume;
import com.redenergy.Quality;
import com.redenergy.SimpleNem12Parser;
import com.redenergy.csvreader.CSVReader;
import com.redenergy.csvreader.Reader;


/**
 * @author manickas
 *
 */
public class SimpleNem12ParserImpl implements SimpleNem12Parser {
	
	private final Logger logger = LoggerFactory.getLogger(SimpleNem12ParserImpl.class);
	private static final String DDMMYYY= "yyyyMMdd";
	private static final String CONST_200= "200";
	private static final String CONST_900= "900";
	private static final String CONST_300= "300";
	private static final String CONST_100= "100";
    private Reader csvReader;
    
    public SimpleNem12ParserImpl() {
    	csvReader = new CSVReader();
    }

    /**
     * Parses Simple NEM12 file.
     *
     * @param simpleNem12File file in Simple NEM12 format
     * @return Collection of <code>MeterRead</code> that represents the data in the given file.
     */
    @Override
    public Collection<MeterRead> parseSimpleNem12(File simpleNem12File) {
    	List<MeterRead> meterReads = new ArrayList<MeterRead>();
    	try {
			csvReader.setFile(simpleNem12File);
	    	List<String[]> records = csvReader.readLines();
	    	if(records!= null && records.size() > 0) {
	    		validateDataInTheCsv(records);
	            records.forEach(record -> {
	                try {
	                    parserLine(record, meterReads);
	                } catch (Exception e) {
	                	logger.error("Exception thrown when parsing the record {}", record, e);
	                }
	            });
	    	}
    	} 
    	catch(Exception e) {
    		logger.error("Exception thrown when parsing the record {}", e.getMessage());
    	}
		return meterReads;
    }

    /**
     * validate data in the csv
     *
     * @param records
     * @throws Exception
     */
    private void validateDataInTheCsv(List<String[]> records) throws Exception {
        startingRecordShouldBe100(records);
        endingRecordShouldBe900(records);

    }

    /**
     * verify of the end of the csv is 900
     *
     * @param records - input the csv data
     * @throws Exception
     */
    private void endingRecordShouldBe900(List<String[]> records) throws Exception {
        Optional<String[]> last = records.stream().reduce((first, second) -> second);
        compareValues(last, CONST_900, "last");
    }

    /**
     * verify if the start of csv is 100
     *
     * @param records - input the csv data
     * @throws Exception
     */
    private void startingRecordShouldBe100(List<String[]> records) throws Exception {
        Optional<String[]> first = records.stream().findFirst();
        compareValues(first, CONST_100, "first");
    }

    /**
     * Compare values provided as input
     *
     * @param line
     * @param value
     * @throws Exception
     */
    private void compareValues(Optional<String[]> line, String value, String logMessage) throws Exception {
        if (line.isPresent() && !value.equals(line.get()[0])) {
            throw new Exception(format("RecordType %s must be the %s line in the file", value, logMessage));
        }
    }

    /**
     * Parse the records and read meter read data
     *
     * @param record     - input records
     * @param meterReads - list to add meter reads
     * @throws Exception
     */
    private void parserLine(String[] record, List<MeterRead> meterReads) throws Exception {
        String firstColumn = record[0].trim();
        //if 100 or 900 as start of line then return
        if (CONST_100.equals(firstColumn) || CONST_900.equals(firstColumn)) {
            return;
        }
        //RecordType 200 represents the start of a meter read block
        addMeterReadBlockIfStartsWith200(record, meterReads, firstColumn);
        //Add MeterVolume to meter reads
        addMeterVolumeToMeterReadsIfStartWith300(record, meterReads, firstColumn);
    }

    /**
     * Add meter read volume data to meter reads
     *
     * @param recordType  - input record
     * @param meterReads  - list of meter reads
     * @param firstColumn - first column of the record
     * @throws Exception
     */
    private void addMeterVolumeToMeterReadsIfStartWith300(String[] recordType, List<MeterRead> meterReads, String firstColumn) throws Exception {
        if (CONST_300.equals(firstColumn)) {
        	String quality  = recordType[3];
        	if (isNull(quality)) {
                throw new Exception("Quality is null");
            }
        	if (quality.equalsIgnoreCase(Quality.A.name()) && quality.equalsIgnoreCase(Quality.E.name())) {
        		throw new Exception(format("Quality value '%s' is not valid,value should be either 'A' or 'E'", quality));
        	}
            //Get the last element from meter reads list and add the meter volume
            MeterRead meterRead = meterReads.get(meterReads.size() - 1);
            MeterVolume meterVolume = new MeterVolume(BigDecimal.valueOf(Double.parseDouble(recordType[2])), Quality.valueOf(quality));
            meterRead.appendVolume(parseDate(recordType[1]), meterVolume);
        }
    }

    /**
     * Add meter read data to meterreads list
     *
     * @param record      - input records
     * @param meterReads  - list of meter reads
     * @param firstColumn - first column of the record.
     * @throws Exception
     */
    private void addMeterReadBlockIfStartsWith200(String[] record, List<MeterRead> meterReads, String firstColumn) throws Exception {
        if (CONST_200.equals(firstColumn)) {
            MeterRead meterRead = createMeterRead(record);
            meterReads.add(meterRead);
        }
    }

    /**
     * Parse the meter volume date to local date
     *
     * @param date - input date
     * @return - LocalDate object
     * @throws Exception
     */
    private LocalDate parseDate(String date) throws Exception {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DDMMYYY);
            formatter = formatter.withLocale(Locale.ENGLISH);
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new Exception(format("Not able to parse date %s", date));
        }
    }

    /**
     * Create Meter read record
     *
     * @param record
     * @return - created meter read
     * @throws Exception
     */
    private MeterRead createMeterRead(String[] record) throws Exception {
    	String nmi = record[1];
    	String energyUnit = record[2];
    	if (isNull(nmi)) {
            throw new Exception("Input NMI is null");
        }
        if (nmi.length() != 10) {
            throw new Exception(format("NMI '%s' length should be 10", nmi));
        }
        if (isNull(energyUnit)) {
            throw new Exception("EnergyUnit is null");
        }
        MeterRead meterRead = new MeterRead(nmi, EnergyUnit.valueOf(energyUnit));
        return meterRead;
    }
   
}
