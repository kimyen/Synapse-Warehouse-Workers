package org.sagebionetworks.warehouse.workers.log;

import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.common.util.progress.ProgressCallback;
import org.sagebionetworks.warehouse.workers.model.LogRecord;

public class AmazonLoggerImplTest {
	@Mock
	S3Logger mockS3Logger;
	@Mock
	CloudWatchLogger mockCloudWatchLogger;
	@Mock
	ProgressCallback<Integer> mockProgressCallback;
	
	ArgumentCaptor<LogRecord> argCaptor;
	AmazonLoggerImpl logger;
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		this.logger = new AmazonLoggerImpl(mockS3Logger, mockCloudWatchLogger);
		argCaptor = ArgumentCaptor.forClass(LogRecord.class);
	}

	@Test
	public void testLog() {
		Exception e = new Exception("test");
		Integer toCallback = 3;
		logger.logNonRetryableError(mockProgressCallback, toCallback, "workerName", e);
		verify(mockProgressCallback).progressMade(toCallback);
		verify(mockS3Logger).log(eq(mockProgressCallback), eq(toCallback), argCaptor.capture());
		LogRecord s3Input = argCaptor.getValue();
		verify(mockCloudWatchLogger).log(eq(mockProgressCallback), eq(toCallback), argCaptor.capture());
		LogRecord cloudWatchInput = argCaptor.getValue();
		assertEquals(s3Input, cloudWatchInput);
		assertEquals("workerName", s3Input.getClassName());
		assertEquals(e, s3Input.getThrowable());
		assertNotNull(s3Input.getTimestamp());
	}

}
