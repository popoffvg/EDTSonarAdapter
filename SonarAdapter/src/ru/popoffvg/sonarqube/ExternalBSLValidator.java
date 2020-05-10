package ru.popoffvg.sonarqube;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;

import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.resource.BslEObjectAtOffsetHelper;
import com._1c.g5.v8.dt.bsl.validation.CustomValidationMessageAcceptor;
import com._1c.g5.v8.dt.bsl.validation.IExternalBslValidator;
import com._1c.g5.v8.dt.core.platform.IV8ProjectManager;
import com.google.gson.Gson;
import com.google.inject.Inject;

import ru.popoffvg.sonaradapter.SonarAdapterPlugin;
import ru.popoffvg.sonarqube.Issue.TextRange;

public class ExternalBSLValidator implements IExternalBslValidator {

	private EObject moduleObject;
	private CustomValidationMessageAcceptor acceptor;
	private String moduleText;
	@Inject
	private IV8ProjectManager projectManager;
	private IPreferenceStore prefs;

	public ExternalBSLValidator() {
		super();
		
	}
	
	@Override
	public boolean needValidation(EObject object) {
		return object instanceof Module;
	}

	@Override
	public void validate(EObject object, CustomValidationMessageAcceptor messageAcceptor, CancelIndicator monitor) {

		IProject project = projectManager.getProject(object).getProject();
		prefs = SonarAdapterPlugin.getDefault().getPreferenceStore(project);
		if(!prefs.getBoolean(SonarAdapterPlugin.LOAD_SONAR_ISSUES_PROPERTY)) return;
		
		String projectId = prefs.getString(SonarAdapterPlugin.PROJECT_ID_PROPERTY);
		String server = prefs.getString(SonarAdapterPlugin.SERVER_ID_PROPERTY);
		
		if(projectId.isEmpty() || server.isEmpty()) {
			SonarAdapterPlugin.log(SonarAdapterPlugin.createWarningStatus("Missing Sonar check. Server parameters are empty."));
			return;
		}
		
		this.moduleObject = object;
		this.acceptor = messageAcceptor;
		moduleText = NodeModelUtils.findActualNodeFor(object).getText();

		IFile moduleFile = ResourcesPlugin.getWorkspace().getRoot()
				.getFile(new Path(EcoreUtil.getURI(moduleObject).toPlatformString(true)));
		var path = moduleFile.getProjectRelativePath().toString();
		
		HashMap<String, String> params = new HashMap<>();
		params.put("componentKeys", projectId + ":" + path);
		params.put("resolved", "false");

		URI uri = URI.create(server + "/api/issues/search?" + paramsQuery(params));
		
		// TODO paging
		// TODO timeout
		HttpClient httpClient = HttpClient.newBuilder().build();
		HttpRequest request = HttpRequest.newBuilder().GET()
				.uri(uri)
				.build();

		try {
			HttpResponse<String> responce = httpClient.send(request, BodyHandlers.ofString());

			SearchResponce data = new Gson().fromJson(responce.body(), SearchResponce.class);
			for (Issue issue : data.getIssues()) {
				regIssueFromSonar(issue);
			}
		} catch (Exception e) {
			SonarAdapterPlugin.logError(e);
		}

	}

	private String paramsQuery(HashMap<String, String> parameters) {
		return  parameters.entrySet().stream().map(this::paramsToString)
				.collect(Collectors.joining("&"));
	}

	private String paramsToString(Entry<String, String> entry) {
		try {
			return entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	public void regIssueFromSonar(Issue issue) {
		int startOffset = startOffsetFromTextRange(issue.getTextRange());
		acceptor.acceptInfo("acc: " + issue.getMessage(),
				moduleObject,
				startOffset,
				issue.getTextRange().getEndOffset() - issue.getTextRange().getStartOffset(),
				"QUICKFIX_CODE");
	}

	private int startOffsetFromTextRange(TextRange range) {
		return calculateLineOffset(range.getStartLine())+range.getStartOffset();
	}

	private int calculateLineOffset(int startLine) {
		return moduleText.lines().limit(startLine).mapToInt(String::length).sum();
	}
}
