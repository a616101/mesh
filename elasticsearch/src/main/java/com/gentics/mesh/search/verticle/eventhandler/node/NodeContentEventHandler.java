package com.gentics.mesh.search.verticle.eventhandler.node;

import static com.gentics.mesh.core.rest.MeshEvent.NODE_CONTENT_CREATED;
import static com.gentics.mesh.core.rest.MeshEvent.NODE_CONTENT_DELETED;
import static com.gentics.mesh.core.rest.MeshEvent.NODE_PUBLISHED;
import static com.gentics.mesh.core.rest.MeshEvent.NODE_UNPUBLISHED;
import static com.gentics.mesh.core.rest.MeshEvent.NODE_UPDATED;
import static com.gentics.mesh.core.rest.event.EventCauseAction.SCHEMA_MIGRATION;
import static com.gentics.mesh.search.verticle.eventhandler.Util.requireType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gentics.mesh.cli.BootstrapInitializer;
import com.gentics.mesh.core.data.NodeGraphFieldContainer;
import com.gentics.mesh.core.data.schema.SchemaContainer;
import com.gentics.mesh.core.data.search.request.BulkRequest;
import com.gentics.mesh.core.data.search.request.CreateDocumentRequest;
import com.gentics.mesh.core.data.search.request.DeleteDocumentRequest;
import com.gentics.mesh.core.data.search.request.SearchRequest;
import com.gentics.mesh.core.rest.MeshEvent;
import com.gentics.mesh.core.rest.event.EventCauseInfo;
import com.gentics.mesh.core.rest.event.migration.SchemaMigrationMeshEventModel;
import com.gentics.mesh.core.rest.event.node.NodeMeshEventModel;
import com.gentics.mesh.core.rest.schema.SchemaReference;
import com.gentics.mesh.etc.config.MeshOptions;
import com.gentics.mesh.etc.config.search.ComplianceMode;
import com.gentics.mesh.graphdb.model.MeshElement;
import com.gentics.mesh.search.verticle.MessageEvent;
import com.gentics.mesh.search.verticle.entity.MeshEntities;
import com.gentics.mesh.search.verticle.eventhandler.EventCauseHelper;
import com.gentics.mesh.search.verticle.eventhandler.EventHandler;
import com.gentics.mesh.search.verticle.eventhandler.MeshHelper;
import com.gentics.mesh.search.verticle.eventhandler.Util;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

@Singleton
public class NodeContentEventHandler implements EventHandler {
	private final MeshHelper helper;
	private final MeshEntities entities;
	private final BootstrapInitializer boot;
	private final ComplianceMode complianceMode;

	@Inject
	public NodeContentEventHandler(MeshHelper helper, MeshEntities entities, BootstrapInitializer boot, MeshOptions options) {
		this.helper = helper;
		this.entities = entities;
		this.boot = boot;
		this.complianceMode = options.getSearchOptions().getComplianceMode();
	}

	@Override
	public Collection<MeshEvent> handledEvents() {
		return Arrays.asList(NODE_CONTENT_CREATED, NODE_UPDATED, NODE_CONTENT_DELETED, NODE_PUBLISHED, NODE_UNPUBLISHED);
	}

	@Override
	public Flowable<? extends SearchRequest> handle(MessageEvent messageEvent) {
		MeshEvent event = messageEvent.event;
		NodeMeshEventModel message = requireType(NodeMeshEventModel.class, messageEvent.message);

		switch (event) {
			case NODE_CONTENT_CREATED:
			case NODE_UPDATED:
			case NODE_PUBLISHED:
				EventCauseInfo cause = message.getCause();
				if (cause != null && cause.getAction() == SCHEMA_MIGRATION) {
					return migrationUpdate(message);
				} else {
					return upsertNodes(message).toFlowable();
				}
			case NODE_CONTENT_DELETED:
			case NODE_UNPUBLISHED:
				if (EventCauseHelper.isProjectDeleteCause(message)) {
					return Flowable.empty();
				} else {
					return getSchemaVersionUuid(message)
						.map(uuid -> deleteNodes(message, uuid))
						.toFlowable();
				}
			default:
				throw new RuntimeException("Unexpected event " + event.address);

		}
	}

	private Flowable<SearchRequest> migrationUpdate(NodeMeshEventModel message) {
		SchemaMigrationMeshEventModel cause = (SchemaMigrationMeshEventModel) message.getCause();
		return getSchemaVersionUuid(cause.getFromVersion())
			.map(uuid -> deleteNodes(message, uuid))
			.flatMapSingleElement(delete -> upsertNodes(message)
				// The requests are bulked together to make sure that these request are in the same bulk
				.<SearchRequest>map(req -> new BulkRequest(req, delete))
				.toSingle(delete))
			.toFlowable();
	}

	private Maybe<CreateDocumentRequest> upsertNodes(NodeMeshEventModel message) {
		return Maybe.zip(
			helper.getDb().singleTxImmediate(() -> entities.nodeContent.getDocument(message))
				.to(Util::toMaybe),
			getSchemaVersionUuid(message),
			(doc, schemaVersionUuid) -> helper.createDocumentRequest(
				getIndexName(message, schemaVersionUuid),
				NodeGraphFieldContainer.composeDocumentId(message.getUuid(), message.getLanguageTag()),
				doc, complianceMode)
		);
	}

	private DeleteDocumentRequest deleteNodes(NodeMeshEventModel message, String schemaVersionUuid) {
		return helper.deleteDocumentRequest(
			getIndexName(message, schemaVersionUuid), NodeGraphFieldContainer.composeDocumentId(message.getUuid(), message.getLanguageTag()),
			complianceMode);
	}

	private String getIndexName(NodeMeshEventModel message, String schemaVersionUuid) {
		return NodeGraphFieldContainer.composeIndexName(
			message.getProject().getUuid(),
			message.getBranchUuid(),
			schemaVersionUuid,
			message.getType());
	}

	private Maybe<String> getSchemaVersionUuid(NodeMeshEventModel message) {
		return helper.getDb().maybeTxImmediate(tx -> {
			SchemaContainer schema = boot.schemaContainerRoot().findByUuid(message.getSchema().getUuid());
			return Optional.ofNullable(boot.projectRoot().findByUuid(message.getProject().getUuid())
				.getBranchRoot().findByUuid(message.getBranchUuid())
				.findLatestSchemaVersion(schema))
				.map(MeshElement::getUuid)
				.orElse(null);
		});
	}

	private Maybe<String> getSchemaVersionUuid(SchemaReference reference) {
		return helper.getDb().maybeTxImmediate(tx -> Optional.ofNullable(boot.schemaContainerRoot()
			.findByUuid(reference.getUuid()))
			.map(schema -> schema.findVersionByRev(reference.getVersion()))
			.map(MeshElement::getUuid)
			.orElse(null));
	}
}
