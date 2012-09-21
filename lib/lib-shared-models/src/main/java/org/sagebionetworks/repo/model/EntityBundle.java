package org.sagebionetworks.repo.model;

import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

/**
 * Low-level bundle to transport an Entity and related data objects between the 
 * Synapse platform and external clients.
 * 
 * @author bkng
 *
 */
public class EntityBundle implements JSONEntity {
	
	/**
	 * Masks for requesting what should be included in the bundle.
	 */
	public static int ENTITY 		      	= 0x1;
	public static int ANNOTATIONS	      	= 0x2;
	public static int PERMISSIONS	     	= 0x4;
	public static int ENTITY_PATH	      	= 0x8;
	public static int ENTITY_REFERENCEDBY 	= 0x10;
	public static int HAS_CHILDREN			= 0x20;
	public static int ACL					= 0x40;
	public static int USERS					= 0x80;
	public static int GROUPS				= 0x100;
	
	private static AutoGenFactory autoGenFactory = new AutoGenFactory();
	

	protected static final String JSON_ENTITY 			= "entity";
	protected static final String JSON_ENTITY_TYPE 		= "entityType";
	protected static final String JSON_ANNOTATIONS 		= "annotations";
	protected static final String JSON_PERMISSIONS 		= "permissions";
	protected static final String JSON_PATH				= "path";
	protected static final String JSON_REFERENCED_BY 	= "referencedBy";
	protected static final String JSON_HAS_CHILDREN 	= "hasChildren";
	protected static final String JSON_ACL 				= "accessControlList";
	protected static final String JSON_USERS 			= "users";
	protected static final String JSON_GROUPS 			= "groups";

	private Entity entity;
	private String entityType;
	private Annotations annotations;
	private UserEntityPermissions permissions;
	private EntityPath path;
	private PaginatedResults<EntityHeader> referencedBy;
	private Boolean hasChildren;
	private AccessControlList acl;
	private PaginatedResults<UserProfile> users;
	private PaginatedResults<UserGroup> groups;
	
	/**
	 * Create a new EntityBundle
	 */
	public EntityBundle() {}
	
	/**
	 * Create a new EntityBundle and initialize from a JSONObjectAdapter.
	 * 
	 * @param initializeFrom
	 * @throws JSONObjectAdapterException
	 */
	public EntityBundle(JSONObjectAdapter initializeFrom) throws JSONObjectAdapterException {
		this();
		initializeFromJSONObject(initializeFrom);
	}

	@Override
	public JSONObjectAdapter initializeFromJSONObject(
			JSONObjectAdapter toInitFrom) throws JSONObjectAdapterException {
		if (toInitFrom == null) {
            throw new IllegalArgumentException("org.sagebionetworks.schema.adapter.JSONObjectAdapter cannot be null");
        }	
		if (toInitFrom.has(JSON_ENTITY)) {
			entityType = toInitFrom.getString(JSON_ENTITY_TYPE);
			JSONObjectAdapter joa = (JSONObjectAdapter) toInitFrom.getJSONObject(JSON_ENTITY);
			entity = (Entity) autoGenFactory.newInstance(entityType);
			entity.initializeFromJSONObject(joa);
		}
		if (toInitFrom.has(JSON_ANNOTATIONS)) {
			JSONObjectAdapter joa = (JSONObjectAdapter) toInitFrom.getJSONObject(JSON_ANNOTATIONS);
			if (annotations == null)
				annotations = new Annotations();
			annotations.initializeFromJSONObject(joa);
		}
		if (toInitFrom.has(JSON_PERMISSIONS)) {
			JSONObjectAdapter joa = (JSONObjectAdapter) toInitFrom.getJSONObject(JSON_PERMISSIONS);
			if (permissions == null)
				permissions = (UserEntityPermissions) autoGenFactory.newInstance(UserEntityPermissions.class.getName());
			permissions.initializeFromJSONObject(joa);
		}
		if (toInitFrom.has(JSON_PATH)) {
			JSONObjectAdapter joa = (JSONObjectAdapter) toInitFrom.getJSONObject(JSON_PATH);
			if (path == null)
				path = (EntityPath) autoGenFactory.newInstance(EntityPath.class.getName());
			path.initializeFromJSONObject(joa);
		}
		if (toInitFrom.has(JSON_REFERENCED_BY)) {
			JSONObjectAdapter joa = (JSONObjectAdapter) toInitFrom.getJSONObject(JSON_REFERENCED_BY);
			if (referencedBy == null)
				referencedBy = new PaginatedResults<EntityHeader>(EntityHeader.class);
			referencedBy.initializeFromJSONObject(joa);
		}
		if (toInitFrom.has(JSON_HAS_CHILDREN)) {
			hasChildren = toInitFrom.getBoolean(JSON_HAS_CHILDREN);
		}
		if (toInitFrom.has(JSON_ACL)) {
			JSONObjectAdapter joa = (JSONObjectAdapter) toInitFrom.getJSONObject(JSON_ACL);
			if (acl == null)
				acl = (AccessControlList) autoGenFactory.newInstance(AccessControlList.class.getName());
			acl.initializeFromJSONObject(joa);
		}
		if (toInitFrom.has(JSON_USERS)) {
			JSONObjectAdapter joa = (JSONObjectAdapter) toInitFrom.getJSONObject(JSON_USERS);
			if (users == null)
				users = new PaginatedResults<UserProfile>(UserProfile.class);
			users.initializeFromJSONObject(joa);
		}
		if (toInitFrom.has(JSON_GROUPS)) {
			JSONObjectAdapter joa = (JSONObjectAdapter) toInitFrom.getJSONObject(JSON_GROUPS);
			if (groups == null)
				groups = new PaginatedResults<UserGroup>(UserGroup.class);
			groups.initializeFromJSONObject(joa);
		}
		return toInitFrom;
	}

	@Override
	public JSONObjectAdapter writeToJSONObject(JSONObjectAdapter writeTo)
			throws JSONObjectAdapterException {
		if (writeTo == null) {
		        throw new IllegalArgumentException("JSONObjectAdapter cannot be null");
		}
		if (entity != null) {
			JSONObjectAdapter joa = writeTo.createNew();
			entity.writeToJSONObject(joa);
			writeTo.put(JSON_ENTITY, joa);
			writeTo.put(JSON_ENTITY_TYPE, entityType);
		}
		if (annotations != null) {
			JSONObjectAdapter joa = writeTo.createNew();
			annotations.writeToJSONObject(joa);
			writeTo.put(JSON_ANNOTATIONS, joa);
		}
		if (permissions != null) {
			JSONObjectAdapter joa = writeTo.createNew();
			permissions.writeToJSONObject(joa);
			writeTo.put(JSON_PERMISSIONS, joa);
		}
		if (path != null) {
			JSONObjectAdapter joa = writeTo.createNew();
			path.writeToJSONObject(joa);
			writeTo.put(JSON_PATH, joa);
		}
		if (referencedBy != null) {
			JSONObjectAdapter joa = writeTo.createNew();
			referencedBy.writeToJSONObject(joa);
			writeTo.put(JSON_REFERENCED_BY, joa);
		}
		if (hasChildren != null) {
			writeTo.put(JSON_HAS_CHILDREN, hasChildren);
		}
		if (acl != null) {
			JSONObjectAdapter joa = writeTo.createNew();
			acl.writeToJSONObject(joa);
			writeTo.put(JSON_ACL, joa);
		}
		if (users != null) {
			JSONObjectAdapter joa = writeTo.createNew();
			users.writeToJSONObject(joa);
			writeTo.put(JSON_USERS, joa);
		}
		if (groups != null) {
			JSONObjectAdapter joa = writeTo.createNew();
			groups.writeToJSONObject(joa);
			writeTo.put(JSON_GROUPS, joa);
		}
		return writeTo;
	}

	@Override
	public String getJSONSchema() {
		// Auto-generated method stub
		return null;
	}

	/**
	 * Get the Entity in this bundle.
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Set the Entity in this bundle.
	 */
	public void setEntity(Entity entity) {
		this.entity = entity;
		String s = entity.getClass().toString();
		// trim "Class " from the above String
		entityType = s.substring(s.lastIndexOf(" ") + 1);
	}

	/**
	 * Get the Annotations for the Entity in this bundle.
	 */
	public Annotations getAnnotations() {
		return annotations;
	}

	/**
	 * Set the Annotations for this bundle. Should correspond to the Entity in
	 * the bundle.
	 */
	public void setAnnotations(Annotations annotations) {
		this.annotations = annotations;
	}

	/**
	 * Get the UserEntityPermissions in this bundle.
	 */
	public UserEntityPermissions getPermissions() {
		return permissions;
	}

	/**
	 * Set the UserEntityPermissions for this bundle. Should be the requesting
	 * user's permissions on the Entity in the bundle.
	 */
	public void setPermissions(UserEntityPermissions permissions) {
		this.permissions = permissions;
	}

	/**
	 * Get the hierarchical path to the Entity in this bundle.
	 */
	public EntityPath getPath() {
		return path;
	}

	/**
	 * Set the Path for this bundle. Should point to the Entity in the bundle.
	 */
	public void setPath(EntityPath path) {
		this.path = path;
	}

	/**
	 * Get the collection of names of Entities which reference the Entity in 
	 * this bundle.
	 */
	public PaginatedResults<EntityHeader> getReferencedBy() {
		return referencedBy;
	}

	/**
	 * Set the collection of names of referencing Entities in this bundle. 
	 * Should contain all Entities which reference the Entity in this bundle.
	 */
	public void setReferencedBy(PaginatedResults<EntityHeader> referencedBy) {
		this.referencedBy = referencedBy;
	}

	/**
	 * Does this entity have children?
	 */
	public Boolean getHasChildren() {
		return hasChildren;
	}

	/**
	 * Does this entity have children?
	 */
	public void setHasChildren(Boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	/**
	 * Get the AccessControlList for the Entity in this bundle.
	 */
	public AccessControlList getAccessControlList() {
		return acl;
	}

	/**
	 * Set the AccessControlList for this bundle. Should correspond to the
	 * Entity in this bundle.
	 */
	public void setAccessControlList(AccessControlList acl) {
		this.acl = acl;
	}

	/**
	 * Get a collection all of the UserProfiles in the repository.
	 */
	public PaginatedResults<UserProfile> getUsers() {
		return users;
	}

	/**
	 * Set a collection of UserProfiles in this bundle. Should contain all 
	 * UserProfiles in the repository.
	 */
	public void setUsers(PaginatedResults<UserProfile> users) {
		this.users = users;
	}

	/**
	 * Get a collection all of the UserGroups in the repository.
	 */
	public PaginatedResults<UserGroup> getGroups() {
		return groups;
	}

	/**
	 * Set a collection of UserGroups in this bundle. Should contain all 
	 * UserGroups in the repository.
	 */
	public void setGroups(PaginatedResults<UserGroup> groups) {
		this.groups = groups;
	}

	@Override
	public String toString() {
		if (entity == null)
			return "EntityBundle (empty)";
		StringBuilder sb = new StringBuilder();
		sb.append("EntityBundle (" + entity.getName() + ") contains [");
		if (entity != null)
			sb.append("ENTITY");
		if (annotations != null)
			sb.append("ANNOTATIONS, ");
		if (permissions != null)
			sb.append("PERMISSIONS, ");
		if (path != null)
			sb.append("ENTITY_PATH, ");
		if (referencedBy != null)
			sb.append("ENTITY_REFERENCEDBY, ");
		if (hasChildren != null)
			sb.append("HAS_CHILDREN, ");
		if (acl != null)
			sb.append("ACCESS_CONTROL_LIST, ");
		if (users != null)
			sb.append("USERS, ");
		if (groups != null)
			sb.append("GROUPS, ");
		if (sb.lastIndexOf(",") >= 0)
			sb.delete(sb.length()-2, sb.length());
		sb.append("]");
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((acl == null) ? 0 : acl.hashCode());
		result = prime * result
				+ ((annotations == null) ? 0 : annotations.hashCode());
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result
				+ ((entityType == null) ? 0 : entityType.hashCode());
		result = prime * result + ((groups == null) ? 0 : groups.hashCode());
		result = prime * result
				+ ((hasChildren == null) ? 0 : hasChildren.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result
				+ ((permissions == null) ? 0 : permissions.hashCode());
		result = prime * result
				+ ((referencedBy == null) ? 0 : referencedBy.hashCode());
		result = prime * result + ((users == null) ? 0 : users.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityBundle other = (EntityBundle) obj;
		if (acl == null) {
			if (other.acl != null)
				return false;
		} else if (!acl.equals(other.acl))
			return false;
		if (annotations == null) {
			if (other.annotations != null)
				return false;
		} else if (!annotations.equals(other.annotations))
			return false;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity))
			return false;
		if (entityType == null) {
			if (other.entityType != null)
				return false;
		} else if (!entityType.equals(other.entityType))
			return false;
		if (groups == null) {
			if (other.groups != null)
				return false;
		} else if (!groups.equals(other.groups))
			return false;
		if (hasChildren == null) {
			if (other.hasChildren != null)
				return false;
		} else if (!hasChildren.equals(other.hasChildren))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (permissions == null) {
			if (other.permissions != null)
				return false;
		} else if (!permissions.equals(other.permissions))
			return false;
		if (referencedBy == null) {
			if (other.referencedBy != null)
				return false;
		} else if (!referencedBy.equals(other.referencedBy))
			return false;
		if (users == null) {
			if (other.users != null)
				return false;
		} else if (!users.equals(other.users))
			return false;
		return true;
	}
}
