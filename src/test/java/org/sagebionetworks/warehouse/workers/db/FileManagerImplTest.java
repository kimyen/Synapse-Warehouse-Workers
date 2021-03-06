package org.sagebionetworks.warehouse.workers.db;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import org.sagebionetworks.aws.utils.s3.KeyGeneratorUtil;
import org.sagebionetworks.warehouse.workers.bucket.BucketTopicPublisher;
import org.sagebionetworks.warehouse.workers.log.AmazonLogger;
import org.sagebionetworks.warehouse.workers.model.FileState;
import org.sagebionetworks.warehouse.workers.model.FolderState;
import org.sagebionetworks.warehouse.workers.model.FileState.State;
import org.sagebionetworks.common.util.progress.ProgressCallback;

import com.amazonaws.services.s3.model.S3ObjectSummary;

public class FileManagerImplTest {
	@Mock
	FolderMetadataDao mockFolderMetadataDao;
	@Mock
	FileMetadataDao mockFileMetadataDao;
	@Mock
	BucketTopicPublisher mockBucketToTopicManager;
	@Mock
	ProgressCallback<Void> mockCallback;
	@Mock
	AmazonLogger mockAmazonLogger;
	
	S3ObjectSummary rollingOne;
	S3ObjectSummary rollingTwo;
	
	S3ObjectSummary fileOne;
	S3ObjectSummary fileTwo;
	S3ObjectSummary badKeyFile;
	
	FileManagerImpl manger;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		manger = new FileManagerImpl(mockFolderMetadataDao, mockFileMetadataDao,
				mockBucketToTopicManager, mockAmazonLogger);
		
		rollingOne = new S3ObjectSummary();
		rollingOne.setBucketName("bucketone");
		rollingOne.setKey(KeyGeneratorUtil.createNewKey(123, 1, true));
		
		rollingTwo = new S3ObjectSummary();
		rollingTwo.setBucketName("buckettwo");
		rollingTwo.setKey(KeyGeneratorUtil.createNewKey(456, 1, true));
		
		fileOne = new S3ObjectSummary();
		fileOne.setBucketName("bucketthree");
		fileOne.setKey(KeyGeneratorUtil.createNewKey(789, 1, false));
		
		fileTwo = new S3ObjectSummary();
		fileTwo.setBucketName("bucketfour");
		fileTwo.setKey(KeyGeneratorUtil.createNewKey(999, 1, false));
		
		badKeyFile = new S3ObjectSummary();
		badKeyFile.setBucketName("bucketfour");
		badKeyFile.setKey("this is a bad key");
	}
	
	@Test
	public void testAddRollingFilesSamePath(){
		List<S3ObjectSummary> list = Arrays.asList(rollingOne, rollingOne);
		// call under test
		manger.addS3Objects(list.iterator(), mockCallback);
		// progress should be made for each file.
		verify(mockCallback, times(2)).progressMade(null);
		// Even though there are two files they have the same path so onl one call should be made.
		verify(mockFolderMetadataDao, times(1)).createOrUpdateFolderState(new FolderState(rollingOne.getBucketName(), "000000123/1970-01-01", FolderState.State.ROLLING, new Timestamp(1)));
	}

	@Test
	public void testAddRollingFilesMixedPath(){
		List<S3ObjectSummary> list = Arrays.asList(rollingOne, rollingTwo);
		// call under test
		manger.addS3Objects(list.iterator(), mockCallback);
		// progress should be made for each file.
		verify(mockCallback, times(2)).progressMade(null);
		// both paths should be marked as rolling.
		verify(mockFolderMetadataDao, times(1)).createOrUpdateFolderState(new FolderState(rollingOne.getBucketName(), "000000123/1970-01-01", FolderState.State.ROLLING, new Timestamp(1)));
		verify(mockFolderMetadataDao, times(1)).createOrUpdateFolderState(new FolderState(rollingTwo.getBucketName(), "000000456/1970-01-01", FolderState.State.ROLLING, new Timestamp(1)));
	}
	
	@Test
	public void testAddFileUnknown(){
		List<S3ObjectSummary> list = Arrays.asList(fileOne);
		// unknown state for a new file.
		FileState stateOne = new FileState();
		stateOne.setState(State.UNKNOWN);
		when(mockFileMetadataDao.getFileState(fileOne.getBucketName(), fileOne.getKey())).thenReturn(stateOne);
		// call under test
		manger.addS3Objects(list.iterator(), mockCallback);
		// progress should be made for each file.
		verify(mockCallback, times(1)).progressMade(null);
		verify(mockFileMetadataDao).setFileState(fileOne.getBucketName(), fileOne.getKey(), State.SUBMITTED);
		verify(mockBucketToTopicManager).publishS3ObjectToTopic(fileOne.getBucketName(), fileOne.getKey());
	}
	
	@Test
	public void testAddFileSubmitted(){
		List<S3ObjectSummary> list = Arrays.asList(fileOne);
		// submitted state for an exiting file.
		FileState stateOne = new FileState();
		stateOne.setState(State.SUBMITTED);
		when(mockFileMetadataDao.getFileState(fileOne.getBucketName(), fileOne.getKey())).thenReturn(stateOne);
		// call under test
		manger.addS3Objects(list.iterator(), mockCallback);
		// progress should be made for each file.
		verify(mockCallback, times(1)).progressMade(null);
		verify(mockFileMetadataDao, never()).setFileState(fileOne.getBucketName(), fileOne.getKey(), State.SUBMITTED);
		verify(mockBucketToTopicManager, never()).publishS3ObjectToTopic(fileOne.getBucketName(), fileOne.getKey());
	}

	@Test
	public void testAddFileWithBadKey(){
		List<S3ObjectSummary> list = Arrays.asList(fileOne, badKeyFile);
		// unknown state for a new file.
		FileState stateOne = new FileState();
		stateOne.setState(State.UNKNOWN);
		when(mockFileMetadataDao.getFileState(fileOne.getBucketName(), fileOne.getKey())).thenReturn(stateOne);
		// call under test
		manger.addS3Objects(list.iterator(), mockCallback);
		// progress should be made for each file.
		verify(mockCallback, times(2)).progressMade(null);
		verify(mockFileMetadataDao, never()).getFileState(badKeyFile.getBucketName(), badKeyFile.getKey());
		verify(mockFileMetadataDao).getFileState(fileOne.getBucketName(), fileOne.getKey());
		verify(mockFolderMetadataDao, never()).createOrUpdateFolderState((FolderState) any());
	}
	
	@Test
	public void testLogger() {
		List<S3ObjectSummary> list = Arrays.asList(rollingOne);
		Exception e = new IllegalArgumentException();
		doThrow(e).when(mockFolderMetadataDao).createOrUpdateFolderState(any(FolderState.class));
		manger.addS3Objects(list.iterator(), mockCallback);
		verify(mockAmazonLogger).logNonRetryableError(eq(mockCallback),
				any(Void.class), eq("FileManagerImpl"), any(Throwable.class));
	}
	
	// WW-76
	@Test
	public void testUnknownKeyFormat() {
		rollingOne.setKey("fake-key");
		List<S3ObjectSummary> list = Arrays.asList(rollingOne);
		manger.addS3Objects(list.iterator(), mockCallback);
		verify(mockAmazonLogger, never()).logNonRetryableError(eq(mockCallback),
				any(Void.class), eq("FileManagerImpl"), any(Throwable.class));
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testValidateNull(){
		FileManagerImpl.validateObjectSummary(null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testValidateBucketNull(){
		fileTwo.setBucketName(null);
		FileManagerImpl.validateObjectSummary(fileTwo);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testValidateKeyNull(){
		fileTwo.setKey(null);
		FileManagerImpl.validateObjectSummary(fileTwo);
	}
	
	@Test
	public void testValidateKeyHappy(){
		FileManagerImpl.validateObjectSummary(fileTwo);
	}
}
