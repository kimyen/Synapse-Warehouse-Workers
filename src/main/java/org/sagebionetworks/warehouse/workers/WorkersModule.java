package org.sagebionetworks.warehouse.workers;

import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.database.semaphore.CountingSemaphore;
import org.sagebionetworks.warehouse.workers.audit.UserActivityPerMonthWorkerConfigurationProvider;
import org.sagebionetworks.warehouse.workers.bucket.BucketInfo;
import org.sagebionetworks.warehouse.workers.bucket.BucketInfoList;
import org.sagebionetworks.warehouse.workers.bucket.BucketScanningConfigurationProvider;
import org.sagebionetworks.warehouse.workers.bucket.BucketTopicPublisher;
import org.sagebionetworks.warehouse.workers.bucket.BucketTopicPublisherImpl;
import org.sagebionetworks.warehouse.workers.bucket.RealTimeBucketListenerConfigurationProvider;
import org.sagebionetworks.warehouse.workers.bucket.RealtimeBucketListenerTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.bucket.TopicDaoProvider;
import org.sagebionetworks.warehouse.workers.bucket.TopicDaoProviderImpl;
import org.sagebionetworks.warehouse.workers.collate.CollateFolderConfigurationProvider;
import org.sagebionetworks.warehouse.workers.collate.CollateMessageQueue;
import org.sagebionetworks.warehouse.workers.collate.StreamResourceProvider;
import org.sagebionetworks.warehouse.workers.collate.StreamResourceProviderImpl;
import org.sagebionetworks.warehouse.workers.collate.FolderCollateWorker;
import org.sagebionetworks.warehouse.workers.collate.LockedFolderRunner;
import org.sagebionetworks.warehouse.workers.collate.PeriodicRollingFolderConfigurationProvider;
import org.sagebionetworks.warehouse.workers.collate.S3ObjectCollator;
import org.sagebionetworks.warehouse.workers.collate.S3ObjectCollatorImpl;
import org.sagebionetworks.warehouse.workers.config.Configuration;
import org.sagebionetworks.warehouse.workers.db.FileManager;
import org.sagebionetworks.warehouse.workers.db.FileManagerImpl;
import org.sagebionetworks.warehouse.workers.db.WarehouseWorkersStateDao;
import org.sagebionetworks.warehouse.workers.snapshot.AccessRecordConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.AccessRecordTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.AclSnapshotConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.AclSnapshotTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.BulkFileDownloadRecordConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.BulkFileDownloadRecordTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.BulkFileHandleDownloadRecordConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.BulkFileHandleDownloadRecordTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.CertifiedQuizQuestionRecordConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.CertifiedQuizQuestionRecordTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.CertifiedQuizRecordConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.CertifiedQuizRecordTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.DeletedNodeSnapshotConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.DeletedNodeSnapshotTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.FileDownloadRecordTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.FileDownloadRecordWorkerConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.FileHandleCopyRecordTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.FileHandleCopyRecordWorkerConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.FileHandleDownloadRecordTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.FileHandleDownloadRecordWorkerConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.FileHandleRecordConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.FileHandleRecordTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.NodeSnapshotConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.NodeSnapshotTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.ProcessAccessRecordConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.ProcessAccessRecordTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.TeamMemberSnapshotConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.TeamMemberSnapshotTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.TeamSnapshotConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.TeamSnapshotTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.UserActivityPerClientPerDayConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.UserActivityPerClientPerDayTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.UserGroupSnapshotConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.UserGroupSnapshotTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.UserProfileSnapshotConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.UserProfileSnapshotTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.VerificationSubmissionRecordConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.VerificationSubmissionRecordTopicBucketInfo;
import org.sagebionetworks.warehouse.workers.snapshot.VerificationSubmissionStateRecordConfigurationProvider;
import org.sagebionetworks.warehouse.workers.snapshot.VerificationSubmissionStateRecordTopicBucketInfo;
import org.sagebionetworks.workers.util.aws.message.MessageQueueConfiguration;
import org.sagebionetworks.workers.util.aws.message.MessageQueueImpl;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;


/**
 * Bindings for workers.
 * 
 */
public class WorkersModule extends AbstractModule {

	private static final int COLLATE_WORKER_QUEUE_MSG_VISIBILITY_TIMEOUT_SEC = 120;

	@Override
	protected void configure() {
		bind(BucketDaoProvider.class).to(BucketDaoProviderImpl.class);
		bind(TopicDaoProvider.class).to(TopicDaoProviderImpl.class);
		bind(BucketTopicPublisher.class).to(BucketTopicPublisherImpl.class);
		bind(FileManager.class).to(FileManagerImpl.class);
		bind(LockedFolderRunner.class).to(FolderCollateWorker.class);
		bind(StreamResourceProvider.class).to(StreamResourceProviderImpl.class);
		bind(S3ObjectCollator.class).to(S3ObjectCollatorImpl.class);
	}

	/**
	 * Configuration for all buckets that are to be collated.
	 * 
	 * @param config
	 * @return
	 */
	@Provides
	public BucketInfoList getBucketsToCollate(Configuration config) {
		List<BucketInfo> list = new LinkedList<BucketInfo>();
		// access record
		list.add(new BucketInfo()
				.withBucketName(
						config.getProperty("org.sagebionetworks.warehouse.worker.bucket.access.record"))
				.withTimestampColumnIndex(2));
		// object snapshots
		list.add(new BucketInfo()
				.withBucketName(
						config.getProperty("org.sagebionetworks.warehouse.worker.bucket.snapshot.record"))
				.withTimestampColumnIndex(0));
		return new BucketInfoList(list);
	}
	
	@Provides
	public RealtimeBucketListenerTopicBucketInfo getBucketListenerConfig(Configuration config){
		RealtimeBucketListenerTopicBucketInfo rtbls = new RealtimeBucketListenerTopicBucketInfo();
		rtbls.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.all.bucket.events"));
		rtbls.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.all.bucket.events"));
		return rtbls;
	}

	@Provides
	public AccessRecordTopicBucketInfo getAccessRecordConfig(Configuration config){
		AccessRecordTopicBucketInfo info = new AccessRecordTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.accessrecord.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.accessrecord.snapshot"));
		return info;
	}

	@Provides
	public ProcessAccessRecordTopicBucketInfo getProcessedAccessRecordConfig(Configuration config){
		ProcessAccessRecordTopicBucketInfo info = new ProcessAccessRecordTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.accessrecord.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.processaccessrecord.snapshot"));
		return info;
	}

	@Provides
	public UserActivityPerClientPerDayTopicBucketInfo getUserActivityPerClientPerDayConfig(Configuration config){
		UserActivityPerClientPerDayTopicBucketInfo info = new UserActivityPerClientPerDayTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.accessrecord.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.useractivityperclientperday.snapshot"));
		return info;
	}

	@Provides
	public NodeSnapshotTopicBucketInfo getNodeSnapshotConfig(Configuration config){
		NodeSnapshotTopicBucketInfo info = new NodeSnapshotTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.node.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.node.snapshot"));
		return info;
	}

	@Provides
	public TeamSnapshotTopicBucketInfo getTeamSnapshotConfig(Configuration config){
		TeamSnapshotTopicBucketInfo info = new TeamSnapshotTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.team.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.team.snapshot"));
		return info;
	}

	@Provides
	public TeamMemberSnapshotTopicBucketInfo getTeamMemberSnapshotConfig(Configuration config){
		TeamMemberSnapshotTopicBucketInfo info = new TeamMemberSnapshotTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.teammember.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.teammember.snapshot"));
		return info;
	}

	@Provides
	public UserProfileSnapshotTopicBucketInfo getUserProfileSnapshotConfig(Configuration config){
		UserProfileSnapshotTopicBucketInfo info = new UserProfileSnapshotTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.userprofile.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.userprofile.snapshot"));
		return info;
	}

	@Provides
	public AclSnapshotTopicBucketInfo getAclRecordSnapshotConfig(Configuration config){
		AclSnapshotTopicBucketInfo info = new AclSnapshotTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.aclrecord.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.aclrecord.snapshot"));
		return info;
	}

	@Provides
	public UserGroupSnapshotTopicBucketInfo getUserGroupSnapshotConfig(Configuration config){
		UserGroupSnapshotTopicBucketInfo info = new UserGroupSnapshotTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.usergroup.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.usergroup.snapshot"));
		return info;
	}

	@Provides
	public CertifiedQuizRecordTopicBucketInfo getCertifiedQuizRecordConfig(Configuration config){
		CertifiedQuizRecordTopicBucketInfo info = new CertifiedQuizRecordTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.certifiedquizrecord.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.certifiedquizrecord.snapshot"));
		return info;
	}

	@Provides
	public CertifiedQuizQuestionRecordTopicBucketInfo getCertifiedQuizQuestionRecordConfig(Configuration config){
		CertifiedQuizQuestionRecordTopicBucketInfo info = new CertifiedQuizQuestionRecordTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.certifiedquizrecord.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.certifiedquizquestionrecord.snapshot"));
		return info;
	}

	@Provides
	public VerificationSubmissionRecordTopicBucketInfo getVerificationSubmissionRecordConfig(Configuration config){
		VerificationSubmissionRecordTopicBucketInfo info = new VerificationSubmissionRecordTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.verificationsubmission.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.verificationsubmission.snapshot"));
		return info;
	}

	@Provides
	public VerificationSubmissionStateRecordTopicBucketInfo getVerificationSubmissionStateRecordConfig(Configuration config){
		VerificationSubmissionStateRecordTopicBucketInfo info = new VerificationSubmissionStateRecordTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.verificationsubmission.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.verificationsubmissionstate.snapshot"));
		return info;
	}

	@Provides
	public BulkFileDownloadRecordTopicBucketInfo getBulkFileDownloadRecordConfig(Configuration config){
		BulkFileDownloadRecordTopicBucketInfo info = new BulkFileDownloadRecordTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.bulkfiledownloadresponse.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.bulkfiledownloadresponse.snapshot"));
		return info;
	}

	@Provides
	public BulkFileHandleDownloadRecordTopicBucketInfo getBulkFileHandleDownloadRecordConfig(Configuration config){
		BulkFileHandleDownloadRecordTopicBucketInfo info = new BulkFileHandleDownloadRecordTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.bulkfilehandledownloadresponse.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.bulkfilehandledownloadresponse.snapshot"));
		return info;
	}

	@Provides
	public FileDownloadRecordTopicBucketInfo getFileDownloadRecordConfig(Configuration config){
		FileDownloadRecordTopicBucketInfo info = new FileDownloadRecordTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.filedownloadrecord.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.filedownloadrecord.snapshot"));
		return info;
	}

	@Provides
	public FileHandleDownloadRecordTopicBucketInfo getFileHandleDownloadRecordConfig(Configuration config){
		FileHandleDownloadRecordTopicBucketInfo info = new FileHandleDownloadRecordTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.filedownloadrecord.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.filedownloadrecord.snapshot"));
		return info;
	}

	@Provides
	public FileHandleCopyRecordTopicBucketInfo getFileHandleCopyRecordConfig(Configuration config){
		FileHandleCopyRecordTopicBucketInfo info = new FileHandleCopyRecordTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.filehandlecopyrecord.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.filehandlecopyrecord.snapshot"));
		return info;
	}

	@Provides
	public DeletedNodeSnapshotTopicBucketInfo getDeletedNodeSnapshotConfig(Configuration config){
		DeletedNodeSnapshotTopicBucketInfo info = new DeletedNodeSnapshotTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.deletednode.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.deletednode.snapshot"));
		return info;
	}

	@Provides
	public FileHandleRecordTopicBucketInfo getFileHandleRecordConfig(Configuration config){
		FileHandleRecordTopicBucketInfo info = new FileHandleRecordTopicBucketInfo();
		info.setTopicName(config.getProperty("org.sagebionetworks.warehouse.worker.topic.filehandle.snapshot"));
		info.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.queue.filehandle.snapshot"));
		return info;
	}

	/**
	 * This the binding for all workers stacks. To add a new worker stack to the the application
	 * its class must be added to this list.
	 * 
	 * @return
	 */
	@Provides
	public WorkerStackConfigurationProviderList getWorkerStackConfigurationProviderList(){
		WorkerStackConfigurationProviderList list = new WorkerStackConfigurationProviderList();
		list.add(RealTimeBucketListenerConfigurationProvider.class);
		list.add(BucketScanningConfigurationProvider.class);
		list.add(PeriodicRollingFolderConfigurationProvider.class);
		list.add(CollateFolderConfigurationProvider.class);
		list.add(AccessRecordConfigurationProvider.class);
		list.add(ProcessAccessRecordConfigurationProvider.class);
		list.add(NodeSnapshotConfigurationProvider.class);
		list.add(TeamSnapshotConfigurationProvider.class);
		list.add(TeamMemberSnapshotConfigurationProvider.class);
		list.add(UserProfileSnapshotConfigurationProvider.class);
		list.add(AclSnapshotConfigurationProvider.class);
		list.add(TablePartitionConfigurationProvider.class);
		list.add(HealthCheckConfigurationProvider.class);
		list.add(MaintenanceConfigurationProvider.class);
		list.add(UserGroupSnapshotConfigurationProvider.class);
		list.add(CertifiedQuizRecordConfigurationProvider.class);
		list.add(CertifiedQuizQuestionRecordConfigurationProvider.class);
		list.add(VerificationSubmissionRecordConfigurationProvider.class);
		list.add(VerificationSubmissionStateRecordConfigurationProvider.class);
		list.add(BulkFileDownloadRecordConfigurationProvider.class);
		list.add(BulkFileHandleDownloadRecordConfigurationProvider.class);
		list.add(UserActivityPerClientPerDayConfigurationProvider.class);
		list.add(UserActivityPerMonthWorkerConfigurationProvider.class);
		list.add(DeletedNodeSnapshotConfigurationProvider.class);
		list.add(FileHandleRecordConfigurationProvider.class);
		list.add(FileDownloadRecordWorkerConfigurationProvider.class);
		list.add(FileHandleDownloadRecordWorkerConfigurationProvider.class);
		list.add(FileHandleCopyRecordWorkerConfigurationProvider.class);
		return list;
	}
	
	@Provides
	public WorkerStackList buildWorkerStackList(Injector injetor, WorkerStackConfigurationProviderList providerList){
		WorkerStackList list = new WorkerStackList();
		// create each worker stack from the providers
		for(Class<? extends WorkerStackConfigurationProvider> providerClass: providerList.getList()){
			WorkerStackConfigurationProvider provider = injetor.getInstance(providerClass);
			list.add(new WorkerStackImpl(provider.getWorkerConfiguration()));
		}
		return list;
	}
	
	@Provides
	public SemaphoreGatedRunnerProvider createSemaphoreGatedRunnerProvider(CountingSemaphore semaphore){
		return new SemaphoreGatedRunnerProviderImpl(semaphore);
	}
	
	@Provides
	public CollateMessageQueue createCollateMessageQueue(AmazonSQSClient awsSQSClient, AmazonSNSClient awsSNSClient, Configuration config){
		MessageQueueConfiguration messageConfig = new MessageQueueConfiguration();
		messageConfig.setQueueName(config.getProperty("org.sagebionetworks.warehouse.worker.collate.worker.queue.name"));
		messageConfig.setDefaultMessageVisibilityTimeoutSec(COLLATE_WORKER_QUEUE_MSG_VISIBILITY_TIMEOUT_SEC);
		MessageQueueImpl queue = new MessageQueueImpl(awsSQSClient, awsSNSClient, messageConfig);
		return new CollateMessageQueue(queue);
	}

	@Provides
	public RunDuringMaintenanceStateGate getMaintainanceStateGate(WarehouseWorkersStateDao dao) {
		return new RunDuringMaintenanceStateGate(dao);
	}

	@Provides
	public RunDuringNormalStateGate getNormalStateGate(WarehouseWorkersStateDao dao) {
		return new RunDuringNormalStateGate(dao);
	}
}
