package com.gentics.mesh.core.data.model;

import java.util.List;

import com.gentics.mesh.core.data.model.impl.TagFamilyImpl;

public interface TagFamily extends MeshVertex {

	String getName();

	void setName(String name);

	String getDescription();

	void setDescription(String description);

	Tag create(String name);

	void removeTag(Tag tag);

	void addTag(Tag tag);

	List<Tag> getTags();

	TagFamilyImpl getImpl();
}
