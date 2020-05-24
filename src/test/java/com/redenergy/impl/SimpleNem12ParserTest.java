/**
 * 
 */
package com.redenergy.impl;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import com.redenergy.EnergyUnit;
import com.redenergy.MeterRead;
import com.redenergy.MeterVolume;
import com.redenergy.Quality;
import com.redenergy.SimpleNem12Parser;

/**
 * @author manickas
 *
 */
public class SimpleNem12ParserTest {
	
	private SimpleNem12Parser simpleNem12Parser;

    @Before
    public void setUp() throws Exception {
        simpleNem12Parser = new SimpleNem12ParserImpl();
    }

    @Test
    public void shouldReadNem12File() throws Exception {
        assertNotNull(simpleNem12Parser.parseSimpleNem12(getFile()));
    }

    @Test
    public void shouldReturnMeterReadsWithValidData() throws Exception {
        Collection<MeterRead> meterReads = simpleNem12Parser.parseSimpleNem12(getFile());
        assertNotNull(meterReads);
        MeterRead firstMeterRead = meterReads.stream().findFirst().get();
        assertNotNull(firstMeterRead);
        assertEquals(firstMeterRead.getNmi(), "6123456789");
        assertEquals(firstMeterRead.getEnergyUnit(), EnergyUnit.KWH);
        SortedMap<LocalDate, MeterVolume> firstMeterReadVolumes = firstMeterRead.getVolumes();
        assertEquals(firstMeterReadVolumes.size(), 7);
        Iterator<Map.Entry<LocalDate, MeterVolume>> iterator = firstMeterReadVolumes.entrySet().iterator();
        assertVolumeData(iterator, 2016, 11, 13, -50.8, Quality.A);
        assertVolumeData(iterator, 2016, 11, 14, 23.96, Quality.A);
        assertVolumeData(iterator, 2016, 11, 15, 32.0, Quality.A);
        assertVolumeData(iterator, 2016, 11, 16, -33, Quality.A);
        assertVolumeData(iterator, 2016, 11, 17, 0, Quality.A);
        assertVolumeData(iterator, 2016, 11, 18, 0, Quality.E);
        assertVolumeData(iterator, 2016, 11, 19, -9, Quality.A);
        MeterRead secondMeterRead = meterReads.stream().reduce((first, second) -> second).get();
        assertNotNull(secondMeterRead);
        assertEquals(secondMeterRead.getNmi(), "6987654321");
        assertEquals(secondMeterRead.getEnergyUnit(), EnergyUnit.KWH);
        SortedMap<LocalDate, MeterVolume> secondMeterReadVolumes = secondMeterRead.getVolumes();
        assertEquals(secondMeterReadVolumes.size(), 6);
        Iterator<Map.Entry<LocalDate, MeterVolume>> secondMeterIterator = secondMeterReadVolumes.entrySet().iterator();
        assertVolumeData(secondMeterIterator, 2016, 12, 15, -3.8, Quality.A);
        assertVolumeData(secondMeterIterator, 2016, 12, 16, 0, Quality.A);
        assertVolumeData(secondMeterIterator, 2016, 12, 17, 3.0, Quality.E);
        assertVolumeData(secondMeterIterator, 2016, 12, 18, -12.8, Quality.A);
        assertVolumeData(secondMeterIterator, 2016, 12, 19, 23.43, Quality.E);
        assertVolumeData(secondMeterIterator, 2016, 12, 21, 4.5, Quality.A);
    }

    @SuppressWarnings("unused")
	private void assertVolumeData(Iterator<Map.Entry<LocalDate, MeterVolume>> iterator, int year, int month, int day, double volume, Quality quality) {
        Map.Entry<LocalDate, MeterVolume> next = iterator.next();
        assertEquals(LocalDate.of(year, month, day), next.getKey());

        assertEquals(new MeterVolume(BigDecimal.valueOf(volume), quality), next.getValue());
    }
    
	protected File getFile() throws FileNotFoundException {
        return ResourceUtils.getFile("classpath:SimpleNem12.csv");
    }

}
