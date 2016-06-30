package com.gentics.mesh.core.node;

import static com.gentics.mesh.demo.TestDataProvider.PROJECT_NAME;
import static com.gentics.mesh.test.performance.StopWatch.loggingStopWatch;
import static com.gentics.mesh.util.MeshAssert.assertSuccess;
import static com.gentics.mesh.util.MeshAssert.latchFor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gentics.mesh.core.AbstractSpringVerticle;
import com.gentics.mesh.core.rest.node.NodeCreateRequest;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.schema.SchemaReference;
import com.gentics.mesh.core.verticle.node.NodeVerticle;
import com.gentics.mesh.parameter.impl.PagingParameters;
import com.gentics.mesh.test.AbstractIsolatedRestVerticleTest;
import com.gentics.mesh.test.performance.StopWatchLogger;
import com.gentics.mesh.util.FieldUtil;

import io.vertx.core.Future;

public class NodeVerticlePerformanceTest extends AbstractIsolatedRestVerticleTest {

	@Autowired
	private NodeVerticle verticle;

	private StopWatchLogger logger = StopWatchLogger.logger(getClass());

	@Override
	public List<AbstractSpringVerticle> getAdditionalVertices() {
		List<AbstractSpringVerticle> list = new ArrayList<>();
		list.add(verticle);
		return list;
	}

	@Test
	public void testReadPerformance() {
		loggingStopWatch(logger, "node.read", 200, (step) -> {
			call(() -> getClient().findNodes(PROJECT_NAME, new PagingParameters().setPerPage(100)));
		});
	}

	@Test
	public void testCreatePerformance() throws Exception {
		String uuid = db.noTrx(() -> folder("news").getUuid());
		loggingStopWatch(logger, "node.create", 200, (step) -> {
			NodeCreateRequest request = new NodeCreateRequest();
			request.setSchema(new SchemaReference().setName("content"));
			request.setLanguage("en");
			request.getFields().put("title", FieldUtil.createStringField("some title"));
			request.getFields().put("name", FieldUtil.createStringField("some name"));
			request.getFields().put("filename", FieldUtil.createStringField("new-page_" + step + ".html"));
			request.getFields().put("content", FieldUtil.createStringField("Blessed mealtime again!"));
			request.setParentNodeUuid(uuid);
			call(() -> getClient().createNode(PROJECT_NAME, request));
		});
	}

}
