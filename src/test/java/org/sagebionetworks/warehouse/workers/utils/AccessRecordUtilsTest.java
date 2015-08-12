package org.sagebionetworks.warehouse.workers.utils;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sagebionetworks.repo.model.audit.AccessRecord;
import org.sagebionetworks.warehouse.workers.model.Client;
import org.sagebionetworks.warehouse.workers.model.ProcessedAccessRecord;
import org.sagebionetworks.warehouse.workers.utils.AccessRecordUtils;

public class AccessRecordUtilsTest {

	/*
	 * Client Tests
	 */
	@Test
	public void rClientTest() {
		assertEquals(Client.R, AccessRecordUtils.getClient("synapseRClient/1.3-0"));
	}

	@Test
	public void pythonClientTest() {
		assertEquals(Client.PYTHON, AccessRecordUtils.getClient("synapseclient/1.0.1 python-requests/2.1.0 CPython/2.7.3 Linux/3.2.0-54-virtual"));
	}

	@Test
	public void webClientTest() {
		assertEquals(Client.WEB, AccessRecordUtils.getClient("Synpase-Java-Client/64.0  Synapse-Web-Client/67.0"));
	}

	@Test
	public void javaClientTest() {
		assertEquals(Client.JAVA, AccessRecordUtils.getClient("Synpase-Java-Client/64.0"));
	}

	@Test
	public void commandLineClientTest() {
		assertEquals(Client.COMMAND_LINE,AccessRecordUtils. getClient("synapsecommandlineclient"));
	}

	@Test
	public void elbHealthCheckerClientTest() {
		assertEquals(Client.ELB_HEALTHCHECKER, AccessRecordUtils.getClient("ELB-HealthChecker/1.0"));
	}

	@Test
	public void unknownClientTest() {
		assertEquals(Client.UNKNOWN, AccessRecordUtils.getClient(""));
	}

	/*
	 * Entity ID Tests
	 */
	@Test
	public void entityIdWithPrefixTest() {
		Long entityId = 4623841L;
		assertEquals(entityId, AccessRecordUtils.getEntityId("/repo/v1/entity/syn4623841/bundle"));
	}

	@Test
	public void entityIdWithUperCasePrefixTest() {
		assertEquals((Long)4623841L, AccessRecordUtils.getEntityId("/repo/v1/entity/SYN4623841/bundle"));
	}

	@Test
	public void entityIdWithProperCasePrefixTest() {
		assertEquals((Long)4623841L, AccessRecordUtils.getEntityId("/repo/v1/entity/Syn4623841/bundle"));
	}

	@Test
	public void entityIdWithoutPrefixTest() {
		assertEquals((Long)4623841L, AccessRecordUtils.getEntityId("/repo/v1/entity/4623841/bundle"));
	}

	@Test
	public void endingEntityIdTest() {
		assertEquals((Long)4623841L, AccessRecordUtils.getEntityId("/repo/v1/entity/syn4623841"));
	}

	@Test
	public void nullEntityIdTest() {
		assertNull(AccessRecordUtils.getEntityId("/repo/v1/version"));
	}

	/*
	 * Synapse API Tests
	 */
	@Test
	public void urlWithoutParameters() {
		assertEquals("POST /certifiedusertestresponse", AccessRecordUtils.normalizeMethodSignature("/repo/v1/certifiedUserTestResponse", "POST"));
	}

	@Test
	public void urlWithSynId() {
		assertEquals("GET /entity/#/bundle", AccessRecordUtils.normalizeMethodSignature("/repo/v1/entity/syn1571204/bundle", "GET"));
	}

	@Test
	public void urlWithEvaluationSubmissionId() {
		assertEquals("GET /evaluation/submission/#/status", AccessRecordUtils.normalizeMethodSignature("/repo/v1/evaluation/submission/2813223/status", "GET"));
	}

	@Test
	public void urlWithEntityIdAndVersionNumber() {
		assertEquals("GET /entity/#/version/#/filepreview", AccessRecordUtils.normalizeMethodSignature("/repo/v1/entity/syn2785825/version/1/filepreview", "GET"));
	}

	@Test
	public void urlWithWiki2AndWikiVersionNumber() {
		assertEquals("PUT /evaluation/#/wiki2/#/#", AccessRecordUtils.normalizeMethodSignature("/repo/v1/evaluation/2785825/wiki2/2813234/2", "PUT"));
	}

	@Test
	public void urlWith4IdFields() {
		assertEquals("GET /entity/#/table/column/#/row/#/version/#/filepreview", AccessRecordUtils.normalizeMethodSignature("/repo/v1/entity/syn3456789/table/column/1/row/12/version/2/filepreview", "GET"));
	}

	@Test
	public void urlStartWithFileV1() {
		assertEquals("POST /createchunkedfileuploadchunkurl", AccessRecordUtils.normalizeMethodSignature("/file/v1/createChunkedFileUploadChunkURL", "POST"));
	}

	@Test
	public void urlStartWithAuthV1() {
		assertEquals("POST /session", AccessRecordUtils.normalizeMethodSignature("/auth/v1/session", "POST"));
	}

	/*
	 * processAccessRecord() Test
	 */
	@Test
	public void processAccessRecordTest() {
		AccessRecord ar = new AccessRecord();
		ar.setUserAgent("Synpase-Java-Client/64.0  Synapse-Web-Client/67.0");
		ar.setMethod("GET");
		ar.setRequestURL("/repo/v1/entity/syn2600225/descendants");
		ar.setSessionId("28a75682-f056-40f7-9a1e-416cb703bed5");

		ProcessedAccessRecord expected = new ProcessedAccessRecord();
		expected.setSessionId("28a75682-f056-40f7-9a1e-416cb703bed5");
		expected.setEntityId(2600225L);
		expected.setClient(Client.WEB);
		expected.setNormalizedMethodSignature("GET /entity/#/descendants");

		assertEquals(expected, AccessRecordUtils.processAccessRecord(ar));
	}

	/*
	 * isValidated() Test
	 */
	@Test
	public void validatedARTest() {
		AccessRecord ar = AccessRecordTestUtil.createValidatedAccessRecord();
		assertTrue(AccessRecordUtils.isValidated(ar));
	}

	@Test
	public void nullSessionIdTest() {
		AccessRecord ar = AccessRecordTestUtil.createValidatedAccessRecord();
		ar.setSessionId(null);
		assertFalse(AccessRecordUtils.isValidated(ar));
	}

	@Test
	public void nullElapseMsTest() {
		AccessRecord ar = AccessRecordTestUtil.createValidatedAccessRecord();
		ar.setElapseMS(null);
		assertFalse(AccessRecordUtils.isValidated(ar));
	}

	@Test
	public void nullTimestampTest() {
		AccessRecord ar = AccessRecordTestUtil.createValidatedAccessRecord();
		ar.setTimestamp(null);
		assertFalse(AccessRecordUtils.isValidated(ar));
	}

	@Test
	public void nullThreadIdTest() {
		AccessRecord ar = AccessRecordTestUtil.createValidatedAccessRecord();
		ar.setThreadId(null);
		assertFalse(AccessRecordUtils.isValidated(ar));
	}

	@Test
	public void nullRequestUrlTest() {
		AccessRecord ar = AccessRecordTestUtil.createValidatedAccessRecord();
		ar.setRequestURL(null);
		assertFalse(AccessRecordUtils.isValidated(ar));
	}

	@Test
	public void nullDateTest() {
		AccessRecord ar = AccessRecordTestUtil.createValidatedAccessRecord();
		ar.setDate(null);
		assertFalse(AccessRecordUtils.isValidated(ar));
	}

	@Test
	public void nullMethodTest() {
		AccessRecord ar = AccessRecordTestUtil.createValidatedAccessRecord();
		ar.setMethod(null);
		assertFalse(AccessRecordUtils.isValidated(ar));
	}

	@Test
	public void nullVmIdTest() {
		AccessRecord ar = AccessRecordTestUtil.createValidatedAccessRecord();
		ar.setVmId(null);
		assertFalse(AccessRecordUtils.isValidated(ar));
	}

	@Test
	public void nullInstanceTest() {
		AccessRecord ar = AccessRecordTestUtil.createValidatedAccessRecord();
		ar.setInstance(null);
		assertFalse(AccessRecordUtils.isValidated(ar));
	}

	@Test
	public void nullStackTest() {
		AccessRecord ar = AccessRecordTestUtil.createValidatedAccessRecord();
		ar.setStack(null);
		assertFalse(AccessRecordUtils.isValidated(ar));
	}

	@Test
	public void nullSuccessTest() {
		AccessRecord ar = AccessRecordTestUtil.createValidatedAccessRecord();
		ar.setSuccess(null);
		assertFalse(AccessRecordUtils.isValidated(ar));
	}

	@Test
	public void nullResponseStatusTest() {
		AccessRecord ar = AccessRecordTestUtil.createValidatedAccessRecord();
		ar.setResponseStatus(null);
		assertFalse(AccessRecordUtils.isValidated(ar));
	}
}
