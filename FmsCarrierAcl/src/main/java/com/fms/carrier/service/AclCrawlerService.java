package com.fms.carrier.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Matcher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fms.carrier.constants.AppConstants;
import com.fms.carrier.dao.CarrierDAO;
import com.fms.carrier.dto.QueryTrackerDTO;
import com.fms.carrier.dto.ResultTableDTO;
import com.fms.carrier.dto.SourceTableDTO;
import com.fms.carrier.enums.StatusPattern;
import com.fms.carrier.util.AppUtils;


@Service
public class AclCrawlerService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AclCrawlerService.class);
	
	@Autowired
	private CarrierDAO carrierDAO;
	
	@Autowired
	private CarrierService carrierService;
	
	@Value("${carrier.url}")
	private String carrierUrl;
		
	/*
	 * Functionality to load,crawl the webpage and save status of container in output table
	 */
	public void crawlCarrierPage(String carrierCode, List<SourceTableDTO> inputList, QueryTrackerDTO queryTrackerDTO) {
		LOGGER.info("Started crawling for carrier: {}",carrierCode);
		
		String status = AppConstants.OK;
		Document page = null;
		String errorElement = "table table table table table table tbody tr td:nth-of-type(2) span.subheader";
		boolean isContainerStatus = false;
		int countOfContianersFound = 0; // Represents the containers that found
		String msg_template_for_invalidation = "Entered %s - %s is not valid";
		String logMsg = "";
		try {
			WebDriver driver = AppUtils.getDriverInstance(carrierUrl);
			LOGGER.info("Web driver instantiated for carrier: {}",carrierCode);
			if (driver.findElement(By.xpath("/html/body/div/div/a")).isDisplayed()) {
				driver.findElement(By.xpath("/html/body/div/div/a")).click();
			}
			LOGGER.info("Loaded webpage for carrier: {}",carrierCode);
			
			for (SourceTableDTO input : inputList) {
				isContainerStatus = false;
				status = AppConstants.OK;
				countOfContianersFound = 0;
				
				try {
					page = getPageDocument(driver, input.getBookingRef(),queryTrackerDTO);

					if (page.select(errorElement).size() > 0) {
						logMsg = String.format(msg_template_for_invalidation, "booking reference",input.getBookingRef());
						LOGGER.info(logMsg);
						page = getPageDocument(driver, input.getBlNumber(),queryTrackerDTO);
						
						if (page.select(errorElement).size() > 0) {
							logMsg = String.format(msg_template_for_invalidation, "bl number",input.getBlNumber());
							LOGGER.info(logMsg);
							if (AppUtils.isNotNullNdEmpty(input.getContainerno())) {
								String[] containernos = input.getContainerno().split(",");
								isContainerStatus = true;
								for (String containerno : containernos) {
									try {
										page = getPageDocument(driver, containerno,queryTrackerDTO);
										if (page.select(errorElement).size() > 0) {
											logMsg = String.format(msg_template_for_invalidation, "container number",containerno);
											LOGGER.info(logMsg);
										} else {
											++countOfContianersFound;
											LOGGER.info("Crawling started for container : {}",containerno);
											crawlContainer(driver, page, carrierCode, "", "", input,queryTrackerDTO);
											LOGGER.info("Crawling completed for container : {}",containerno);
										}
									} catch (Exception e) {
										LOGGER.error(e.getMessage());
										continue;
									}
								}
							}  else {
								status = AppConstants.ERROR;
							}
						} else {
							LOGGER.info("Crawling started for bl number : {}",input.getBlNumber());
							crawlContainer(driver, page, carrierCode, "", input.getBlNumber(),input,queryTrackerDTO);
							LOGGER.info("Crawling completed for bl number : {}",input.getBlNumber());
						}
					} else {
						LOGGER.info("Crawling started for booking reference : {}" , input.getBookingRef());
						crawlContainer(driver, page, carrierCode, input.getBookingRef(), input.getBlNumber(),input,queryTrackerDTO);
						LOGGER.info("Crawling completed for booking reference : {}",input.getBookingRef());

					}
					if (isContainerStatus) {
						if (countOfContianersFound == 0) {
							status = AppConstants.ERROR;
						}
					}
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
					status = AppConstants.ERROR;
				} finally {
					carrierDAO.updateInputProcessingTimeAndStatus(input.getCarrier(), input.getContainerno(),
							input.getBookingRef(), input.getBlNumber(), status, new Timestamp(new java.util.Date().getTime()));
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured :",e);
		} finally {
			LOGGER.info("Web Crawling Completed for carrier: {}",carrierCode);
			
		}
	}

	private Document getPageDocument(WebDriver driver, String input,QueryTrackerDTO queryTrackerDTO) {
		driver.findElement(By.cssSelector("form[name='track_cargo'] textarea")).clear();
		driver.findElement(By.cssSelector("form[name='track_cargo'] textarea")).sendKeys(input);
		//driver.findElement(By.cssSelector("form[name='track_cargo'] input[type='submit']")).click();
		driver = AppUtils.webQueryCaller(driver, By.cssSelector("form[name='track_cargo'] input[type='submit']"), queryTrackerDTO);
		return Jsoup.parse(driver.getPageSource());
	}

	private void crawlContainer(WebDriver driver, Document page, String carrierCode, String bookingRef, String blNumber,SourceTableDTO sourceTableDTO, QueryTrackerDTO queryTrackerDTO) throws Exception{
		String statusVoyage = page.select("table table table table table table table table:nth-of-type(1) tbody tr:nth-of-type(3) td:first-child").text().split(":")[1].trim();
		
		int noOfContainers = driver.findElements(By.cssSelector("table table table table table table table table")).size();
		ResultTableDTO resultTableDTO = null;
		
		for(int index=1; index<=noOfContainers; index++){
			
			driver = AppUtils.webQueryCaller(driver, By.cssSelector("table table table table table table table table:nth-of-type("+index+") tbody tr:nth-of-type(4) td:first-child input"), queryTrackerDTO);
			Document doc = Jsoup.parse(driver.getPageSource());
			
			String containerNo = doc.select("table table table table table table tbody tr td:nth-of-type(2) span.subheader").text().split(":")[1].trim();
			Elements data = doc.select("table table table table table table tbody tr td:nth-of-type(2) div.gray");
			for(Element div : data){
			     String text = div.text();
			     
			     if(!text.trim().equals("")){
			    	 for(StatusPattern pattern: StatusPattern.values()){
			    		 String[] dateFormats = {AppConstants.ACL_DATE_FORMAT};
			    		 Matcher m = pattern.getPattern().matcher(text);
			    		 
			    		 if(m.matches() && pattern.name().matches("P1|P2|P3")){
			    			 resultTableDTO = AppUtils.prepareResultTableDto(carrierCode, containerNo, bookingRef, blNumber, m.group(1),
			    					 AppUtils.convertStringToDate(m.group(4), dateFormats), 
			    					 m.group(3), m.group(2), statusVoyage);
			    			 carrierService.validateAndSaveResultTableDTO(resultTableDTO,sourceTableDTO.getMinDate());
			    			 break;
			    		 } else if(m.matches() && pattern.name().matches("P5|P6|P7")){
			    			 resultTableDTO = AppUtils.prepareResultTableDto(carrierCode, containerNo, bookingRef, blNumber, m.group(1),
			    					 AppUtils.convertStringToDate(m.group(3), dateFormats),
			    					 m.group(2), "", statusVoyage);
			    			 carrierService.validateAndSaveResultTableDTO(resultTableDTO,sourceTableDTO.getMinDate());
			    			 break;
			    		 } else if(m.matches() && pattern.name().matches("P4")){
			    			 resultTableDTO = AppUtils.prepareResultTableDto(carrierCode, containerNo, bookingRef, blNumber, m.group(1),
			    					 AppUtils.convertStringToDate(m.group(4), dateFormats),
			    					 m.group(2), m.group(3), statusVoyage);
			    			 carrierService.validateAndSaveResultTableDTO(resultTableDTO,sourceTableDTO.getMinDate());
			    			 break;
			    		 }
			    	 }
			     }
			}
			LOGGER.info("Status saved for container: {} ",containerNo);
			driver.navigate().back();
		}
	}
	
	public String append(String msg1, String msg2) {
		StringBuffer buffer = (new StringBuffer()).append(msg1).append(msg2);
		return buffer.toString();
	}
}
