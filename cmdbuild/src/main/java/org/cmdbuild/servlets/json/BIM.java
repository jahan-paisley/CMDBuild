package org.cmdbuild.servlets.json;

import static org.cmdbuild.bim.utils.BimConstants.isValidId;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.ATTRIBUTE;
import static org.cmdbuild.servlets.json.ComunicationConstants.BIM_LAYER;
import static org.cmdbuild.servlets.json.ComunicationConstants.BIM_PROJECTS;
import static org.cmdbuild.servlets.json.ComunicationConstants.BIM_ROOT;
import static org.cmdbuild.servlets.json.ComunicationConstants.CARD;
import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.CONTAINER;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.EXPORT;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILE_IFC;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.LIMIT;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.ROOT_REFERENCE;
import static org.cmdbuild.servlets.json.ComunicationConstants.START;
import static org.cmdbuild.servlets.json.ComunicationConstants.VALUE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.bim.LayerLogic.Layer;
import org.cmdbuild.logic.bim.project.ProjectLogic.Project;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.bim.DefaultBimDataView.BimCard;
import org.cmdbuild.servlets.json.serializers.ProjectSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class BIM extends JSONBaseWithSpringContext {

	private static class JsonProject implements Project {

		private String projectId, name, description, importMapping, exportMapping;
		private boolean active, synch;
		private DateTime lastCheckin;
		private final Iterable<String> cardBinding = Lists.newArrayList();
		private File fileToLoad;

		@Override
		public File getFile() {
			return fileToLoad;
		}

		@Override
		public String getProjectId() {
			return projectId;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public boolean isActive() {
			return active;
		}

		@Override
		public boolean isSynch() {
			return synch;
		}

		@Override
		public String getImportMapping() {
			return importMapping;
		}

		@Override
		public String getExportMapping() {
			return exportMapping;
		}

		@Override
		public DateTime getLastCheckin() {
			return lastCheckin;
		}

		@Override
		public Iterable<String> getCardBinding() {
			return cardBinding;
		}

		public void setActive(final boolean active) {
			this.active = active;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public void setDescription(final String description) {
			this.description = description;
		}

		public void setFileToLoad(final File file) {
			this.fileToLoad = file;
		}

		public void setProjectId(final String projectId) {
			this.projectId = projectId;
		}

		public void setSynch(final boolean b) {
			this.synch = b;
		}
	}

	@JSONExported
	public JSONObject read( //
			final @Parameter(value = START) int start, //
			final @Parameter(value = LIMIT) int limit //
	) throws JSONException, CMDBException {

		final Iterable<Project> projects = bimProjectLogic().readAllProjects();
		final JSONArray jsonProjects = ProjectSerializer.toClient(projects);
		final JSONObject response = new JSONObject();
		response.put(BIM_PROJECTS, jsonProjects);

		return response;
	}

	@JSONExported
	public JSONObject create( //
			final @Parameter(NAME) String name, //
			final @Parameter(DESCRIPTION) String description, //
			final @Parameter(ACTIVE) boolean active, //
			final @Parameter(value = FILE_IFC, required = false) FileItem fileIFC, //
			final @Parameter(value = CARD, required = false) String bindCard, //
			final Map<String, Object> attributes //
	) throws Exception {

		final JsonProject projectToCreate = new JsonProject();
		projectToCreate.setActive(active);
		projectToCreate.setName(name);
		projectToCreate.setDescription(description);
		projectToCreate.setFileToLoad(fileFromFileItem(fileIFC));
		projectToCreate.setSynch(false);
		if (!bindCard.isEmpty()) {
			((ArrayList<String>) projectToCreate.getCardBinding()).add(bindCard);
		}
		final String projectId = bimProjectLogic().createProject(projectToCreate).getProjectId();
		final JSONObject response = new JSONObject();
		response.put(ID, projectId);
		return response;
	}

	@JSONExported
	public void update( //
			final @Parameter(ID) String projectId, //
			final @Parameter(DESCRIPTION) String description, //
			final @Parameter(ACTIVE) boolean active, //
			final @Parameter(value = FILE_IFC, required = false) FileItem fileIFC, //
			final @Parameter(value = CARD, required = false) String bindCard //
	) throws Exception {

		final JsonProject projectToUpdate = new JsonProject();
		projectToUpdate.setActive(active);
		projectToUpdate.setDescription(description);
		projectToUpdate.setFileToLoad(fileFromFileItem(fileIFC));
		projectToUpdate.setProjectId(projectId);
		if (!bindCard.isEmpty()) {
			((ArrayList<String>) projectToUpdate.getCardBinding()).add(bindCard);
		}
		bimProjectLogic().updateProject(projectToUpdate);

	}

	@JSONExported
	public void enableProject( //
			final @Parameter(ID) String projectId //
	) {
		final JsonProject project = new JsonProject();
		project.setProjectId(projectId);
		bimProjectLogic().enableProject(project);
	}

	@JSONExported
	public void disableProject( //
			final @Parameter(ID) String projectId //
	) {
		final JsonProject project = new JsonProject();
		project.setProjectId(projectId);
		bimProjectLogic().disableProject(project);
	}

	@JSONExported
	public JSONObject getRoidForCardId( //
			final @Parameter("cardId") Long cardId, //
			final @Parameter("className") String className, //
			final @Parameter("withExport") boolean withExport//
	) throws JSONException {
		if (withExport) {
			return getExportedRevisionIdForViewer(cardId, className);
		} else {
			return getBaseRevisionIdForViewer(cardId, className);
		}
	}

	private JSONObject getExportedRevisionIdForViewer(final Long cardId, final String className) throws JSONException {

		final String rootDescription = viewerLogic().getDescriptionOfRoot(cardId, className);

		final String baseProjectId = viewerLogic().getBaseProjectId(cardId, className);

		final String outputRevisionId = viewerLogic().getExportedRevisionIdForViewer(cardId, className);

		return createResponse(rootDescription, outputRevisionId, baseProjectId);
	}

	private JSONObject createResponse(final String rootDescription, final String outputRevisionId,
			final String baseProjectId) throws JSONException {
		final JSONObject out = new JSONObject();
		if (isValidId(outputRevisionId)) {
			out.put("ROID", outputRevisionId);
		}
		out.put("BASE_POID", baseProjectId);
		out.put("DESCRIPTION", rootDescription);
		return out;
	}

	private JSONObject getBaseRevisionIdForViewer( //
			final @Parameter("cardId") Long cardId, //
			final @Parameter("className") String className) throws JSONException {

		final String rootDescription = viewerLogic().getDescriptionOfRoot(cardId, className);

		final String baseProjectId = viewerLogic().getBaseProjectId(cardId, className);

		final String outputRevisionId = viewerLogic().getBaseRevisionIdForViewer(cardId, className);

		return createResponse(rootDescription, outputRevisionId, baseProjectId);
	}

	@JSONExported
	public JSONObject readBimLayer() throws JSONException {
		final Iterable<Layer> layerList = bimLayerLogic().readLayers();

		final JSONArray jsonLayerList = new JSONArray();
		final JSONObject response = new JSONObject();

		for (final Layer layer : layerList) {
			final JSONObject jsonLayer = LAYER_TO_JSON.apply(layer);
			jsonLayerList.put(jsonLayer);
		}

		response.put(BIM_LAYER, jsonLayerList);
		return response;
	}

	private static final Function<Layer, JSONObject> LAYER_TO_JSON = new Function<Layer, JSONObject>() {

		@Override
		public JSONObject apply(Layer input) {
			final JSONObject out = new JSONObject();

			try {
				out.put(CLASS_NAME, input.getClassName());
				out.put(ACTIVE, input.isActive());
				out.put(BIM_ROOT, input.isRoot());
				out.put(EXPORT, input.isExport());
				out.put(CONTAINER, input.isContainer());
				out.put(ROOT_REFERENCE, input.getRootReference());
				out.put(DESCRIPTION, input.getDescription());
			} catch (JSONException e) {
				throw new BimError(e);
			}
			return out;
		}
	};

	@JSONExported
	public void saveBimLayer( //
			final @Parameter(CLASS_NAME) String className, //
			final @Parameter(ATTRIBUTE) String attribute, //
			final @Parameter(VALUE) String value) throws Exception {
		bimLayerLogic().updateBimLayer(className, attribute, value);
	}

	private File fileFromFileItem(final FileItem fileIFC) throws Exception {
		File output = null;
		if (fileIFC != null && fileIFC.getSize() > 0) {
			output = File.createTempFile("theIFCToUpload", ".ifc", FileUtils.getTempDirectory());
			fileIFC.write(output);
		}

		return output;
	}

	@JSONExported
	public void importIfc( //
			final @Parameter("projectId") String projectId //
	) {
		bimConnectorLogic().importIfc(projectId);
	}

	@JSONExported
	public void exportIfc( //
			final @Parameter("projectId") String projectId //
	) {
		bimConnectorLogic().exportIfc(projectId);
	}

	@Admin
	@JSONExported
	public DataHandler download( //
			final @Parameter("projectId") String projectId //
	) throws Exception {
		final DataHandler content = bimProjectLogic().download(projectId);
		if (content == null) {
			return null;
		}
		return new DataHandler(new DataSource() {

			@Override
			public OutputStream getOutputStream() throws IOException {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getName() {
				return projectId + ".ifc";
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return content.getInputStream();
			}

			@Override
			public String getContentType() {
				return "application/ifc";
			}
		});
	}

	@JSONExported
	public JSONObject rootClassName() throws JSONException {
		final JSONObject out = new JSONObject();
		final Layer rootLayer = bimLayerLogic().getRootLayer();

		if (rootLayer == null) {
			out.put("root", "");
		} else {
			out.put("root", rootLayer.getClassName());
		}
		return out;
	}

	@JSONExported
	public JSONObject fetchCardFromViewewId( //
			final @Parameter("objectId") String objectId, //
			final @Parameter("revisionId") String revisionId //
	) throws JSONException {
		final BimCard bimCard = viewerLogic().fetchCardDataFromObjectId(objectId, revisionId);
		final DataAccessLogic dataLogic = userDataAccessLogic();
		if (bimCard.isValid()) {
			final Card fetchedCard = dataLogic.fetchCard(bimCard.getClassId(), bimCard.getId());
			return cardSerializer().toClient(fetchedCard, CARD);
		} else {
			return new JSONObject();
		}
	}

	@JSONExported
	public JSONObject fetchJsonForBimViewer( //
			final @Parameter("revisionId") String revisionId, //
			final @Parameter("baseProjectId") String baseProjectId //
	) throws JSONException {
		return new JSONObject(viewerLogic().getOutputForBimViewer(revisionId, baseProjectId));
	}

	@JSONExported
	public JSONObject getActiveForClassname(final @Parameter("className") String className) throws JSONException {
		final JSONObject out = new JSONObject();
		final boolean isActive = bimLayerLogic().isActive(className);
		out.put(CLASS_NAME, className);
		out.put(ACTIVE, isActive);
		return out;
	}

	@JSONExported
	public JSONObject moveObjectToPosition(//
			final @Parameter("projectId") String projectId, //
			final @Parameter("className") String className, //
			final @Parameter("objectId") String objectId, //
			final @Parameter("x") double x, //
			final @Parameter("y") double y, //
			final @Parameter("z") double z //
	) throws JSONException {
		List<Double> coordinates = Lists.newArrayList(x, y, z);
		viewerLogic().moveObjectToPosition(projectId, className, objectId, coordinates);
		JSONObject response = new JSONObject();
		return response;
	}

}
