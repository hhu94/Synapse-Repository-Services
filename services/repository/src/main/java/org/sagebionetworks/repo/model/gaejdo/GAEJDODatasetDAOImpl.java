package org.sagebionetworks.repo.model.gaejdo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.sagebionetworks.repo.model.AnnotationDAO;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Dataset;
import org.sagebionetworks.repo.model.DatasetDAO;
import org.sagebionetworks.repo.model.DatastoreException;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class GAEJDODatasetDAOImpl implements DatasetDAO {
	
	private GAEJDOBaseDAOHelper<Dataset,GAEJDODataset> baseDAO = null;
	private GAEJDORevisableDAOHelper<Dataset,GAEJDODataset> revisableDAO = null;
	
	public GAEJDODatasetDAOImpl() {
		final GAEJDODatasetDAOImpl parent = this;
		revisableDAO = new GAEJDORevisableDAOHelper<Dataset,GAEJDODataset>() {
			public Class<GAEJDODataset> getJdoClass() {return GAEJDODataset.class;}
			public GAEJDODataset newJDO() {return parent.newJDO();}
			public Dataset newDTO() {return parent.newDTO();}
			public void copyToDto(GAEJDODataset gae, Dataset dto) {parent.copyToDto(gae, dto);}
			public void copyFromDto(Dataset dto, GAEJDODataset gae) {parent.copyFromDto(dto, gae);}
		};
		baseDAO = new GAEJDOBaseDAOHelper<Dataset,GAEJDODataset>() {
			public Class<GAEJDODataset> getJdoClass() {return GAEJDODataset.class;}
			public GAEJDODataset newJDO() {return parent.newJDO();}
			public Dataset newDTO() {return parent.newDTO();}
			public void copyToDto(GAEJDODataset gae, Dataset dto) {parent.copyToDto(gae, dto);}
			public void copyFromDto(Dataset dto, GAEJDODataset gae) {parent.copyFromDto(dto, gae);}
		};
	}
	
	public Dataset newDTO() {
		Dataset dto = new Dataset();
		return dto;
	}
	
	public GAEJDODataset newJDO() {
		GAEJDODataset jdo = new GAEJDODataset();
		GAEJDOAnnotations a = new GAEJDOAnnotations();
		jdo.setAnnotations(a);
		GAEJDORevision<GAEJDODataset> r = new GAEJDORevision<GAEJDODataset>();
		jdo.setRevision(r);
		return jdo;
	}
	
	public void copyToDto(GAEJDODataset gae, Dataset dto) {
		dto.setId(KeyFactory.keyToString(gae.getId()));
		dto.setName(gae.getName());
		dto.setDescription(gae.getDescription());
		dto.setCreator(gae.getCreator());
		dto.setCreationDate(gae.getCreationDate());
		dto.setStatus(gae.getStatus());
		dto.setReleaseDate(gae.getReleaseDate());
//		GAEJDORevision<GAEJDODataset> rev = gae.getRevision();
//		Version version = rev.getVersion();
//		String versionString = version.toString();
		dto.setVersion(gae.getRevision().getVersion().toString());
		Collection<String> layers = new HashSet<String>();
		for (Key l : gae.getLayers()) layers.add(KeyFactory.keyToString(l));
		dto.setLayers(layers);
	}
	
	// Note:  This method does NOT copy layers or revision info to the GAEJDO object,
	// those being done by the 'revise' method
	public void copyFromDto(Dataset dto, GAEJDODataset gae) {
		gae.setName(dto.getName());
		gae.setDescription(dto.getDescription());
		gae.setCreator(dto.getCreator());
		gae.setCreationDate(dto.getCreationDate());
		gae.setStatus(dto.getStatus());
		gae.setReleaseDate(dto.getReleaseDate());
	}
	
	public Collection<String> getPrimaryFields() {
		return Arrays.asList(new String[]{"name", "description", "creator", "status", "releaseDate", "version"});
	}

	public List<Dataset> getInRange(int start, int end) {
		return revisableDAO.getInRange(start, end);
	}
	
	public List<Dataset> getInRangeSortedByPrimaryField(int start, int end, String sortBy, boolean asc) {
		return revisableDAO.getInRangeSortedByPrimaryField(start, end, sortBy, asc);
	}
	
	public List<Dataset> getInRangeHavingPrimaryField(int start, int end, String attribute, Object value) {
		return revisableDAO.getInRangeHavingPrimaryField(start, end, attribute, value);
	}
	

	
	/**
	 * @param dataset an original (not revised) dataset
	 * @return the id of the newly created dataset
	 * @throws DatastoreException
	 */
	public String create(Dataset dataset) throws DatastoreException {
		PersistenceManager pm = PMF.get();	
		Transaction tx=null;
		try {
			 	tx=pm.currentTransaction();
				tx.begin();
				GAEJDODataset jdo = revisableDAO.create(pm, dataset);
				tx.commit();				
				return KeyFactory.keyToString(jdo.getId());
		} catch (Exception e) {
			throw new DatastoreException(e);
		} finally {
				if(tx.isActive()) {
					tx.rollback();
				}
				pm.close();
		}	
	}
	
	public Dataset get(String id) throws DatastoreException {
		return baseDAO.get(id);
	}

	public void delete(String id) throws DatastoreException {
		baseDAO.delete(id);
		
	}


	/**
	 * This updates the 'shallow' properties.  Neither Version nor deep properties change.
	 * @param dto non-null id is required
	 * @throws DatastoreException
	 */
	public void update(Dataset dto) throws DatastoreException {
		PersistenceManager pm = PMF.get();	
		Transaction tx=null;
		try {
		 	tx=pm.currentTransaction();
			tx.begin();
			revisableDAO.update(pm, dto);
			tx.commit();
		} catch (Exception e) {
			throw new DatastoreException(e);
		} finally {
			if(tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}	
	}
	
	/**
	 * Create a revision of the object specified by the 'id' and 'version' fields, having
	 * the shallow properties from the given 'revision', and the deep properties
	 * of the given 'version'.  The new revision will have the version given by the
	 * 'newVersion' parameter.
	 * 
	 * @param revision
	 * @param newVersion
	 * @param revisionDate
	 */
	public String revise(Dataset revision, Date revisionDate) throws DatastoreException {
		PersistenceManager pm = PMF.get();	
		Transaction tx=null;
		try {
		 	tx=pm.currentTransaction();
			tx.begin();
			GAEJDODataset newRevision = revisableDAO.revise(pm, revision, revisionDate);
			// now copy the 'deep' properties
			Key reviseeId = KeyFactory.stringToKey(revision.getId());
			GAEJDODataset revisee = (GAEJDODataset)pm.getObjectId(reviseeId);
			GAEJDOAnnotations a = GAEJDOAnnotations.clone(revisee.getAnnotations());
			newRevision.setAnnotations(a);
			newRevision.setLayers(new HashSet<Key>(revisee.getLayers()));
			pm.makePersistent(newRevision); // don't know if this is necessary
			tx.commit();
			return KeyFactory.keyToString(newRevision.getId());
		} catch (Exception e) {
			throw new DatastoreException(e);
		} finally {
			if(tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}	
	}
	
	public int getCount() throws DatastoreException {
		PersistenceManager pm = PMF.get();	
		try {
			int count = revisableDAO.getCount(pm);
			return count;
		} catch (Exception e) {
			throw new DatastoreException(e);
		} finally {
			pm.close();
		}	
	}
	
	/**
	 * 
	 * @param id the id of any revision of the object
	 * @return the latest version of the object
	 * @throws DatastoreException if no result
	 */
	public Dataset getLatest(String id) throws DatastoreException {
		PersistenceManager pm = PMF.get();	
		try {
			Dataset latest = revisableDAO.getLatest(pm, id);
			return latest;
		} catch (Exception e) {
			throw new DatastoreException(e);
		} finally {
			pm.close();
		}	
	}
	
	/**
	 * Get all versions of an object
	 * @param id
	 * @return all revisions of the given object
	 */
	public Collection<Dataset> getAllVersions(String id) throws DatastoreException {
		PersistenceManager pm = PMF.get();	
		try {
			Collection<Dataset> allVersions = revisableDAO.getAllVersions(pm, id);
			return allVersions;
		} catch (Exception e) {
			throw new DatastoreException(e);
		} finally {
			pm.close();
		}	
	}
	
	/**
	 * Deletes all revisions of a Dataset
	 * @param id the id of any version of a revision series
	 * @throws DatastoreException
	 */
	public void deleteAllVersions(String id) throws DatastoreException {
		PersistenceManager pm = PMF.get();		
		Transaction tx=null;
		try {
			 	tx=pm.currentTransaction();
				tx.begin();
				Key key = KeyFactory.stringToKey(id);
				Collection<GAEJDODataset> allVersions = revisableDAO.getAllVersions(pm, key);
				for (GAEJDODataset jdo : allVersions) {
					for (Key layerKey : jdo.getLayers()) {
						// may have to check whether it's a InputDataLayer or AnalysisResult
						GAEJDODatasetLayer layer = (GAEJDODatasetLayer)pm.getObjectById(GAEJDODatasetLayer.class, layerKey);
						pm.deletePersistent(layer);
					}
					pm.deletePersistent(jdo);
				}
				tx.commit();
		} finally {
				if(tx.isActive()) {
					tx.rollback();
				}
				pm.close();
		}	
	}
	

	/**
	 * @param id
	 * @return annotations for the given object of the given type
	 */
	public Annotations getAnnotations(String id) throws DatastoreException {
		Annotations ans = new Annotations();
		ans.setStringAnnotations(getStringAnnotationDAO().getAnnotations(id));
		ans.setNumberAnnotations(getNumberAnnotationDAO().getAnnotations(id));
		ans.setDateAnnotations(getDateAnnotationDAO().getAnnotations(id));
		return ans;
	}
	
	public AnnotationDAO<Dataset, String> getStringAnnotationDAO() {
		final GAEJDODatasetDAOImpl parent = this;
		return new GAEJDORevisableAnnotationDAOImpl<Dataset, GAEJDODataset, String>() {
			protected Class<? extends GAEJDOAnnotation<String>> getAnnotationClass() {return GAEJDOStringAnnotation.class;} 
			protected Class<String> getValueClass() {return String.class;}
			protected GAEJDOAnnotation<String> newAnnotation(String attribute, String value) {
				return new GAEJDOStringAnnotation(attribute, value);
			}
			protected String getCollectionName() {return "stringAnnotations";}
			protected Set<GAEJDOAnnotation<String>> getAnnotationSet(GAEJDOAnnotations annots) {
				return annots.getStringAnnotations();
			}
			public Dataset newDTO() {return parent.newDTO();}
			public GAEJDODataset newJDO() {return parent.newJDO();}
			public void copyToDto(GAEJDODataset jdo, Dataset dto) {parent.copyToDto(jdo, dto);}
			public void copyFromDto(Dataset dto, GAEJDODataset jdo) {parent.copyFromDto(dto, jdo);}
			protected Class<GAEJDODataset> getOwnerClass() {return GAEJDODataset.class;}
		};
	}
	
	public AnnotationDAO<Dataset, Number> getNumberAnnotationDAO()  {
		final GAEJDODatasetDAOImpl parent = this;
		return new GAEJDORevisableAnnotationDAOImpl<Dataset, GAEJDODataset, Number>() {
			protected Class<? extends GAEJDOAnnotation<Number>> getAnnotationClass() {return GAEJDONumberAnnotation.class;} 
			protected Class<Number> getValueClass() {return Number.class;}
			protected GAEJDOAnnotation<Number> newAnnotation(String attribute, Number value) {
				return new GAEJDONumberAnnotation(attribute, value);
			}
			protected String getCollectionName() {return "numberAnnotations";}
			protected Set<GAEJDOAnnotation<Number>> getAnnotationSet(GAEJDOAnnotations annots) {
				return annots.getNumberAnnotations();
			}
			public Dataset newDTO() {return parent.newDTO();}
			public GAEJDODataset newJDO() {return parent.newJDO();}
			public void copyToDto(GAEJDODataset jdo, Dataset dto) {parent.copyToDto(jdo, dto);}
			public void copyFromDto(Dataset dto, GAEJDODataset jdo) {parent.copyFromDto(dto, jdo);}
			protected Class<GAEJDODataset> getOwnerClass() {return GAEJDODataset.class;}
		};
	}
	
	public AnnotationDAO<Dataset, Date> getDateAnnotationDAO()  {
		final GAEJDODatasetDAOImpl parent = this;
		return new GAEJDORevisableAnnotationDAOImpl<Dataset, GAEJDODataset, Date>() {
			protected Class<? extends GAEJDOAnnotation<Date>> getAnnotationClass() {return GAEJDODateAnnotation.class;} 
			protected Class<Date> getValueClass() {return Date.class;}
			protected GAEJDOAnnotation<Date> newAnnotation(String attribute, Date value) {
				return new GAEJDODateAnnotation(attribute, value);
			}
			protected String getCollectionName() {return "dateAnnotations";}
			protected Set<GAEJDOAnnotation<Date>> getAnnotationSet(GAEJDOAnnotations annots) {
				return annots.getDateAnnotations();
			}
			public Dataset newDTO() {return parent.newDTO();}
			public GAEJDODataset newJDO() {return parent.newJDO();}
			public void copyToDto(GAEJDODataset jdo, Dataset dto) {parent.copyToDto(jdo, dto);}
			public void copyFromDto(Dataset dto, GAEJDODataset jdo) {parent.copyFromDto(dto, jdo);}
			protected Class<GAEJDODataset> getOwnerClass() {return GAEJDODataset.class;}
		};
	}



	public void addLayer(String datasetId, String layerId) {
		throw new RuntimeException("Not yet implemented");
	}
	
	public void removeLayer(String datasetId, String layerId) {
		throw new RuntimeException("Not yet implemented");
	}
	
	public Collection<String> getLayers(String datasetId) {
		throw new RuntimeException("Not yet implemented");
	}

}
