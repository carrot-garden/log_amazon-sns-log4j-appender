package com.twitsprout.appender.sns;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;

public class SnsAsyncAppenderTest extends TestCase {

	private static final String SAMPLE_TOPIC_NAME = "log_topic";
	private static final String SAMPLE_TOPIC_ARN = "log_topic_arn";
	private static final String SAMPLE_SHORT_LOG_MESSAGE = "This is a short log message";

	private Logger logger;

	@Override
	public void setUp() {
		logger = Logger.getLogger(SnsAsyncAppenderTest.class);
	}

	private static class CreateTopicRequestArgumentMatcher implements
			IArgumentMatcher {
		private final CreateTopicRequest expected;

		public CreateTopicRequestArgumentMatcher(
				final CreateTopicRequest expected) {
			this.expected = expected;
		}

		public static CreateTopicRequest eqCreateTopicRequest(
				final CreateTopicRequest expected) {
			EasyMock.reportMatcher(new CreateTopicRequestArgumentMatcher(
					expected));
			return null;
		}

		public boolean matches(final Object argument) {
			if (argument == null)
				return false;
			if (!(argument instanceof CreateTopicRequest))
				return false;
			return expected.getName().equals(
					((CreateTopicRequest) argument).getName());
		}

		public void appendTo(final StringBuffer buffer) {
			buffer.append("eqCreateTopicRequest(")
					.append(expected.getClass().getName())
					.append(" with topicName \"").append(expected.getName())
					.append("\")");
		}
	}

	private static class PublishRequestArgumentMatcher implements
			IArgumentMatcher {
		private final PublishRequest expected;

		public PublishRequestArgumentMatcher(final PublishRequest expected) {
			this.expected = expected;
		}

		public static PublishRequest eqPublishRequest(
				final PublishRequest expected) {
			EasyMock.reportMatcher(new PublishRequestArgumentMatcher(expected));
			return null;
		}

		public boolean matches(final Object argument) {
			if (argument == null)
				return false;
			if (!(argument instanceof PublishRequest))
				return false;
			final PublishRequest req = (PublishRequest) argument;
			return expected.getTopicArn().equals(req.getTopicArn())
					&& expected.getMessage().equals(req.getMessage())
					&& expected.getSubject().equals(req.getSubject());
		}

		public void appendTo(final StringBuffer buffer) {
			buffer.append("eqPublishRequest(")
					.append(expected.getClass().getName())
					.append(" with message \"").append(expected.getMessage())
					.append("\")");
		}

	}

	public void testSnsAsyncAppender_constructor() {
		final AmazonSNSAsync mockSns = EasyMock
				.createMock(AmazonSNSAsync.class);
		EasyMock.expect(
				mockSns.createTopic(CreateTopicRequestArgumentMatcher
						.eqCreateTopicRequest(new CreateTopicRequest(
								SAMPLE_TOPIC_NAME)))).andReturn(
				new CreateTopicResult().withTopicArn(SAMPLE_TOPIC_ARN));
		EasyMock.replay(mockSns);

		final SnsAsyncAppender testAppender = new SnsAsyncAppender(mockSns,
				SAMPLE_TOPIC_NAME);
		Assert.assertEquals(SAMPLE_TOPIC_ARN, testAppender.getTopicArn());
	}

	public void testAppend() {
		final AmazonSNSAsync mockSns = EasyMock
				.createMock(AmazonSNSAsync.class);
		EasyMock.expect(
				mockSns.createTopic(CreateTopicRequestArgumentMatcher
						.eqCreateTopicRequest(new CreateTopicRequest(
								SAMPLE_TOPIC_NAME)))).andReturn(
				new CreateTopicResult().withTopicArn(SAMPLE_TOPIC_ARN));
		EasyMock.expect(
				mockSns.publishAsync(PublishRequestArgumentMatcher
						.eqPublishRequest(new PublishRequest(SAMPLE_TOPIC_ARN,
								SAMPLE_SHORT_LOG_MESSAGE, logger.getName()
										+ " log: FATAL")))).andReturn(null);
		EasyMock.replay(mockSns);

		final SnsAsyncAppender testAppender = new SnsAsyncAppender(mockSns,
				SAMPLE_TOPIC_NAME);
		final LoggingEvent loggingEvent = new LoggingEvent("", logger,
				Level.FATAL, SAMPLE_SHORT_LOG_MESSAGE, null);
		testAppender.append(loggingEvent);
	}

	public void test_Logger() {

	}

}
