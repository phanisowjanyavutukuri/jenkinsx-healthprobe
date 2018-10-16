package com.fms.carrier.job;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fms.carrier.constants.AppConstants;
import com.fms.carrier.dto.QueryTrackerDTO;
import com.fms.carrier.enums.CarrierName;
import com.fms.carrier.service.AclCrawlerService;
import com.fms.carrier.service.CarrierService;

@Component
public class AclCarrierJob {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AclCarrierJob.class);
	
	@Autowired
	private AclCrawlerService crawlerService;
	
	@Autowired
	private CarrierService carrierService;
	
	@Value("${scraping.frequency}")
	private int scrapingFrequency;
	
	@Scheduled(cron = "${cron.expression}")
	public void schedule(){
		LOGGER.info("******* SCHEDULER STARTED *******");
		final QueryTrackerDTO queryTrackerDTO = new QueryTrackerDTO(null,null,scrapingFrequency,0);
		LOGGER.info("Target queries count : {} ",queryTrackerDTO.getScrapingFrequencyCount());
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		Runnable runnable = () -> {
			LOGGER.info("Executed query count in last minute : {} : {} ", queryTrackerDTO.getExecutedQryCountPerMin(), new java.util.Date());
			queryTrackerDTO.setExecutedQryCountPerMin(0);
		};
		service.scheduleAtFixedRate(runnable, 0, AppConstants.TIMER_DURATION_IN_SECS, TimeUnit.SECONDS);
		
		crawlerService.crawlCarrierPage(CarrierName.ACL.toString(), carrierService.getInputDataset(CarrierName.ACL.toString()),queryTrackerDTO);
		
		service.shutdownNow();
	}
}
