package com.gentics.mesh.core.data.model.impl;

import static com.gentics.mesh.core.data.model.relationship.MeshRelationships.HAS_ROOT_NODE;
import static com.gentics.mesh.core.data.model.relationship.MeshRelationships.HAS_SCHEMA_ROOT;
import static com.gentics.mesh.core.data.model.relationship.MeshRelationships.HAS_TAGFAMILY_ROOT;

import com.gentics.mesh.core.data.model.MeshAuthUser;
import com.gentics.mesh.core.data.model.Project;
import com.gentics.mesh.core.data.model.generic.AbstractGenericNode;
import com.gentics.mesh.core.data.model.node.MeshNode;
import com.gentics.mesh.core.data.model.node.impl.MeshNodeImpl;
import com.gentics.mesh.core.data.model.root.SchemaRoot;
import com.gentics.mesh.core.data.model.root.TagFamilyRoot;
import com.gentics.mesh.core.data.model.root.impl.SchemaRootImpl;
import com.gentics.mesh.core.data.model.root.impl.TagFamilyRootImpl;
import com.gentics.mesh.core.rest.project.response.ProjectResponse;

public class ProjectImpl extends AbstractGenericNode implements Project {

	// TODO index to name + unique constraint
	public String getName() {
		return getProperty("name");
	}

	public void setName(String name) {
		setProperty("name", name);
	}

	public TagFamilyRoot getTagFamilyRoot() {
		return out(HAS_TAGFAMILY_ROOT).has(TagFamilyRootImpl.class).nextOrDefaultExplicit(TagFamilyRootImpl.class, null);
	}

	public void setTagFamilyRoot(TagFamilyRoot root) {
		outE(HAS_TAGFAMILY_ROOT).removeAll();
		linkOut((TagFamilyRootImpl) root, HAS_TAGFAMILY_ROOT);
	}

	public TagFamilyRoot createTagFamilyRoot() {
		TagFamilyRootImpl root = getGraph().addFramedVertex(TagFamilyRootImpl.class);
		setTagFamilyRoot(root);
		return root;
	}

	public SchemaRoot getSchemaRoot() {
		return out(HAS_SCHEMA_ROOT).has(SchemaRootImpl.class).nextOrDefault(SchemaRootImpl.class, null);
	}

	public void setSchemaRoot(SchemaRoot schemaRoot) {
		linkOut(schemaRoot.getImpl(), HAS_SCHEMA_ROOT);
	}

	public MeshNode getRootNode() {
		return out(HAS_ROOT_NODE).has(MeshNodeImpl.class).nextOrDefault(MeshNodeImpl.class, null);
	}

	public void setRootNode(MeshNode rootNode) {
		linkOut((MeshNodeImpl) rootNode, HAS_ROOT_NODE);
	}

	public ProjectResponse transformToRest(MeshAuthUser user) {
		ProjectResponse projectResponse = new ProjectResponse();
		projectResponse.setUuid(getUuid());
		projectResponse.setName(getName());
		projectResponse.setPermissions(user.getPermissionNames(this));

		// if (rootNode != null) {
		// projectResponse.setRootNodeUuid(rootNode.getUuid());
		// } else {
		// log.info("Inconsistency detected. Project {" + project.getUuid() + "} has no root node.");
		// }
		// return projectResponse;
		return null;
	}

	public MeshNode getOrCreateRootNode() {
		MeshNode rootNode = getRootNode();
		if (rootNode == null) {
			rootNode = getGraph().addFramedVertex(MeshNodeImpl.class);
			setRootNode(rootNode);
		}
		return rootNode;

	}

	public void delete() {
		// TODO handle this correctly
		getVertex().remove();
	}

	@Override
	public ProjectImpl getImpl() {
		return this;
	}

}
