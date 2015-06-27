package com.gentics.mesh.core.data.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gentics.mesh.core.Page;
import com.gentics.mesh.core.data.model.MeshUser;
import com.gentics.mesh.core.data.model.Project;
import com.gentics.mesh.core.data.model.impl.ProjectImpl;
import com.gentics.mesh.core.data.model.root.ProjectRoot;
import com.gentics.mesh.core.data.model.root.impl.ProjectRootImpl;
import com.gentics.mesh.paging.PagingInfo;
import com.gentics.mesh.util.InvalidArgumentException;
import com.gentics.mesh.util.TraversalHelper;
import com.syncleus.ferma.traversals.VertexTraversal;

@Component
public class ProjectService extends AbstractMeshService {

	// private static Logger log = LoggerFactory.getLogger(ProjectService.class);

	@Autowired
	protected MeshUserService userService;

	public Project findByName(String projectName) {
		return fg.v().has("name", projectName).has(ProjectImpl.class).nextOrDefault(ProjectImpl.class, null);
	}

	public Project findByUUID(String uuid) {
		return fg.v().has("uuid", uuid).has(ProjectImpl.class).nextOrDefault(ProjectImpl.class, null);
	}

	public List<? extends Project> findAll() {
		return fg.v().has(ProjectImpl.class).toListExplicit(ProjectImpl.class);
	}

	public void deleteByName(String name) {
	}

	public Page<? extends Project> findAllVisible(MeshUser requestUser, PagingInfo pagingInfo) throws InvalidArgumentException {
		// @Query(value =
		// "MATCH (requestUser:User)-[:MEMBER_OF]->(group:Group)<-[:HAS_ROLE]-(role:Role)-[perm:HAS_PERMISSION]->(project:Project) where id(requestUser) = {0} and perm.`permissions-read` = true return project ORDER BY project.name",
		// countQuery =
		// "MATCH (requestUser:User)-[:MEMBER_OF]->(group:Group)<-[:HAS_ROLE]-(role:Role)-[perm:HAS_PERMISSION]->(project:Project) where id(requestUser) = {0} and perm.`permissions-read` = true return count(project)")
		// TODO check whether it is faster to use meshroot for starting the traversal
		VertexTraversal<?, ?, ?> traversal = fg.v().has(ProjectRootImpl.class);
		VertexTraversal<?, ?, ?> countTraversal = fg.v().has(ProjectRootImpl.class);

		return TraversalHelper.getPagedResult(traversal, countTraversal, pagingInfo, ProjectImpl.class);

	}

	public ProjectRoot findRoot() {
		return fg.v().has(ProjectRootImpl.class).nextOrDefault(ProjectRootImpl.class, null);
	}

}
